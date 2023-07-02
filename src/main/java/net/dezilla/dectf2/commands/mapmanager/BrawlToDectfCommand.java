package net.dezilla.dectf2.commands.mapmanager;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class BrawlToDectfCommand extends Command {
	private static final String RE_SIGN = Pattern.quote("{{") + "(.*)" + Pattern.quote("}}");
    private static final String RE_VAL_SIGN = Pattern.quote("{{") + "(.*)=(.*)" + Pattern.quote("}}");
    private static final String RE_TEAM_SIGN = Pattern.quote("{{") + "(.*) (.*)" + Pattern.quote("}}");

    public BrawlToDectfCommand() {
        super("brawltodectf");
        setUsage("/brawltodectf");
    }

    @Override
    public boolean execute(final CommandSender sender, String alias, final String[] args) {
    	if(!(sender instanceof Player)) {
    		sender.sendMessage(ChatColor.RED+"You must be a player to use this command.");
    		return false;
    	}
    	Player p = (Player) sender;
        sender.sendMessage("Converting McCTF configs to DeCTF2");
    	for(Chunk c : p.getWorld().getLoadedChunks()) {
    		for(BlockState s : c.getTileEntities()) {
    			if(s instanceof Sign) {
    				Sign sign = (Sign) s;
    				for(Side side : Side.values()) {
    					int lineIndex = 0;
    					for(String line : sign.getSide(side).getLines()) {
    						if(line.matches(RE_VAL_SIGN)) {
    							String key = line.replaceAll(RE_VAL_SIGN, "$1").toLowerCase();
    							String value = line.replaceAll(RE_VAL_SIGN, "$2").toLowerCase();
    							if(key.equals("caps"))
    								key = "score";
    							if(key.equals("time")) {
    								try {
    									value = ""+(Integer.parseInt(value)*60);
    								}catch(Exception e) {}
    							}
    							sign.getSide(side).setLine(lineIndex, "{"+key+"="+value+"}");
    							sign.update();
    						}
    						else if(line.matches(RE_TEAM_SIGN)) {
    							try {
    								String key = line.replaceAll(RE_TEAM_SIGN, "$1").toLowerCase();
        							int value = Integer.parseInt(line.replaceAll(RE_TEAM_SIGN, "$2"));
        							sign.getSide(side).setLine(lineIndex, "{"+key+"="+(value-1)+"}");
        							sign.update();
    							}catch(Exception e) {}
    						} else if(line.matches(RE_SIGN)) {
    							String key = line.replaceAll(RE_SIGN, "$1").toLowerCase();
    							if(key.equals("spawn_box"))
    								key = "spawn";
    							sign.getSide(side).setLine(lineIndex, "{"+key+"}");
    							sign.update();
    						}
    						lineIndex++;
    					}
    				}
    			}
    		}
    	}
    	sender.sendMessage("Done");
        return true;
    }

}