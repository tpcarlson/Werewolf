package no.arcticdrakefox.wolfbot.management;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;
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

public class PlayerFactory
{
	public static Player makePlayer(String name, Role role){
		switch (role){
			case villager:
				return new Villager(name);
			case diseased:
				return new Diseased(name);
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
				int wolves = Collections2.filter(WolfBotModel.getInstance().getPlayers().players, new Predicate<Player>() {
					@Override
					public boolean apply(Player player) {
						return player.isWolf();
					}
				}).size();
				return new OldMan(name, wolves);
			case mayor:
				return new Mayor (name);
			case aura_scry:
				return new AuraScry(name);
			case apprentice_scry:
				return new ApprenticeScry(name);
			default:
				return null;
		}
	};
}
