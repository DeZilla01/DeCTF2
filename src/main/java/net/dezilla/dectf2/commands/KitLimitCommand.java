package net.dezilla.dectf2.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.ClassManager.KitManager;
import net.dezilla.dectf2.kits.BaseKit;
import net.md_5.bungee.api.ChatColor;

public class KitLimitCommand extends Command{

	public KitLimitCommand() {
		super("kitlimit");
		setUsage("/kitlimit");
		setDescription("Configure kit limits");
		setPermission("dectf2.command.kitlimit");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
		String kitName = args[0].toLowerCase();
		Map<String, Class<? extends BaseKit>> m = GameMain.getInstance().kitMap();
		if(!m.containsKey(kitName)){
			sender.sendMessage(ChatColor.RED+"Kit not found.");
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
		KitManager manager = GameMain.getClassManager().getManager(kitName);
		if(args.length == 1) {
			sender.sendMessage(manager.getKitName()+": "+manager.getLimit());
			for(Entry<String, Integer> e : manager.getLimits().entrySet()) {
				sender.sendMessage(e.getKey()+": "+e.getValue());
			}
			return true;
		}
		if(args.length == 2) {
			try {
				int amount = Integer.parseInt(args[1]);
				manager.setLimit(amount);
				sender.sendMessage("set");
				return true;
			} catch(Exception e) {
				sender.sendMessage(ChatColor.RED+"Invalid value");
				sender.sendMessage(ChatColor.RED+getUsage());
				return false;
			}
		}
		try {
			int amount = Integer.parseInt(args[2]);
			manager.setLimit(args[1].toLowerCase(), amount);
			sender.sendMessage("set");
			return true;
		} catch(Exception e) {
			sender.sendMessage(ChatColor.RED+"Invalid value");
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		if(args.length==1 && GameMain.getInstance().kitMap().containsKey(alias.toLowerCase())) {
			try {
				BaseKit k = GameMain.getInstance().kitMap().get(alias.toLowerCase()).getConstructor(new Class[] {GamePlayer.class}).newInstance(GamePlayer.get(null));
				for(String s : k.getVariations()) {
					if(s.startsWith(args[0].toLowerCase()))
						list.add(s);
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		} else if(args.length == 1) {
			for(Entry<String, Class<? extends BaseKit>> e : GameMain.getInstance().kitMap().entrySet()) {
				if(e.getKey().startsWith(args[0].toLowerCase()))
					list.add(e.getKey());
			}
		} else if(args.length == 2 && GameMain.getInstance().kitMap().containsKey(args[0].toLowerCase())) {
			try {
				BaseKit k = GameMain.getInstance().kitMap().get(args[0].toLowerCase()).getConstructor(new Class[] {GamePlayer.class}).newInstance(GamePlayer.get(null));
				for(String s : k.getVariations()) {
					if(s.startsWith(args[1].toLowerCase()))
						list.add(s);
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

}
