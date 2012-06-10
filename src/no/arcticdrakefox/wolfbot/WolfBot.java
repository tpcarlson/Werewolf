package no.arcticdrakefox.wolfbot;

import gnu.getopt.Getopt;

import java.util.Collection;
import java.util.List;
import java.util.Timer;

import no.arcticdrakefox.wolfbot.management.BotConstants;
import no.arcticdrakefox.wolfbot.management.GameCore;
import no.arcticdrakefox.wolfbot.management.Messages;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.management.commands.Command;
import no.arcticdrakefox.wolfbot.model.MessageType;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.State;
import no.arcticdrakefox.wolfbot.model.Team;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;
import no.arcticdrakefox.wolfbot.predicates.CommandSelectorPredicate;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

// TODO: Wolfbot needs moving out into a message handler and a core game
//       logic thing...
public class WolfBot extends PircBot {

	public static void main(String[] args) throws Exception {
		
		String server = "irc.lessthan3.net"; //$NON-NLS-1$
		String nick = "Wolfbot"; //$NON-NLS-1$
		String channel = "#wolfbot"; //$NON-NLS-1$
	
		 Getopt g = new Getopt("WolfBot", args, "c:n:s:"); //$NON-NLS-1$ //$NON-NLS-2$
		 //
		int c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'c':
				channel = g.getOptarg();
				break;
			case 'n':
				nick = g.getOptarg();
				break;
			case 's':
				server = g.getOptarg();
			case '?':
				System.out.println("help?! There is no stinking help."); //TODO
				break; // getopt() already printed an error
			default:
				System.out.print("getopt() returned " + c + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		
	
		PircBot bot = new WolfBot(nick, "ruffruff"); //$NON-NLS-1$
		bot.setVerbose(true);
		bot.connect(server);
		bot.joinChannel(channel);
	}

	public static String bold(String s) {
		return Colors.BOLD + s + Colors.NORMAL;
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
	protected void onDisconnect ()
	{
		int reconnectCount = 0;
		while (! isConnected ())
		{
			reconnectCount++;
			try
			{
				System.err.println("Attempting to reconnect... (Attempt# " + reconnectCount + ")");
				reconnect ();
			}
			catch (Exception e)
			{
				try
				{
					Thread.sleep(BotConstants.RECONNECT_POLL * 1000);
				}
				catch (InterruptedException ie)
				{
					System.err.println("Something is very wrong. Quitting.");
					System.exit(1);
				}
			}
		}
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
		String[] args = message.split(" "); //$NON-NLS-1$
		String command = args[0];

		// So, we ask the model for all commands matching the command string:
		Collection<Command> validCommands = Collections2.filter(data.getCommands(), new CommandSelectorPredicate (command, MessageType.CHANNEL));

		if (validCommands.size() > 1)
		{
			sendIrcMessage(data.getChannel(), Messages.getString("WolfBot.error.multipleCommands")); //$NON-NLS-1$
			return;
		}
		else if (validCommands.isEmpty() &&
			WolfBotModel.getInstance().isShowInvalidCommandEnabled())
		{
			sendIrcMessage(data.getChannel(), Messages.getString("WolfBot.error.unknownCommand")); //$NON-NLS-1$
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
		
		// Ignore private messages that aren't commands
		if (message.charAt(0) != '!')
			return;
		String[] args = message.split(" "); //$NON-NLS-1$
		String command = args[0];
		
		Player player = data.getPlayers().getPlayer(sender);
		
		// Collection<String> pmWhitelist = Lists.newArrayList("!join","!help","!list", "!time");
		
		// Ignore PMs from players that are not playing, but whitelist a few commands
		//if ((! pmWhitelist.contains(command.trim().toLowerCase())) && player == null)
		//	return;
		
		// As with CHANNEL messages, filter out anything we don't want to see:
		// So, we ask the model for all commands matching the command string...:
		Collection<Command> validCommands = Collections2.filter(data.getCommands(), new CommandSelectorPredicate (command, MessageType.PRIVATE));

		if (validCommands.size() > 1)
		{
			sendIrcMessage(sender, Messages.getString("WolfBot.error.multipleCommands")); //$NON-NLS-1$
			return;
		}/*
		else if (validCommands.isEmpty())
		{
			sendIrcMessage(sender, "Unknown command...");
		} */
		
		for (Command comm : validCommands)
		{
			// So while we have the right command here
			// still need to verify that the command can be used in this state:
			if (comm.getValidIn().contains(MessageType.PRIVATE))
			{
				if (comm.getValidStates().contains(data.getState()))
				{
					// All good - run this command:
					comm.runCommand(args, sender, MessageType.PRIVATE);
				}
				else
				{
					// Not so good - we're in the wrong state
					comm.runInvalidCommand(args, sender, MessageType.PRIVATE);
				}
			}
			else
			{
				// Command was meant for channel - ignore
			}
		}		
		
		// Commands specific to each role apply here:
		if (data.getState() == State.Night) {
			if (player.isAlive()) {
				String msg = player.nightAction(message, data.getPlayers());
				if (msg != null)
					sendIrcMessage(sender, msg);
				if (data.getPlayers().allReady())
					GameCore.endNight(data);
			}
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
				if (data.getSilentMode()) {
					sendIrcMessage(data.getChannel(), Messages.getString("WolfBot.fled.noReveal",  //$NON-NLS-1$
							bold(sourceNick)));
				} else {
					sendIrcMessage(data.getChannel(), Messages.getString("WolfBot.fled.Reveal",  //$NON-NLS-1$
							bold(sourceNick), player.getRole().toStringColor()));
				}
			}
			GameCore.drop(player.getName(), sourceNick, data);
		}
	}

    @Override
	protected void onPart(String sourceNick, String sourceLogin, String sourceHostname, String reason){
		onQuit(sourceLogin, sourceNick, sourceHostname, reason);
	}
	
    @Override
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason)
    {
    	if (recipientNick.equalsIgnoreCase(getNick ()))
    	{
    		this.joinChannel (channel);
    	}
    	else
    	{
    		onQuit (recipientNick, kickerNick, kickerHostname, reason);
    	}
    }
        

	public void sendRoleMessages() {
		List<Player> playerList = data.getPlayers().getLivingPlayers();
		for (Player player : playerList) {
			String message = player.roleInfo(data.getPlayers());
			if (message != null) {
				sendIrcMessage(player.getName(), message);
			}

			Team team = player.getRole().getTeam();
			switch (team) {
			case Wolves:
				sendIrcMessage(player.getName(), 
						Messages.getString("WolfBot.intro.wolf",  //$NON-NLS-1$
								StringHandler.colorise(Team.Wolves.getColor(), "wolf"),  //$NON-NLS-1$
								StringHandler.colorise(Team.Villagers.getColor(), "villagers"))); //$NON-NLS-1$
				break;
			case Villagers:
				sendIrcMessage(player.getName(), 
						Messages.getString("WolfBot.intro.villagers", //$NON-NLS-1$
								Role.villager.toStringColor(),
								StringHandler.colorise(Team.Wolves.getColor(), "wolf"))); //$NON-NLS-1$
				break;
			case LoneWolf:
				sendIrcMessage(player.getName(),
						Messages.getString("WolfBot.intro.LoneWolf",  //$NON-NLS-1$
						StringHandler.colorise(Team.LoneWolf.getColor(), "OWN"))); //$NON-NLS-1$
				break;
			default:
				sendIrcMessage(player.getName(),
						Messages.getString("WolfBot.intro.unknown")); //$NON-NLS-1$
			}
		}
	}

	public void sendNightStartMessages() {
		List<Player> playerList = data.getPlayers().getPlayersWithNightActions();
		for (Player player : playerList) {
			String message = player.nightStart();
			if (message != null)
				sendIrcMessage(player.getName(), message);
		}
	}

	public void sendNightEndMessages() {
		sendNightEndMessages(Role.vigilante, false);
		sendNightEndMessages(Role.devil, false);
		sendNightEndMessages(Role.scry, false);
		sendNightEndMessages(Role.ghost, true);
		sendNightEndMessages(Role.aura_scry, false);
	}

	private void sendNightEndMessages(Role role, boolean publicMessage) {
		List<Player> playerList = data.getPlayers().getPlayersWithNightActions();
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

	public void voiceAll() {
		massMode(data.getPlayers().getLivingPlayers(), true, "v"); //$NON-NLS-1$
	}

	public void deVoiceAll() {
		massMode(data.getPlayers().getList(), false, "v"); //$NON-NLS-1$
	}

	private static final int MAX_MODE = 5;
	
	private void massMode(List<Player> toChange, boolean add, String mode) {
		String modeToApply = ""; //$NON-NLS-1$
		if (add) {
			modeToApply += "+"; //$NON-NLS-1$
		} else {
			modeToApply += "-"; //$NON-NLS-1$
		}
		
		List<List<Player>> smallerLists = Lists.partition(toChange, MAX_MODE);
		for (List<Player> ps : smallerLists) {
			for (int i = 0; i < ps.size(); ++i)
				modeToApply += mode;
				setMode(data.getChannel(), modeToApply + " " //$NON-NLS-1$
						+ StringHandler.listToStringSimplePlayers(ps));
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