package no.arcticdrakefox.wolfbot.roles;

import java.util.Collection;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

public class Scry extends Player {

	public Scry(String name) {
		super(name);
	}

	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public void die(String causeOfDeath) {
		super.die(causeOfDeath);
		Collection<Player> aps =
		Collections2.filter(WolfBotModel.getInstance().getPlayers().players,
				new Predicate<Player>() {
					@Override
					public boolean apply(Player player) {
						return player.getRole() == Role.apprentice_scry;
					}
				});
		if (!aps.isEmpty()) {
			ApprenticeScry ap = (ApprenticeScry) Iterables.getFirst(aps, null);
			ap.setActive();
		}
	}

	@Override
	public Role getRole() {
		return Role.scry;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return Messages.getString("Scry.intro"); //$NON-NLS-1$
	}

	@Override
	public String nightStart() {
		isReady = false;
		return Messages.getString("Scry.nightHelp"); //$NON-NLS-1$
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2); //$NON-NLS-1$
		if (args[0].equals("!scry")) { //$NON-NLS-1$
			if (args.length != 2)
				return Messages.getString("Scry.nightError"); //$NON-NLS-1$
			if (isReady)
				return Messages.getString("Scry.twoScries"); //$NON-NLS-1$
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.equals(this)) {
					return Messages.getString("Scry.selfScry"); //$NON-NLS-1$
				} else if (target.isAlive()) {
					isReady = true;
					vote = target;
					return Messages.getString("Scry.success",new Object []{target});
				} else
					return Messages.getString("Scry.deadError", new Object[]{target}); //$NON-NLS-1$
			}
		} else if (args[0].equals("!rest")) { //$NON-NLS-1$
			isReady = true;
			vote = null;
			return Messages.getString("Scry.rest"); //$NON-NLS-1$
		} else
			return null;
	}

	@Override
	public String nightEnd() {
		if (vote == null) {
			return null;
		} else {
			return vote.isWolf() ? Messages.getString("Scry.foundWolf",new Object[]{vote})
					: Messages.getString("Scry.foundNonWolf",new Object[]{vote});
		}
	}

	@Override
	public String helpText() {
		return Messages.getString("Scry.help"); //$NON-NLS-1$
	}
}