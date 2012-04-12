package no.arcticdrakefox.wolfbot.management;

import static no.arcticdrakefox.wolfbot.WolfBot.bold;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import no.arcticdrakefox.wolfbot.management.commands.Commands;
import no.arcticdrakefox.wolfbot.model.MessageType;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.State;
import no.arcticdrakefox.wolfbot.model.Team;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

import org.jibble.pircbot.Colors;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class GameCore {
	public static void lynchVote(String senderS, String targetS, WolfBotModel data, MessageType type) {
		Player sender = data.getPlayers().getPlayer(senderS);
		Player target = data.getPlayers().getPlayer(targetS);
		if (sender == null) {
			Commands.sendIrcMessage(data.getChannel(), String.format(
					"%s, you are not entered in the game.", bold(senderS)), senderS, type);
		} else if (!sender.isAlive()) {
			Commands.sendIrcMessage(data.getChannel(),
					String.format("%s, you are currently dead..", bold(senderS)), senderS, type);
		} else if (target == null) {
			Commands.sendIrcMessage(
					data.getChannel(),
					String.format(
							"%s, you may not vote for %s as they aren't entered in the game.",
							bold(senderS), bold(targetS)), senderS, type);
		} else if (!target.isAlive()) {
			Commands.sendIrcMessage(data.getChannel(), String.format(
					"%s, you may not vote for %s as they are currently dead.",
					bold(senderS), bold(targetS)), senderS, type);
		} else {
			sender.vote(target);
			Commands.sendIrcMessage(data.getChannel(),
					String.format("%s has voted for %s.", senderS, targetS), senderS, type);
		}
		if (checkLynchMajority(data))
			endDay(data);
	}
	
	public static void drop(String name, String sender, WolfBotModel data){
		if (data.getState() == State.None || data.getState() == State.Starting)
		{
			// We still need to remove from the game:
			boolean playerRemoved = data.getPlayers().removePlayer(name);
			if (playerRemoved)
			{
				if (name.equals(sender))
				{
					// And send a neutral message:
					data.getWolfBot().sendIrcMessage (data.getChannel(), bold(name) + 
							" has retired from the game - before it even started! What a coward.");
				}
				else
				{
					data.getWolfBot().sendIrcMessage(data.getChannel(), bold (sender) + " cut off " + bold(name) + "'s arm. "+ bold(name) + " retires from the game."); 
				}
			}
			else
			{
				data.getWolfBot().sendIrcMessage(data.getChannel(), bold(name) + 
						" wasn't found among the entered players.");
			}
			return;
		}

		// Night or day at this point
		if (data.getPlayers().removePlayer(name)) {
			if (name.equals(sender))
			{
				data.getWolfBot().sendIrcMessage(data.getChannel(), bold(name)
						+ " has retired from the game!");
			}
			else
			{
				data.getWolfBot().sendIrcMessage(data.getChannel(), bold(name)
						+ " has been retired from the game by " + bold(sender) + "!");
			}
			data.getWolfBot().setMode(data.getChannel(), "-v " + name);
		} else {
			data.getWolfBot().sendIrcMessage(data.getChannel(), bold(name)
					+ " wasn't found among the entered players.");
		}
		
		// Two stages here - we want to check the lynch majority to 
		// see whether to go to night
		// We also need to checkVictory
		if (data.getState() == State.Day) // If we're in daytime (ie. voting)
		{
			if (checkLynchMajority(data))
				endDay(data);
		}
		
		// In both day and night, we check victory:
		checkVictory(data);
	}
	
	public static boolean checkLynchMajority(WolfBotModel data) {
		List<Player> livingPlayers = data.getPlayers().getLivingPlayers();
		VoteTable table = new VoteTable(livingPlayers);
		return (table.getHighestVote() > livingPlayers.size() / 2);
	}

	private static boolean checkVictory(WolfBotModel data) {
		int wolfCount = data.getPlayers().wolfCount();
		if (wolfCount < 1) {
			data.getWolfBot().sendIrcMessage(data.getChannel(),
					"With all wolves exterminated, the village is safe once again.");
			endGame(data);
			return true;
		} else if (wolfCount * 2 >= data.getPlayers().playerCount()) {
			if (wolfCount == 1)
				data.getWolfBot().sendIrcMessage(
						data.getChannel(),
						String.format(
								"After turning on the last remaining  %svillager%s, %s prowls on to terrorize somewhere else.",
								Team.Villagers.getColor(), Colors.NORMAL,
								StringHandler.listToString(data.getPlayers().getWolves())));
			else
				data.getWolfBot().sendIrcMessage(
						data.getChannel(),
						String.format("%s turn on the last villagers. With all food depleted, " +
								"they leave the village behind to find fresh meat elsewhere.",
								bold(StringHandler.listToString(data.getPlayers().getWolves()))));
			endGame(data);
			
			// End of the game - return true here.
			return true;
		}
		return false;
	}

	public static void startDay(WolfBotModel data) {
		data.getWolfBot().sendIrcMessage(data.getChannel(),
				"It is now day. Vote for someone to lynch!");
		data.setState(State.Day);
		data.getWolfBot().voiceAll();
		data.getPlayers().clearVotes();
	}

	public static void endDay(WolfBotModel data) {
		Player vote = data.getPlayers().getVote();
		
		// If players have voted to skip then we should send an appropriate message
		if (vote == BotConstants.SKIP_VOTE_PLAYER)
		{
			data.getWolfBot().sendIrcMessage (data.getChannel(), "The villagers can't agree on who to lynch and decide to drink beer instead. Hurrah!");
		}
		else
		{
			if (WolfBotModel.getInstance().getSilentMode()) {
				vote.die(String
						.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
								+ "A volunteer plunges the village's treasured silver dagger into their heart(a bread knife would do)! "
								+ "%s is dead!", bold(vote.getName())));
			} else {
				if (vote.isWolf()) {
					vote.die(String
							.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
									+ "A volunteer plunges the village's treasured silver dagger into their heart, and the wound catches fire! "
									+ "A "+ Role.wolf.toStringColor() + " was lynched today, and the village is a little safer. %s the %s is dead!",
									bold(vote.getName()), vote.getRole().toStringColor()));
				} else {
					vote.die(String
							.format("The lynched gets dragged by the mob to the village square and tied up to a tree. "
									+ "A volunteer plunges the village's treasured silver dagger into their heart. "
									+ "They scream in agony as life and blood leave their body. %s the %s is dead!",
									bold(vote.getName()), vote.getRole().toStringColor()));
				}
			}
		}
		
		checkDead(data);
		
		if (!checkVictory(data))
			startNight(data);
	}

	private static void startNight(WolfBotModel data) {
		data.setState(State.Night);
		data.getWolfBot().deVoiceAll();
		data.getWolfBot().sendIrcMessage( data.getChannel(),
				"It is now night, and most villagers can only sleep. " +
				"Some forces are busily at work, however...");
		data.getPlayers().clearVotes();
		data.getWolfBot().sendNightStartMessages();
	}

	public static void endNight(WolfBotModel data) {
		killWolfVote(data);
		data.getWolfBot().sendNightEndMessages();
		checkDead(data);
		if (!checkVictory(data))
			startDay(data);
	}

	private static void checkDead(WolfBotModel data) {
		List<Player> deceased = data.getPlayers().getRecentlyDead();
		for (Player player : deceased) {
			data.getWolfBot().sendIrcMessage(data.getChannel(), player.getCauseOfDeath());
			data.getWolfBot().deVoice(data.getChannel(), player.getName());
		}
		data.getPlayers().clearRecentlyDead();
	}

	public static void endGame(WolfBotModel data){
		if (data.getState() != State.None) // Night or day
		{
			data.getStartGameTimer().cancel(); // Kill the existing timer, if we have one
			if (data.getState() == State.Starting)
			{
				data.getWolfBot().sendIrcMessage(data.getChannel(), "Start cancelled.");
			}
			else
			{
				printCast (data);
				data.getPlayers().reset();
				data.getWolfBot().setMode(data.getChannel(), "-m");
				data.getWolfBot().deVoiceAll();
				data.getWolfBot().sendIrcMessage(data.getChannel(), 
						"Thanks for playing! Say !start to go again!");
			}
			
			data.setState(State.None);
		}
	}
	
	static class TeamPredicate implements Predicate<Player>
	{
		Team team;
		
		public TeamPredicate (Team team)
		{
			this.team = team;
		}
		
		@Override
		public boolean apply(Player player) {
			return player.getRole().getTeam().equals(team);
		}
	}
	
	private static void printCast (WolfBotModel data)
	{
		// For each team, build up a string:
		for (Team team : Team.values())
		{
			Collection<Player> teamPlayers = Collections2.filter(data.getPlayers().getList(), new TeamPredicate (team));
			List<Player> sortedList = Lists.newArrayList(teamPlayers);
			Collections.sort(sortedList, new Comparator<Player> () {
				@Override
				public int compare(Player o1, Player o2)
				{
					return o1.getName().compareToIgnoreCase(o2.getName());
				} });
			
			if (sortedList.isEmpty())
			{
				// Skip teams that have nobody in them
				continue;
			}
			else
			{
				// It's possible this string is going to be too long for IRC. Will have to see.
				String rolesForTeam = StringHandler.playersListToStringWithRoles (sortedList);
				data.getWolfBot().sendIrcMessage(data.getChannel(), team.getColored() + ": " + rolesForTeam);
			}
		}
	}
	
	public static void killWolfVote(WolfBotModel data) {
		Player wolfVote = data.getPlayers().getVote(true);
		if (wolfVote != null) {
			Player baner = data.getPlayers().getPlayerTargeting(wolfVote,
					Role.baner);
			if (baner != null) {
				wolfVote = null;
				if (baner.getVote().equals(baner)) {
					data.getWolfBot().sendIrcMessage(
							baner.getName(),
							"You hear a wolf yelp as they step in your cleverly concealed beartrap. "
									+ "You rush out trying to finish the job, but the monster has already escaped! "
									+ "It seems as if you will both live to fight another day.");
				} else {
					data.getWolfBot().sendIrcMessage(
							baner.getName(),
							String.format(
									"Your detective skills have paid off!"
											+ "As you spot a wolf about to break into %s house, you pounce upon him from the rooftop and brawl valiantly. "
											+ "Clearly not expecting resitance tonight, the wolf flees in surprise."
											+ "The purple avenger has done a good deed tonight!",
									baner.getVote()));
				}
			}
		}
		if (wolfVote == null
				|| data.getPlayers().getPlayerTargeting(wolfVote, Role.baner) != null) {
			data.getWolfBot().sendIrcMessage(data.getChannel(),
					"It appears the wolves didn't kill anybody tonight.");
		} else {
			if (WolfBotModel.getInstance().getSilentMode()) {
				wolfVote.die(String.format(
						"As the villagers gather, they notice someone missing. "
								+ "After some searching, their mauled corpse is found in their home. "
								+ "%s is dead!", bold(wolfVote.getName())));		
			} else {
				wolfVote.die(String.format(
						"As the villagers gather, they notice someone missing. "
								+ "After some searching, their mauled corpse is found in their home. "
								+ "%s the %s is dead!", bold(wolfVote.getName()),
								wolfVote.getRole().toStringColor()));
			}
		}
	}
}