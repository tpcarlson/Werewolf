package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.PlayerList;

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
		return "You are the village Mayor and your opinion is well respected by your fellow villagers. Try not to get eaten or lynched!";
	}
}
