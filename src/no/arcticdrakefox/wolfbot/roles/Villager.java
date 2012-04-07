package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.*;

public class Villager extends Player {

	public Villager(String name){
		super(name);
	}
	
	@Override
	public Role getRole() {
		return Role.villager;
	}

	@Override
	public String roleInfo(PlayerList players){
		return "You are a common villager. Try not to get eaten or lynched!";
	}
	
	@Override
	public boolean isWolf(){
		return false;
	}
	
	@Override
	public String nightStart() {
		return null;
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		return "You do not have any night actions. Please stop pestering me, filthy commoner.";
	}
	
	@Override
	public String nightEnd() {
		return null;
	}
}