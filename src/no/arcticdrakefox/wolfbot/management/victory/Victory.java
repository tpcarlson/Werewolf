package no.arcticdrakefox.wolfbot.management.victory;

import no.arcticdrakefox.wolfbot.management.PlayerList;

public abstract class Victory implements Comparable<Victory>
{
	private int priority;

	public Victory (int priority)
	{
		this.priority = priority;
	}
	
	public int getPriority() {
		return priority;
	}

	/*
	 * isVictory must be called before getVictoryMessage
	 */
	public abstract boolean isVictory (PlayerList players);
	/*
	 * isVictory must be called before getVictoryMessage
	 */
	public abstract String getVictoryMessage(PlayerList players);
	
	public int compareTo (Victory comparator)
	{
		return comparator.getPriority() - this.priority;
	}

	
}
