package no.arcticdrakefox.wolfbot.roles;

import java.util.Collection;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

public class Diseased extends Player {

	public Diseased(String name) {
		super(name);
	}

	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.diseased;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return "You are diseased, your man-meat will poison any wolf that eats you.";
	}

	@Override
	public String nightStart() {
		return null;
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		return null;
	}

	@Override
	public String nightEnd() {
		return null;
	}

	@Override
	public void die(String causeOfDeath) {
		super.die(causeOfDeath);
		Collection<Player> aps =
		Collections2.filter(WolfBotModel.getInstance().getPlayers().players,
				new Predicate<Player>() {
					@Override
					public boolean apply(Player player) {
						return player.isWolf();
					}
				});
		for (Player p : aps) {
			if (p instanceof Wolf) {
				Wolf w = (Wolf) p;
				w.setIll(true);
			}
		}
	}

	@Override
	public String helpText() {
		return null;
	}

	
	
}
