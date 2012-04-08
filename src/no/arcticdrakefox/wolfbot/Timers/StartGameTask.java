package no.arcticdrakefox.wolfbot.Timers;

import java.util.TimerTask;

import no.arcticdrakefox.wolfbot.WolfBot;
import no.arcticdrakefox.wolfbot.management.PlayerList;

public class StartGameTask extends TimerTask {

	private WolfBot parent;
	private PlayerList players;
	
	public StartGameTask (WolfBot parent, PlayerList players)
	{
		this.parent = parent;
		this.players = players;
	}
	
	@Override
	public void run()
	{
		players.reset();
		players.assignRoles();
		parent.sendRoleMessages();
		parent.setMode(parent.getModel ().getChannel (), "+m");
		parent.startDay();
	}

}
