package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

public class Vigilante extends Player {
	
	public Vigilante(String name){
		super(name);
	}
	
	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.vigilante;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return "You are the vigilante, capable of enforcing justice on your own at night.";
	}

	@Override
	public String nightStart() {
		isReady = false;
		return "Under the cover of darkness you may !kill a suspect or just !rest";
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
				if (target.isAlive()){
					if (target.equals(this))
						return "Killing yourself doesn't seem very productive.";
					else {
						vote(target);
						isReady = true;
						return String.format("You sharpen your blade. %s will die tonight.", target);
					}
				} else
					return String.format("You are too late to kill %s, they are dead already.", target);
			}
		} else if (args[0].equals("!rest")){
			isReady = true;
			vote = null;
			return "You deem it unwise to take anyone's life without more evidence. Tonight, you will gather your strenght.";
		} else
			return null;
	}
	
	@Override
	public String nightEnd() {
		if (vote == null)
			return null;
		else if (isAlive){
			if (vote.isAlive()){
				vote.die(String.format("As the villagers search %s's home, they find a silver blade stuck in their throat. A vigilante struck down this %s tonight!", vote, vote.getRole()));
				if (vote.isWolf()){
					return String.format("Under the cover of darkness, you hide behind %s's bedroom door and jab a silver knife in their throat as they pass. Their body slumps to the ground and reverts to its true, grotesque form. You have done your village a great service!", vote);
				} else{
					return String.format("Under the cover of darkness, you hide behind %s's bedroom door and jab a silver knife in their throat as they pass. As they lay bleeding, you realize that you have taken an innocent life. You mutter a respectful prayer and head home sullenly.", vote);
				}
			} else {
				return String.format("Under the cover of darkness, you sneak into %s's abode, only to find them dead already. Someone got here before you!", vote);
			}
		} else {
			return String.format("Under the cover of darkness, you open %s's door, but before you can step in you feel a searing pain running down your back. As your slump around on the ground, a large, gnarling shadow stands above you...", vote);
		}
	}

	@Override
	public String helpText() {
		return "The vigilante can attempt to !kill someone at night - hopefully a wolf, if they guess right. If not, then the villagers die even faster!";
	}
}
