package no.arcticdrakefox.wolfbot.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.arcticdrakefox.wolfbot.model.Role;


public class VoteTable {
	private HashMap<Player, List<Player>> votes = new HashMap<Player, List<Player>>();
	private int livingPlayerCount;
	
	public VoteTable(List<Player> livingPlayers){
		livingPlayerCount = livingPlayers.size();
		
		for (Player player : livingPlayers){
			Player vote = player.getVote();
			if (vote == null){
				continue;
			} else {
				List<Player> list = votes.get(vote);
				if (list == null){
					list = new ArrayList<Player>();
					votes.put(vote, list);
				}
				list.add(player);
				
				// Mayors get double votes:
				// TODO: Refactor me
				if (player.getRole() == Role.mayor)
				{
					list.add(player);
				}
			}
		}
	}
	
	public List<String> getSortedStringList(){
		int highestVote = livingPlayerCount + 1;
		List<String> ret = new ArrayList<String>();
		while (ret.size() < votes.size()){
			highestVote = getHighestVote(highestVote);
			List<Player> players = getPlayersWithVoteCount(highestVote);
			for (Player player : players){
				ret.add(String.format("%s: (%s)", player, StringHandler.listToString(votes.get(player))));
			}
		}
		return ret;
	}
	
	public List<Player> getPlayersWithVoteCount(int voteCount){
		List<Player> ret = new ArrayList<Player>();
		for (Player player : votes.keySet()){ 
			if (votes.get(player).size() == voteCount){
				ret.add(player);
			}
		}
		return ret;
	}
	
	public int getHighestVote(){
		return getHighestVote(livingPlayerCount + 1);
	}
	
	public int getHighestVote(int limit){
		int highest = 0;
		for (Player player : votes.keySet()){
			int vote = votes.get(player).size(); 
			if (vote > highest && vote < limit){
				highest = votes.get(player).size();
			}
		}
		
		if (votes.get(BotConstants.SKIP_VOTE_PLAYER) != null)
		{
			// We must also check the skip player:
			int skipvote = votes.get(BotConstants.SKIP_VOTE_PLAYER).size();
			if (skipvote > highest && skipvote < limit)
			{
				highest = skipvote;
			}
		}
		
		return highest;
	}
	
	public List<Player> getTargets(){
		return getPlayersWithVoteCount(getHighestVote());
	}
}