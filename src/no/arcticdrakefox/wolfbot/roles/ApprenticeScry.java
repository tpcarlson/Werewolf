package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

public class ApprenticeScry extends Scry {

	private boolean active = false;
	private boolean informed = false;
	
	public ApprenticeScry(String name) {
		super(name);
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
		return Messages.getString("ApprenticeScry.intro"); //$NON-NLS-1$
	}

	@Override
	public String nightStart() {
		if (active && !informed) return Messages.getString("ApprenticeScry.upgrade") + super.nightStart(); //$NON-NLS-1$
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
		return Messages.getString("ApprenticeScry.help"); //$NON-NLS-1$
	}
}
