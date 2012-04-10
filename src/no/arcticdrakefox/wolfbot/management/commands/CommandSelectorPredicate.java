package no.arcticdrakefox.wolfbot.management.commands;

import com.google.common.base.Predicate;

public class CommandSelectorPredicate implements Predicate<Command> {

	private String command;
	
	public CommandSelectorPredicate (String commandString)
	{
		this.command = commandString;
	}
	
	@Override
	public boolean apply(Command comm)
	{
		return comm.getCommandPrefixes().contains(command.trim().toLowerCase());
	}

}
