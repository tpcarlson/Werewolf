package no.arcticdrakefox.wolfbot;

import java.util.List;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.management.VoteTable;

import org.jibble.pircbot.PircBot;

public class WolfBot extends PircBot {
	public enum State {
		None, Day, Night
	}
	
	private String channel;
	private String password;
	private PlayerList players = new PlayerList();
	
	private State state = State.None;

	public static void main(String[] args) throws Exception {
		PircBot bot = new WolfBot("WolfBot", "[password-removed]");
		bot.setVerbose(true);
		bot.connect("irc.lessthan3.net");
		bot.joinChannel("#wolfbot");
	}
	
	private WolfBot(String name, String password){
		super();
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
				sendMessage(channel, "Correct usage is:  !drop [player]");
				return;
			}
			break;
		case "!set":
			if (args.length == 3){
				if (state != State.None){
					sendMessage(channel, "Don't mess with rolecount during the game. :/");
				} else {
					setCount(args[1], args[2].trim());
				}
					
			} else
				sendMessage(channel, "Correct usage is:  !set <role> <amount>");
			break;
		case "!autorole":
			if (state != State.None){
				sendMessage(channel, "Don't mess with rolecount during the game. :/");
			} else {
				players.autoRole();
				sendMessage(channel, players.roleCountToString());
			}
			break;
		case "!list":
			sendMessage(channel, StringHandler.listToString(players.getLivingPlayers()));
			break;
		case "!rolecount":
			sendMessage(channel, players.roleCountToString());
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
				sendMessage(channel, "Correct usage is: !lynch <target>");
			break;
		case "!votes":
			listVotes();
			break;
		case "!time":
			sendMessage(channel, "It is currently " + state);
			break;
		case "!help":
			if (args.length == 2)
				sendMessage(channel, help(args[1]));
			else 
				sendMessage(channel,
						"!join, !drop [player], !start, !end, !set <role> <count>, "
						+ "!list, !rolecount, !lynch/!vote/!kill, !votes, !time, !help"
				);
			break;
		case "!test":
			sendMessage(channel, String.format("Bluh"));
			break;
		default:
			sendMessage(channel, "Unknown command.");
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
				sendMessage(sender, String.format("You are a %s", player.getRole()));
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
				sendMessage(sender, StringHandler.listToString(players.getLivingPlayers()));
				break;
			case "!rolecount":
				sendMessage(sender, players.roleCountToString());
				break;
			case "!time":
				sendMessage(sender, "It is currently " + state);
				break;
			case "!help":
				if (args.length == 2)
					sendMessage(sender, help(args[1]));
				else
					sendMessage(sender,
							"!join, !drop, !list, !role, !rolecount, "
							+ "!time, !help, !ghost, !kill, !bane, !scry"
					);
				break;
			default:
				if (state == State.Night){
					if (player.isAlive()){
						String msg = player.nightAction(message, players);
						if (msg != null)
							sendMessage(sender, msg);
						if (players.allReady())
							endNight();	
					}
				} else
					sendMessage(sender, "Can only do actions at night.");
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
			sendMessage(channel, String.format("%s has fled, they were a %s", sourceNick, player.getRole()));
			drop(player.getName());
		}
	}
	
	@Override
	protected void onPart(String sourceNick, String sourceLogin, String sourceHostname, String reason){
		onQuit(sourceNick, sourceLogin, sourceHostname, reason);
	}
		
	private void join(String name){
		if (state != State.None){
			sendMessage(channel, name  + " cannot join now, game is in progress.");
		} else if (players.addPlayer(name)){
			sendMessage(channel, name + " has joined the game!");
		} else {
			sendMessage(channel, name + " is already entered.");
		}
	}
	
	private void drop(String name){
		if (players.removePlayer(name)){
			sendMessage(channel, name + " has retired from the game!");
		} else {
			sendMessage(channel, name + " wasn't found among the entered players.");
		}
		if (state != State.None)
			checkVictory();
	}
	
	private void setCount(String role, String amountS){
		int amount;
		if (role.toLowerCase().equals("villager")){
			sendMessage(channel, "Villagers are automatically adjusted.");
		} else if (StringHandler.isInt(amountS)){
			amount = StringHandler.parseInt(amountS);
			if (players.setRoleCount(role, amount))
				sendMessage(channel, String.format("%s%s set to %d", role,amount == 1 ? "s" : "" ,amount));
			else
				sendMessage(channel, String.format("Failed. Could not resolve %s to a role", role));
		} else {
			sendMessage(channel, amountS + " cannot be parsed to an int.");
		}
	}
	
	private boolean checkVictory(){
		int wolfCount = players.wolfCount();
		if (wolfCount < 1){
			sendMessage(channel, "With all wolves exterminated, the village is safe once again.");
			endGame();
			return true;
		} else if (wolfCount * 2 >= players.playerCount()){
			if (wolfCount == 1)
				sendMessage(channel, String.format(
						"After turning on the last remaining villager, %s prowls on to terrorize somewhere else.",
						StringHandler.listToString(players.getWolves()))
				);
			else
				sendMessage(channel, String.format(
						"%s turn on the last villagers. With all food depleted, they leave the village behind to find fresh meat elsewhere.",
						StringHandler.listToString(players.getWolves()))
				);
			endGame();
			return true;
		}
		return false;
	}
	
	private void startGame(){
		int playerCount = players.getList().size(); 
		if (playerCount < 3){
			sendMessage(channel, "Need at least three players to go.");
			return;
		} else if (playerCount < players.totalRoleCount()){
			sendMessage(channel, "There are more special roles than players!");
			return;
		}
		players.reset();
		players.assignRoles();
		sendRoleMessages();
		setMode(channel, "+m");
		startDay();
	}
	
	private void startDay(){
		sendMessage(channel, "It is now day. Vote for someone to lynch!");
		state = State.Day;
		voiceAll();
		players.clearVotes();
	}
	
	private void endDay(){
		Player vote = players.getVote();
		if (vote.isWolf()){
			vote.die(String.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
					+ "A voulenteer plunges the village's treasured silver dagger into their heart, and the wound catches fire! "
					+ "A werewolf was lynched today, and the village is a little safer. %s the %s is dead!",
					vote.getName(), vote.getRole())
			);
		} else {
			vote.die(String.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
					+ "A voulenteer plunges the village's treasured silver dagger into their heart. "
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
		sendMessage(channel, "It is now night, and most villagers can only sleep. Some forces are busily at work, however...");
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
			sendMessage(channel, player.getCauseOfDeath());
			deVoice(channel, player.getName());
		}
		players.clearRecentlyDead();
	}
	
	private void endGame(){
		state = State.None;
		players.reset();
		setMode(channel, "-m");
		deVoiceAll();
		sendMessage(channel, "Thanks for playing! Say !start to go again!");
	}
	
	private void sendRoleMessages(){
		List<Player> playerList = players.getLivingPlayers();
		for (Player player : playerList){
			String message = player.roleInfo(players);
			if (message != null)
				sendMessage(player.getName(), message);
		}
	}
	
	private void sendNightStartMessages(){
		List<Player> playerList = players.getLivingPlayers();
		for (Player player : playerList){
			String message = player.nightStart();
			if (message != null)
				sendMessage(player.getName(), message);
		}
	}
	
	private void sendNightEndMessages(){
		sendNightEndMessages(Player.Role.vigilante, false);
		sendNightEndMessages(Player.Role.devil, false);
		sendNightEndMessages(Player.Role.scry, false);
		sendNightEndMessages(Player.Role.ghost, true);
	}
	
	private void sendNightEndMessages(Player.Role role, boolean publicMessage){
		List<Player> playerList = players.getLivingPlayers();
		for (Player player : playerList){
			if (player.getRole() != role)
				continue;
			String message = player.nightEnd();
			if (message != null)
				sendMessage(publicMessage ? channel : player.getName(), message);
		}
	}
	
	private void killWolfVote(){
		Player wolfVote = players.getVote(true);
		if (wolfVote != null){
			 Player baner = players.getPlayerTargeting(wolfVote, Player.Role.baner);
			 if (baner != null){
				 wolfVote = null;
				 if (baner.getVote().equals(baner)){
					 sendMessage(
							baner.getName(),
							"You hear a wolf yelp as they step in your cleverly concealed beartrap. "
							+ "You rush out trying to finish the job, but the monster has already escaped! "
							+ "It seems as if you will both live to fight another day."
					);
				 } else { 
					 sendMessage(
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
		if (wolfVote == null || players.getPlayerTargeting(wolfVote, Player.Role.baner) != null){
			sendMessage(channel, "It appears the wolves didn't kill anybody tonight.");
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
			sendMessage(channel, "The game hasn't even started yet!");
			return;
		} else if (state !=  State.Day){
			sendMessage(channel, "You can only cast lynchvotes at day.");
			return;
		}
		Player sender = players.getPlayer(senderS);
		Player target = players.getPlayer(targetS);
		if (sender == null){
			sendMessage(channel, String.format("%s, you are not enterd in the game.", senderS));
		} else if (!sender.isAlive()){
			sendMessage(channel, String.format("%s, you are currently dead..", senderS));
		} else if (target == null){
			sendMessage(channel, String.format("%s, you may not vote for %s as they aren't enterd in the game.", senderS, targetS));
		} else if (!target.isAlive()){
			sendMessage(channel, String.format("%s, you may not vote for %s as they are currently dead.", senderS, targetS));
		} else {
			sender.vote(target);
			sendMessage(channel, String.format("%s, has voted for %s.", senderS, targetS));
		}
		if (checkLynchMajority())
			endDay();
	}
	
	private void listVotes(){
		if (state == State.None){
			sendMessage(channel, "The game hasn't even started yet!");
		} else if (state !=  State.Day){
			sendMessage(channel, "You can only view lynchvotes at day.");
		} else {
			sendMessage(channel, players.votesToString());
		}
	}
	
	private boolean checkLynchMajority(){
		List<Player> livingPlayers = players.getLivingPlayers();
		VoteTable table = new VoteTable(livingPlayers);
		return (table.getHighestVote() > livingPlayers.size() / 2);
	}
	
	private void voiceAll(){
		List<Player> livingPlayers = players.getLivingPlayers();
		String mode = "+";
		for (int i = 0; i < livingPlayers.size(); ++i)
			mode += "v";
		setMode(channel, mode + " " + StringHandler.listToStringSimple(livingPlayers));
	}
	
	private void deVoiceAll(){
		List<Player> playerList = players.getList();
		String mode = "-";
		for (int i = 0; i < playerList.size(); ++i)
			mode += "v";
		deVoice(channel, mode + " " + StringHandler.listToStringSimple(playerList));
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
		default:
			return "Unknown command";
		}
	}
}