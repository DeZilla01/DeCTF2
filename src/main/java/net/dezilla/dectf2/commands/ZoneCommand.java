package net.dezilla.dectf2.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.zc.Zone;
import net.dezilla.dectf2.game.zc.ZonesGame;
import net.md_5.bungee.api.ChatColor;

public class ZoneCommand extends Command {

	public ZoneCommand() {
		super("zone");
		setDescription("Change certain parameters for a zone");
		setUsage("/zone [ID/name] [material/iscaptured/progress/name/rate] <value>");
		setPermission("dectf2.command.zone");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null || !match.isLoaded()) {
			sender.sendMessage(ChatColor.RED+"No game currently loaded. Try again shortly.");
			return false;
		}
		if(!(match.getGame() instanceof ZonesGame)) {
			sender.sendMessage(ChatColor.RED+"This command can only be used during a ZC game.");
			return false;
		}
		if(args.length < 3) {
			sender.sendMessage(ChatColor.RED+"Missing arguments.");
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
		ZonesGame game = (ZonesGame) match.getGame();
		Zone zone = null;
		try {
			zone = game.getZones().get(Integer.parseInt(args[0]));
		}catch(Exception e) {}
		if(zone == null) {
			for(Zone z : game.getZones()) {
				if(z.getName().equalsIgnoreCase(args[0])) {
					zone = z;
					break;
				}
			}
		}
		if(zone == null) {
			sender.sendMessage(ChatColor.RED+"Invalid zone.");
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
		
		if(args[1].equalsIgnoreCase("material")) {
			try {
				Material mat = Material.valueOf(args[2].toUpperCase());
				zone.setZoneMaterial(mat);
				sender.sendMessage("Material of "+zone.getName()+" has been changed to "+mat.toString());
				return true;
			} catch(Exception e) {
				sender.sendMessage(ChatColor.RED+"Invalid material");
				sender.sendMessage(ChatColor.RED+getUsage());
				return false;
			}
		}
		if(args[1].equalsIgnoreCase("iscaptured")) {
			try {
				boolean value = Boolean.valueOf(args[2].toLowerCase());
				zone.setCaptured(value);
				sender.sendMessage(zone.getName()+" captured set to "+value);
				return true;
			}catch(Exception e) {
				sender.sendMessage(ChatColor.RED+"Invalid value");
				sender.sendMessage(ChatColor.RED+getUsage());
				return false;
			}
		}
		if(args[1].equalsIgnoreCase("name")) {
			String name = "";
			for(int i = 2 ; i<args.length ; i++) {
				name+=args[i];
				if(i+1<args.length)
					name+=" ";
			}
			zone.setName(name);
			sender.sendMessage("Zone name changed to "+name);
			return true;
		}
		if(args[1].equalsIgnoreCase("progress")) {
			try {
				Double value = Double.valueOf(args[2].toLowerCase());
				zone.setCaptureProgress(value);
				sender.sendMessage(zone.getName()+" progress changed to "+((int)(value*100))+"%");
				return true;
			}catch(Exception e) {
				sender.sendMessage(ChatColor.RED+"Invalid value");
				sender.sendMessage(ChatColor.RED+getUsage());
				return false;
			}
		}
		if(args[1].equalsIgnoreCase("rate")) {
			try {
				Double value = Double.valueOf(args[2].toLowerCase());
				zone.setCaptureRate(value);
				sender.sendMessage(zone.getName()+" capture rate changed to "+((int)(value*100))+"%");
				return true;
			}catch(Exception e) {
				sender.sendMessage(ChatColor.RED+"Invalid value");
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
		if(!(match.getGame() instanceof ZonesGame))
			return list;
		ZonesGame game = (ZonesGame) match.getGame();
		if(args.length==1) {
			for(int i = 0; i<game.getZones().size(); i++) {
				String s = ""+i;
				if(s.toLowerCase().startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		}
		if(args.length==2) {
			for(String s : Arrays.asList("material", "iscaptured", "progress", "name", "rate")) {
				if(s.toLowerCase().startsWith(args[1].toLowerCase()))
					list.add(s);
			}
		}
		if(args.length==3 && args[1].equalsIgnoreCase("material")) {
			for(Material m : Material.values()) {
				if(m.toString().toLowerCase().startsWith(args[2].toLowerCase()))
					list.add(m.toString().toLowerCase());
			}
		}
		if(args.length==3 && args[1].equalsIgnoreCase("iscaptured")) {
			for(String s : Arrays.asList("true", "false")) {
				if(s.startsWith(args[2].toLowerCase()))
					list.add(s);
			}
		}
		return list;
	}

}
