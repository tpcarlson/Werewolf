package no.arcticdrakefox.wolfbot.roles;

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
		return "You are a burly villager, the blacksmith's apprentice.";
	}
	
	@Override
	public String nightStart() {
		if (hit)
		{
			if (WolfBotModel.getInstance().getSilentMode()) {
				die (delayedCause);
			} else {
				die(String.format("In the dead of night %s finally succumbs to their wounds, blood pooling like tar by the anvil...", getName()));
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
				this.causeOfDeath = String.format ("%s is down but not out. It doesn't look like they'll live much longer though.", getName());
			}
		}
		else
		{
			super.die(causeOfDeath);
		}
	}
	@Override
	public String helpText() {
		return "The tough guy can survive a wolf's bite - for a while! He will succumb to his injuries the following evening.";
	}
}
