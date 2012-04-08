package no.arcticdrakefox.wolfbot.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.arcticdrakefox.wolfbot.management.Player.Role;
import no.arcticdrakefox.wolfbot.roles.Baner;
import no.arcticdrakefox.wolfbot.roles.Devil;
import no.arcticdrakefox.wolfbot.roles.Ghost;
import no.arcticdrakefox.wolfbot.roles.Mason;
import no.arcticdrakefox.wolfbot.roles.Mayor;
import no.arcticdrakefox.wolfbot.roles.OldMan;
import no.arcticdrakefox.wolfbot.roles.Scry;
import no.arcticdrakefox.wolfbot.roles.ToughGuy;
import no.arcticdrakefox.wolfbot.roles.Vigilante;
import no.arcticdrakefox.wolfbot.roles.Villager;
import no.arcticdrakefox.wolfbot.roles.Wolf;

import com.google.common.collect.Lists;

public class PlayerList {
	public List<Player> players = new ArrayList<Player>();
	private int[] roleCount = new int[Role.values().length];
	
	//Constructor
	
	public PlayerList(){
		autoRole(5);
	}
	
	//List information
	
	public int wolfCount(){
		return getWolves().size();
	}
	
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
	
	public List<Player> getRole(Player.Role role){
		List<Player> ret = new ArrayList<Player>();
		for (Player player : players){
			Player.Role playerRole = player.getRole();
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
				roleCountList.add(String.format("%s: %d", Player.Role.values()[i].toString(), roleCount[i]));
		return StringHandler.listToString(roleCountList);
	}
	
	public List<Player> getLivingPlayers(){
		List<Player> ret = new ArrayList<Player>(players.size());
		System.out.println(players.size());
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
	
		public boolean setRoleCount(String roleS, int amount){
			Player.Role role = Player.Role.valueOf(roleS.toLowerCase());
			if (role == null)
				return false;
			roleCount[role.ordinal()] = amount;
				return true;
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
	
	public Player makePlayer(String name, Player.Role role){
		switch (role){
			case villager:
				return new Villager(name);
			case wolf:
				return new Wolf(name);
			case scry:
				return new Scry(name);
			case baner:
				return new Baner(name);
			case devil:
				return new Devil(name);
			case vigilante:
				return new Vigilante(name);
			case ghost:
				return new Ghost(name);
			case mason:
				return new Mason(name);
			case toughguy:
				return new ToughGuy(name);
			case oldman:
				return new OldMan(name, wolfCount());
			case mayor:
				return new Mayor (name);
			default:
				return null;
		}
	};
	
	public Player getVote(){
		return getVote(false);
	}
	
	public Player getVote(boolean justWolves){
		VoteTable table = new VoteTable(
			justWolves ? getWolves() : getLivingPlayers() 
		);
		List<Player> targets = table.getTargets();
		int targetCount = targets.size();
		if (targetCount < 1){
			return null;
		} else if (targetCount == 1){
			return targets.get(0);
		} else {
			return targets.get(new Random().nextInt(targetCount));
		}
	}
	
	public Player getRandomPlayer(){
		return getLivingPlayers().get(new Random().nextInt(players.size()));
	}
	
	public Player getRandomPlayer(Player.Role role){
		List<Player> players = getRole(role); 
		return players.get(new Random().nextInt(players.size()));
	}
	
	public Player getPlayerTargeting(Player target, Player.Role role){
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
				Player player = getRandomPlayer(Player.Role.villager);
				int index = players.indexOf(player);
				players.set(index, makePlayer(player.getName(), Player.Role.values()[i]));
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
		case 3:
			roleCount[Player.Role.wolf.ordinal()] = 1;
			roleCount[Player.Role.mason.ordinal()] = 1;
			break;
		case 4:
			roleCount[Player.Role.wolf.ordinal()] = 1;
			addWarrior();
			break;
		case 6:
			addWarrior();
			//break intentionally omitted
		case 5:
			roleCount[Player.Role.wolf.ordinal()] = 1;
			roleCount[Player.Role.scry.ordinal()] = 1;
			break;
		case 7:
			roleCount[Player.Role.wolf.ordinal()] = 2;
			roleCount[Player.Role.vigilante.ordinal()] = 1;
			addWarrior();
			break;
		case 8:
		case 9:
			roleCount[Player.Role.wolf.ordinal()] = 2;
			roleCount[Player.Role.scry.ordinal()] = 1;
			addWarrior();
		default:
			roleCount[Player.Role.mason.ordinal()]++;
			roleCount[Player.Role.wolf.ordinal()]++;
		case 17:
		case 16:
		case 15:
		case 14:
			roleCount[Player.Role.mason.ordinal()] += 2;
			roleCount[Player.Role.wolf.ordinal()]++;
		case 13:
			addWarrior();
		case 12:
		case 10:
		case 11:
			roleCount[Player.Role.wolf.ordinal()]++;
			roleCount[Player.Role.devil.ordinal()] = 1;
			roleCount[Player.Role.scry.ordinal()] = 1;
			addWarrior();
			break;
		}
	}
	
	public void addWarrior(){
		if (new Random().nextBoolean())
			roleCount[Player.Role.baner.ordinal()]++;
		else
			roleCount[Player.Role.vigilante.ordinal()]++;
	}
}