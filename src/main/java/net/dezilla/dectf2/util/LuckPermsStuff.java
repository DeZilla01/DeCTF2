package net.dezilla.dectf2.util;

import org.bukkit.entity.Player;

import net.luckperms.api.LuckPerms;

public class LuckPermsStuff {
	
	public static LuckPerms api = null;
	
	public static String getPrefix(Player p) {
		return api.getUserManager().getUser(p.getName()).getCachedData().getMetaData().getPrefix();
	}

}
