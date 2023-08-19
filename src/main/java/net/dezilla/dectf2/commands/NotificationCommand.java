package net.dezilla.dectf2.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.GamePlayer.PlayerNotificationType;
import net.md_5.bungee.api.ChatColor;

public class NotificationCommand extends Command{

	public NotificationCommand() {
		super("notification");
		setUsage("/notification <Notification Type>");
		setAliases(Arrays.asList("notif"));
		setDescription("Change notification type for the player");
		setPermission("dectf2.command.notification");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to use this command");
			return false;
		}
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED+"Usage: "+getUsage());
			return false;
		}
		GamePlayer p = GamePlayer.get((Player) sender);
		PlayerNotificationType type = p.getNotificationType();
		try {
			type = PlayerNotificationType.valueOf(args[0].toUpperCase());
		} catch(Exception e) {
			sender.sendMessage("Invalid type. Usage: "+getUsage());
			return false;
		}
		p.setNotificationType(type);
		sender.sendMessage("Notification type changed to "+type.toString());
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		if(args.length==1) {
			for(PlayerNotificationType t : PlayerNotificationType.values()) {
				String s = ""+t.toString().toLowerCase();
				if(s.startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		}
		return list;
	}

}
