package net.dezilla.dectf2.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.viaversion.viaversion.api.Via;

public class ViaUtil {
	
	public static boolean ViaInstalled() {
		return Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
	}
	
	public static int getMCVersion(Player player) {
		return Via.getAPI().getPlayerVersion(player.getUniqueId());
	}
	
	public static String getDisplayVersion(int protocolVersion) {
		if(protocolVersion < 0)
			return "Server Version";
		switch(protocolVersion) {
			case 0: return "13w41";
			case 1: return "13w42";
			case 2: return "13w43";
			case 3: return "1.7/1.7.1";
			case 4: return "1.7.2/5";
			case 5: return "1.7.6/10";
			case 47: return "1.8.X";
			case 107: return "1.9";
			case 108: return "1.9.1";
			case 109: return "1.9.2";
			case 110: return "1.9.3/4";
			case 210: return "1.10.X";
			case 315: return "1.11";
			case 316: return "1.11.X";
			case 335: return "1.12";
			case 338: return "1.12.1";
			case 340: return "1.12.2";
			case 393: return "1.13";
			case 401: return "1.13.1";
			case 404: return "1.13.2";
			case 477: return "1.14";
			case 480: return "1.14.1";
			case 485: return "1.14.2";
			case 490: return "1.14.3";
			case 498: return "1.14.4";
			case 573: return "1.15";
			case 575: return "1.15.1";
			case 578: return "1.15.2";
			case 735: return "1.16";
			case 736: return "1.16.1";
			case 751: return "1.16.2";
			case 753: return "1.16.3";
			case 754: return "1.16.4/5";
			case 755: return "1.17";
			case 756: return "1.17.1";
			case 757: return "1.18/1.18.1";
			case 758: return "1.18.2";
			case 759: return "1.19";
			case 760: return "1.19.1/2";
			case 761: return "1.19.3";
			case 762: return "1.19.4";
			case 763: return "1.20/1.20.1";
			case 764: return "1.20.2";
			case 765: return "1.20.3/4";
			default: return "Unknown "+protocolVersion;
		}
	}

}
