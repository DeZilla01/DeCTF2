package net.dezilla.dectf2.util;

import org.bukkit.Material;

import net.dezilla.dectf2.game.ctf.CTFFlag.FlagType;

public class GameConfig {
	public static String mapFolder = "GAMEMAP";
	public static String gameFolderName = "game";
	public static boolean launchSponge = true;
	public static String defaultMap = null;
	public static int playersToStart = 2;
	public static String serverName = "dezilla.net";
	
	//kit stuff
	public static Material foodMaterial = Material.COOKED_BEEF;
	
	//ctf config
	public static FlagType flagType = FlagType.BANNER;
	public static float flagStealRadius = 1.5f;
	public static int stealDelay = 3;
	public static int flagReset = 16;
	
}
