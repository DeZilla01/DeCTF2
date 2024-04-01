package net.dezilla.dectf2.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.dezilla.dectf2.util.GameConfig;
import net.md_5.bungee.api.ChatColor;

public class DeCTF2Command extends Command{

	public DeCTF2Command() {
		super("dectf2");
		setUsage("/dectf2 <reload>");
		setDescription("Reload configs.");
		setPermission("dectf2.command.reload");
	}

	@Override
	public boolean execute(CommandSender sender, String alias, String[] args) {
		if(args.length != 0 && args[0].toLowerCase().equals("reload")) {
			GameConfig.reloadConfig();
			sender.sendMessage("Configs reloaded");
			return true;
		}
		sender.sendMessage(ChatColor.RED+getUsage());
		return false;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		if(args.length==1) {
			if("reload".startsWith(args[0].toLowerCase()))
				list.add("reload");
		}
		return list;
	}

}
