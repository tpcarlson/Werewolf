package no.arcticdrakefox.wolfbot;

import java.util.Collection;
import java.util.List;
import java.util.Timer;

import no.arcticdrakefox.wolfbot.management.GameCore;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.PlayerList;
import no.arcticdrakefox.wolfbot.management.StringHandler;
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
import com.google.common.collect.Lists;

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
		Collection<Command> validCommands = Collections2.filter(data.getCommands(), new CommandSelectorPredicate (command, MessageType.CHANNEL));

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
		
		// Ignore private messages that aren't commands
		if (message.charAt(0) != '!')
			return;
		String[] args = message.split(" ");
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
			sendIrcMessage(sender, "More than one valid command. This is probably a bug. Aborting!");
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
				sendIrcMessage(data.getChannel(), String.format("%s has fled - they were a %s", sourceNick, player.getRole()));
			}
			GameCore.drop(player.getName(), data);
		}
	}

        @Override
	protected void onPart(String sourceNick, String sourceLogin, String sourceHostname, String reason){
		onQuit(sourceLogin, sourceNick, sourceHostname, reason);
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

	public void sendNightStartMessages() {
		List<Player> playerList = data.getPlayers().getLivingPlayers();
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

	public void voiceAll() {
		massMode(data.getPlayers().getLivingPlayers(), true, "v");
	}

	public void deVoiceAll() {
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