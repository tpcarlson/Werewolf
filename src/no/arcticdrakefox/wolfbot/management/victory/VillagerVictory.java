package no.arcticdrakefox.wolfbot.management.victory;

import no.arcticdrakefox.wolfbot.management.PlayerList;

public class VillagerVictory extends Victory{

	public VillagerVictory(int priority) {
		super(priority);
	}

	@Override
	public boolean isVictory(PlayerList players) {
		return players.wolfCount() < 1;
	}
	
	@Override
	public String getVictoryMessage(PlayerList players) {
		return "With all wolves exterminated, the village is safe once again.";
	}

	@Override
	public boolean inhibitsOthersVictory(PlayerList players) {
		return false;
	}
}
