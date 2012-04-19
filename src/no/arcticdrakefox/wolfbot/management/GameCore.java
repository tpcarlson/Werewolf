package no.arcticdrakefox.wolfbot.management;

import static no.arcticdrakefox.wolfbot.WolfBot.bold;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import no.arcticdrakefox.wolfbot.Timers.EndDayTask;
import no.arcticdrakefox.wolfbot.Timers.EndNightTask;
import no.arcticdrakefox.wolfbot.management.commands.Commands;
import no.arcticdrakefox.wolfbot.management.victory.Victory;
import no.arcticdrakefox.wolfbot.model.MessageType;
import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.State;
import no.arcticdrakefox.wolfbot.model.Team;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;
import no.arcticdrakefox.wolfbot.predicates.TeamPredicate;

import org.jibble.pircbot.Colors;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class GameCore {
	public static void lynchVote(String senderS, String targetS, WolfBotModel data, MessageType type) {
		Player sender = data.getPlayers().getPlayer(senderS);
		Player target = data.getPlayers().getPlayer(targetS);
		if (sender == null) {
			Commands.sendIrcMessage(data.getChannel(), 
					Messages.getString("GameCore.error.youAreNotInGame", //$NON-NLS-1$
					bold(senderS)), senderS, type); 
		} else if (!sender.isAlive()) {
			Commands.sendIrcMessage(data.getChannel(),
					Messages.getString("GameCore.error.youAreDead", //$NON-NLS-1$
					bold(senderS)), senderS, type); 
		} else if (target == null) {
			Commands.sendIrcMessage(
					data.getChannel(),
							Messages.getString("GameCore.error.target.notInGAme", //$NON-NLS-1$
							bold(senderS), bold(targetS)), senderS, type);
		} else if (!target.isAlive()) {
			Commands.sendIrcMessage(data.getChannel(), Messages.getString("GameCore.error.target.dead", //$NON-NLS-1$
					bold(senderS), bold(targetS)), senderS, type);
		} else {
			sender.vote(target);
			Commands.sendIrcMessage(data.getChannel(),Messages.getString("GameCore.voted",
					senderS, targetS), senderS, type); //$NON-NLS-1$
		}
		if (checkLynchMajority(data))
			endDay(true, data);
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
					data.getWolfBot().sendIrcMessage (data.getChannel(), Messages.getString("GameCore.retired.preGame", bold(name))); //$NON-NLS-1$
				}
				else
				{
					data.getWolfBot().sendIrcMessage(data.getChannel(),  Messages.getString("GameCore.retired.someoneElse", bold (sender), bold(name), bold(name)));  //$NON-NLS-1$
				}
			}
			else
			{
				data.getWolfBot().sendIrcMessage(data.getChannel(), 
						Messages.getString("GameCore.errro.targetNotingame", bold(name) )); //$NON-NLS-1$
			}
			return;
		}

		// Night or day at this point
		if (data.getPlayers().removePlayer(name)) {
			if (name.equals(sender))
			{
				data.getWolfBot().sendIrcMessage(data.getChannel(), 
						Messages.getString("GameCore.hasretired",  bold(name))); //$NON-NLS-1$
			}
			else
			{
				data.getWolfBot().sendIrcMessage(data.getChannel(), 
						Messages.getString("GameCore.wasretired", bold(name), bold(sender))); //$NON-NLS-1$
			}
			data.getWolfBot().setMode(data.getChannel(), "-v " + name); //$NON-NLS-1$
		} else {
			data.getWolfBot().sendIrcMessage(data.getChannel(), 
					Messages.getString("GameCore.error.target.notPlaying", bold(name))); //$NON-NLS-1$
		}
		
		// Two stages here - we want to check the lynch majority to 
		// see whether to go to night
		// We also need to checkVictory
		if (data.getState() == State.Day) // If we're in daytime (ie. voting)
		{
			if (checkLynchMajority(data))
				endDay(true, data);
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
		List<Victory> victoryConditions = Lists.newArrayList(data.getVictoryConditions());
		Collections.sort(victoryConditions);
		boolean isGameOver = false;
		String victoryString = null;
		for (Victory v : victoryConditions)
		{
//			data.getWolfBot().sendIrcMessage(data.getChannel(),String.format("testing role %s.", 
//					v.getClass().getCanonicalName()));
			if (v.isVictory(data.getPlayers()))
			{
				isGameOver = true;
				victoryString = v.getVictoryMessage(data.getPlayers());
				break;
			} else if(v.inhibitsOthersVictory(data.getPlayers())) {
				break;
			}
		}
		
		if (isGameOver && victoryString == null)
		{
			data.getWolfBot().sendIrcMessage(data.getChannel(), Messages.getString("GameCore.error.noVictoryString")); //$NON-NLS-1$
		}
		else if (isGameOver)
		{
			data.getWolfBot().sendIrcMessage(data.getChannel(), victoryString);
		}
		
		// Regardless, we want to endGame here
		if (isGameOver)
		{
			endGame(data);
		}
		
		return isGameOver;
	}

	static Timer endDayWarningTimer;
	static Timer endDayTimer;
	public static void startDay(final WolfBotModel data) {
		data.getWolfBot().sendIrcMessage(data.getChannel(),
				Messages.getString("GameCore.dayStart")); //$NON-NLS-1$
		data.setState(State.Day);
		data.getWolfBot().voiceAll();
		data.getPlayers().clearVotes();
		// Kick off a new task to endNight after 2 minutes:
		TimerTask endDayTask = new EndDayTask (data);
		endDayTimer = new Timer ();
		endDayTimer.schedule (endDayTask, 5*60*1000); // Should this be configurable?
		endDayWarningTimer = new Timer ();
		endDayWarningTimer.schedule(new TimerTask ()
		{
			@Override
			public void run() {
				data.getWolfBot().sendIrcMessage(data.getChannel(), "The sun is setting - come to a verdict, villagers! " + Colors.BOLD + "One minute" + Colors.NORMAL + " remaining.");
			}
		}, 4*60*1000); // 4 minutes time - 1 minute before the time is up
	}

	public static void endDay(boolean villagersVoted, WolfBotModel data) {
		endDayTimer.cancel();
		endDayWarningTimer.cancel();
		if (! villagersVoted)
		{
			data.getWolfBot().sendIrcMessage(data.getChannel(), "The sky turns blood-red - the villagers have run out of time! They flee to their homes as darkness decends...");
		}
		else
		{

			// Villagers voted and endDay is called due to lynchMajority succeess...
			Player vote = data.getPlayers().getVote();
			
			// If players have voted to skip then we should send an appropriate message
			if (vote == BotConstants.SKIP_VOTE_PLAYER)
			{
				data.getWolfBot().sendIrcMessage (data.getChannel(), Messages.getString("GameCore.skip")); //$NON-NLS-1$
			}
			else
			{
				if (WolfBotModel.getInstance().getSilentMode()) {
					vote.die(Messages.getString("GameCore.kill.noreveal", bold(vote.getName()))); //$NON-NLS-1$
				} else {
					if (vote.isWolf()) {
						vote.die(Messages.getString("GameCore.kill.reveal.wolf",  //$NON-NLS-1$
										Role.wolf.toStringColor(), bold(vote.getName()), vote.getRole().toStringColor()));
					} else {
						vote.die(Messages.getString("GameCore.kill.reveal.notwolf", //$NON-NLS-1$
										bold(vote.getName()), vote.getRole().toStringColor()));
					}
				}
			}
		}
		
		checkDead(data);
		
		if (!checkVictory(data))
			startNight(data);
	}

	public static Timer endNightTimer;
	public static void startNight(WolfBotModel data) {
		data.setState(State.Night);
		data.getWolfBot().deVoiceAll();
		data.getWolfBot().sendIrcMessage( data.getChannel(),
		Messages.getString("GameCore.nightstart")); //$NON-NLS-1$

		data.getPlayers().clearVotes();
		data.getWolfBot().sendNightStartMessages();
		data.getWolfBot().sendIrcMessage(data.getChannel(), "You have " + Colors.BOLD + "two minutes" + Colors.NORMAL + " to act.");
		// Kick off a new task to endNight after 2 minutes:
		TimerTask endNightTask = new EndNightTask (data);
		endNightTimer = new Timer ();
		endNightTimer.schedule (endNightTask, 2*60*1000); // Should this be configurable?
	}

	public static void endNight(WolfBotModel data) {
		// Kill the timer first:
		endNightTimer.cancel(); // Javadoc says this is safe when called by a TimerTask. We'll see.
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
			// Kill the other timers too:
			endDayWarningTimer.cancel();
			endDayTimer.cancel();
			endNightTimer.cancel();
			if (data.getState() == State.Starting)
			{
				data.getWolfBot().sendIrcMessage(data.getChannel(), Messages.getString("GameCore.startCancled")); //$NON-NLS-1$
			}
			else
			{
				printCast (data);
				data.getPlayers().reset();
				data.getWolfBot().setMode(data.getChannel(), "-m"); //$NON-NLS-1$
				data.getWolfBot().deVoiceAll();
				data.getWolfBot().sendIrcMessage(data.getChannel(), 
						Messages.getString("GameCore.thanks")); //$NON-NLS-1$
			}
			
			data.setState(State.None);
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
				data.getWolfBot().sendIrcMessage(data.getChannel(), team.getColored() + ": " + rolesForTeam); //$NON-NLS-1$
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
							Messages.getString("GameCore.vigilanti.multualTarget")); //$NON-NLS-1$
				} else {
					data.getWolfBot().sendIrcMessage(
							baner.getName(), Messages.getString("GameCore.vigilanti.killWolf", //$NON-NLS-1$
									baner.getVote()));
				}
			}
		}
		if (wolfVote == null
				|| data.getPlayers().getPlayerTargeting(wolfVote, Role.baner) != null) {
			data.getWolfBot().sendIrcMessage(data.getChannel(),
					Messages.getString("GameCore.noWolfKill")); //$NON-NLS-1$
		} else {
			if (WolfBotModel.getInstance().getSilentMode()) {
				wolfVote.die(Messages.getString("GameCore.wolfKill.noReveal", //$NON-NLS-1$
						bold(wolfVote.getName())));		
			} else {
				wolfVote.die(Messages.getString("GameCore.wolfKill.reveal", //$NON-NLS-1$
						bold(wolfVote.getName()), wolfVote.getRole().toStringColor()));
			}
		}
	}
}