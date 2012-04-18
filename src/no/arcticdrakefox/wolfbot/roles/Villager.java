package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

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
		return Messages.getString("Villager.intro"); //$NON-NLS-1$
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
		return Messages.getString("Villager.nightError"); //$NON-NLS-1$
	}
	
	@Override
	public String nightEnd() {
		return null;
	}

	@Override
	public String helpText() {
		return Messages.getString("Villager.help"); //$NON-NLS-1$
	}
}