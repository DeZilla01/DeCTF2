package net.dezilla.dectf2.commands;

import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMatch;

public class TestCommand extends Command{
	//This command is for testing shit during development.
	public TestCommand() {
		super("test");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		GameMatch match = GameMatch.currentMatch;
		Player p = (Player) sender;
		
		return true;
	}
}
