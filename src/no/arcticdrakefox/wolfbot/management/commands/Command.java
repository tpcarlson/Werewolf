package no.arcticdrakefox.wolfbot.management.commands;

import java.util.Collection;

import com.google.common.collect.Lists;

import no.arcticdrakefox.wolfbot.model.MessageType;
import no.arcticdrakefox.wolfbot.model.State;

// TO ADD A NEW COMMAND:
// -Create a new Command object in Commands
// -Implement runCommand ()
// -Implement runInvalidCommand ()
// -Add your Command to the list of commands in WolfBotModel
public abstract class Command {
	private Collection<String> commandPrefixes; // eg. !help.
	public Collection<String> getCommandPrefixes() {
		return commandPrefixes;
	}
	private Collection<MessageType> validIn; // PMs? Public messages? Could extend to onQuit/onPart, but they are quite specific.

	private Collection<State> validStates; // Places this command may be used

	public Collection<MessageType> getValidIn() {
		return validIn;
	}

	public Collection<State> getValidStates() {
		return validStates;
	}

	// For commands with no alias
	public Command (String prefix, Collection<State> validStates, Collection<MessageType> validIn)
	{
		this.commandPrefixes = Lists.newArrayList(prefix);
		this.validStates   = validStates;
		this.validIn       = validIn;
	}
	
	// For commands with an alias
	public Command (Collection<String> prefixes, Collection<State> validStates, Collection<MessageType> validIn)
	{
		this.commandPrefixes = prefixes;
		this.validStates   = validStates;
		this.validIn       = validIn;
	}
	
	// In both cases, channel is null when the command type is Private...
	// runCommand is run when this is a valid command to run
	public abstract void runCommand (String[] args, String sender, MessageType type);
	// runInvalidCommand is run when this is not a valid command to run
	// (For example, voting during the night)
	public abstract void runInvalidCommand (String[] args, String sender, MessageType type);
}
