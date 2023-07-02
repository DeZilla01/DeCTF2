package net.dezilla.dectf2.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

//This class is loaded when using Map Manager mode
public class MapManagerListener implements Listener{
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		p.setGameMode(GameMode.CREATIVE);
		p.sendMessage("Welcome to Map Manager mode. You can revert back to normal game mode by disabling 'mapManager' in the config file.");
		p.sendMessage("Don't forget to save any changes you make.");
	}

}
