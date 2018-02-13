package Agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

public class Admin extends Agent{
	
	
	@Override
	protected void setup() {
		
	   System.out.println("Admin "+this.getAID().getName());
		
		
	   addBehaviour(new CyclicBehaviour() {
			
		private int[] carte = {48,100,25,87,10,41,74};
			
		@Override
		public void action() {
		try {
				
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				
				
			ACLMessage aclMessage = receive(template);
			
			if(aclMessage != null){
				
				switch(aclMessage.getPerformative()){
				
					case ACLMessage.REQUEST :
					
						int nouvelleCarte = (int)( Math.random()*( 100 - 10 + 1 ) ) + 10; 
						
						ACLMessage reply = new ACLMessage(ACLMessage.INFORM);			

						reply.addReceiver(aclMessage.getSender());
						reply.addUserDefinedParameter("nouvelleCarte", nouvelleCarte+"");
						
						send(reply);
							
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

}
