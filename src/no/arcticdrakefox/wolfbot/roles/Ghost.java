package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

public class Ghost extends Player {
	
	public Ghost(String name){
		super(name);
	}
	
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
		return "You are a ghost, capable of bringing people back from the dead as you once managed to do for yourself.";
	}

	@Override
	public String nightStart() {
		isReady = false;
		return "The time is right. You may !ghost a person back to life or just !rest";
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2);
		if (args[0].equals("!ghost")){
			if (args.length != 2)
				return "Correct usage: !ghost <someone>";
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.isAlive()){
					return String.format("%s is still amongst the living.", target);
				} else {
					vote(target);
					isReady = true;
					return String.format("Life leaves your body as you trail off the spirit world, trying to find %s", target);
				}
			}
		} else if (args[0].equals("!rest")){
			isReady = true;
			vote = null;
			return "You cannot think of a worthy man to bring back. Tonight, you will rest.";
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
			return String.format("A villager emerges at dawn, walking about as if they had never died at all. %s lives again!", vote);
		}
	}

	@Override
	public String helpText() {
		return "The ghost is capable of raising the dead at night.";
	}
}
