package net.dezilla.dectf2.util;

import net.dezilla.dectf2.game.ctf.CTFFlag.FlagType;

public class GameConfig {
	public static String mapFolder = "GAMEMAP";
	public static String gameFolderName = "game";
	public static boolean launchSponge = true;
	public static String defaultMap = null;
	public static int playersToStart = 2;
	
	//ctf config
	public static FlagType flagType = FlagType.BANNER;
	public static float flagStealRadius = 1.5f;
}
