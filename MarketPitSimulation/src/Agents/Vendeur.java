package Agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

public class Vendeur extends Agent {
	
	private int carte = 300;
	
	@Override
	protected void setup() {
		
		System.out.println("Vendeur "+this.getAID().getName());
		
		//ACHETEUR DEMANDE UNE NOUVELLE CARTE A L'ADMIN
		demandeNouvelleCarte();
		
		//ATTENDRE DES REPONSES
		addBehaviour(new CyclicBehaviour() {
			
			private int prixAcheteur;
			private String content;
			private AID requester;
			
			@Override
			public void action() {
				try {
					
					//LES TYPES DE MESSAGE
					MessageTemplate messageTemplate = 
							MessageTemplate.or(
									MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
									MessageTemplate.or(
												MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
												MessageTemplate.or(
														MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
														MessageTemplate.MatchPerformative(ACLMessage.INFORM)
												)
									)
						   );
					
					ACLMessage aclMessage = receive(messageTemplate);
					
					if(aclMessage!=null){
						
						switch(aclMessage.getPerformative()){
						
							//MESSAGE DE L'ADMIN
							case ACLMessage.INFORM :
								
									carte = Integer.parseInt(aclMessage.getUserDefinedParameter("nouvelleCarte"));
									
									System.out.println("--------------------------------");
									System.out.println("> Réception d'une carte :" + carte);
									System.out.println("Expéditeur :" + aclMessage.getSender().getName());
									
									break;
									
						
							//DEMANDE D'ACHAT
							case ACLMessage.REQUEST :
								
									content = aclMessage.getContent();
									prixAcheteur = Integer.parseInt(aclMessage.getUserDefinedParameter("carteAchat"));
									AID crequester = aclMessage.getSender();
								
									System.out.println("--------------------------------");
									System.out.println("> Demande d'achat ");
									System.out.println("From :" + crequester.getName());
									System.out.println("Contenu : " + content);
									
									System.out.println("acheteur : " + prixAcheteur);
									System.out.println("vendeur : " + carte);

									ACLMessage replyRequest = aclMessage.createReply();
									
									if(prixAcheteur < carte) {
										if( ! crequester.equals(requester) ) {
											replyRequest.setPerformative(ACLMessage.REFUSE);
											replyRequest.setContent("Votre demande est refusée");
										}
										else {
											replyRequest.setPerformative(ACLMessage.PROPOSE);
											replyRequest.setContent("Je vous donne pas proposition");
											int carteSugg =  (int)( Math.random()*( carte - prixAcheteur + 1 ) ) + prixAcheteur; 
											replyRequest.addUserDefinedParameter("carteSuggeree", carteSugg+"");
										}
									}
									else {
										replyRequest.setPerformative(ACLMessage.PROPOSE);
										replyRequest.setContent("Voila ma proposition");
										replyRequest.addUserDefinedParameter("carteSuggeree", carte+"");
									}
									
									requester = crequester;
									

									Thread.sleep(5000);
									send(replyRequest);
									
									break;
							
									
							//PROPOSITION ACCEPTEE
							case ACLMessage.ACCEPT_PROPOSAL:
								
									prixAcheteur = Integer.parseInt(aclMessage.getUserDefinedParameter("carteAcheteur"));
								
									System.out.println("--------------------------------");
									System.out.println("> Validation de la transaction ");
									
									ACLMessage replyAcceptProposal = aclMessage.createReply();
									replyAcceptProposal.setPerformative(ACLMessage.CONFIRM);
									replyAcceptProposal.setContent("<transaction>"
											+ "<Acheteur name='"+aclMessage.getSender().getName()+"'>"+prixAcheteur+"</Acheteur>"
											+ "<Vendeur name='"+getAID().getName()+"'>"+carte+"</Vendeur>"
											+ "<transaction>");
									
									Thread.sleep(5000);
									send(replyAcceptProposal);
									
									break;
									
									
							//PROPOSITION REFUSEE
							case ACLMessage.REJECT_PROPOSAL:
								
									System.out.println("--------------------------------");
									System.out.println("> Réception d'un refus ");
									System.out.println("From :" + aclMessage.getSender().getName());
									System.out.println("Contenu : " + aclMessage.getContent());
									
									break;
									
						} 
						
					}
					else{
							System.out.println("Block");
							block();
						}
					} catch (Exception e) {e.printStackTrace(); }}
			
		});
									}
					
	
	@Override
	protected void takeDown() {
		System.out.println("Bye Bye");
	}
	
	
	
	private void demandeNouvelleCarte() {
		
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		
		message.addReceiver(new AID("Admin", AID.ISLOCALNAME));
		message.setContent("Je vous demande une nouvelle carte !");

		send(message);
		
	}

}
