package no.arcticdrakefox.wolfbot.management.victory;

import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.model.Team;
import static no.arcticdrakefox.wolfbot.WolfBot.bold;

import org.jibble.pircbot.Colors;

public class WolfVictory extends Victory
{
	public WolfVictory(int priority) {
		super(priority);
	}

	@Override
	public boolean isVictory(PlayerList players) {
		return players.wolfCount() * 2 >= players.playerCount();
	}

	@Override
	public String getVictoryMessage(PlayerList players)
	{
		if (players.wolfCount() == 1)
		{
			return String.format(
					"After turning on the last remaining %svillager%s, %s prowls on to terrorize somewhere else.",
					Team.Villagers.getColor(), Colors.NORMAL,
					StringHandler.listToString(players.getWolves()));
		}
		else
		{
			return String.format("%s turn on the last villagers. With all food depleted, " +
							"they leave the village behind to find fresh meat elsewhere.",
							bold(StringHandler.listToString(players.getWolves())));
		}
	}

	@Override
	public boolean inhibitsOthersVictory(PlayerList players) {
		return false;
	}

	
	
}
