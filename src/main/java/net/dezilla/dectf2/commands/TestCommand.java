package net.dezilla.dectf2.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TestCommand extends Command{
	//This command is for testing shit during development.
	public TestCommand() {
		super("test");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		sender.sendMessage("don't fuck with my test command >=(");
		
		return true;
	}
}
