package net.dezilla.dectf2.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;

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
			p.setKit(GameMain.getInstance().kitMap().get(commandLabel.toLowerCase()));
		}
		sender.sendMessage("Kit Command");
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return execute(sender, label, args);
	}
}
