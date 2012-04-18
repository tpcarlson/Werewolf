package no.arcticdrakefox.wolfbot.roles;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Role;

// Placeholder class for SkipPlayer...
public class SkipPlayer extends Player{

	public SkipPlayer() {
		super("Skipped!");
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isAlive ()
	{
		return true;
	}
	
	@Override
	public boolean isWolf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String roleInfo(PlayerList players) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String nightStart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String nightAction(String message, PlayerList players) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String nightEnd() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String helpText() {
		// TODO Auto-generated method stub
		return null;
	}

}
