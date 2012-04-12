package no.arcticdrakefox.wolfbot.management.commands;

import static no.arcticdrakefox.wolfbot.WolfBot.bold;

import java.util.Timer;

import no.arcticdrakefox.wolfbot.Timers.StartGameTask;
import no.arcticdrakefox.wolfbot.management.BotConstants;
import no.arcticdrakefox.wolfbot.management.GameCore;
import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.management.WerewolfException;
import no.arcticdrakefox.wolfbot.model.MessageType;
import no.arcticdrakefox.wolfbot.model.State;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

import com.google.common.collect.Lists;

public class Commands
{
	// Ctrl-shift-/ and Ctrl-shift-* are your friend.
	// (Note: numpad / and *)
	public static final Command JOIN_COMMAND = new Command ("!join",
			Lists.newArrayList(State.None, State.Starting),
			Lists.newArrayList(MessageType.values()))
	{			
		@Override
		public void runCommand(String[] args, String sender, MessageType type)
		{
			if (model.getPlayers().addPlayer(sender))
			{
				// Joins should be announced
				sendIrcMessage(model.getChannel(), bold(sender) + " has joined the game!", sender, MessageType.CHANNEL);
			}
			else
			{
				sendIrcMessage(model.getChannel(), bold(sender) + " has already entered.", sender, type);
			}
		}
		
		@Override
		public void runInvalidCommand(String[] args, String sender, MessageType type)
		{
			sendIrcMessage(model.getChannel(), bold(sender) + " cannot join now, a game is in progress.", sender, type);				
		}
	};

	public static final Command SET_COMMAND = new Command ("!set",
			Lists.newArrayList(State.None, State.Starting),
			Lists.newArrayList(MessageType.CHANNEL))
	{
		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			if (args.length == 3)
			{
				setCount(args[1], args[2].trim(), sender, type);
			}
			else
			{
				sendIrcMessage(model.getChannel(),
						"Correct usage is:  !set <role> <amount>", sender, type);
			}
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type)
		{
			sendIrcMessage(model.getChannel(), "Don't mess with rolecount during the game. :/", sender, type);
		}	
		
