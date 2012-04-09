package no.arcticdrakefox.wolfbot.roles;

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
		return "You are the village Mayor and your opinion is well respected by your fellow villagers. Try not to get eaten or lynched!";
	}
	@Override
	public String helpText() {
		return "The Mayor is well respected by the villagers - his voice counts more than anyone else when it comes to lynching time!";
	}
}
