package net.dezilla.dectf2.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.md_5.bungee.api.ChatColor;

public class ChangeMapCommand extends Command{

	public ChangeMapCommand() {
		super("changemap");
		setUsage("/changemap <map file>");
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
		try {
			GameMatch.nextMatch = new GameMatch(sourceZip.getName());
			if(GameMatch.currentMatch == null)
				GameMatch.nextMatch.Load((w) -> {
					for(Player p : Bukkit.getOnlinePlayers()) {
						GameMatch.nextMatch.addPlayer(GamePlayer.get(p));
					}
				});
			else
				GameMatch.currentMatch.endPostGame();
			sender.sendMessage("Match changed to "+sourceZip.getName());
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED+"An error occured.");
		}
		return false;
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