		private void setCount(String role, String amountS, String sender, MessageType type) {
			int amount;
			if (role.toLowerCase().equals("villager")) {
				sendIrcMessage(model.getChannel(),
						"Villagers are automatically adjusted.", sender, type);
			} else if (StringHandler.isInt(amountS)) {
				amount = StringHandler.parseInt(amountS);
				try
				{
					if (model.getPlayers().setRoleCount(role, amount))
						model.sendIrcMessage(model.getChannel(), String.format("%s%s set to %d", role ,amount == 1 ? "s" : "" ,amount));
					else // Should never get here
						throw new WerewolfException ("Meep");
				}
				catch (WerewolfException wolfy)
				{
					sendIrcMessage(model.getChannel(), String.format("Failed. Could not resolve %s to a role", role), sender, type);
				}
			} else {
				model.sendIrcMessage(model.getChannel(), amountS
						+ " cannot be parsed to an int.");
			}
		}
	};
	
	public static final Command AUTOROLE_COMMAND = new Command ("!autorole",
			Lists.newArrayList(State.None, State.Starting),
			Lists.newArrayList(MessageType.CHANNEL))
	{
				@Override
				public void runCommand(String[] args, String sender, MessageType type) {
					model.getPlayers().autoRole();
					sendIrcMessage(model.getChannel(), model.getPlayers().roleCountToString(), sender, type);
				}
				
				@Override
				public void runInvalidCommand(String[] args, String sender, MessageType type) {
					sendIrcMessage(model.getChannel(), "Don't mess with rolecount during the game. :/", sender, type);
				}
	};
	
	public static final Command TIME_COMMAND = new Command ("!time",
			Lists.newArrayList(State.None, State.Starting, State.Night, State.Day),
			Lists.newArrayList(MessageType.CHANNEL, MessageType.PRIVATE))
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			sendIrcMessage(model.getChannel(), "It is currently " + model.getState(), sender, type);
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {	}
	};
	
	public static final Command LIST_COMMAND = new Command ("!list",
			Lists.newArrayList(State.values()),
			Lists.newArrayList(MessageType.values()))
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type)
		{
			sendIrcMessage(model.getChannel(), StringHandler.listToString(model
					.getPlayers().getLivingPlayers()), sender, type);
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {	}
		
	};
	
	public static final Command ROLECOUNT_COMMAND = new Command ("!roles",
			Lists.newArrayList(State.values()),
			Lists.newArrayList(MessageType.values()))
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type)
		{
			String toSend = model.getPlayers().roleCountToString();
			sendIrcMessage(model.getChannel(), toSend, sender, type);
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) { }
	};
	
	public static final Command NOTICES_COMMAND = new Command ("!notices",
			Lists.newArrayList(State.values()),
			Lists.newArrayList(MessageType.CHANNEL))
	{
		@Override
		public void runCommand(String[] args, String sender, MessageType type)
		{
			if (args.length == 2) {
				if (args[1].equalsIgnoreCase("on")) {
					model.setEnableNotices(true);
					sendIrcMessage(model.getChannel(), "Notices enabled", sender, type);
				} else if (args[1].equalsIgnoreCase("off")) {
					model.setEnableNotices(false);
					sendIrcMessage(model.getChannel(), "Notices disabled", sender, type);
				}
			}
			else
			{
				sendIrcMessage(model.getChannel(), "Correct usage is:  !notices on|off", sender, type);
			}
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) { }
		
	};
	
	public static final Command REVEAL_COMMAND = new Command ("!reveal",
			Lists.newArrayList(State.None, State.Starting),
			Lists.newArrayList(MessageType.CHANNEL))
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			if (args.length == 2 && args[1].trim().equalsIgnoreCase("off")) {
				WolfBotModel.getInstance().setSilentMode(true);
				sendIrcMessage(model.getChannel(), "No reveal off.", sender, type);
			} else if (args.length == 2 && args[1].equals("on")) {
				WolfBotModel.getInstance().setSilentMode(false);	
				sendIrcMessage(model.getChannel(), "No reveal on.", sender, type);
			} else
				sendIrcMessage(model.getChannel(), "Correct usage is: !anondeath on|off", sender, type);
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {	}
		
	};
	
	public static final Command START_COMMAND = new Command ("!start",
			Lists.newArrayList(State.None),
			Lists.newArrayList(MessageType.CHANNEL))
	{
		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			startGame (sender, type);
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {
			if (model.getState() == State.Starting) {
				sendIrcMessage(model.getChannel(),
						"The game is already starting. Use !end to stop starting.", sender, type);
				return;
			}
		}
		
		private void startGame(String sender, MessageType type)
		{
			int playerCount = model.getPlayers().getList().size();
			if (playerCount < 3) {
				sendIrcMessage(model.getChannel(),
						"Need at least three players to go.", sender, type);
				return;
			} else if (playerCount < model.getPlayers().totalRoleCount()) {
				sendIrcMessage(model.getChannel(),
						"There are more special roles than players!", sender, type);
				return;
			}
			
			model.setStartGameTimer(new Timer()); // Make a new timer every time
			// Fire off a 30s timer:
			model.getStartGameTimer().schedule(
					new StartGameTask(model), 30 * 1000); // 30s
			sendIrcMessage(model.getChannel(), "The game will begin in "
					+ bold( "30 seconds.")
					+ " Type !join to join!", sender, type);
			model.setState(State.Starting);
		}
	};	
	public static final Command END_COMMAND   = new Command ("!end",
			Lists.newArrayList(Lists.newArrayList(State.Day, State.Night, State.Starting)),
			Lists.newArrayList(MessageType.CHANNEL))
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			GameCore.endGame(model);
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {	}
	};
	public static final Command HELP_COMMAND  = new Command ("!help",
			Lists.newArrayList(State.values()),
			Lists.newArrayList(MessageType.values()))
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			if (args.length == 2)
				sendIrcMessage(model.getChannel(), BotConstants.help(args[1]), sender, type);
			if (args.length == 3)
				sendIrcMessage(model.getChannel(), BotConstants.help(args[1], args[2]), sender, type);
			else
				sendIrcMessage(
						model.getChannel(), BotConstants.HELP_COMMANDS, sender, type);
		}

		// Help should never get here
		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {
			throw new RuntimeException ("Help! Help is broken!");
		}
		
	};
	public static final Command VOTES_COMMAND = new Command ("!votes",
			Lists.newArrayList(State.Day), // May only vote during the day...
			Lists.newArrayList(MessageType.CHANNEL)) // And only in public
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			String nonVoters = model.getPlayers().nonvotersToString();
			sendIrcMessage(model.getChannel(), model.getPlayers().votesToString(), sender, type);

			if (!nonVoters.isEmpty()) {
				sendIrcMessage(model.getChannel(), "Not voted: "
						+ model.getPlayers().nonvotersToString(), sender, type);
			}
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {
			if (model.getState() == State.None || model.getState() == State.Starting) {
				sendIrcMessage(model.getChannel(),
						"The game hasn't even started yet!", sender, type);
			} else if (model.getState() != State.Day) {
				sendIrcMessage(model.getChannel(),
						"You can only view lynchvotes at day.", sender, type);
			}
		}
	};
	public static final Command SKIPLYNCH_COMMAND = new Command ("!skip",
			Lists.newArrayList(State.Day),
			Lists.newArrayList(MessageType.CHANNEL))
	{
		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			Player senderPlayer = model.getPlayers().getPlayer(sender);
			if (senderPlayer == null)
			{
				sendIrcMessage (model.getChannel(), "Player " + bold(sender) + " was not found...", sender, type);
			}
			else
			{
				senderPlayer.vote(BotConstants.SKIP_VOTE_PLAYER);
				sendIrcMessage (model.getChannel(), bold(sender) + 
						" scratches their head in confusion and then tears "+
						"up their ballot paper.", sender, type);
				// We must also check the majority at this point:
				// TODO: Refactor checkLynchMajority etc.
				if (GameCore.checkLynchMajority(model))
					GameCore.endDay(model);
			}
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {
			if (model.getState() == State.Night)
			{
				sendIrcMessage (model.getChannel(), "You may only vote during the day.", sender, type);
			}
			else
			{
				sendIrcMessage (model.getChannel(), "The game hasn't even started yet!", sender, type);
			}
		}
	};
	public static final Command DROP_COMMAND = new Command ("!drop",
			Lists.newArrayList(State.values()),
			Lists.newArrayList(MessageType.values()))
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			if (args.length == 2)
			{
				if (type != MessageType.PRIVATE)
				{
					GameCore.drop(args[1], sender, model);
				}
				else
				{
					sendIrcMessage(model.getChannel(), bold(sender) + " tried to drop " + args[1] + " in PM! What a scumbag!", sender, MessageType.CHANNEL);
				}
			}
			else if (args.length == 1)
				GameCore.drop(sender, sender, model);
			else {
				sendIrcMessage(model.getChannel(), "Correct usage is:  !drop [player]", sender, type);
			}
		}

		// Will never be called - drop is valid at all times
		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {	}
		
	};
	public static final Command LYNCH_COMMAND = new Command (Lists.newArrayList("!lynch", "!kill", "!vote"),
			Lists.newArrayList(State.Day),
			Lists.newArrayList(MessageType.CHANNEL))
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			if (args.length == 2)
			{
				GameCore.lynchVote(sender, args[1], model, type);
			}
			else
			{
				sendIrcMessage(model.getChannel(), "Correct usage is: !lynch <target>", sender, type);
			}
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type)
		{
			if (model.getState() == State.None || model.getState() == State.Starting) {
				Commands.sendIrcMessage(model.getChannel(),
						"The game hasn't even started yet!", sender, type);
				return;
			} else if (model.getState() == State.Night) {
				Commands.sendIrcMessage(model.getChannel(),
						"You can only cast lynchvotes at day.", sender, type);
				return;
			}
		}

	};
	public static final Command MY_ROLE_COMMAND = new Command (Lists.newArrayList("!whoami", "!role"),
			Lists.newArrayList(State.Day, State.Night),
			Lists.newArrayList(MessageType.PRIVATE))
	{

		@Override
		public void runCommand(String[] args, String sender, MessageType type) {
			Player player = model.getPlayers().getPlayer(sender);
			sendIrcMessage(sender, String.format("You are a %s", player.getRole().toStringColor()), sender, type);
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type) {
			sendIrcMessage (sender, "The game hasn't even started yet!", sender, type);
		}
	};
	
	// Shorthand for model.sendIrcMessage.
	public static void sendIrcMessage (String channel, String message, String sender, MessageType type)
	{
		if (model == null)
		{
			throw new RuntimeException("Set the model first...");
		}
		else
		{
			if (type == MessageType.CHANNEL)
			{
				model.sendIrcMessage(channel, message);
			}
			else
			{
				model.sendIrcMessage(sender, message);
			}
		}
	}
	
	private static WolfBotModel model;
	
	public static void setModel(WolfBotModel wolfBotModel) {
		model = wolfBotModel;
	}
}
