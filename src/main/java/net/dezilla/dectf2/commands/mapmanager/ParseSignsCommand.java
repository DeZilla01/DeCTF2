package net.dezilla.dectf2.commands.mapmanager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.util.MapManagerWorld;

public class ParseSignsCommand extends Command{

	public ParseSignsCommand() {
		super("parsesigns");
		setUsage("/parsesigns");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		World w = null;
		if(args.length == 0 && sender instanceof Player) {
			Player p = (Player) sender;
			w = p.getWorld();
		} else if (args.length > 0) {
			w = Bukkit.getWorld(args[0]);
		}
			
		if(w == null) {
			sender.sendMessage(ChatColor.RED+"Invalid World.");
			return false;
		}
		MapManagerWorld world = MapManagerWorld.get(w);
		if(world == null) {
			sender.sendMessage(ChatColor.RED+"You cannot parse signs here.");
			return false;
		}
		world.parseSigns();;
		sender.sendMessage("Parsing signs for "+w.getName()+"...");
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		if(args.length==1) {
			for(World world : Bukkit.getWorlds()) {
				String s = ""+world.getName();
				if(s.toLowerCase().startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		}
		return list;
	}

}
