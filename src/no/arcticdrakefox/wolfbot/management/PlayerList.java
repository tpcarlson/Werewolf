package no.arcticdrakefox.wolfbot.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.roles.Villager;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class PlayerList{
	public List<Player> players = new ArrayList<Player>();
	private int[] roleCount = new int[Role.values().length];
	
	//Constructor
	
	public PlayerList(){
		autoRole(5);
	}
	
	//List information
	
	/**
	 * 
	 * @return Count of wolves
	 */
	public int wolfCount(){
		return getWolves().size();
	}
	
	/**
	 * Returns the number of living players
	 * 
	 * @return Number of players still alive
	 */
	public int playerCount(){
		return getLivingPlayers().size();
	}
	
	
	public String votesToString(){
		return StringHandler.listToString(new VoteTable(getLivingPlayers()).getSortedStringList());
	}
	
	public String nonvotersToString ()
	{
		List<Player> playersNotVoted = Lists.newArrayList ();
		for (Player player : getLivingPlayers())
		{
			if (player.getVote() == null)
			{
				playersNotVoted.add(player);
			}
		}
		
		String nonVoters = StringHandler.listToString(playersNotVoted);
		if (nonVoters.equals(BotConstants.NO_VOTES))
		{
			return "";
		}
		else
		{
			return nonVoters;
		}
	}
	
	public List<Player> getWolves(){
		List<Player> ret = new ArrayList<Player>();
		for (Player player : players)
			if (player.isWolf() && player.isAlive())
				ret.add(player);
		return ret;
	}
	
	public List<Player> getRole(Role role){
		List<Player> ret = new ArrayList<Player>();
		for (Player player : players){
			Role playerRole = player.getRole();
			if (playerRole == role && player.isAlive())
				ret.add(player);
		}
		return ret;
	}
	
	public String toString(){
		return StringHandler.listToString(players);
	}
	
	public String roleCountToString(){
		List<String> roleCountList = new ArrayList<String>(roleCount.length-1);
		for (int i = 1; i < roleCount.length; ++i)
			if (roleCount[i] > 0)
				roleCountList.add(String.format("%s: %d", Role.values()[i].toString(), roleCount[i]));
		return StringHandler.listToString(roleCountList);
	}
	
	public List<Player> getPlayersWithNightActions() {
		List<Player> ret = new ArrayList<Player>(players.size());

		for (Player player : players)
		{
			if (player.isAlive() || player.hasDeadActions())
			{
				ret.add(player);
			}
		}	
		return ret;
	}
	
	public List<Player> getLivingPlayers(){
		List<Player> ret = new ArrayList<Player>(players.size());

		for (Player player : players)
		{
			if (player.isAlive())
			{
				ret.add(player);
			}
		}	
		return ret;
	}
	
	public List<Player> getRecentlyDead(){
		List<Player> ret = new ArrayList<Player>(players.size());
		for (Player player : players)
			if (player.getCauseOfDeath() != null)
				ret.add(player);
		return ret;
	}
	
	public List<Player> getList(){
		return players;
	}
	
	public boolean allReady(){
		for (Player player : getLivingPlayers())
			if (!player.isReady)
				return false;
		return true;
	}
	
	public int totalRoleCount(){
		int ret = 0;
		for(int i : roleCount)
			ret += i;
		return ret;
	}
	
	//Setting list data
	
		public boolean setRoleCount(String roleS, int amount) throws WerewolfException{
			try
			{
				Role role = Role.valueOf(roleS.toLowerCase());
				if (role == null)
					throw new WerewolfException ("Role ended up null...");
				roleCount[role.ordinal()] = amount;
					return true;
			}
			catch (IllegalArgumentException illegal) // If a bad type is 
			{
				throw new WerewolfException ("Sorry, I don't know what a " + roleS + " is...");
			}
		}
	
	//Individual player operations
	
	public boolean addPlayer(String name){
		if (getPlayer(name) == null){
			players.add(new Villager(name));
			return true;
		} else
			return false;
	}
	
	public boolean removePlayer(String name){
		Player player = getPlayer(name);
		if (player == null){
			return false;
		} else {
			players.remove(player);
			return true;
		}
	}
	
	public Player getPlayer(String name){
		for (Player player : players)
			if (player.getName().equalsIgnoreCase(name))
				return player;
		return null;
	}
		
	public Player getVote(){
		return getVote(false);
	}
	
	public Player getVote(boolean justWolves){
		VoteTable table = new VoteTable(
			justWolves ? getWolves() : getLivingPlayers() 
		);
		List<Player> targets = table.getTargets();
		int targetCount = targets.size();
		if (targetCount == 0){ // No targets were picked
			return null;
		} else if (targetCount == 1){ // Exactly one target was picked
			return Iterables.getFirst(targets, null);
		} else { // Multiple targets were picked (Perhaps NOT ok for Wolves)
			if (justWolves)
			{
				return getMajorityVoteFromVoteTable(table);
			}
			else
			{
				return targets.get(new Random().nextInt(targetCount));
			}
		}
	}
	
	/*
	 * This MUST be called with a VoteTable that has a series of votes in
	 * As the only caller of this method is getVote () this is safe.
	 */
	private Player getMajorityVoteFromVoteTable (VoteTable table)
	{
		if (table.getTargets().isEmpty()) // Called wrongly
		{
			throw new RuntimeException ("getMajorityVoteFromVoteTable must be called with a non-empty vote table");
		}
		
		if (table.getTargets().size() > 1) // Majority not reached
		{
			return null;
		}
		else // There is a majority (But this could be 2 of 3 wolves)
		{
			return Iterables.getFirst(table.getTargets(), null);
		}
	}
	
	public Player getRandomPlayer(){
		return getLivingPlayers().get(new Random().nextInt(players.size()));
	}
	
	public Player getRandomPlayer(Role role){
		List<Player> players = getRole(role); 
		return players.get(new Random().nextInt(players.size()));
	}
	
	public Player getPlayerTargeting(Player target, Role role){
		for (Player player : getRole(role))
			if (target.equals(player.getVote()))
				return player;
		return null;
	}
		
	//Mass player operations
	
	public void reset(){
		List<Player> newList = new ArrayList<Player>(players.size());
		for (Player player : players){
			newList.add(new Villager(player.getName()));
		}
		players = newList;
	}
	
	public void clearVotes() {
		for (Player player : players)
			player.clearVote();
	}
	
	public void clearRecentlyDead(){
		for (Player player : players)
			if (player.getCauseOfDeath() != null)
				player.clearCauseOfDeath();
	}
	
	public void assignRoles(){
		for (int i = 0; i < roleCount.length; ++i){
			for (int j = 0; j < roleCount[i]; ++j){
				Player player = getRandomPlayer(Role.villager);
				int index = players.indexOf(player);
				Player p = Role.values()[i].init(player.getName());
				players.set(index, p);
			}
		}
	}
	
	//Autorole
	
	public void clearRoles(){
		for (int i = 0; i < roleCount.length; ++i)
			roleCount[i] = 0;
	}
	
	public void autoRole(){
		autoRole(players.size());
	}
	
	public void autoRole(int players){
		clearRoles();
		switch (players){
		default:
			roleCount[Role.mason.ordinal()]++;
			roleCount[Role.wolf.ordinal()]++;
		case 17:
		case 16:
			addOther();
		case 15:
		case 14:
			roleCount[Role.mason.ordinal()] += 2;
			roleCount[Role.wolf.ordinal()]++;
		case 13:
			addWarrior();
		case 12:
		case 11:
		case 10:
			roleCount[Role.wolf.ordinal()]++;
			roleCount[Role.devil.ordinal()] = 1;
			roleCount[Role.scry.ordinal()] = 1;
			addWarrior();
			break;
		case 9:
			roleCount[Role.scry.ordinal()] = 1;
		case 8:
		case 7:
			roleCount[Role.wolf.ordinal()] = 2;
			roleCount[Role.vigilante.ordinal()] = 1;
			addWarrior();
			break;
		case 6:
			addWarrior();
		case 5:
			roleCount[Role.wolf.ordinal()] = 1;
			roleCount[Role.scry.ordinal()] = 1;
			break;
		case 4:
			roleCount[Role.wolf.ordinal()] = 1;
			addWarrior();
			break;
		case 3:
		case 2:
		case 1:
		case 0:
			roleCount[Role.wolf.ordinal()] = 1;
			break;
		}
	}
	
	public void addWarrior(){
		if (new Random().nextBoolean())
			roleCount[Role.baner.ordinal()]++;
		else
			roleCount[Role.vigilante.ordinal()]++;
	}
	public void addOther(){
		switch (new Random().nextInt(3)){
		case 0: 
			roleCount[Role.oldman.ordinal()]++;
		case 1: 
			roleCount[Role.toughguy.ordinal()]++;
		case 2: 
			roleCount[Role.mason.ordinal()]++;
		}
	}
}