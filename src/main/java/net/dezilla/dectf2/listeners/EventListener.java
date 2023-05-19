package net.dezilla.dectf2.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.dezilla.dectf2.game.GameMatch;

public class EventListener implements Listener{

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(GameMatch.currentMatch != null)
			event.getPlayer().teleport(GameMatch.currentMatch.getSpawn());
	}
}
