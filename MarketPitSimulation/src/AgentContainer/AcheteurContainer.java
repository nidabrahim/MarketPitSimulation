package AgentContainer;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;


public class AcheteurContainer {

	public static void main(String[] args) {
		
		try {
			
			Runtime runtime = Runtime.instance();
			ProfileImpl profileImpl = new ProfileImpl(false);
			profileImpl.setParameter(ProfileImpl.MAIN_HOST,"localhost");
			AgentContainer agentContainer = runtime.createAgentContainer(profileImpl);
			
			for(int i=1; i<=10; i++) {
				AgentController agentController = agentContainer.createNewAgent("Acheteur"+i,"Agents.Acheteur", new Object[]{});
				agentController.start();
			}
		
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
		
	}

	}


