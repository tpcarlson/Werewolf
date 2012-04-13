package no.arcticdrakefox.wolfbot.predicates;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.model.Team;

import com.google.common.base.Predicate;

public class TeamPredicate implements Predicate<Player>
{
	Team team;
	
	public TeamPredicate (Team team)
	{
		this.team = team;
	}
	
	@Override
	public boolean apply(Player player) {
		return player.getRole().getTeam().equals(team);
	}
}