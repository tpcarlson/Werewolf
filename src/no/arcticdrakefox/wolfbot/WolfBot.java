package no.arcticdrakefox.wolfbot;

import java.util.List;
import java.util.Timer;

import no.arcticdrakefox.wolfbot.Timers.StartGameTask;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerFactory;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.management.VoteTable;
import no.arcticdrakefox.wolfbot.management.WerewolfException;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.State;
import no.arcticdrakefox.wolfbot.model.Team;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;

public class WolfBot extends PircBot {

	public static void main(String[] args) throws Exception {
		PircBot bot = new WolfBot("WolfBot", "ruffruff");
		bot.setVerbose(true);
		bot.connect("irc.lessthan3.net");
		bot.joinChannel("#wolfbot");
	}

	
	WolfBotModel data = new WolfBotModel(new PlayerList(), State.None,
			new Timer(), true);

	private WolfBot(String name, String password) {
		super();
		setMessageDelay(200);
		this.setName(name);
		this.data.setPassword(password);
	}

	@Override
	protected void onJoin(String channel, String sender, String login,
			String hostname) {
		identify(data.getPassword());
		this.data.setChannel(channel);
	}

	@Override
	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		message = StringHandler.stripColour(message).trim();
		if (message.charAt(0) != '!')
			return;
		String[] args = message.split(" ");
		String command = args[0];

		switch (command.toLowerCase()) {
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
				if (data.getState() == State.None || data.getState() == State.Starting){
					setCount(args[1], args[2].trim());
				} else {
					sendIrcMessage(channel, "Don't mess with rolecount during the game. :/");
				}

			} else
				sendIrcMessage(channel,
						"Correct usage is:  !set <role> <amount>");
			break;
		case "!autorole":
			if (data.getState() == State.None || data.getState() == State.Starting){
				data.getPlayers().autoRole();
				sendIrcMessage(channel, data.getPlayers().roleCountToString());
			} else {
				sendIrcMessage(channel, "Don't mess with rolecount during the game. :/");
			}
			break;
		case "!list":
			sendIrcMessage(channel, StringHandler.listToString(data
					.getPlayers().getLivingPlayers()));
			break;
		case "!rolecount":
			sendIrcMessage(channel, data.getPlayers().roleCountToString());
			break;
		case "!start":
			startGame();
			break;
		case "!end":
			endGame();
			break;
		/*
		 * case "!thatwillbeallthankyou": disconnect(); break;
		 */
			
