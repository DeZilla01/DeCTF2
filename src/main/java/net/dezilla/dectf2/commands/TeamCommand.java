package net.dezilla.dectf2.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.util.GameColor;

public class TeamCommand extends Command {

	public TeamCommand() {
		super("teams");
		setDescription("Change certain parameters for a team");
		setUsage("/teams [Team Name/ID] [name/score/color] [value]");
		setPermission("dectf2.command.teams");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null || !match.isLoaded()) {
			sender.sendMessage(ChatColor.RED+"No game currently loaded. Try again shortly.");
			return false;
		}
		if(args.length < 3) {
			sender.sendMessage(ChatColor.RED+"Missing arguments.");
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
		GameTeam team = null;
		try {
			team = match.getTeam(Integer.parseInt(args[0]));
		}catch(Exception e) {
			team = match.getTeam(args[0]);
		}
		if(team == null) {
			sender.sendMessage(ChatColor.RED+"Invalid team.");
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
		if(args[1].equalsIgnoreCase("name")) {
			String n = "";
			for(int i = 2; i<args.length; i++) {
				n+=args[i];
				if(i!=args.length-1)
					n+=" ";
			}
			team.setTeamName(n);
			sender.sendMessage("Changed team name to "+team.getColoredTeamName());
			return true;
		}
		if(args[1].equalsIgnoreCase("color")) {
			if(!match.isBlockParsed()) {
				sender.sendMessage(ChatColor.RED+"Please wait a moment while blocks are parsing.");
				return false;
			}
			try {
				GameColor color = GameColor.valueOf(args[2].toUpperCase());
				team.changeTeamColor(color);
				sender.sendMessage("Changed team color to "+color.getPrefix()+color.getName());
				for(Player p : Bukkit.getOnlinePlayers()) {
					GamePlayer.get(p).updateScoreboardTeams();
				}
				return true;
			}catch(Exception e) {
				sender.sendMessage(ChatColor.RED+"Invalid color.");
				sender.sendMessage(ChatColor.RED+getUsage());
				return false;
			}
		}
		if(args[1].equalsIgnoreCase("score")) {
			try {
				int amount = Integer.parseInt(args[2]);
				team.setScore(amount);
				sender.sendMessage("Changed team score to "+amount);
				return true;
			}catch(Exception e) {
				sender.sendMessage(ChatColor.RED+"Invalid amount.");
				sender.sendMessage(ChatColor.RED+getUsage());
				return false;
			}
		}
		sender.sendMessage(ChatColor.RED+getUsage());
		return false;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		GameMatch match = GameMatch.currentMatch;
		if(match==null)
			return list;
		if(args.length==1) {
			for(GameTeam team : match.getTeams()) {
				String s = ""+team.getId();
				if(s.toLowerCase().startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		}
		if(args.length==2) {
			for(String s : Arrays.asList("score", "color", "name")) {
				if(s.toLowerCase().startsWith(args[1].toLowerCase()))
					list.add(s);
			}
		}
		if(args.length==3 && args[1].equalsIgnoreCase("color")) {
			for(GameColor c : GameColor.values()) {
				if(c.toString().toLowerCase().startsWith(args[2].toLowerCase()))
					list.add(c.toString().toLowerCase());
			}
			if(list.contains("custom"))
				list.remove("custom");
		}
		return list;
	}

}
