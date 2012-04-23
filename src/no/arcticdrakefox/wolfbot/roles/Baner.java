package no.arcticdrakefox.wolfbot.roles;

import java.util.Collection;
import java.util.Iterator;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.State;
import no.arcticdrakefox.wolfbot.management.Pair;

public class Baner extends Player {
	
	public Baner(String name){
		super(name);
	}
	
	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.baner;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return Messages.getString("Baner.description"); //$NON-NLS-1$
	}

	@Override
	public String nightStart() {
		isReady = false;
		return Messages.getString("Baner.nightInstructions"); //$NON-NLS-1$
	}
	
	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2); //$NON-NLS-1$
		if (args[0].equals("!bane")){ //$NON-NLS-1$
			if (args.length != 2)
				return Messages.getString("Baner.correctUsage"); //$NON-NLS-1$
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.isAlive()){
					vote(target);
					isReady = true;
					if (target.equals(this))
						return Messages.getString("Baner.selfBane"); //$NON-NLS-1$
					else
						return Messages.getString("Baner.bane", new Object[] {target}); //$NON-NLS-1$
				} else
					return Messages.getString("Baner.tooLate", new Object[] {target}); //$NON-NLS-1$
			}
		} else if (args[0].equals("!rest")){ //$NON-NLS-1$
			isReady = true;
			vote = null;
			return Messages.getString("Baner.rest"); //$NON-NLS-1$
		} else
			return null;
	}
	
	
	@Override
	public void save(State time, Collection<Pair<Player, String>> players)  {
		super.save(time, players);
		
		for (Iterator<Pair<Player, String>> iterator = players.iterator(); iterator.hasNext();) {
			Pair<Player, String> pair = (Pair<Player, String>) iterator.next();
			if (pair.fst().equals(getVote())) {
				players.remove(pair);
			}
		}
	}

	@Override
	public String nightEnd() {
		return null;
	}

	@Override
	public String helpText() {
		return Messages.getString("Baner.help"); //$NON-NLS-1$
	}
}
