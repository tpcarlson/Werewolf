package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

public class Ghost extends Player {

	public Ghost(String name) {
		super(name);
	}

	String msg = null;
	
	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.ghost;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return Messages.getString("Ghost.init"); //$NON-NLS-1$
	}

	@Override
	public String nightStart() {
		if (isAlive()) return null;
		return Messages.getString("Ghost.nightHelp"); //$NON-NLS-1$
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		if (!isAlive()) {
			String[] args = message.trim().split(" ", 2); //$NON-NLS-1$
			if (args[0].equals("!say")) { //$NON-NLS-1$
				if (args.length != 2)
					return Messages.getString("Ghost.nightError"); //$NON-NLS-1$
				msg = args[1].trim().substring(0, 1);
				isReady = true;
				WolfBotModel.getInstance().sendIrcMessage(WolfBotModel.getInstance().getChannel(), 
						Messages.getString("Ghost.say", new Object[] {msg})); //$NON-NLS-1$
				return Messages.getString("Ghost.feedback", new Object[] { msg}); //$NON-NLS-1$
			}
		}

		return null;
	}

	@Override
	public String nightEnd() {
		return null;
	}

	@Override
	public String helpText() {
		return Messages.getString("Ghost.help"); //$NON-NLS-1$
	}

}
