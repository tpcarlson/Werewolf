package no.arcticdrakefox.wolfbot.model;

import no.arcticdrakefox.wolfbot.management.victory.LoneWolfVictory;
import no.arcticdrakefox.wolfbot.management.victory.Victory;
import no.arcticdrakefox.wolfbot.management.victory.VillagerVictory;
import no.arcticdrakefox.wolfbot.management.victory.WolfVictory;

import org.jibble.pircbot.Colors;

public enum Team {
	Villagers (Colors.GREEN, new VillagerVictory(1)),
	Wolves (Colors.RED, new WolfVictory(1)),
	LoneWolf (Colors.BROWN, new LoneWolfVictory(0));
	
	private String color;
	private Victory victory;

	private Team(String color, Victory victory) {
		this.color = color;
		this.victory = victory;
	}
	
	public String getColored() {
		return color + this.toString() + Colors.NORMAL;
	}
	
	public String getColor () {
		return color;
	}
	
	public Victory getVictory() {
		return victory;
	}
}