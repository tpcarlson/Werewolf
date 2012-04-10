package no.arcticdrakefox.wolfbot.management.commands;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.model.Commands;
import no.arcticdrakefox.wolfbot.model.MessageType;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

// Heavyweight command stuff should probably live here...
public class CommandUtils {
	public static void lynchVote(String senderS, String targetS, WolfBotModel data, MessageType type) {
		Player sender = data.getPlayers().getPlayer(senderS);
		Player target = data.getPlayers().getPlayer(targetS);
		if (sender == null) {
			Commands.sendIrcMessage(data.getChannel(), String.format(
					"%s, you are not entered in the game.", senderS), senderS, type);
		} else if (!sender.isAlive()) {
			Commands.sendIrcMessage(data.getChannel(),
					String.format("%s, you are currently dead..", senderS), senderS, type);
		} else if (target == null) {
			Commands.sendIrcMessage(
					data.getChannel(),
					String.format(
							"%s, you may not vote for %s as they aren't entered in the game.",
							senderS, targetS), senderS, type);
		} else if (!target.isAlive()) {
			Commands.sendIrcMessage(data.getChannel(), String.format(
					"%s, you may not vote for %s as they are currently dead.",
					senderS, targetS), senderS, type);
		} else {
			sender.vote(target);
			Commands.sendIrcMessage(data.getChannel(),
					String.format("%s has voted for %s.", senderS, targetS), senderS, type);
		}
		if (data.getWolfBot().checkLynchMajority())
			data.getWolfBot().endDay();
	}
}