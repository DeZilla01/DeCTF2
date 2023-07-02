package net.dezilla.dectf2.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.ctf.CTFFlag;
import net.dezilla.dectf2.game.ctf.CTFFlag.FlagType;
import net.dezilla.dectf2.game.ctf.CTFGame;

public class FlagCommand extends Command {

	public FlagCommand() {
		super("flag");
		setDescription("Change certain parameters for a flag");
		setUsage("/team [Team Name/ID] [type/reset] <flag type>");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null || !match.isLoaded()) {
			sender.sendMessage(ChatColor.RED+"No game currently loaded. Try again shortly.");
			return false;
		}
		if(!(match.getGame() instanceof CTFGame)) {
			sender.sendMessage(ChatColor.RED+"This command can only be used during a CTF match.");
			return false;
		}
		CTFGame game = (CTFGame) match.getGame();
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED+"Missing arguments.");
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
		GameTeam team = null;
		try {
			team = match.getTeam(Integer.parseInt(args[0]));
		}catch(Exception e) {
			team = match.getTeam(args[0]);
		}
		if(team == null) {
			sender.sendMessage(ChatColor.RED+"Invalid team.");
			sender.sendMessage(ChatColor.RED+getUsage());
			return false;
		}
		CTFFlag flag = game.getFlag(team);
		if(args[1].equalsIgnoreCase("reset")) {
			flag.resetFlag();
			sender.sendMessage(team.getColoredTeamName()+ChatColor.RESET+" flag has been reset.");
			return true;
		}
		if(args[1].equalsIgnoreCase("type")) {
			if(args.length < 3) {
				sender.sendMessage(ChatColor.RED+"Missing arguments.");
				sender.sendMessage(ChatColor.RED+getUsage());
				return false;
			}
			if(!flag.isHome()) {
				sender.sendMessage(ChatColor.RED+"Flag must be home to change type.");
				return false;
			}
			try {
				FlagType type = FlagType.valueOf(args[2].toUpperCase());
				flag.setFlagType(type);
				sender.sendMessage(team.getColoredTeamName()+ChatColor.RESET+" flag type set to "+type.toString()+".");
				return true;
			}catch(IllegalArgumentException e) {
				sender.sendMessage(ChatColor.RED+"Invalid flag type.");
				sender.sendMessage(ChatColor.RED+getUsage());
				return false;
			}
		}
		
		sender.sendMessage(ChatColor.RED+getUsage());
		return false;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> list = new ArrayList<String>();
		GameMatch match = GameMatch.currentMatch;
		if(match==null)
			return list;
		if(args.length==1) {
			for(GameTeam team : match.getTeams()) {
				String s = ""+team.getId();
				if(s.toLowerCase().startsWith(args[0].toLowerCase()))
					list.add(s);
			}
		}
		if(args.length==2) {
			for(String s : Arrays.asList("reset", "type")) {
				if(s.toLowerCase().startsWith(args[1].toLowerCase()))
					list.add(s);
			}
		}
		if(args.length==3 && args[1].equalsIgnoreCase("type")) {
			for(FlagType f : FlagType.values()) {
				if(f.toString().toLowerCase().startsWith(args[2].toLowerCase()))
					list.add(f.toString().toLowerCase());
			}
		}
		return list;
	}

}
