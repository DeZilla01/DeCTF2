package net.dezilla.dectf2.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import de.joshicodes.newlabyapi.LabyModProtocol;
import de.joshicodes.newlabyapi.api.LabyModAPI;
import de.joshicodes.newlabyapi.api.LabyModPlayer;
import net.dezilla.dectf2.game.GameMatch;

public class LabyUtil{
	
	public static boolean APIPluginInstalled() {
		return Bukkit.getPluginManager().isPluginEnabled("NewLabyAPI");
	}
	
	public static boolean playerUseLaby(Player player) {
		if(!APIPluginInstalled())
			return false;
		LabyModPlayer p = LabyModAPI.getLabyModPlayer(player);
		if(p != null)
			sendBanner(p);
		return p != null;
	}
	
	public static void updateLabyStatus(Player p, String gamemode) {
		JsonObject obj = new JsonObject();
		obj.addProperty("show_gamemode", true);
		obj.addProperty("gamemode_name", gamemode);
		
		LabyModProtocol.sendLabyModMessage(p, "server_gamemode", obj);
		
	    JsonObject object = new JsonObject();
	    object.addProperty( "hasGame", true );

	    if (GameMatch.currentMatch != null) {
	    	GameMatch m = GameMatch.currentMatch;
	        object.addProperty( "game_mode", gamemode);
	        object.addProperty( "game_startTime", 0 ); // Set to 0 for countdown
	        object.addProperty( "game_endTime", m.getTimer().getSeconds() ); // // Set to 0 for timer
	    }

	    // Send to user
	    LabyModProtocol.sendLabyModMessage(p, "discord_rpc", object );
	}
	
	public static void sendBanner(LabyModPlayer p) {
		p.sendServerBanner("https://dezilla.net/mcctf_banner.png");
	}
}
