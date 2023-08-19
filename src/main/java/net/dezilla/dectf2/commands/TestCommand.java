package net.dezilla.dectf2.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class TestCommand extends Command{
	//This command is for testing shit during development.
	public TestCommand() {
		super("test");
		setPermission("dectf2.command.test");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		sender.sendMessage("don't fuck with my test command >=(");
		String a = "";
		for(int i = 0; i <= 255; i++) {
			a += ChatColor.of(new java.awt.Color(i, 0, 0))+"@";
		}
		sender.sendMessage(a);
		
		return true;
	}
}
