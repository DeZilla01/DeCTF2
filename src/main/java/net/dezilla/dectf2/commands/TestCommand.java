package net.dezilla.dectf2.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.LabyUtil;

public class TestCommand extends Command{
	//This command is for testing shit during development.
	public TestCommand() {
		super("test");
		setPermission("dectf2.command.test");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		sender.sendMessage("don't fuck with my test command >=(");
		Player p = (Player) sender;
		GamePlayer gp = GamePlayer.get(p);
		p.sendMessage(gp.getVersionProtocol()+" "+gp.isUsingLabyMod());
		p.sendMessage(""+LabyUtil.playerUseLaby(p));
		
		return true;
	}
}
