package Agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import jade.core.AID;

public class Vendeur extends Agent {
	
	private final static String PATH = "transactions.txt";
	private int carte = 300;
	
	@Override
	protected void setup() {
		
		System.out.println("Vendeur "+this.getAID().getName());
		
		//ACHETEUR DEMANDE UNE NOUVELLE CARTE A L'ADMIN
		demandeNouvelleCarte();
		
		//PUBLIER SES SERVICES
		register();
		
		//ATTENDRE DES REPONSES
		addBehaviour(new CyclicBehaviour() {
			
			private int prixAcheteur;
			private String content;
			private AID requester;
			private List<AID> requesters = new ArrayList<AID>();
			private boolean finish = false;
			private int counter = 1+(int)( Math.random()*5);
			
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
									
									if(!finish) {
										
										if(prixAcheteur < carte) {
											if( ! requesters.contains(crequester) ) {
												replyRequest.setPerformative(ACLMessage.REFUSE);
												replyRequest.setContent("Votre demande est refusée");
												requesters.add(crequester);
											}
											else {
												
												if(counter != 0) {
													replyRequest.setPerformative(ACLMessage.PROPOSE);
													replyRequest.setContent("Je vous donne une proposition");
													int carteSugg =  (int)( Math.random()*( carte - prixAcheteur + 1 ) ) + prixAcheteur; 
													replyRequest.addUserDefinedParameter("carteSuggeree", carteSugg+"");
													counter--;
												}
												else {
													replyRequest.setPerformative(ACLMessage.FAILURE);
													replyRequest.setContent("Impossible de vous vendre ma catre");
												}
											}
										}
										else {
											replyRequest.setPerformative(ACLMessage.PROPOSE);
											replyRequest.setContent("Voila ma proposition");
											replyRequest.addUserDefinedParameter("carteSuggeree", carte+"");
										}
									}
									else {
										replyRequest.setPerformative(ACLMessage.CANCEL);
										replyRequest.setContent("J'ai deja vendu ma carte");
									}
									
									requester = crequester;
									

									Thread.sleep(5000);
									send(replyRequest);
									
									break;
							
									
							//PROPOSITION ACCEPTEE
							case ACLMessage.ACCEPT_PROPOSAL:
								
									ACLMessage replyAcceptProposal = aclMessage.createReply();
									
									if(!finish) {
										
										prixAcheteur = Integer.parseInt(aclMessage.getUserDefinedParameter("carteAchat"));
										int carteAcheteur = Integer.parseInt(aclMessage.getUserDefinedParameter("carteAcheteur"));
									
										System.out.println("--------------------------------");
										System.out.println("> Validation de la transaction ");
										
										
										replyAcceptProposal.setPerformative(ACLMessage.CONFIRM);
										
										//VENDEUR:CARTE:ACHETEUR:CARTE:PRIX
										String transaction = getAID().getLocalName()+":"+carte+":"+aclMessage.getSender().getLocalName()+":"+carteAcheteur+":"+prixAcheteur+"\n";
										
										replyAcceptProposal.setContent(transaction);
										
										Thread.sleep(5000);
										send(replyAcceptProposal);
										
										saveTransaction(transaction);
										
										finish = true;
									}
									else {
										replyAcceptProposal.setPerformative(ACLMessage.CANCEL);
										replyAcceptProposal.setContent("J'ai deja vendu ma carte");
									}
									
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
	
	private void saveTransaction(String transaction) {
		
		try {
			
			File fic = new File(PATH);
			FileOutputStream fos = new FileOutputStream(fic,true);
			PrintStream ps = new PrintStream(fos);
			
			ps.append(transaction);
			
			
			ps.close();
			
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void register() {
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("carteSelling");
		sd.setName("carteTrading");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	

}
