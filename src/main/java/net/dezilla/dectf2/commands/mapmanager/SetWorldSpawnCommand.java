package net.dezilla.dectf2.commands.mapmanager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWorldSpawnCommand extends Command {
    public SetWorldSpawnCommand() {
        super("setworldspawn");
        setUsage("/setworldspawn");
    }

    @Override
    public boolean execute(final CommandSender sender, String alias, final String[] args) {
    	if(!(sender instanceof Player)) {
    		sender.sendMessage(ChatColor.RED+"You must be a player to use this command.");
    		return false;
    	}
    	Player p = (Player) sender;
        p.getWorld().setSpawnLocation(p.getLocation());
        p.sendMessage("Spawn set");
        return true;
    }

}