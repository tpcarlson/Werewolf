package no.arcticdrakefox.wolfbot.management.victory;

import java.util.Collection;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.model.Team;
import no.arcticdrakefox.wolfbot.predicates.TeamPredicate;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;


public class LoneWolfVictory extends Victory {

	public LoneWolfVictory(int priority) {
		super(priority);
	}

	@Override
	public boolean isVictory(PlayerList players) {
		return players.getLivingPlayers().size() <= 2 && 
				players.getLivingPlayers().get(0).getRole().getTeam() == Team.LoneWolf;
	}

	@Override
	public String getVictoryMessage(PlayerList players) {
		if (players.getLivingPlayers().size() == 2) 
			return "The Lone Wolf turns on the last remaing player.";
		else
			return "A 'Lone Wolf' survived. Killing of villiagers and brother alike.";
	}

	@Override
	public boolean inhibitsOthersVictory(PlayerList players) {
		// Predicate<Player> p_wolves = new TeamPredicate(Team.Wolves);
		Predicate<Player> p_loneWolf = new TeamPredicate(Team.LoneWolf);
		// Predicate<Player> p_others = Predicates.and(Predicates.not(p_wolves), Predicates.not(p_loneWolf));
		
		
		Collection<Player> loneWolves = Collections2.filter(players.getLivingPlayers(), p_loneWolf);
		// Collection<Player> wolves = Collections2.filter(players.getLivingPlayers(), p_wolves);
		// Collection<Player> others = Collections2.filter(players.getLivingPlayers(), p_others);
		
//		if (others.isEmpty() && !isVictory(players)) {
//			for (Player p: loneWolves) {
//				p.die("Was killed by the remaing Wolves");
//				return false;
//			}
//		}
		
		return (!loneWolves.isEmpty());
	}
}
