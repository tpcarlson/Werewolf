package no.arcticdrakefox.wolfbot;

import java.util.Collection;
import java.util.List;
import java.util.Timer;

import no.arcticdrakefox.wolfbot.Timers.StartGameTask;
import no.arcticdrakefox.wolfbot.management.BotConstants;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.management.VoteTable;
import no.arcticdrakefox.wolfbot.management.commands.Command;
import no.arcticdrakefox.wolfbot.management.commands.CommandSelectorPredicate;
import no.arcticdrakefox.wolfbot.model.MessageType;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.State;
import no.arcticdrakefox.wolfbot.model.Team;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;

import com.google.common.collect.Collections2;

// TODO: Wolfbot needs moving out into a message handler and a core game
//       logic thing...
public class WolfBot extends PircBot {

	public static void main(String[] args) throws Exception {
		PircBot bot = new WolfBot("WolfBot", "ruffruff");
		bot.setVerbose(true);
		bot.connect("irc.lessthan3.net");
		bot.joinChannel("#wolfbot");
	}

	
	WolfBotModel data = new WolfBotModel(new PlayerList(), State.None,
			new Timer(), true, this);

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

		// So, we ask the model for all commands matching the command string:
		Collection<Command> validCommands = Collections2.filter(data.getCommands(), new CommandSelectorPredicate (command));

		if (validCommands.size() > 1)
		{
			sendIrcMessage(data.getChannel(), "More than one valid command. This is probably a bug. Aborting!");
			return;
		}
		else if (validCommands.isEmpty())
		{
			sendIrcMessage(data.getChannel(), "Unknown command...");
			return;
		}
		
		for (Command comm : validCommands)
		{
			// So while we have the right command here
			// still need to verify that the command can be used in this state:
			if (comm.getValidIn().contains(MessageType.CHANNEL))
			{
				if (comm.getValidStates().contains(data.getState()))
				{
					// All good - run this command:
					comm.runCommand(args, sender, MessageType.CHANNEL);
				}
				else
				{
					// Not so good - we're in the wrong state
					comm.runInvalidCommand(args, sender, MessageType.CHANNEL);
				}
			}
			else
			{
				// Command was meant for PM - ignore
			}
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
				sendIrcMessage(sender, BotConstants.help(args[1]));
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
				sendIrcMessage(data.getChannel(), String.format("%s has fled - they were a %s", sourceNick, player.getRole()));
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
					+ " cannot join now, a game is in progress.");
		} else if (data.getPlayers().addPlayer(name)) {
			sendIrcMessage(data.getChannel(), name + " has joined the game!");
		} else {
			sendIrcMessage(data.getChannel(), name + " has already entered.");
		}
	}
	
	public void drop(String name){
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
		
		// Two stages here - we want to check the lynch majority to 
		// see whether to go to night
		// We also need to checkVictory
		if (data.getState() == State.Day) // If we're in daytime (ie. voting)
		{
			if (checkLynchMajority())
				endDay();
		}
		
		// In both day and night, we check victory:
		checkVictory();
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

	public void startDay() {
		sendIrcMessage(data.getChannel(),
				"It is now day. Vote for someone to lynch!");
		data.setState(State.Day);
		voiceAll();
		data.getPlayers().clearVotes();
	}

	public void endDay() {
		Player vote = data.getPlayers().getVote();
		
		// If players have voted to skip then we should send an appropriate message
		if (vote == BotConstants.SKIP_VOTE_PLAYER)
		{
			sendIrcMessage (data.getChannel(), "The villagers can't agree on who to lynch and decide to drink beer instead. Hurrah!");
		}
		else
		{
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

	public void endGame(){
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

	public boolean checkLynchMajority() {
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