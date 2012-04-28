package no.arcticdrakefox.wolfbot.Timers;

import java.util.Collection;
import java.util.Set;
import java.util.TimerTask;

import no.arcticdrakefox.wolfbot.management.GameCore;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Team;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

public class StartGameTask extends TimerTask {

	private WolfBotModel model;
	private PlayerList players;
	
	public StartGameTask (WolfBotModel model)
	{
		this.model = model;
		this.players = model.getPlayers();
	}
	
	@Override
	public void run()
	{
		players.reset();
		players.assignRoles();
		
		if (! verifyRoles ())
		{
			model.sendIrcMessage(model.getChannel(), "Must have more than one team to begin.");
		}
		else
		{
			model.getWolfBot ().sendRoleMessages();
			model.getWolfBot ().setMode(model.getChannel (), "+m");
			
			// Verify that we have enough players to start during the night:
			if (model.isStartOnNight() && players.getList().size() < 4)
			{
				model.setStartOnNight(false);
				model.sendIrcMessage(model.getChannel(), "Night start disabled - not enough players (Need 4+)");
			}
			
			if (model.isStartOnNight())
			{
				GameCore.startNight (model);
			}
			else
			{
				GameCore.startDay(model);
			}
		}
	}
	
	private boolean verifyRoles ()
	{
		// Count the teams:
		Collection<Player> allPlayers = model.getPlayers().getList();
		
		// Transform the players list into a teams set:
		Set<Team> teams = Sets.newHashSet(Collections2.transform(allPlayers, new Function<Player, Team> () {
			@Override
			public Team apply(Player arg0) {
				return arg0.getRole().getTeam();
			} }));
		
		return teams.size() > 1;
	}

}
