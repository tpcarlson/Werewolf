package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.Team;

public class ApprenticeScry extends Scry {

	private boolean active = false;
	private boolean informed = false;
	
	public ApprenticeScry(String name) {
		super(name);
		setTeam(Team.Villagers);
	}
	
	public void setActive() {
		active = true;
	}

	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.apprentice_scry;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return "You are the aprentive Scry, you will take over if the seer dies.";
	}

	@Override
	public String nightStart() {
		if (active && !informed) return "You are now the seer." + super.nightStart();
		if (active) return super.nightStart();
		return null;
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		if (active) return super.nightAction(message, players);
		return null;
	}

	@Override
	public String nightEnd() {
		if (active) return super.nightEnd();
		return null;
	}

}
