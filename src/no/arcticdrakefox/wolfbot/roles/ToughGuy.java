package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.PlayerList;

public class ToughGuy extends Villager {

	private boolean hit = false;
	
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
			die(String.format("In the dead of night %s finally succombs to their wounds, blood pooling like tar by the anvil...", getName()));
		}
		
		return null;
	}
	
	@Override
	public void die (String causeOfDeath)
	{
		if (! hit)
		{
			this.causeOfDeath = String.format ("%s is down but not out. It doesn't look like they'll live much longer though.", getName());
		}
		else
		{
			super.die(causeOfDeath);
		}
	}
}
