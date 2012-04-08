package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.*;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.Team;

public class Wolf extends Player {
	
	public Wolf(String name){
		super(name);
		setTeam (Team.Wolves);
	}
	
	@Override
	public boolean isWolf() {
		return true;
	}

	@Override
	public Role getRole() {
		return Role.wolf;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return String.format("You're a werewolf! The wolves are %s",
				StringHandler.listToString(players.getWolves())
		);
	}

	@Override
	public String nightStart() {
		isReady = false;
		return "As a wolf, you can !kill someone tonight or just !rest";
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2);
		if (args[0].equals("!kill")){
			if (args.length != 2)
				return "Correct usage: !kill <someone>";
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.equals(this)){
					return "Killing yourself doesn't seem very productive.";
				} else if (target.isAlive()){
					vote(target);
					isReady = true;
					return String.format("You sharpen your fangs. They will taste %s's blood tonight!", target);
				} else
					return String.format("%s is already dead.", target);
			}
		} else if (args[0].equals("!rest")){
			isReady = true;
			vote = null;
			return "You decide to quell your bloodlust tonight.";
		} else
			return null;
	}
	
	@Override
	public String nightEnd() {
		return null;
	}
}
