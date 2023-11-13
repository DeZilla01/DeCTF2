package net.dezilla.dectf2.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.InvSave;
import net.md_5.bungee.api.ChatColor;

public class InvSaveCommand extends Command{
	public InvSaveCommand() {
		super("invsave");
		setPermission("dectf2.command.invsave");
		setDescription("Change the layout of a kit inventory");
		setUsage("/invsave [reset]");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED+"You must be a player to use this command.");
			return false;
		}	
		GamePlayer p = GamePlayer.get((Player) sender);
		if(args.length > 0 && args[0].equalsIgnoreCase("reset")) {
			if(p.resetInvSave()) {
				p.getPlayer().sendMessage("Inventory save has been removed for current kit.");
			} else {
				p.getPlayer().sendMessage("No inventory save to reset for this kit.");
			}
			return true;
		}
		InvSave save = new InvSave(p.getKit().getClass(), p.getKit().getVariation(), p.getPlayer().getInventory());
		p.addInvSave(save);
		p.getPlayer().sendMessage("Current inventory layout has been saved for this kit.");
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		if(args.length==1) {
			for(String s : Arrays.asList("reset")) {
				if(s.toLowerCase().startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		}
		return list;
	}
}
