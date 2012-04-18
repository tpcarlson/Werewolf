package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

public class Mayor extends Villager {

	public Mayor(String name) {
		super(name);
	}

	@Override
	public Role getRole ()
	{
		return Role.mayor;
	}
	
	@Override
	public String roleInfo(PlayerList players){
		return Messages.getString("Mayor.intro"); //$NON-NLS-1$
	}
	@Override
	public String helpText() {
		return Messages.getString("Mayor.help"); //$NON-NLS-1$
	}
}
