package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

public class Vigilante extends Player {
	
	public Vigilante(String name){
		super(name);
	}
	
	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.vigilante;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return Messages.getString("Vigilante.intro"); //$NON-NLS-1$
	}

	@Override
	public String nightStart() {
		isReady = false;
		return Messages.getString("Vigilante.nightHelp"); //$NON-NLS-1$
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2); //$NON-NLS-1$
		if (args[0].equals("!kill")){ //$NON-NLS-1$
			if (args.length != 2)
				return Messages.getString("Vigilante.nightError"); //$NON-NLS-1$
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.isAlive()){
					if (target.equals(this))
						return Messages.getString("Vigilante.errorSelfKill"); //$NON-NLS-1$
					else {
						vote(target);
						isReady = true;
						return Messages.getString("Vigilante.successTarget", new Object []{target}); //$NON-NLS-1$
					}
				} else
					return Messages.getString("Vigilante.tooLate", new Object[] {target}); //$NON-NLS-1$
			}
		} else if (args[0].equals("!rest")){ //$NON-NLS-1$
			isReady = true;
			vote = null;
			return Messages.getString("Vigilante.successSkip"); //$NON-NLS-1$
		} else
			return null;
	}
	
	@Override
	public String nightEnd() {
		if (vote == null)
			return null;
		else if (isAlive){
			if (vote.isAlive()){
				vote.die(Messages.getString("Vigilante.killed", vote, vote.getRole())); //$NON-NLS-1$
				if (vote.isWolf()){
					return Messages.getString("Vigilante.killedWolf", new Object[]{vote}); //$NON-NLS-1$
				} else{
					return Messages.getString("Vigilante.KilledInnosent", new Object[]{vote}); //$NON-NLS-1$
				}
			} else {
				return Messages.getString("Vigilante.wolvesGotThereFirst", new Object[]{vote}); //$NON-NLS-1$
			}
		} else {
			return Messages.getString("Vigilante.KilledFirst", new Object[]{vote}); //$NON-NLS-1$
		}
	}

	@Override
	public String helpText() {
		return Messages.getString("Vigilante.help"); //$NON-NLS-1$
	}
}
