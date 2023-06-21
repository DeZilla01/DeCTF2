package net.dezilla.dectf2.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.game.GameMapVote;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.gui.MapVoteGui;

public class VoteCommand extends Command {

	public VoteCommand() {
		super("vote");
		setUsage("/vote");
		setDescription("Vote for the next map.");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED+"You must be a player to use this command.");
			return false;
		}
		Player player = (Player) sender;
		GameMatch match = GameMatch.currentMatch;
		if(match == null) {
			sender.sendMessage(ChatColor.RED+"No game currently loaded. Try again shortly.");
			return false;
		}
		GameMapVote vote = match.getMapVote();
		if(vote == null) {
			sender.sendMessage(ChatColor.RED+"There is currently no voting happening.");
			return false;
		}
		new MapVoteGui(player, vote).display();
		return true;
	}

}
