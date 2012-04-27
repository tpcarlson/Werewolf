package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

public class BodyGuard extends Player {
	
	Player lastTarget = null;
	
	public BodyGuard(String name){
		super(name);
	}
	
	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.bodyguard;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return Messages.getString("BodyGuard.description"); //$NON-NLS-1$
	}

	@Override
	public String nightStart() {
		isReady = false;
		return Messages.getString("BodyGuard.nightInstructions"); //$NON-NLS-1$
	}
	
	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2); //$NON-NLS-1$
		if (args[0].equals("!bane")){ //$NON-NLS-1$
			if (args.length != 2)
				return Messages.getString("BodyGuard.correctUsage"); //$NON-NLS-1$
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else if (target == lastTarget) {
				return Messages.getString("BodyGuard.sameTarget");
			} else {
				if (target.isAlive()){
					vote(target);
					lastTarget= target;
					isReady = true;
					if (target.equals(this))
						return Messages.getString("BodyGuard.selfGuard"); //$NON-NLS-1$
					else
						return Messages.getString("BodyGuard.guard", new Object[] {target}); //$NON-NLS-1$
				} else
					return Messages.getString("BodyGuard.tooLate", new Object[] {target}); //$NON-NLS-1$
			}
		} else if (args[0].equals("!rest")){ //$NON-NLS-1$
			lastTarget = null;
			isReady = true;
			vote = null;
			return Messages.getString("BodyGuard.rest"); //$NON-NLS-1$
		} else
			return null;
	}
	
	@Override
	public String nightEnd() {
		return null;
	}

	@Override
	public String helpText() {
		return Messages.getString("BodyGuard.help"); //$NON-NLS-1$
	}
}