		case "!anondeath":
			if (args.length == 2 && args[1] == "off")
				WolfBotModel.getInstance().setSilentMode(false);
			if (args.length == 2 && args[1] == "on")
				WolfBotModel.getInstance().setSilentMode(true);	
			else
				sendIrcMessage(channel, "Correct usage is: !anondeath on|off");
			break;
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
			sendIrcMessage(channel, "It is currently " + data.getState());
			break;
		case "!help":
			if (args.length == 2)
				sendIrcMessage(channel, help(args[1]));
			if (args.length == 2)
				sendIrcMessage(channel, help(args[1], args[2]));
			else
				sendIrcMessage(
						channel,
						"!join, !drop [player], !start, !end, !set <role> <count>, "
								+ "!list, !rolecount, !autorole, !lynch/!vote/!kill, !votes, !time, !help");
			break;
		case "!test":
			sendIrcMessage(channel, String.format("Bluh"));
			break;
		case "!notices":
			if (args.length == 2) {
				if (args[1].equalsIgnoreCase("on")) {
					data.setEnableNotices(true);
					sendIrcMessage(channel, "Notices enabled");
					break;
				} else if (args[1].equalsIgnoreCase("off")) {
					data.setEnableNotices(false);
					sendIrcMessage(channel, "Notices disabled");
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
	protected void onPrivateMessage(String sender, String login,
			String hostname, String message) {
		message = StringHandler.stripColour(message).trim();
		if (message.charAt(0) != '!')
			return;
		String[] args = message.split(" ");
		String command = args[0];
		Player player = data.getPlayers().getPlayer(sender);
		if (player == null)
			return;
		switch (command.toLowerCase()) {
		case "!role":
			sendIrcMessage(sender,
					String.format("You are a %s", player.getRole()));
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
			sendIrcMessage(sender, StringHandler.listToString(data.getPlayers()
					.getLivingPlayers()));
			break;
		case "!rolecount":
			sendIrcMessage(sender, data.getPlayers().roleCountToString());
			break;
		case "!time":
			sendIrcMessage(sender, "It is currently " + data.getState());
			break;
		case "!help":
			if (args.length == 2)
				sendIrcMessage(sender, help(args[1]));
			else
				sendIrcMessage(sender,
						"!join, !drop, !list, !role, !rolecount, !autorole, "
								+ "!time, !help, !ghost, !kill, !bane, !scry");
			break;
		default:
			if (data.getState() == State.Night) {
				if (player.isAlive()) {
					String msg = player.nightAction(message, data.getPlayers());
					if (msg != null)
						sendIrcMessage(sender, msg);
					if (data.getPlayers().allReady())
						endNight();
				}
			} else
				sendIrcMessage(sender, "Can only do actions at night.");
		}
	}

	@Override
	protected void onNickChange(String oldNick, String login, String hostname,
			String newNick) {
		Player player = data.getPlayers().getPlayer(oldNick);
		if (player != null)
			player.rename(newNick);
	}

	@Override

	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason){
		Player player = data.getPlayers().getPlayer(sourceNick);
		if (player != null){
			// ie. We're in day or night ...
			if (! (data.getState() == State.None || data.getState() == State.Starting))
			{
				sendIrcMessage(data.getChannel(), String.format("%s has fled, they were a %s", sourceNick, player.getRole()));
			}
			drop(player.getName());
		}
	}

        @Override
	protected void onPart(String sourceNick, String sourceLogin, String sourceHostname, String reason){
		onQuit(sourceLogin, sourceNick, sourceHostname, reason);
	}

	private void join(String name) {
		if (!(data.getState() == State.None || data.getState() == State.Starting)) {
			sendIrcMessage(data.getChannel(), name
					+ " cannot join now, game is in progress.");
		} else if (data.getPlayers().addPlayer(name)) {
			sendIrcMessage(data.getChannel(), name + " has joined the game!");
		} else {
			sendIrcMessage(data.getChannel(), name + " is already entered.");
		}
	}
	
	private void drop(String name){
		if (data.getState() == State.None || data.getState() == State.Starting)
		{
			// We still need to remove from the game:
			boolean playerRemoved = data.getPlayers().removePlayer(name);
			if (playerRemoved)
			{
				// And send a neutral message:
				sendIrcMessage (data.getChannel(), name + " has retired from the game - before it even started! What a coward.");
			}
			else
			{
				sendIrcMessage(data.getChannel(), name + " wasn't found among the entered players.");
			}
			return;
		}

		if (data.getPlayers().removePlayer(name)) {
			sendIrcMessage(data.getChannel(), name
					+ " has retired from the game!");
		} else {
			sendIrcMessage(data.getChannel(), name
					+ " wasn't found among the entered players.");
		}
		if (data.getState() != State.None && data.getState() != State.Starting)
			checkVictory();
	}

	private void setCount(String role, String amountS) {
		int amount;
		if (role.toLowerCase().equals("villager")) {
			sendIrcMessage(data.getChannel(),
					"Villagers are automatically adjusted.");
		} else if (StringHandler.isInt(amountS)) {
			amount = StringHandler.parseInt(amountS);
			try
			{
				if (data.getPlayers().setRoleCount(role, amount))
					sendIrcMessage(data.getChannel(), String.format("%s%s set to %d", role,amount == 1 ? "s" : "" ,amount));
				else // Should never get here
					throw new WerewolfException ("Meep");
			}
			catch (WerewolfException wolfy)
			{
				sendIrcMessage(data.getChannel(), String.format("Failed. Could not resolve %s to a role", role));
			}
		} else {
			sendIrcMessage(data.getChannel(), amountS
					+ " cannot be parsed to an int.");
		}
	}

	private boolean checkVictory() {
		int wolfCount = data.getPlayers().wolfCount();
		if (wolfCount < 1) {
			sendIrcMessage(data.getChannel(),
					"With all wolves exterminated, the village is safe once again.");
			endGame();
			return true;
		} else if (wolfCount * 2 >= data.getPlayers().playerCount()) {
			if (wolfCount == 1)
				sendIrcMessage(
						data.getChannel(),
						String.format(
								"After turning on the last remaining villager, %s prowls on to terrorize somewhere else.",
								StringHandler.listToString(data.getPlayers()
										.getWolves())));
			else
				sendIrcMessage(
						data.getChannel(),
						String.format(
								"%s turn on the last villagers. With all food depleted, they leave the village behind to find fresh meat elsewhere.",
								StringHandler.listToString(data.getPlayers()
										.getWolves())));
			endGame();
			return true;
		}
		return false;
	}

	private void startGame() {

		// Drop any starts that happen after the game has started
		if (data.getState() != State.None && data.getState() != State.Starting)
		{
			return;
		}

		if (data.getState() == State.Starting) {
			sendIrcMessage(data.getChannel(),
					"The game is already starting. Use !end to stop starting.");
			return;
		}

		int playerCount = data.getPlayers().getList().size();
		if (playerCount < 3) {
			sendIrcMessage(data.getChannel(),
					"Need at least three players to go.");
			return;
		} else if (playerCount < data.getPlayers().totalRoleCount()) {
			sendIrcMessage(data.getChannel(),
					"There are more special roles than players!");
			return;
		}
		data.setStartGameTimer(new Timer()); // Make a new timer every time
		// Fire off a 30s timer:
		data.getStartGameTimer().schedule(
				new StartGameTask(this, data.getPlayers()), 30 * 1000); // 30s
		sendIrcMessage(data.getChannel(), "The game will begin in "
				+ Colors.BOLD + "30 seconds." + Colors.NORMAL
				+ " Type !join to join!");
		data.setState(State.Starting);
	}

	public void startDay() {
		sendIrcMessage(data.getChannel(),
				"It is now day. Vote for someone to lynch!");
		data.setState(State.Day);
		voiceAll();
		data.getPlayers().clearVotes();
	}

	private void endDay() {
		Player vote = data.getPlayers().getVote();
		if (WolfBotModel.getInstance().getSilentMode()) {
			vote.die(String
					.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
							+ "A volunteer plunges the village's treasured silver dagger into their heart(a bread knife would do)! "
							+ "*%s* is dead!",
							vote.getName()));
		} else {
			if (vote.isWolf()) {
				vote.die(String
						.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
								+ "A volunteer plunges the village's treasured silver dagger into their heart, and the wound catches fire! "
								+ "A *werewolf* was lynched today, and the village is a little safer. *%s* the *%s* is dead!",
								vote.getName(), vote.getRole()));
			} else {
				vote.die(String
						.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
								+ "A volunteer plunges the village's treasured silver dagger into their heart. "
								+ "They scream in agony as life and blood leave their body. *%s* the *%s* is dead!",
								vote.getName(), vote.getRole()));
			}
		}
		checkDead();
		if (!checkVictory())
			startNight();
	}

	private void startNight() {
		data.setState(State.Night);
		deVoiceAll();
		sendIrcMessage(
				data.getChannel(),
				"It is now night, and most villagers can only sleep. Some forces are busily at work, however...");
		data.getPlayers().clearVotes();
		sendNightStartMessages();
	}

	private void endNight() {
		killWolfVote();
		sendNightEndMessages();
		checkDead();
		if (!checkVictory())
			startDay();
	}

	private void checkDead() {
		List<Player> deceased = data.getPlayers().getRecentlyDead();
		for (Player player : deceased) {
			sendIrcMessage(data.getChannel(), player.getCauseOfDeath());
			deVoice(data.getChannel(), player.getName());
		}
		data.getPlayers().clearRecentlyDead();
	}

	private void endGame(){
		if (data.getState() != State.None && data.getState() != State.Starting) // Night or day
		{
			data.getStartGameTimer().cancel(); // Kill the existing timer, if we have one
			data.getPlayers().reset();
			setMode(data.getChannel(), "-m");
			deVoiceAll();
			sendIrcMessage(data.getChannel(), "Thanks for playing! Say !start to go again!");
		}
		data.setState(State.None);
	}

	public void sendRoleMessages() {
		List<Player> playerList = data.getPlayers().getLivingPlayers();
		for (Player player : playerList) {
			String message = player.roleInfo(data.getPlayers());
			if (message != null) {
				sendIrcMessage(player.getName(), message);
			}

			Team team = player.getTeam();
			switch (team) {
			case Wolves:
				sendIrcMessage(player.getName(), "You are on the " + Colors.RED
						+ "wolf" + Colors.NORMAL
						+ " team. You must attempt to eat the " + Colors.BLUE
						+ " villagers!");
				break;
			case Villagers:
				sendIrcMessage(player.getName(), "You are on the "
						+ Colors.BLUE + "villager" + Colors.NORMAL
						+ " team. Defend against the invading " + Colors.RED
						+ "wolf" + Colors.NORMAL + " incursion!");
				break;
			case LoneWolf:
				sendIrcMessage(player.getName(), "You are on your"
						+ Colors.PURPLE + "OWN" + Colors.NORMAL
						+ " team. Kill EVERYONE!");
			default:
				sendIrcMessage(player.getName(),
						"You are on an unknown team. Something has probably gone wrong here.");
			}
		}
	}

	private void sendNightStartMessages() {
		List<Player> playerList = data.getPlayers().getLivingPlayers();
		for (Player player : playerList) {
			String message = player.nightStart();
			if (message != null)
				sendIrcMessage(player.getName(), message);
		}
	}

	private void sendNightEndMessages() {
		sendNightEndMessages(Role.vigilante, false);
		sendNightEndMessages(Role.devil, false);
		sendNightEndMessages(Role.scry, false);
		sendNightEndMessages(Role.ghost, true);
		sendNightEndMessages(Role.aura_scry, false);
	}

	private void sendNightEndMessages(Role role, boolean publicMessage) {
		List<Player> playerList = data.getPlayers().getLivingPlayers();
		for (Player player : playerList) {
			if (player.getRole() != role)
				continue;
			String message = player.nightEnd();
			if (message != null)
				sendIrcMessage(
						publicMessage ? data.getChannel() : player.getName(),
						message);
		}
	}

	private void killWolfVote() {
		Player wolfVote = data.getPlayers().getVote(true);
		if (wolfVote != null) {
			Player baner = data.getPlayers().getPlayerTargeting(wolfVote,
					Role.baner);
			if (baner != null) {
				wolfVote = null;
				if (baner.getVote().equals(baner)) {
					sendIrcMessage(
							baner.getName(),
							"You hear a wolf yelp as they step in your cleverly concealed beartrap. "
									+ "You rush out trying to finish the job, but the monster has already escaped! "
									+ "It seems as if you will both live to fight another day.");
				} else {
					sendIrcMessage(
							baner.getName(),
							String.format(
									"Your detective skills have paid off!"
											+ "As you spot a wolf about to break into %s house, you pounce upon him from the rooftop and brawl valiantly. "
											+ "Clearly not expecting resitance tonight, the wolf flees in surprise."
											+ "The purple avenger has done a good deed tonight!",
									baner.getVote()));
				}
			}
		}
		if (wolfVote == null
				|| data.getPlayers().getPlayerTargeting(wolfVote, Role.baner) != null) {
			sendIrcMessage(data.getChannel(),
					"It appears the wolves didn't kill anybody tonight.");
		} else {
			if (WolfBotModel.getInstance().getSilentMode()) {
				wolfVote.die(String.format(
						"As the villagers gather, they notice someone missing. "
								+ "After some searching, their mauled corpse is found in their home. "
								+ "*%s* is dead!", wolfVote.getName()));		
			} else {
			wolfVote.die(String.format(
					"As the villagers gather, they notice someone missing. "
							+ "After some searching, their mauled corpse is found in their home. "
							+ "%s the %s is dead!", wolfVote.getName(),
					wolfVote.getRole()));
			}
		}
	}

	private void lynchVote(String senderS, String targetS) {
		if (data.getState() == State.None || data.getState() == State.Starting) {
			sendIrcMessage(data.getChannel(),
					"The game hasn't even started yet!");
			return;
		} else if (data.getState() != State.Day) {
			sendIrcMessage(data.getChannel(),
					"You can only cast lynchvotes at day.");
			return;
		}
		Player sender = data.getPlayers().getPlayer(senderS);
		Player target = data.getPlayers().getPlayer(targetS);
		if (sender == null) {
			sendIrcMessage(data.getChannel(), String.format(
					"%s, you are not enterd in the game.", senderS));
		} else if (!sender.isAlive()) {
			sendIrcMessage(data.getChannel(),
					String.format("%s, you are currently dead..", senderS));
		} else if (target == null) {
			sendIrcMessage(
					data.getChannel(),
					String.format(
							"%s, you may not vote for %s as they aren't enterd in the game.",
							senderS, targetS));
		} else if (!target.isAlive()) {
			sendIrcMessage(data.getChannel(), String.format(
					"%s, you may not vote for %s as they are currently dead.",
					senderS, targetS));
		} else {
			sender.vote(target);
			sendIrcMessage(data.getChannel(),
					String.format("%s has voted for %s.", senderS, targetS));
		}
		if (checkLynchMajority())
			endDay();
	}

	private void listVotes() {
		if (data.getState() == State.None || data.getState() == State.Starting) {
			sendIrcMessage(data.getChannel(),
					"The game hasn't even started yet!");
		} else if (data.getState() != State.Day) {
			sendIrcMessage(data.getChannel(),
					"You can only view lynchvotes at day.");
		} else {
			String nonVoters = data.getPlayers().nonvotersToString();
			sendIrcMessage(data.getChannel(), data.getPlayers().votesToString());

			if (!nonVoters.isEmpty()) {
				sendIrcMessage(data.getChannel(), "Not voted: "
						+ data.getPlayers().nonvotersToString());
			}
		}
	}

	private boolean checkLynchMajority() {
		List<Player> livingPlayers = data.getPlayers().getLivingPlayers();
		VoteTable table = new VoteTable(livingPlayers);
		return (table.getHighestVote() > livingPlayers.size() / 2);
	}

	private void voiceAll() {
		massMode(data.getPlayers().getLivingPlayers(), true, "v");
	}

	private void deVoiceAll() {
		massMode(data.getPlayers().getList(), false, "v");
	}

	private void massMode(List<Player> toChange, boolean add, String mode) {
		String modeToApply = "";
		if (add) {
			modeToApply += "+";
		} else {
			modeToApply += "-";
		}
		for (int i = 0; i < toChange.size(); ++i)
			modeToApply += mode;
		setMode(data.getChannel(),
				modeToApply + " "
						+ StringHandler.listToStringSimplePlayers(toChange));
	}
	private String help(String command, String arg) {
		switch (command) {
		case "role":
			Role role = Role.valueOf(arg.toLowerCase());
			return PlayerFactory.makePlayer("tmp", role).helpText();
		}
		return "Sorry I can't help you with that!";
		
	}
	
	private String help(String command) {
		switch (command) {
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
		case "autorole":
			return "!autorole sets a standard set of roles for the current number of players. Use just before !start.";
		case "anondeath":
			return "!anondeath on|off: When off you will not be told what class a player is when they die.";
		
		default:
			return "Unknown command";
		}
	}

	// This will allow for the bot to be customised at runtime to either send
	// notices or messages
	// By default, send notices.
	public void sendIrcMessage(String target, String message) {
		if (data.isEnableNotices()
				&& target.equalsIgnoreCase(data.getChannel()))
				// Note that we always send straight-up messages for PMs.
		{
			sendNotice(target, message);
		} else {
			sendMessage(target, message);
		}
	}

	public WolfBotModel getModel() {
		return data;
	}
}