package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

public class Necromancer extends Player {
	
	public Necromancer(String name){
		super(name);
	}
	
	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.necromancer;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return Messages.getString("Necromancer.init"); //$NON-NLS-1$
	}

	@Override
	public String nightStart() {
		isReady = false;
		return Messages.getString("Necromancer.nightHelp"); //$NON-NLS-1$
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2); //$NON-NLS-1$
		if (args[0].equals("!ghost")){ //$NON-NLS-1$
			if (args.length != 2)
				return Messages.getString("Necromancer.nightError"); //$NON-NLS-1$
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.isAlive()){
					return Messages.getString("Necromancer.targetLiving", new Object[]{target}); //$NON-NLS-1$
				} else {
					vote(target);
					isReady = true;
					return Messages.getString("Necromancer.target", new Object[]{target}); //$NON-NLS-1$
				}
			}
		} else if (args[0].equals("!rest")){ //$NON-NLS-1$
			isReady = true;
			vote = null;
			return Messages.getString("Necromancer.rested"); //$NON-NLS-1$
		} else
			return null;
	}
	
	@Override
	public String nightEnd() {
		if (vote == null)
			return null;
		else if (vote.isAlive()){
			return null;
		} else {
			vote.revive();
			return String.format(Messages.getString("Necromancer.success"), vote); //$NON-NLS-1$
		}
	}

	@Override
	public String helpText() {
		return Messages.getString("Necromancer.help"); //$NON-NLS-1$
	}
}
