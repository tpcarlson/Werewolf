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
import no.arcticdrakefox.wolfbot.roles.Mason;
import no.arcticdrakefox.wolfbot.roles.Mayor;
import no.arcticdrakefox.wolfbot.roles.OldMan;
import no.arcticdrakefox.wolfbot.roles.Scry;
import no.arcticdrakefox.wolfbot.roles.ToughGuy;
import no.arcticdrakefox.wolfbot.roles.Vigilante;
import no.arcticdrakefox.wolfbot.roles.Villager;
import no.arcticdrakefox.wolfbot.roles.Wolf;

public enum Role {	
	villager (Villager.class), 
	wolf (Wolf.class), 
	scry (Scry.class),
	devil (Devil.class),
	baner (Baner.class),
	ghost (Ghost.class),
	vigilante (Vigilante.class),
	mason (Mason.class),
	toughguy (ToughGuy.class),
	oldman (OldMan.class),
	mayor (Mayor.class),
	aura_scry (AuraScry.class),
	apprentice_scry (ApprenticeScry.class),
	diseased (Diseased.class);
	
	private Class<? extends Player> c;
	
	private Role (Class<? extends Player> c) {
		this.c = c;
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
}

