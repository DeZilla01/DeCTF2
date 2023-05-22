package net.dezilla.dectf2.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTimer;

public class TimerCommand extends Command {

	public TimerCommand() {
		super("timer");
		setUsage("/timer <pause/unpause/seconds>");
		setDescription("Pause, unpause or change the time left for the timer");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null) {
			sender.sendMessage(ChatColor.RED+"No game currently loaded. Try again shortly.");
			return false;
		}
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
		GameTimer timer = match.getTimer();
		if(args[0].equalsIgnoreCase("pause")) {
			timer.pause();
			sender.sendMessage("Timer has been paused.");
			return true;
		} else if(args[0].equalsIgnoreCase("unpause")) {
			if(match.isWaitingForPlayers())
				match.setWaitingForPlayers(false);
			timer.unpause();
			sender.sendMessage("Timer has been unpaused.");
			return true;
		}
		try {
			int i = Integer.parseInt(args[0]);
			timer.setSeconds(i);
			sender.sendMessage("Timer has been set to "+timer.getTimeLeftDisplay());
			return true;
		} catch(Exception e) {}
		sender.sendMessage(ChatColor.RED+getUsage());
		return false;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		if(args.length==1) {
			for(String s : Arrays.asList("pause", "unpause")) {
				if(s.toLowerCase().startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		}
		return list;
	}

}
