package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

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
		return "You are the terror that flaps into the night! You are Wolfsbane! Baner of wolves! Each night you may choose one person to protect from wolf attacks.";
	}

	@Override
	public String nightStart() {
		isReady = false;
		return "It's time. !bane the wolves from someone tonight or !rest";
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2);
		if (args[0].equals("!bane")){
			if (args.length != 2)
				return "Correct usage: !bane <someone>";
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.isAlive()){
					vote(target);
					isReady = true;
					if (target.equals(this))
						return "You fortify your defenses and set up traps. The wolves will never catch you!";
					else
						return String.format("You don your purple cape and matching bell-shaped hat. %s will not die on your watch!", target);
				} else
					return String.format("You are too late to save %s, they are dead already.", target);
			}
		} else if (args[0].equals("!rest")){
			isReady = true;
			vote = null;
			return "You give up. The village can die for all you care.";
		} else
			return null;
	}
	
	@Override
	public String nightEnd() {
		return null;
	}

	@Override
	public String helpText() {
		return "The Baner can protect a member of the village each night, including themselves. If the wolves attack that person, they will be driven off and nobody will die.";
	}
}
