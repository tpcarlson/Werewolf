package no.arcticdrakefox.wolfbot.model;

import java.util.Collection;
import java.util.Timer;

import no.arcticdrakefox.wolfbot.WolfBot;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.commands.Command;
import no.arcticdrakefox.wolfbot.management.commands.Commands;
import no.arcticdrakefox.wolfbot.management.victory.Victory;

import com.google.common.collect.Lists;

public class WolfBotModel {
	
	// New commands should have entries added here:
	private void initCommands ()
	{
		commands = Lists.newArrayList();
		
		commands.add(Commands.JOIN_COMMAND);
		commands.add(Commands.SET_COMMAND);
		commands.add(Commands.AUTOROLE_COMMAND);
		commands.add(Commands.LIST_COMMAND);
		commands.add(Commands.TIME_COMMAND);
		commands.add(Commands.ROLECOUNT_COMMAND);
		commands.add(Commands.START_COMMAND);
		commands.add(Commands.HELP_COMMAND);
		commands.add(Commands.VOTES_COMMAND);
		commands.add(Commands.SKIPLYNCH_COMMAND);
		commands.add(Commands.DROP_COMMAND);
		commands.add(Commands.LYNCH_COMMAND);
		commands.add(Commands.REVEAL_COMMAND);
		commands.add(Commands.END_COMMAND);
		commands.add(Commands.START_ON_NIGHT_COMMAND);
		commands.add(Commands.TOGGLE_SHOW_INVALID_COMMAND);
	}
	
	private void initVictoryConditions ()
	{
		victoryConditions = Lists.newArrayList();
		
		// The argument to the constructor here tells the game what priority you
		// wish to assign to this victory condition. See GameCore#checkVictory
		// for more.
		
		for(Team t : Team.values()) {
			victoryConditions.add(t.getVictory());
		}
		
	}
	
	private String channel;
	private String password;
	private PlayerList players;
	private State state;
	private Timer startGameTimer;
	private boolean enableNotices;
	private boolean silentMode = false;
	private boolean showInvalidCommandMessage = true;
	
	private Collection<Command> commands;
	private Collection<Victory> victoryConditions;
	
	/**
	 * @return True if the bot is to report invalid commands
	 */
	public boolean isShowInvalidCommandEnabled(){
	    return showInvalidCommandMessage;
	}
	
	/**
	 * Sets if the bot should report invalid commands or not
	 * @param status True if the invalid commands are to be reported
	 */
	public void setShowInvalidCommandEnabled(boolean status){
	    this.showInvalidCommandMessage = status;
	}
	
	public Collection<Victory> getVictoryConditions() {
		return victoryConditions;
	}

	public Collection<Command> getCommands() {
		return commands;
	}

	public void setSilentMode (boolean silentMode) {
		this.silentMode = silentMode;
	}
	
	public boolean getSilentMode() {
		return silentMode;
	}
	
	private static WolfBotModel instance;
	public static WolfBotModel getInstance ()
	{
		return instance;
	}
	
	private WolfBot wolfBot;
	
	public WolfBotModel(PlayerList players, State state, Timer startGameTimer,
			boolean enableNotices, WolfBot wolfBot) {
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
		this.wolfBot = wolfBot;
		Commands.setModel (this);
		initCommands ();
		initVictoryConditions ();
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

	public void sendIrcMessage(String channel2, String string) {
		wolfBot.sendIrcMessage(channel2, string);
	}

	/*
	 * Try and use this sparingly...
	 */
	public WolfBot getWolfBot() {
		return wolfBot;
	}

	private boolean startOnNight;

	public boolean isStartOnNight() {
		return startOnNight;
	}

	public void setStartOnNight(boolean startOnNight) {
		this.startOnNight = startOnNight;
	}
}