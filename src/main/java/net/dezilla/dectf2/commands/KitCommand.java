package net.dezilla.dectf2.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTimer;

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
		sender.sendMessage("Kit Command");
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return execute(sender, label, args);
	}
}
