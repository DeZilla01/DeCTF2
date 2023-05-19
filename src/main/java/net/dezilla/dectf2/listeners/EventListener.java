package net.dezilla.dectf2.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.dezilla.dectf2.Util;

public class EventListener implements Listener{

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(Util.currentMatch != null)
			event.getPlayer().teleport(Util.currentMatch.getSpawn());
	}
}
