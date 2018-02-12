package Agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;


public class Vendeur extends Agent {
	
	
	@Override
	protected void setup() {
		System.out.println("Vendeur "+this.getAID().getName());
		
		addBehaviour(new TickerBehaviour(this,1000) {
			
			private int compteur;
			
			@Override
			protected void onTick() {
				++compteur;
				System.out.println(" J'attends un acheteur ");
			}
			
		});
		
	}
	
	@Override
	protected void takeDown() {
		System.out.println("Bye Bye");
	}

}
