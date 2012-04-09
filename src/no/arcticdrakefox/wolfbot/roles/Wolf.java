package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.*;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.Team;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

public class Wolf extends Player {
	
	private boolean ill = false;
	
	public boolean isIll() {
		return ill;
	}

	public void setIll(boolean ill) {
		this.ill = ill;
	}

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
		if (ill) {
			return "Something you ate last night did not agree with you. You rest till you feel better";
		} else {
			return "As a wolf, you can !kill someone tonight or just !rest";
		}
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		if (!ill) {
			String[] args = message.trim().split(" ", 2);
			if (args[0].equals("!kill")) {
				if (args.length != 2)
					return "Correct usage: !kill <someone>";
				Player target = players.getPlayer(args[1]);
				if (target == null)
					return targetNotFound(args[1]);
				else {
					if (target.equals(this)) {
						return "Killing yourself doesn't seem very productive.";
					} else if (target.isAlive()) {
						vote(target);
						isReady = true;
						return String
								.format("You sharpen your fangs. They will taste *%s's* blood tonight!",
										target);
					} else
						return String.format("*%s* is already dead.", target);
				}
			} else if (args[0].equals("!rest")) {
				isReady = true;
				vote = null;
				return "You decide to quell your bloodlust tonight.";
			} else
				return null;
		} else {
			isReady = true;
			vote = null;
			return null;
		}
	}
	
	@Override
	public String nightEnd() {
		ill = false;
		return null;
	}

	@Override
	public String helpText() {
		return "The werewolf is the main enemy to all villagers - their aim is to eat everyone before they get lynched!";
	}
}
