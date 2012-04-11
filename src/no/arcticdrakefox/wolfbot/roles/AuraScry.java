package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

public class AuraScry extends Player {

	public AuraScry(String name) {
		super(name);
	}


	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.aura_scry;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return	"You are the Aura Scry, each night you will be able to see whether " +
				"a player has a special ability.";
	}

	@Override
	public String nightStart() {
		isReady = false;
		return "The stars smile on you tonight. You may !scry a person or just !rest";
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
			Role r = vote.getRole();
			return (r != Role.villager) && (r!=Role.wolf) ?
				String.format("%s has an aura. They clearly have some power, though you know not what it may be.", vote)
				: String.format("%s does not appear to have any powers, whatever their crimes.", vote);
		}
	}

	@Override
	public String helpText() {
		return "The aura scry can look into the spirit world to sense the mental strength of others. He can focus on someone at night and will know if they have any special powers - but won't know what they are! ";
	}

}
