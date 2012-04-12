package no.arcticdrakefox.wolfbot.model;

import org.jibble.pircbot.Colors;

public enum Team {
	Villagers (Colors.GREEN),
	Wolves (Colors.RED),
	LoneWolf (Colors.BROWN);
	
	private String color;

	private Team(String color) {
		this.color = color;
	}
	
	public String getColored() {
		return color + this.toString() + Colors.NORMAL;
	}
	
	public String getColor () {
		return color;
	}
}