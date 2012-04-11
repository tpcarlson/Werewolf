package no.arcticdrakefox.wolfbot.management.commands;

import no.arcticdrakefox.wolfbot.model.MessageType;

import com.google.common.base.Predicate;

public class CommandSelectorPredicate implements Predicate<Command> {

	private String command;
	private MessageType type;
	
	public CommandSelectorPredicate (String commandString, MessageType type)
	{
		this.command = commandString;
		this.type = type;
	}
	
	@Override
	public boolean apply(Command comm)
	{
		return  comm.getValidIn().contains(type) &&
				comm.getCommandPrefixes().contains(command.trim().toLowerCase());
	}

}
