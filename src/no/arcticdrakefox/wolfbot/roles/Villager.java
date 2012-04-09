package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.*;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.Team;

public class Villager extends Player {

	public Villager(String name){
		super(name);
		setTeam (Team.Villagers);
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

	@Override
	public String helpText() {
		return "This role is the normal villager, with no special powers. Try to survive without getting lynched or eaten!";
	}
}