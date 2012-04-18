package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

public class OldMan extends Villager {
	
	@Override
	public String roleInfo(PlayerList players) {
		return Messages.getString("OldMan.intro"); //$NON-NLS-1$
	}

	private int ttl;
	
	public OldMan(String name) {
		super(name);
		this.ttl = WolfBotModel.getInstance().getPlayers().getRole(Role.wolf).size() + 1;
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		return Messages.getString("OldMan.nightError"); //$NON-NLS-1$
	}
	
	@Override
	public String nightStart() {
		ttl--;
		return null;
	}
	
	@Override
	public String nightEnd() {
		if (ttl == 0)
		{
			die (Messages.getString("OldMan.died", new Object[] {getName()})); //$NON-NLS-1$
			return Messages.getString("OldMan.diedFeedback"); //$NON-NLS-1$
		}
		
		return null;
	}
	@Override
	public String helpText() {
		return Messages.getString("OldMan.help"); //$NON-NLS-1$
	}
}
