package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

public class ToughGuy extends Villager {

	private boolean hit = false;
	private String delayedCause = null;
	
	public ToughGuy(String name) {
		super(name);
	}

	@Override
	public boolean isWolf() {
		return false;
	}

	@Override
	public Role getRole() {
		return Role.toughguy;
	}

	@Override
	public String roleInfo(PlayerList players) {
		return Messages.getString("ToughGuy.intro"); //$NON-NLS-1$
	}
	
	@Override
	public String nightStart() {
		if (hit)
		{
			if (WolfBotModel.getInstance().getSilentMode()) {
				die (delayedCause);
			} else {
				die(Messages.getString("ToughGuy.died", new Object[]{getName()})); //$NON-NLS-1$
			}
		}
		
		return null;
	}
	
	@Override
	public void die (String causeOfDeath)
	{
		if (! hit)
		{
			if (WolfBotModel.getInstance().getSilentMode()) {
				this.delayedCause = causeOfDeath;
				// In silent mode players just see no one died
			} else {
				this.causeOfDeath = Messages.getString("ToughGuy.wounded",new Object[]{getName()}); //$NON-NLS-1$
			}
		}
		else
		{
			super.die(causeOfDeath);
		}
	}
	@Override
	public String helpText() {
		return Messages.getString("ToughGuy.help"); //$NON-NLS-1$
	}
}
