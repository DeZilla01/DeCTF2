package net.dezilla.dectf2.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.kits.BaseKit;

public class KitCommand extends Command implements CommandExecutor{

	public KitCommand() {
		super("kit", "Change and select your kit.", "/kit [kit name] [variation]", Arrays.asList("kits", "class", "classes"));
	}
	
	public void addAliases(List<String> aliases) {
		List<String> a = this.getAliases();
		a.addAll(aliases);
		this.setAliases(a);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED+"You must be a player to use this command.");
			return false;
		}
		if(GameMain.getInstance().kitMap().containsKey(commandLabel.toLowerCase())) {
			GamePlayer p = GamePlayer.get((Player) sender);
			if(args.length>0)
				p.setKit(GameMain.getInstance().kitMap().get(commandLabel.toLowerCase()), args[0]);
			else
				p.setKit(GameMain.getInstance().kitMap().get(commandLabel.toLowerCase()));
			System.out.println("set");
		}else {
			System.out.println(commandLabel+" - "+"stuff");
			for(Entry<String, Class<? extends BaseKit>> entry : GameMain.getInstance().kitMap().entrySet()) {
				System.out.println(entry.getKey());
			}
		}
		sender.sendMessage("Kit Command");
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return execute(sender, label, args);
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
		}
		return list;
	}
}
