package net.dezilla.dectf2.util;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

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
	public static boolean joinMessages = false;
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
}
