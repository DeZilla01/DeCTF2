package net.dezilla.dectf2.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;

public class SwitchCommand extends Command {

	public SwitchCommand() {
		super("switch");
		setDescription("Switch your team or someone else's team.");
		setUsage("/switch [player]");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null || !match.isLoaded()) {
			sender.sendMessage(ChatColor.RED+"No game currently loaded. Try again shortly.");
			return false;
		}
		if(match.getTeams().length==0) {
			sender.sendMessage(ChatColor.RED+"No teams found.");
			return false;
		}
		boolean ignoreBalance = false;//This will become revelant when I add permission stuff
		
		Player target = null;
		if(args.length>0) {
			target = Bukkit.getPlayer(args[0]);
			if(target == null) {
				sender.sendMessage(ChatColor.RED+"Invalid player. Usage: "+getUsage());
				return false;
			}
		}
		if(target == null && !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED+"You cannot switch team as console. Usage: "+getUsage());
			return false;
		}
		if(target == null)
			target = (Player) sender;
		
		GameTeam currentTeam = match.getTeam(GamePlayer.get(target));
		if(!ignoreBalance && currentTeam!=null) {
			boolean lowest = true;
			for(GameTeam t : match.getTeams()) {
				if(t.size() < currentTeam.size()) {
					lowest = false;
					break;
				}
			}
			if(lowest) {
				sender.sendMessage(ChatColor.RED+"You cannot switch to uneven teams");
				return false;
			}
		}
		if(currentTeam==null || Arrays.asList(match.getTeams()).indexOf(currentTeam)+1>=match.getTeams().length) {
			match.addPlayerToTeam(GamePlayer.get(target), match.getTeams()[0]);
		} else {
			match.addPlayerToTeam(GamePlayer.get(target), match.getTeams()[Arrays.asList(match.getTeams()).indexOf(currentTeam)+1]);
		}
		currentTeam = match.getTeam(GamePlayer.get(target));
		match.respawnPlayer(GamePlayer.get(target));
		sender.sendMessage("Team switched to: "+currentTeam.getColoredTeamName());
		return true;
	}

}
