package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.model.Role;

public class Devil extends Player {
	private Player scryVote;
	
	public Devil(String name){
		super(name);
	}
	
	@Override
	public boolean isWolf() {
		return true;
	}

	@Override
	public Role getRole() {
		return Role.devil;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return Messages.getString("Devil.intro", //$NON-NLS-1$
				new Object[] {StringHandler.listToString(players.getWolves())}
		);
	}

	@Override
	public String nightStart() {
		isReady = false;
		scryVote = null;
		return Messages.getString("Devil.nightInstructions"); //$NON-NLS-1$
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2); //$NON-NLS-1$
		if (args[0].matches("!scry|!kill")){ //$NON-NLS-1$
			if (args.length != 2)
				return Messages.getString("Devil.nightHelp"); //$NON-NLS-1$
			if (isReady)
				return Messages.getString("Devil.multipleScryError"); //$NON-NLS-1$
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.equals(this)){
					return Messages.getString("Devil.selfScry"); //$NON-NLS-1$
				} else if (target.isAlive()){
					isReady = true;
					if (args[0].equals("!scry")){ //$NON-NLS-1$
						scryVote = target;
						return Messages.getString("Devil.successfulFeedback", new Object[]{target}); //$NON-NLS-1$
					} else {
						vote = target;
						return Messages.getString("Devil.restFeedback", new Object[]{target}); //$NON-NLS-1$
					}
				} else
					return Messages.getString("Devil.targetDied", new Object[]{target}); //$NON-NLS-1$
			}
		} else if (args[0].equals("!rest")){ //$NON-NLS-1$
			isReady = true;
			vote = null;
			scryVote = null;
			return Messages.getString("Devil.rested"); //$NON-NLS-1$
		}
			return null;
	}
	
	@Override
	public String nightEnd() {
		if (scryVote == null){
			return null;
		} else {
			return Messages.getString("Devil.result", new Object[] {vote, vote.getRole()}); //$NON-NLS-1$
		}
	}

	@Override
	public String helpText() {
		return Messages.getString("Devil.roleHelp"); //$NON-NLS-1$
	}
}
