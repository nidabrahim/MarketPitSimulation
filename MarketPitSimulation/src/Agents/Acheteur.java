package Agents;

import jade.core.Agent;


public class Acheteur extends Agent {
	
	
	@Override
	protected void setup() {
		System.out.println("Acheteur "+this.getAID().getName());
	}
	
	@Override
	protected void takeDown() {
		System.out.println("Bye Bye");
	}

}
