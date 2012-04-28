package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.model.Role;

public class Mason extends Player {
	
	public Mason(String name){
		super(name);
	}
	
	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.mason;
	}

	@Override
	public String roleInfo(PlayerList players) {
		Object[] tmp = new Object[] {StringHandler.listToString(players.getRole(Role.mason))};
		
		return Messages.getString("Mason.intro") //$NON-NLS-1$
			 + " "
			 + Messages.getString("Mason.theMasons", tmp);//$NON-NLS-1$
	}

	@Override
	public String nightStart() {
		return null;
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		return Messages.getString("Mason.nightError"); //$NON-NLS-1$
	}
	
	@Override
	public String nightEnd() {
		return null;
	}

	@Override
	public String helpText() {
		return Messages.getString("Mason.help"); //$NON-NLS-1$
	}
}
