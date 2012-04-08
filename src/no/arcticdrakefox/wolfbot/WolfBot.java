package no.arcticdrakefox.wolfbot;

import java.util.List;
import java.util.Timer;

import no.arcticdrakefox.wolfbot.Timers.StartGameTask;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.management.VoteTable;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.State;
import no.arcticdrakefox.wolfbot.model.Team;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;

public class WolfBot extends PircBot {
	private String channel;
	private String password;
	private PlayerList players = new PlayerList();
	
	private State state = State.None;

	public static void main(String[] args) throws Exception {
		PircBot bot = new WolfBot("WolfBot", "ruffruff");
		bot.setVerbose(true);
		bot.connect("irc.lessthan3.net");
		bot.joinChannel("#wolfbot");
	}
	
	private WolfBot(String name, String password){
		super();
		setMessageDelay(200);
		this.setName(name);
		this.password = password;
	}
	
	@Override
	protected void onJoin(String channel, String sender, String login, String hostname){
		identify(password);
		this.channel = channel;
	}
	
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message){
		message = StringHandler.stripColour(message).trim();
		if (message.charAt(0) != '!')
			return;
		String[] args = message.split(" ");
		String command = args[0];
		
		switch (command.toLowerCase()){
		case "!join":
			join(sender);
			break;
		case "!drop":
			if (args.length == 2)
				drop(args[1]);
			else if (args.length == 1)
				drop(sender);
			else {
				sendIrcMessage(channel, "Correct usage is:  !drop [player]");
				return;
			}
			break;
		case "!set":
			if (args.length == 3){
				if (state != State.None){
					sendIrcMessage(channel, "Don't mess with rolecount during the game. :/");
				} else {
					setCount(args[1], args[2].trim());
				}
					
			} else
				sendIrcMessage(channel, "Correct usage is:  !set <role> <amount>");
			break;
		case "!autorole":
			if (state != State.None){
				sendIrcMessage(channel, "Don't mess with rolecount during the game. :/");
			} else {
				players.autoRole();
				sendIrcMessage(channel, players.roleCountToString());
			}
			break;
		case "!list":
			sendIrcMessage(channel, StringHandler.listToString(players.getLivingPlayers()));
			break;
		case "!rolecount":
			sendIrcMessage(channel, players.roleCountToString());
			break;
		case "!start":
			startGame();
			break;
		case "!end":
			endGame();
			break;
		/*
		case "!thatwillbeallthankyou":
			disconnect();
			break;
		*/
		case "!lynch":
		case "!kill":
		case "!vote":
			if (args.length == 2)
				lynchVote(sender, args[1]);
			else
				sendIrcMessage(channel, "Correct usage is: !lynch <target>");
			break;
		case "!votes":
			listVotes();
			break;
		case "!time":
			sendIrcMessage(channel, "It is currently " + state);
			break;
		case "!help":
			if (args.length == 2)
				sendIrcMessage(channel, help(args[1]));
			else 
				sendIrcMessage(channel,
						"!join, !drop [player], !start, !end, !set <role> <count>, "
						+ "!list, !rolecount, !lynch/!vote/!kill, !votes, !time, !help"
				);
			break;
		case "!test":
			sendIrcMessage(channel, String.format("Bluh"));
			break;
		case "!notices":
			if (args.length == 2)
			{
				if (args[1].equalsIgnoreCase("on"))
				{
					enableNotices = true;
					sendIrcMessage (channel, "Notices enabled");
					break;
				}
				else if (args[1].equalsIgnoreCase("off"))
				{
					enableNotices = false;
					sendIrcMessage (channel, "Notices disabled");
					break;
				}
			}
			
			// Usage:
			sendIrcMessage(channel, "Correct usage is:  !notices on|off");
		default:
			sendIrcMessage(channel, "Unknown command.");
		}
	}
	
	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message){
		message = StringHandler.stripColour(message).trim();
		if (message.charAt(0) != '!')
			return;
		String[] args = message.split(" ");
		String command = args[0];
		Player player = players.getPlayer(sender);
		if (player == null)
			return;
		switch (command.toLowerCase()){
			case "!role":
				sendIrcMessage(sender, String.format("You are a %s", player.getRole()));
				break;
			case "!join":
				join(sender);
				break;
			case "!drop":
				if (args.length == 1)
					drop(sender);
				checkVictory();
				break;
			case "!list":
				sendIrcMessage(sender, StringHandler.listToString(players.getLivingPlayers()));
				break;
			case "!rolecount":
				sendIrcMessage(sender, players.roleCountToString());
				break;
			case "!time":
				sendIrcMessage(sender, "It is currently " + state);
				break;
			case "!help":
				if (args.length == 2)
					sendIrcMessage(sender, help(args[1]));
				else
					sendIrcMessage(sender,
							"!join, !drop, !list, !role, !rolecount, "
							+ "!time, !help, !ghost, !kill, !bane, !scry"
					);
				break;
			default:
				if (state == State.Night){
					if (player.isAlive()){
						String msg = player.nightAction(message, players);
						if (msg != null)
							sendIrcMessage(sender, msg);
						if (players.allReady())
							endNight();	
					}
				} else
					sendIrcMessage(sender, "Can only do actions at night.");
		}
	}
	
	@Override
	protected void onNickChange(String oldNick, String login, String hostname, String newNick){
		Player player = players.getPlayer(oldNick);
		if (player != null)
			player.rename(newNick);
	}
	
	@Override
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason){
		Player player = players.getPlayer(sourceNick);
		if (player != null){
			sendIrcMessage(channel, String.format("%s has fled, they were a %s", sourceNick, player.getRole()));
			drop(player.getName());
		}
	}
	
	@Override
	protected void onPart(String sourceNick, String sourceLogin, String sourceHostname, String reason){
		onQuit(sourceNick, sourceLogin, sourceHostname, reason);
	}
		
	private void join(String name){
		if (state != State.None){
			sendIrcMessage(channel, name  + " cannot join now, game is in progress.");
		} else if (players.addPlayer(name)){
			sendIrcMessage(channel, name + " has joined the game!");
		} else {
			sendIrcMessage(channel, name + " is already entered.");
		}
	}
	
	private void drop(String name){
		if (players.removePlayer(name)){
			sendIrcMessage(channel, name + " has retired from the game!");
		} else {
			sendIrcMessage(channel, name + " wasn't found among the entered players.");
		}
		if (state != State.None)
			checkVictory();
	}
	
	private void setCount(String role, String amountS){
		int amount;
		if (role.toLowerCase().equals("villager")){
			sendIrcMessage(channel, "Villagers are automatically adjusted.");
		} else if (StringHandler.isInt(amountS)){
			amount = StringHandler.parseInt(amountS);
			if (players.setRoleCount(role, amount))
				sendIrcMessage(channel, String.format("%s%s set to %d", role,amount == 1 ? "s" : "" ,amount));
			else
				sendIrcMessage(channel, String.format("Failed. Could not resolve %s to a role", role));
		} else {
			sendIrcMessage(channel, amountS + " cannot be parsed to an int.");
		}
	}
	
	private boolean checkVictory(){
		int wolfCount = players.wolfCount();
		if (wolfCount < 1){
			sendIrcMessage(channel, "With all wolves exterminated, the village is safe once again.");
			endGame();
			return true;
		} else if (wolfCount * 2 >= players.playerCount()){
			if (wolfCount == 1)
				sendIrcMessage(channel, String.format(
						"After turning on the last remaining villager, %s prowls on to terrorize somewhere else.",
						StringHandler.listToString(players.getWolves()))
				);
			else
				sendIrcMessage(channel, String.format(
						"%s turn on the last villagers. With all food depleted, they leave the village behind to find fresh meat elsewhere.",
						StringHandler.listToString(players.getWolves()))
				);
			endGame();
			return true;
		}
		return false;
	}
	
	private Timer startGameTimer = new Timer ();
	
	private void startGame(){
		
		// Drop any starts that happen after the game has started
		if (state != State.None)
		{
			return;
		}
		
		int playerCount = players.getList().size(); 
		if (playerCount < 3){
			sendIrcMessage(channel, "Need at least three players to go.");
			return;
		} else if (playerCount < players.totalRoleCount()){
			sendIrcMessage(channel, "There are more special roles than players!");
			return;
		}
		startGameTimer = new Timer (); // Make a new timer every time
		// Fire off a 30s timer:
		startGameTimer.schedule(new StartGameTask (this, players), 30*1000); // 30s
		sendIrcMessage (channel, "The game will begin in " + Colors.BOLD + "30 seconds." + Colors.NORMAL + " Type !join to join!");
	}
	
	public void startDay(){
		sendIrcMessage(channel, "It is now day. Vote for someone to lynch!");
		state = State.Day;
		voiceAll();
		players.clearVotes();
	}
	
	private void endDay(){
		Player vote = players.getVote();
		if (vote.isWolf()){
			vote.die(String.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
					+ "A volunteer plunges the village's treasured silver dagger into their heart, and the wound catches fire! "
					+ "A werewolf was lynched today, and the village is a little safer. %s the %s is dead!",
					vote.getName(), vote.getRole())
			);
		} else {
			vote.die(String.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
					+ "A volunteer plunges the village's treasured silver dagger into their heart. "
					+ "They scream in agony as life and blood leave their body. %s the %s is dead!",
					vote.getName(), vote.getRole())
			);
		}
		checkDead();
		if (!checkVictory())
			startNight();
	}
	
	private void startNight(){
		state = State.Night;
		deVoiceAll();
		sendIrcMessage(channel, "It is now night, and most villagers can only sleep. Some forces are busily at work, however...");
		players.clearVotes();
		sendNightStartMessages();
	}
	
	private void endNight(){
		killWolfVote();
		sendNightEndMessages();
		checkDead();
		if (!checkVictory())
			startDay();
	}
	
	private void checkDead(){
		List<Player> deceased = players.getRecentlyDead();
		for (Player player : deceased){
			sendIrcMessage(channel, player.getCauseOfDeath());
			deVoice(channel, player.getName());
		}
		players.clearRecentlyDead();
	}
	
	private void endGame(){
		startGameTimer.cancel(); // Kill the existing timer, if we have one
		state = State.None;
		players.reset();
		setMode(channel, "-m");
		deVoiceAll();
		sendIrcMessage(channel, "Thanks for playing! Say !start to go again!");
	}
	
	public void sendRoleMessages(){
		List<Player> playerList = players.getLivingPlayers();
		for (Player player : playerList){
			String message = player.roleInfo(players);
			if (message != null)
			{
				sendIrcMessage(player.getName(), message);
			}
			
			Team team = player.getTeam();
			switch (team)
			{
				case Wolves:
					sendIrcMessage (player.getName(), "You are on the " + Colors.RED + "wolf" + Colors.NORMAL + " team. You must attempt to eat the " + Colors.BLUE + " villagers!");
					break;
				case Villagers:
					sendIrcMessage (player.getName(), "You are on the " + Colors.BLUE + "villager" + Colors.NORMAL + " team. Defend against the invading " + Colors.RED + "wolf" + Colors.NORMAL + " incursion!");
					break;
				default:
					sendIrcMessage (player.getName(), "You are on an unknown team. Something has probably gone wrong here.");
			}
		}
	}
	
	private void sendNightStartMessages(){
		List<Player> playerList = players.getLivingPlayers();
		for (Player player : playerList){
			String message = player.nightStart();
			if (message != null)
				sendIrcMessage(player.getName(), message);
		}
	}
	
	private void sendNightEndMessages(){
		sendNightEndMessages(Role.vigilante, false);
		sendNightEndMessages(Role.devil, false);
		sendNightEndMessages(Role.scry, false);
		sendNightEndMessages(Role.ghost, true);
	}
	
	private void sendNightEndMessages(Role role, boolean publicMessage){
		List<Player> playerList = players.getLivingPlayers();
		for (Player player : playerList){
			if (player.getRole() != role)
				continue;
			String message = player.nightEnd();
			if (message != null)
				sendIrcMessage(publicMessage ? channel : player.getName(), message);
		}
	}
	
	private void killWolfVote(){
		Player wolfVote = players.getVote(true);
		if (wolfVote != null){
			 Player baner = players.getPlayerTargeting(wolfVote, Role.baner);
			 if (baner != null){
				 wolfVote = null;
				 if (baner.getVote().equals(baner)){
					 sendIrcMessage(
							baner.getName(),
							"You hear a wolf yelp as they step in your cleverly concealed beartrap. "
							+ "You rush out trying to finish the job, but the monster has already escaped! "
							+ "It seems as if you will both live to fight another day."
					);
				 } else { 
					 sendIrcMessage(
							 baner.getName(),
							 String.format(
									 "Your detective skills have paid off!"
									 + "As you spot a wolf about to break into %s house, you pounce upon him from the rooftop and brawl valiantly. "
									 + "Clearly not expecting resitance tonight, the wolf flees in surprise." 
									 + "The purple avenger has done a good deed tonight!",
									 baner.getVote()
							 )
					);
				 }
			 }
		}
		if (wolfVote == null || players.getPlayerTargeting(wolfVote, Role.baner) != null){
			sendIrcMessage(channel, "It appears the wolves didn't kill anybody tonight.");
		} else {
			wolfVote.die(String.format(
					"As the villagers gather, they notice someone missing. "
					+ "After some searching, their mauled corpse is found in their home. "
					+ "%s the %s is dead!",
					wolfVote.getName(), wolfVote.getRole()
				)
			);
		}
	}
	
	private void lynchVote(String senderS, String targetS){
		if (state == State.None){
			sendIrcMessage(channel, "The game hasn't even started yet!");
			return;
		} else if (state !=  State.Day){
			sendIrcMessage(channel, "You can only cast lynchvotes at day.");
			return;
		}
		Player sender = players.getPlayer(senderS);
		Player target = players.getPlayer(targetS);
		if (sender == null){
			sendIrcMessage(channel, String.format("%s, you are not enterd in the game.", senderS));
		} else if (!sender.isAlive()){
			sendIrcMessage(channel, String.format("%s, you are currently dead..", senderS));
		} else if (target == null){
			sendIrcMessage(channel, String.format("%s, you may not vote for %s as they aren't enterd in the game.", senderS, targetS));
		} else if (!target.isAlive()){
			sendIrcMessage(channel, String.format("%s, you may not vote for %s as they are currently dead.", senderS, targetS));
		} else {
			sender.vote(target);
			sendIrcMessage(channel, String.format("%s has voted for %s.", senderS, targetS));
		}
		if (checkLynchMajority())
			endDay();
	}
	
	private void listVotes(){
		if (state == State.None){
			sendIrcMessage(channel, "The game hasn't even started yet!");
		} else if (state !=  State.Day){
			sendIrcMessage(channel, "You can only view lynchvotes at day.");
		} else {
			String nonVoters = players.nonvotersToString();
			sendIrcMessage(channel, players.votesToString());
			
			if (! nonVoters.isEmpty())
			{
				sendIrcMessage (channel, "Not voted: " + players.nonvotersToString());
			}
		}
	}
	
	private boolean checkLynchMajority(){
		List<Player> livingPlayers = players.getLivingPlayers();
		VoteTable table = new VoteTable(livingPlayers);
		return (table.getHighestVote() > livingPlayers.size() / 2);
	}
	
	private void voiceAll(){
		massMode (players.getLivingPlayers(), true, "v");
	}
	
	private void deVoiceAll(){
		massMode (players.getList(), false, "v");
	}
	
	private void massMode (List<Player> toChange, boolean add, String mode)
	{
		String modeToApply = "";
		if (add) { modeToApply += "+"; } else { modeToApply+= "-"; }
		for (int i = 0; i < toChange.size(); ++i)
			modeToApply += mode;
		setMode(channel, modeToApply + " " + StringHandler.listToStringSimplePlayers(toChange));		
	}
	
	private String help(String command){
		switch (command){
		case "join":
			return "!join: Signs up for the next game. Can only be used when the game is not in progress.";
		case "drop":
			return "!drop [player]: Removes yourself or specified user from game. Don't be a jerk and use it nilly-willy!";
		case "start":
			return "!start: Starts the game! Will restart if the game is in progress.";
		case "end":
			return "!end: Ends game in progress.";
		case "set":
			return "!set <role> <count>: sets specified role to specified amount. Valid roles are wolf, devil, mason, scry, vigilante, baner, ghost";
		case "list":
			return "!list: Shows all living players in game.";
		case "rolecount":
			return "!rolecount: Shows amount of each role the game started with. It will not adapt as roles are killed off.";
		case "lynch":
		case "vote":
			return "!lynch <player>: Votes on a player for lynching. Over half the villagers must agree on a target before day ends.";
		case "votes":
			return "!votes: Displays who voted for which player.";
		case "time":
			return "!time: Shows wether it is night or day. Returns None if game hasn't started.";
		case "help":
			return "!help [command]: Returns a list of all commands (differnt if you PM) or shows description of supplied command.";
		case "role":
			return "!role: Tells you which role you are and what the role does. (PM only)";
		case "ghost":
			return "!ghost <target>: Selects a target for ressurection. If you're killed at night end it will not work. (PM only)";
		case "kill":
			return "!kill <player>: Selects a target for killing as either wolf, devil or vigilante. Wolves just vote for a target not unlike villagers at day. (PM only)";
		case "scry":
			return "!scry <player>: Selects a target for scrying. Scries check wether or not a person is a wolf. Devil retrieve the person's exact role. (PM only)";
		case "bane":
			return "!bane <player>: Selects a target to protect from wolves. You can protect yourself. (PM only)";
		case "rest":
			return "!rest <players>: Opts out of night action. (PM only)";
		case "notices":
			return "!notices on|off: Enable or disable notice messaging";
		default:
			return "Unknown command";
		}
	}
	
	boolean enableNotices = true;
	// This will allow for the bot to be customised at runtime to either send notices or messages
	// By default, send notices.
	public void sendIrcMessage (String target, String message)
	{
		if (enableNotices && target.equalsIgnoreCase(channel)) // Note that we always send straight-up messages for PMs.
		{
			sendNotice (target, message);
		}
		else
		{
			sendMessage(target, message);
		}
	}

	// @author tpcarlson
	// TODO: Refactor so we have a real model and controller here ...
	public String getChannel() {
		return channel;
	}
}