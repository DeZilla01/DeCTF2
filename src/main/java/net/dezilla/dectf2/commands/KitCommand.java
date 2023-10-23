package net.dezilla.dectf2.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.gui.KitSelectionGui;
import net.dezilla.dectf2.kits.BaseKit;
import net.md_5.bungee.api.ChatColor;

public class KitCommand extends Command implements CommandExecutor{

	public KitCommand() {
		super("kit", "Change and select your kit.", "/kit [kit name] [variation]", Arrays.asList("kits", "class", "classes"));
		setPermission("dectf2.command.kit");
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
		GamePlayer p = GamePlayer.get((Player) sender);
		if(GameMain.getInstance().kitMap().containsKey(commandLabel.toLowerCase())) {
			if(args.length>0)
				p.setKit(GameMain.getInstance().kitMap().get(commandLabel.toLowerCase()), args[0]);
			else
				p.setKit(GameMain.getInstance().kitMap().get(commandLabel.toLowerCase()));
			p.getPlayer().sendMessage("You have selected "+p.getKit().getName()+" ("+p.getKit().getVariation()+")");
			return true;
		}else if(args.length>0 && GameMain.getInstance().kitMap().containsKey(args[0].toLowerCase())){
			if(args.length>1)
				p.setKit(GameMain.getInstance().kitMap().get(args[0].toLowerCase()), args[1]);
			else
				p.setKit(GameMain.getInstance().kitMap().get(args[0].toLowerCase()));
			p.getPlayer().sendMessage("You have selected "+p.getKit().getName()+" ("+p.getKit().getVariation()+")");
			return true;
		} else if(args.length>0){
			p.getPlayer().sendMessage("Invalid kit selection");
			return false;
		}
		new KitSelectionGui(p.getPlayer()).display();
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
