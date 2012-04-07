package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.*;

public class Scry extends Player {

	public Scry(String name){
		super(name);
	}
	
	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.scry;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return "You are a scry. Every night you may check a person for lycantrophy. Beware, for you are the wolves primary target.";
	}

	@Override
	public String nightStart() {
		isReady = false;
		return "The moon is in alignment. You may !scry a person or just !rest";
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		String[] args = message.trim().split(" ", 2);
		if (args[0].equals("!scry")){
			if (args.length != 2)
				return "Correct usage: !scry <someone>";
			if (isReady)
				return "You may only scry one person each night.";
			Player target = players.getPlayer(args[1]);
			if (target == null)
				return targetNotFound(args[1]);
			else {
				if (target.equals(this)){
					return "You don't need your special powers to know that you are a scry.";
				} else if (target.isAlive()){
					isReady = true;
					vote = target;
					return String.format("You set up your array of candles, orbs and artifacts, concentrating your efforts on %s", target);
				} else
					return String.format("%s is already dead.", target);
			}
		} else if (args[0].equals("!rest")){
			isReady = true;
			vote = null;
			return "You rest for tonight, confident that you already know all you need.";
		} else
			return null;
	}
	
	@Override
	public String nightEnd() {
		if (vote == null){
			return null;
		} else {
			return vote.isWolf() ?
				String.format("%s shows all the signs of lycantropy. They are undoubtedly a wolf, but do you dare to tell anyone?", vote)
				: String.format("%s, though certainly not an innocent person, do not appear to be a werewolf.", vote);
		}
	}
}