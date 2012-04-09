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
		return "You are the apprentice Scry. You will take over if your master dies.";
	}

	@Override
	public String nightStart() {
		if (active && !informed) return "Your master has been killed - you now have his powers." + super.nightStart();
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
	@Override
	public String helpText() {
		return "The apprentice scry has been learning from his master for years. If his master dies, he will gain his scrying power.";
	}
}
