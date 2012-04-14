package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.model.Role;

public class Wolf extends Player {
	
	private boolean ill = false;
	
	public boolean isIll() {
		return ill;
	}

	public void setIll(boolean ill) {
		this.ill = ill;
	}

	public Wolf(String name){
		super(name);
	}
	
	@Override
	public boolean isWolf() {
		return true;
	}

	@Override
	public Role getRole() {
		return Role.wolf;
	}

	@Override
	public String roleInfo(PlayerList players) {
		Object [] params = new Object[] {StringHandler.listToString(players.getWolves())};
		
		return Messages.getString("Wolf.intro", params);
	}

	@Override
	public String nightStart() {
		isReady = false;
		if (ill) {
			return Messages.getString("Wolf.ill"); //$NON-NLS-1$
		} else {
			return Messages.getString("Wolf.nightHelp"); //$NON-NLS-1$
		}
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		if (!ill) {
			String[] args = message.trim().split(" ", 2); //$NON-NLS-1$
			if (args[0].equals("!kill")) { //$NON-NLS-1$
				if (args.length != 2)
					return Messages.getString("Wolf.nightError"); //$NON-NLS-1$
				Player target = players.getPlayer(args[1]);
				if (target == null)
					return targetNotFound(args[1]);
				else {
					if (target.equals(this)) {
						return Messages.getString("Wolf.selfKill"); //$NON-NLS-1$
					} else if (target.isAlive()) {
						vote(target);
						isReady = true;
						return Messages.getString("Wolf.nightConfirmation", //$NON-NLS-1$
								new Object[] {target});
					} else
						return Messages.getString("Wolf.alreadyDead", new Object[] {target}); //$NON-NLS-1$
				}
			} else if (args[0].equals("!rest")) { //$NON-NLS-1$
				isReady = true;
				vote = null;
				return Messages.getString("Wolf.rested"); //$NON-NLS-1$
			} else
				return null;
		} else {
			ill = false;
			isReady = true;
			vote = null;
			return null;
		}
	}
	
	@Override
	public String nightEnd() {
		ill = false;
		return null;
	}

	@Override
	public String helpText() {
		return Messages.getString("Wolf.help"); //$NON-NLS-1$
	}
}
