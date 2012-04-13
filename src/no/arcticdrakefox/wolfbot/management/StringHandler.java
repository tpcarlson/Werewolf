package no.arcticdrakefox.wolfbot.management;

import java.util.List;

import org.jibble.pircbot.Colors;

import com.google.common.collect.Iterables;

public class StringHandler {
	public static String stripColour(String line){
		return line.replaceAll("\\u0003\\d{0,2}", "");
	}
	
	public static boolean isInt(String number){
		return number.matches("\\d+");
	}
	
	public static int parseInt(String number){
		return Integer.parseInt(number);
	}
	
	public static String listToString(List<?> list){
		if (list.isEmpty())
			return BotConstants.NO_VOTES;
		String ret = list.get(0).toString();
		if (list.size() == 1)
			return ret;
		for (int i = 1; i < list.size() - 1; ++i) 
			ret += ", " + list.get(i);
		ret += " and " + list.get(list.size() - 1);
		return ret;
	}
	
	public static String playersListToStringWithRoles(List<Player> list){
		if (list.isEmpty())
			return null;
		String ret = Colors.BOLD + list.get(0).toString() + Colors.NORMAL + "(" + list.get(0).getRole() + ")";
		if (list.size() == 1)
			return ret;
		for (int i = 1; i < list.size() - 1; ++i) 
			ret += ", " + Colors.BOLD + list.get(i).toString() + Colors.NORMAL + "(" + list.get(i).getRole() + ")";
		ret += " and " + Colors.BOLD + Iterables.getLast(list).toString() + Colors.NORMAL + "(" + Iterables.getLast(list).getRole() + ")";
		return ret;
	}
	
	public static String listToStringSimple(List<?> list){
		String ret = "";
		for (int i = 0; i < list.size(); ++i) 
			ret += " " + list.get(i);
		return ret;
	}
	
	public static String listToStringSimplePlayers(List<Player> list){
		String ret = "";
		for (int i = 0; i < list.size(); ++i) 
			ret += " " + list.get(i).getName();
		return ret;
	}
}
