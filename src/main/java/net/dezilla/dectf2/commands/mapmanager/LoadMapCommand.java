package net.dezilla.dectf2.commands.mapmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.util.MapManagerWorld;
import net.md_5.bungee.api.ChatColor;

public class LoadMapCommand extends Command{

	public LoadMapCommand() {
		super("loadmap");
		setUsage("/loadmap <map file>");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED+"Usage: "+getUsage());
			return false;
		}
		File sourceZip = new File(Util.getGameMapFolder().getPath()+File.separator+args[0]);
		if(!sourceZip.exists()) {
			sender.sendMessage(ChatColor.RED+"File not found");
			return false;
		}
		sender.sendMessage("Loading world...");
		new MapManagerWorld().load(sourceZip, (world) -> {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				p.teleport(world.getSpawnLocation());
			}
			sender.sendMessage(args[0]+" has been loaded.");
		});
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		if(args.length==1) {
			for(File file : Util.getWorldList()) {
				String s = ""+file.getName();
				if(s.startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		}
		return list;
	}

}
