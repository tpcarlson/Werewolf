package no.arcticdrakefox.wolfbot.model;

import java.util.Timer;

import no.arcticdrakefox.wolfbot.management.PlayerList;

public class WolfBotModel {
	private String channel;
	private String password;
	private PlayerList players;
	private State state;
	private Timer startGameTimer;
	private boolean enableNotices;

	private static WolfBotModel instance;
	public static WolfBotModel getInstance ()
	{
		return instance;
	}
	
	public WolfBotModel(PlayerList players, State state, Timer startGameTimer,
			boolean enableNotices) {
		if (instance != null)
		{
			throw new RuntimeException ("May not instantiate model twice");
		}
		else
		{
			instance = this;
		}
		this.players = players;
		this.state = state;
		this.startGameTimer = startGameTimer;
		this.enableNotices = enableNotices;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public PlayerList getPlayers() {
		return players;
	}

	public void setPlayers(PlayerList players) {
		this.players = players;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Timer getStartGameTimer() {
		return startGameTimer;
	}

	public void setStartGameTimer(Timer startGameTimer) {
		this.startGameTimer = startGameTimer;
	}

	public boolean isEnableNotices() {
		return enableNotices;
	}

	public void setEnableNotices(boolean enableNotices) {
		this.enableNotices = enableNotices;
	}
}