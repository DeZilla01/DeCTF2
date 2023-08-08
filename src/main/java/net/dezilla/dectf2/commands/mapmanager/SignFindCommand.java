package net.dezilla.dectf2.commands.mapmanager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SignFindCommand extends Command {

    public SignFindCommand() {
        super("signfind");
        setUsage("/signfind <query>");
    }

    @Override
    public boolean execute(final CommandSender sender, String alias, final String[] args) {
    	if(!(sender instanceof Player)) {
    		sender.sendMessage(ChatColor.RED+"You must be a player to use this command.");
    		return false;
    	}
    	Player p = (Player) sender;
        sender.sendMessage("Searching for signs...");
        sender.sendMessage("Tip: You can click on text to teleport to the sign.");
        boolean isQuery = false;
    	String query = "";
    	if(args.length>0) {
    		for(String s : args) {
    			query+=s+" ";
    		}
    		query = query.substring(0, query.length() - 1);
    		isQuery = true;
    	}
    	List<Sign> signs = new ArrayList<Sign>();
    	for(Chunk c : p.getWorld().getLoadedChunks()) {
    		for(BlockState s : c.getTileEntities()) {
    			if(s instanceof Sign) {
    				Sign sign = (Sign) s;
    				if(isQuery) {
    					String text = "";
    					for(String line : sign.getSide(Side.FRONT).getLines()) {
    						text+=line;
    					}
    					for(String line : sign.getSide(Side.BACK).getLines()) {
    						text+=line;
    					}
    					if(text.contains(query))
    						signs.add(sign);
    				} else {
    					signs.add(sign);
    				}
    			}
    		}
    	}
    	for (Sign s : signs) {
    		String text = "";
			for(String line : s.getSide(Side.FRONT).getLines()) {
				text+=line;
			}
			for(String line : s.getSide(Side.BACK).getLines()) {
				text+=line;
			}
            TextComponent msg = new TextComponent(text);
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + p.getName() + " " + s.getX() + " " + s.getY() + " " + s.getZ()));
            p.spigot().sendMessage(msg);
        }
        return true;
    }

}