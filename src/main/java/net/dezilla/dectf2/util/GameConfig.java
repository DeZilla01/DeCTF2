package net.dezilla.dectf2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.game.ctf.CTFFlag.FlagType;

public class GameConfig {
	public static boolean mapManager = false;
	
	public static String mapFolder = "GAMEMAP";
	public static String gameFolderName = "game";
	public static boolean launchSponge = true;
	public static String defaultMap = "small4team.zip";
	public static int playersToStart = 1;
	public static String serverName = "mcctf.net";
	public static int mapVoteAmount = 5;
	public static double calloutNameRadius = 15.0;
	public static double calloutRadius = 45.0;
	public static boolean displayServerListMotd = true;
	public static boolean joinMessages = true;
	public static EntityType dummyType = EntityType.SKELETON;
	
	//kit stuff
	public static Material foodMaterial = Material.COOKED_BEEF;
	public static int regenDelay = 300;
	
	//ctf config
	public static FlagType flagType = FlagType.BANNER;
	public static float flagStealRadius = 1.5f;
	public static int stealDelay = 3;
	public static int flagReset = 16;
	public static boolean flagGlow = true;
	
	//zc config
	public static int captureBarSize = 45;
	
	public static void generateConfig() {
		//Check is the config file exists first, if not create one
		File folder = GameMain.getInstance().getDataFolder();
		if(!folder.exists() || !folder.isDirectory())
			folder.mkdir();
		File f  = new File(GameMain.getInstance().getDataFolder().getPath(), "config.yml");
		if(!f.exists()) {
			GameMain.getInstance().saveDefaultConfig();
			GameMain.getInstance().reloadConfig();
		}
	}
	
	public static void reloadConfig() {
		GameMain.getInstance().reloadConfig();
		loadConfig();
	}
	
	public static void loadConfig() {
		//Check is the config file exists first, if not create one
		generateConfig();
		
		FileConfiguration config = GameMain.getInstance().getConfig();
		//
		if(config.isBoolean("mapManager")) try{mapManager = config.getBoolean("mapManager");}catch(Exception e) {}
		//
		if(config.isString("mapFolder"))try{mapFolder = config.getString("mapFolder");}catch(Exception e) {}
		if(config.isString("gameFolderName"))try{gameFolderName = config.getString("gameFolderName");}catch(Exception e) {}
		if(config.isBoolean("launchSponge"))try{launchSponge = config.getBoolean("launchSponge");}catch(Exception e) {}
		if(config.isString("defaultMap"))try{defaultMap = config.getString("defaultMap");}catch(Exception e) {}
		if(config.isInt("playersToStart"))try{playersToStart = config.getInt("playersToStart");}catch(Exception e) {}
		if(config.isString("serverName"))try{serverName = config.getString("serverName");}catch(Exception e) {}
		if(config.isInt("mapVoteAmount"))try{mapVoteAmount = config.getInt("mapVoteAmount");}catch(Exception e) {}
		if(config.isDouble("calloutNameRadius"))try{calloutNameRadius = config.getDouble("calloutNameRadius");}catch(Exception e) {}
		if(config.isDouble("calloutRadius"))try{calloutRadius = config.getDouble("calloutRadius");}catch(Exception e) {}
		if(config.isBoolean("displayServerListMotd"))try{displayServerListMotd = config.getBoolean("displayServerListMotd");}catch(Exception e) {}
		if(config.isBoolean("joinMessages"))try{joinMessages = config.getBoolean("joinMessages");}catch(Exception e) {}
		if(config.isString("dummyType"))try{dummyType = EntityType.valueOf(config.getString("dummyType").toUpperCase());}catch(Exception e) {}
		//kit stuff
		if(config.isString("foodMaterial"))try{foodMaterial = Material.valueOf(config.getString("foodMaterial").toUpperCase());}catch(Exception e) {}
		if(config.isInt("regenDelay"))try{regenDelay = config.getInt("regenDelay");}catch(Exception e) {}
		//ctf config
		if(config.isString("flagType"))try{flagType = FlagType.valueOf(config.getString("flagType").toUpperCase());}catch(Exception e) {}
		if(config.isDouble("flagStealRadius"))try{flagStealRadius = (float) config.getDouble("flagStealRadius");}catch(Exception e) {}
		if(config.isInt("stealDelay"))try{stealDelay = config.getInt("stealDelay");}catch(Exception e) {}
		if(config.isInt("flagReset"))try{flagReset = config.getInt("flagReset");}catch(Exception e) {}
		if(config.isBoolean("flagGlow"))try{flagGlow = config.getBoolean("flagGlow");}catch(Exception e) {}
		//zc config
		if(config.isInt("captureBarSize"))try{captureBarSize = config.getInt("captureBarSize");}catch(Exception e) {}
		
	}
	
	public static void loadFile(String source, String target) throws IOException {
        try (InputStream in = GameMain.getInstance().getResource(source); OutputStream out = new FileOutputStream(target)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }
}
