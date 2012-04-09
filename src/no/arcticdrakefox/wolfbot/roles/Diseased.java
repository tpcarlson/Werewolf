package no.arcticdrakefox.wolfbot.roles;

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
		WolfBotModel.getInstance().skipNight();
	}

	
	
}
