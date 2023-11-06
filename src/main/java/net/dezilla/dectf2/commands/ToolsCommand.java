package net.dezilla.dectf2.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.gui.ToolGui;
import net.md_5.bungee.api.ChatColor;

public class ToolsCommand extends Command{
	public ToolsCommand() {
		super("tools");
		setPermission("dectf2.command.tools");
		setDescription("Select tools to add in inventory");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED+"You must be a player to use this command.");
			return false;
		}
		Player p = (Player) sender;
		new ToolGui(p).display();
		return true;
	}
}
