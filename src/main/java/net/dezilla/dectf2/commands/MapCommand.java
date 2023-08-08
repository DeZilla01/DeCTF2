package net.dezilla.dectf2.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.dezilla.dectf2.game.GameMatch;
import net.md_5.bungee.api.ChatColor;

public class MapCommand extends Command {

	public MapCommand() {
		super("map");
		setUsage("/map");
		setDescription("Display information on the current map.");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null) {
			sender.sendMessage(ChatColor.RED+"No game currently loaded. Try again shortly.");
			return false;
		}
		sender.sendMessage("File Name: "+ChatColor.GOLD+match.getSourceZip().getName());
		sender.sendMessage("Map Name: "+ChatColor.GOLD+match.getMapName());
		sender.sendMessage("Map Author: "+ChatColor.GOLD+match.getMapAuthor());
		sender.sendMessage("Gamemode: "+ChatColor.GOLD+match.getGame().getGamemodeName());
		sender.sendMessage("Team Amount: "+ChatColor.GOLD+match.getTeamAmount());
		sender.sendMessage("Map Icon: "+ChatColor.GOLD+match.getMapIcon().getType().toString());
		return true;
	}

}
