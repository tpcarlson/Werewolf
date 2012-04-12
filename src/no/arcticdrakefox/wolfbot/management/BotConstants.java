package no.arcticdrakefox.wolfbot.management;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import no.arcticdrakefox.wolfbot.model.Role;
import no.arcticdrakefox.wolfbot.roles.SkipPlayer;

public class BotConstants {
	public static final String NO_VOTES = "Noone ;_;";
	public static final String SKIP_VOTE_NICKNAME = "Skipped!";
	public static final Player SKIP_VOTE_PLAYER = new SkipPlayer ();
	public static final String HELP_COMMANDS = 
	     "!join, !drop [player], !start, !end, !set <role> <count>, reveal on|off "
       + "!list, !roles, !autorole, !lynch/!vote/!kill, !votes, !time, !help";
	
	// TODO: Roles should handle help?
	public static String help(String command, String arg) {
		switch (command) {
		case "role":
			Role role = Role.valueOf(arg.toLowerCase());
			return role.init("tmp").helpText();
		}
		return "Sorry I can't help you with that!";
	}
	
	// TODO: Commands should implement help
	public static String help(String command) {
		switch (command) {
		case "join":
			return "!join: Signs up for the next game. Can only be used when the game is not in progress.";
		case "drop":
			return "!drop [player]: Removes yourself or specified user from game. Don't be a jerk and use it nilly-willy!";
		case "start":
			return "!start: Starts the game! Will restart if the game is in progress.";
		case "end":
			return "!end: Ends game in progress.";
		case "set":
			return "!set <role> <count>: sets specified role to specified amount. Valid roles are " + StringHandler.listToString(Lists.newArrayList(Role.values ())); //Joiner.on(", ").join(Role.values())+ ".";
		case "list":
			return "!list: Shows all living players in game.";
		case "roles":
			return "!roles: Shows amount of each role the game started with. It will not adapt as roles are killed off.";
		case "lynch":
		case "vote":
			return "!lynch <player>: Votes on a player for lynching. Over half the villagers must agree on a target before day ends.";
		case "votes":
			return "!votes: Displays who voted for which player.";
		case "time":
			return "!time: Shows wether it is night or day. Returns None if game hasn't started.";
		case "help":
			return "!help [command]: Returns a list of all commands (differnt if you PM) or shows description of supplied command.";
		case "role":
			return "!role: Tells you which role you are and what the role does. (PM only)";
		case "ghost":
			return "!ghost <target>: Selects a target for ressurection. If you're killed at night end it will not work. (PM only)";
		case "kill":
			return "!kill <player>: Selects a target for killing as either wolf, devil or vigilante. Wolves just vote for a target not unlike villagers at day. (PM only)";
		case "scry":
			return "!scry <player>: Selects a target for scrying. Scries check wether or not a person is a wolf. Devil retrieve the person's exact role. (PM only)";
		case "bane":
			return "!bane <player>: Selects a target to protect from wolves. You can protect yourself. (PM only)";
		case "rest":
			return "!rest <players>: Opts out of night action. (PM only)";
		case "notices":
			return "!notices on|off: Enable or disable notice messaging";
		case "autorole":
			return "!autorole sets a standard set of roles for the current number of players. Use just before !start.";
		case "reveal":
			return "!reveal on|off: When off you will not be told what class a player is when they die.";
		
		default:
			return "Unknown command";
		}
	}
}
