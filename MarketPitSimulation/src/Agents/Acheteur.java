package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Acheteur extends Agent {
	
	private int carte = 200;
	private boolean success = false;
	private String vendeur = "Vendeur";

	
	
	@Override
	protected void setup() {
	
		System.out.println("Acheteur : "+this.getAID().getName());
		
		//ACHETEUR DEMANDE UNE NOUVELLE CARTE A L'ADMIN
		demandeNouvelleCarte();
		
			
		//ATTENDRE LES REPONSES
		addBehaviour(new CyclicBehaviour() {
			
			private AID requester; 
			private String content; 
			private int carteVendeur;
			
			@Override
			public void action() {
			try {
				
				//LES TYPES DE MESSAGE
				MessageTemplate template =
				MessageTemplate.or(
					MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
					MessageTemplate.or(
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
							MessageTemplate.or(
									MessageTemplate.MatchPerformative(ACLMessage.REFUSE),
									MessageTemplate.or(
											MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
											MessageTemplate.MatchPerformative(ACLMessage.INFORM)
									)
							)
					)
				);
				
			ACLMessage aclMessage = receive(template);
			
			if(aclMessage!=null){
				
				switch(aclMessage.getPerformative()){
				
					//MESSAGE DE L'ADMIN
					case ACLMessage.INFORM :
						
							carte = Integer.parseInt(aclMessage.getUserDefinedParameter("nouvelleCarte"));
							setCarte(carte);
							
							System.out.println("--------------------------------");
							System.out.println("> R�ception d'une carte :" + carte);
							System.out.println("From : " + aclMessage.getSender().getName());
							
							//DEMANDE D'ACHAT
							demandeDAchat(vendeur);
							
							break;
							
					
					//MESSAGE DU VENDEUR
					case ACLMessage.REQUEST :
						
							requester = aclMessage.getSender();
							content = aclMessage.getContent();
							carteVendeur = Integer.parseInt(aclMessage.getUserDefinedParameter("carteSuggere"));
							
							System.out.println("--------------------------------");
							System.out.println("> Demande d'achat ");
							System.out.println("From : " + requester.getName());
							System.out.println("Contenu : " + content);
							
							reponseProposition(aclMessage,carteVendeur);
							
							break;
							
							
					//POPOSITION DU VENDEUR		
					case ACLMessage.PROPOSE :
					
							content = aclMessage.getContent();
							carteVendeur =  Integer.parseInt(aclMessage.getUserDefinedParameter("carteSuggeree"));
							
							System.out.println("-----------------------------------");
							System.out.println("> R�ception de l'offre");
							System.out.println("From :" + aclMessage.getSender().getName());
							System.out.println("Contenu : " + content);
							System.out.println("Prix : " + carteVendeur);
							
							reponseProposition(aclMessage,carteVendeur);
							
							break;
							
							
					case ACLMessage.CONFIRM:
				
							System.out.println("-----------------------------------");
							System.out.println("> R�ception de la confirmation ");
							System.out.println("From :" + aclMessage.getSender().getName());
							System.out.println("Contenu : " + aclMessage.getContent());
							
							success = true;

							break;
							
							
					//PROPOSITION REFUSEE
					case ACLMessage.REFUSE:
						
							System.out.println("--------------------------------");
							System.out.println("> R�ception d'un refus ");
							System.out.println("From :" + aclMessage.getSender().getName());
							System.out.println("Contenu : " + aclMessage.getContent());
							
							success = false;
							
							demandeDAchat(vendeur);
							
							break;
				
				}
				
			}
			else{
				block();
			}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			});
	
		
	
	}
	
	
	public void setCarte(int vcarte) {
		this.carte = vcarte;
	}
	
	private void demandeNouvelleCarte() {
		
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		
		message.addReceiver(new AID("Admin", AID.ISLOCALNAME));
		message.setContent("Je vous demande une nouvelle carte !");

		send(message);
		
	}

	
	private void demandeDAchat(String vendeur) {
		
		System.out.println("Acheteur : Envoie d'une demande ");
		
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		
		message.addReceiver(new AID(vendeur, AID.ISLOCALNAME));
		message.setContent("Je vous demande le prix de ta carte !");
		
		message.addUserDefinedParameter("carteAchat", carte+"");
		send(message);
	}
	
	
	private void isSuccessTransaction(int carteVendeur) {
		
		if(carteVendeur < this.carte) this.success = true;
	}
	
	
	private void reponseProposition(ACLMessage aclMessage, int carteVendeur) throws InterruptedException {
		
		ACLMessage replyRequest = aclMessage.createReply();
		
		if(carteVendeur > carte) {
			replyRequest.setPerformative(ACLMessage.REJECT_PROPOSAL);
			replyRequest.setContent("Carte propos�e est trop ch�re");
			
		}else {
			replyRequest.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			replyRequest.setContent("Carte propos�e est accept�e");
			replyRequest.addUserDefinedParameter("carteAcheteur", carteVendeur+"");
		}

		Thread.sleep(5000); send(replyRequest);
	}
	
	
	@Override
	protected void takeDown() {
		System.out.println("Bye Bye");
	}

}
