package net.dezilla.dectf2.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.dezilla.dectf2.game.GameMatch;

public class TestCommand extends Command{

	public TestCommand() {
		super("test");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(GameMatch.currentMatch != null) {
			sender.sendMessage("Name: "+GameMatch.currentMatch.getMapName());
			sender.sendMessage("Author: "+GameMatch.currentMatch.getMapAuthor());
		}
		return true;
	}
}
