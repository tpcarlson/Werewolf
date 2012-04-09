package no.arcticdrakefox.wolfbot.management;

public class WerewolfException extends Exception
{
	String message;
	
	public String getMessage() {
		return message;
	}

	public WerewolfException(String string) {
		this.message = string;
	}
}
