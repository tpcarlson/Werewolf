package no.arcticdrakefox.wolfbot.management.commands;

import java.util.Collection;

import no.arcticdrakefox.wolfbot.model.MessageType;
import no.arcticdrakefox.wolfbot.model.State;

public abstract class Command {
	private String commandPrefix; // eg. !help.
	private Collection<MessageType> validIn; // PMs? Public messages? Could extend to onQuit/onPart, but they are quite specific.
	
	public String getCommandPrefix() {
		return commandPrefix;
	}

	private Collection<State> validStates; // Places this command may be used

	public Collection<MessageType> getValidIn() {
		return validIn;
	}

	public Collection<State> getValidStates() {
		return validStates;
	}

	public Command (String prefix, Collection<State> validStates, Collection<MessageType> validIn)
	{
		this.commandPrefix = prefix;
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
