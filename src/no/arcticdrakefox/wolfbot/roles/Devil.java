package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.*;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.Team;

public class Devil extends Player {
	private Player scryVote;
	
	public Devil(String name){
		super(name);
		setTeam (Team.Wolves);
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
		return String.format("You are a devil, a lycantropic scry. You may check a person's role each night. The wolves are %s",
				StringHandler.listToString(players.getWolves())
		);
	}

	@Override
	public String nightStart() {
		isReady = false;
		scryVote = null;
		return "The moon is in alignment. You may !scry a person, !kill them or just !rest";
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2);
		if (args[0].matches("!scry|!kill")){
			if (args.length != 2)
				return "Correct usage: !scry <someone> or !kill <someone>";
			if (isReady)
				return "You may only scry one person each night.";
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.equals(this)){
					return "It makes no sense to target yourself.";
				} else if (target.isAlive()){
					isReady = true;
					if (args[0].equals("!scry")){
						scryVote = target;
						return String.format("You set up your array of candles, orbs and artifacts, concentrating your efforts on %s", target);
					} else {
						vote = target;
						return String.format("Though unbefitting of one of your stature, you abandon your artifacts and get out to hunt %s", target);
					}
				} else
					return String.format("%s is already dead.", target);
			}
		} else if (args[0].equals("!rest")){
			isReady = true;
			vote = null;
			scryVote = null;
			return "You rest for tonight, confident that you already know all you need.";
		}
			return null;
	}
	
	@Override
	public String nightEnd() {
		if (scryVote == null){
			return null;
		} else {
			return String.format("%s is a %s", vote, vote.getRole());
		}
	}

	@Override
	public String helpText() {
		return "The devil is an evil creature who can look deep into someone's soul and know what they are capable of. He can also sacrifice people at night, if he prefers.";
	}
}
