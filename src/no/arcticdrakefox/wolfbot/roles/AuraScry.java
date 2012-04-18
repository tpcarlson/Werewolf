package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

public class AuraScry extends Player {

	public AuraScry(String name) {
		super(name);
	}


	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.aura_scry;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return	Messages.getString("AuraScry.info"); //$NON-NLS-1$
	}

	@Override
	public String nightStart() {
		isReady = false;
		return Messages.getString("AuraScry.nightInstructions"); //$NON-NLS-1$
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2); //$NON-NLS-1$
		if (args[0].equals("!scry")){ //$NON-NLS-1$
			if (args.length != 2)
				return Messages.getString("AuraScry.correctUsage"); //$NON-NLS-1$
			if (isReady)
				return Messages.getString("AuraScry.tooManyPeople"); //$NON-NLS-1$
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.equals(this)){
					return Messages.getString("AuraScry.selfScry"); //$NON-NLS-1$
				} else if (target.isAlive()){
					isReady = true;
					vote = target;
					return Messages.getString("AuraScry.Scried", new Object[] {target}); //$NON-NLS-1$
				} else
					return Messages.getString("AuraScry.allreadyDead", new Object[] {target}); //$NON-NLS-1$
			}
		} else if (args[0].equals("!rest")){ //$NON-NLS-1$
			isReady = true;
			vote = null;
			return Messages.getString("AuraScry.rest"); //$NON-NLS-1$
		} else
			return null;
	}

	@Override
	public String nightEnd() {
		if (vote == null){
			return null;
		} else {
			Role r = vote.getRole();
			return (r != Role.villager) && (r!=Role.wolf) ?
				Messages.getString("AuraScry.hasAura", new Object[] {vote}) //$NON-NLS-1$
				: Messages.getString("AuraScry.noAura", new Object[] {vote}); //$NON-NLS-1$
		}
	}

	@Override
	public String helpText() {
		return Messages.getString("AuraScry.help"); //$NON-NLS-1$
	}

}
