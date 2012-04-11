package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.Team;

public class Mason extends Player {
	
	public Mason(String name){
		super(name);
		setTeam (Team.Villagers);
	}
	
	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.mason;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return String.format("Since childhood, you have had close friends."
				+"Ones you know you can absolutely depend upon even in situations like this"
				+"The masons are %s",
				StringHandler.listToString(players.getRole(Role.mason))
		);
	}

	@Override
	public String nightStart() {
		return null;
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		return "There is nothing for you to do at night but rest and hope for the best.";
	}
	
	@Override
	public String nightEnd() {
		return null;
	}

	@Override
	public String helpText() {
		return "The masons have only one difference from the standard villager - they know who all the other masons are.";
	}
}
