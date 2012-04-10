package no.arcticdrakefox.wolfbot.model;

import no.arcticdrakefox.wolfbot.management.StringHandler;
import no.arcticdrakefox.wolfbot.management.WerewolfException;
import no.arcticdrakefox.wolfbot.management.commands.Command;

import com.google.common.collect.Lists;

public class Commands
{
	public static final Command JOIN_COMMAND = new Command ("!join",
			Lists.newArrayList(State.None, State.Starting),
			Lists.newArrayList(MessageType.CHANNEL, MessageType.PRIVATE))
	{			
		@Override
		public void runCommand(String[] args, String sender, MessageType type)
		{
			if (model.getPlayers().addPlayer(sender))
			{
				model.sendIrcMessage(model.getChannel(), sender + " has joined the game!");
			}
			else
			{
				model.sendIrcMessage(model.getChannel(), sender + " has already entered.");
			}
		}
		
		@Override
		public void runInvalidCommand(String[] args, String sender, MessageType type)
		{
			model.sendIrcMessage(model.getChannel(), sender + " cannot join now, a game is in progress.");				
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
				setCount(args[1], args[2].trim());
			}
			else
			{
				model.sendIrcMessage(model.getChannel(),
						"Correct usage is:  !set <role> <amount>");
			}
		}

		@Override
		public void runInvalidCommand(String[] args, String sender,
				MessageType type)
		{
			model.sendIrcMessage(model.getChannel(), "Don't mess with rolecount during the game. :/");
		}	
		
		private void setCount(String role, String amountS) {
			int amount;
			if (role.toLowerCase().equals("villager")) {
				model.sendIrcMessage(model.getChannel(),
						"Villagers are automatically adjusted.");
			} else if (StringHandler.isInt(amountS)) {
				amount = StringHandler.parseInt(amountS);
				try
				{
					if (model.getPlayers().setRoleCount(role, amount))
						model.sendIrcMessage(model.getChannel(), String.format("%s%s set to %d", role,amount == 1 ? "s" : "" ,amount));
					else // Should never get here
						throw new WerewolfException ("Meep");
				}
				catch (WerewolfException wolfy)
				{
					model.sendIrcMessage(model.getChannel(), String.format("Failed. Could not resolve %s to a role", role));
				}
			} else {
				model.sendIrcMessage(model.getChannel(), amountS
						+ " cannot be parsed to an int.");
			}
		}
	};
	
	private static WolfBotModel model;
	
	public static void setModel(WolfBotModel wolfBotModel) {
		model = wolfBotModel;
	}
}
