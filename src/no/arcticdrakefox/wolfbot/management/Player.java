package no.arcticdrakefox.wolfbot.management;

public abstract class Player {
	public static enum Role {
		villager, wolf, scry, devil, baner, ghost, vigilante, mason, toughguy, oldman, mayor
	}
	
	protected String name;
	public String getName(){return name;}
	public void rename(String name){this.name = name;}
	
	protected Player vote;
	public Player getVote(){return vote;}
	public void vote(Player vote){this.vote = vote;}
	public void clearVote(){vote = null;}
	
	protected boolean isAlive = true;
	public boolean isAlive(){return isAlive;}
	public void revive(){isAlive = true;}
	
	protected String causeOfDeath;
	public String getCauseOfDeath(){return causeOfDeath;}
	public void clearCauseOfDeath(){causeOfDeath = null;}
	
	public void die(String causeOfDeath){
		isAlive = false;
		this.causeOfDeath = causeOfDeath;
	}
	
	protected boolean isReady = true;
	public boolean isReady(){return isReady;}
	
	public Player(String name){
		this.name = name;
	}
	
	public String toString(){
		return "\u0002"+name+"\u0002";
	}
	
	protected final String targetNotFound(String name){ 
		return String.format("Target %s not found. They might be dead already or you could've mispelled their name.", name);
	}
	
	public abstract boolean isWolf();
	
	public abstract Role getRole();
	public abstract String roleInfo(PlayerList players);
	
	public abstract String nightStart();
	public abstract String nightAction(String message, PlayerList players);
	public abstract String nightEnd();
}