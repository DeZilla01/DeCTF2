package net.dezilla.dectf2.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.GamePlayer.PlayerChatType;

public class PlayerChatCommand extends Command{

	public PlayerChatCommand() {
		super("all", "Change talking channel.", "/<all, team> [message]", Arrays.asList("team", "a", "t"));
		setPermission("dectf2.command.chat");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to use this command.");
			return false;
		}
		GamePlayer p = GamePlayer.get((Player) sender);
		if(args.length == 0 && commandLabel.toLowerCase().startsWith("a")) {
			p.setChatType(PlayerChatType.GLOBAL);
			sender.sendMessage("Talking channel changed to GLOBAL");
			return true;
		}
		if(args.length == 0 && commandLabel.toLowerCase().startsWith("t")) {
			p.setChatType(PlayerChatType.TEAM);
			sender.sendMessage("Talking channel changed to TEAM");
			return true;
		}
		PlayerChatType pType = p.getChatType();
		PlayerChatType type = PlayerChatType.GLOBAL;
		if(commandLabel.toLowerCase().startsWith("t"))
			type = PlayerChatType.TEAM;
		String msg = "";
		for(String s : args)
			msg+=s+" ";
		p.setChatType(type);
		p.getPlayer().chat(msg);
		p.setChatType(pType);
		return true;
	}

}
