package no.arcticdrakefox.wolfbot.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import no.arcticdrakefox.wolfbot.management.Player;
import no.arcticdrakefox.wolfbot.roles.ApprenticeScry;
import no.arcticdrakefox.wolfbot.roles.AuraScry;
import no.arcticdrakefox.wolfbot.roles.Baner;
import no.arcticdrakefox.wolfbot.roles.Devil;
import no.arcticdrakefox.wolfbot.roles.Diseased;
import no.arcticdrakefox.wolfbot.roles.Ghost;
import no.arcticdrakefox.wolfbot.roles.LoneWolf;
import no.arcticdrakefox.wolfbot.roles.Necromancer;
import no.arcticdrakefox.wolfbot.roles.Mason;
import no.arcticdrakefox.wolfbot.roles.Mayor;
import no.arcticdrakefox.wolfbot.roles.OldMan;
import no.arcticdrakefox.wolfbot.roles.Scry;
import no.arcticdrakefox.wolfbot.roles.ToughGuy;
import no.arcticdrakefox.wolfbot.roles.Vigilante;
import no.arcticdrakefox.wolfbot.roles.Villager;
import no.arcticdrakefox.wolfbot.roles.Wolf;

import org.jibble.pircbot.Colors;

public enum Role {	
	villager (Villager.class, Team.Villagers), 
	wolf (Wolf.class, Team.Wolves), 
	scry (Scry.class, Team.Villagers),
	devil (Devil.class, Team.Wolves),
	baner (Baner.class, Team.Villagers),
	necromancer (Necromancer.class, Team.Villagers),
	vigilante (Vigilante.class, Team.Villagers),
	mason (Mason.class, Team.Villagers),
	toughguy (ToughGuy.class, Team.Villagers),
	oldman (OldMan.class, Team.Villagers),
	mayor (Mayor.class, Team.Villagers),
	aura_scry (AuraScry.class, Team.Villagers),
	apprentice_scry (ApprenticeScry.class, Team.Villagers),
	diseased (Diseased.class, Team.Villagers),
	ghost(Ghost.class, Team.Villagers),
	lonewolf (LoneWolf.class, Team.LoneWolf);
	
	private Class<? extends Player> c;
	private Team team;
	
	public Team getTeam() {
		return team;
	}

	private Role (Class<? extends Player> c, Team team) {
		this.c = c;
		this.team = team;
	}
	
	public Class<? extends Player> getRoleClass() {
		return c;
	}
	
	public Player init(String name) throws RuntimeException {
		try {
			Constructor<? extends Player> constructor =
			        c.getConstructor(new Class[]{String.class});
			return constructor.newInstance(name);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		throw new RuntimeException();
	}
	
	public String toStringColor() {
		return team.getColor() + this.toString() + Colors.NORMAL;
	}
}

