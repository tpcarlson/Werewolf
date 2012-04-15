package no.arcticdrakefox.wolfbot.management.victory;

import java.util.Collection;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Team;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;
import no.arcticdrakefox.wolfbot.predicates.TeamPredicate;

import com.google.common.collect.Collections2;


public class LoneWolfVictory extends Victory {

	public LoneWolfVictory(int priority) {
		super(priority);
	}

	@Override
	public boolean isVictory(PlayerList players) {
		return players.getLivingPlayers().size() == 1 && 
				players.getLivingPlayers().get(0).getRole().getTeam() == Team.LoneWolf;
	}

	@Override
	public String getVictoryMessage(PlayerList players) {
		return "A 'Lone Wolf' survived. Killing of villiagers and brother alike.";
	}

	@Override
	public boolean inhibitsOthersVictory(PlayerList players) {
		WolfBotModel data = WolfBotModel.getInstance();
		
		Collection<Player> loneWolves = Collections2.filter(data.getPlayers().getList(), 
				new TeamPredicate (Team.LoneWolf));
		return !loneWolves.isEmpty();
	}
}
