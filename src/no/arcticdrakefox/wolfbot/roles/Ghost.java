package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

public class Ghost extends Player {

	public Ghost(String name) {
		super(name);
	}

	String msg = null;
	
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
		return "Ghosts can comunicate with the living, one charactor at a time.";
	}

	@Override
	public String nightStart() {
		if (isAlive()) return null;
		return "What would you like to say to the living !Say <somthing>.";
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		if (!isAlive()) {
			String[] args = message.trim().split(" ", 2);
			if (args[0].equals("!say")) {
				if (args.length != 2)
					return "Correct usage: !say <Letter>";
				msg = args[1].trim().substring(0, 1);
				isReady = true;
				WolfBotModel.getInstance().sendIrcMessage(WolfBotModel.getInstance().getChannel(), 
						String.format("The voice of the dead can be heard pn the wind." +
								"You make out the letter '%s'", msg));
				return String.format("Your message '%s' will be relayed", msg);
			}
		}

		return null;
	}

	@Override
	public String nightEnd() {
		return null;
	}

	@Override
	public String helpText() {
		return "Once the ghost dies they will be able to communicate with the living.";
	}

}
