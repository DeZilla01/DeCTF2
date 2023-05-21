package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;

public abstract class BaseKit implements Listener{
	
	GamePlayer gameplayer;
	Player player;
	
	public BaseKit(GamePlayer player) {
		this.gameplayer = player;
		this.player = gameplayer.getPlayer();
		Bukkit.getServer().getPluginManager().registerEvents(this, GameMain.getInstance());
	}
	
	public void unregister() {
		HandlerList.unregisterAll(this);
	}
	
	public abstract void setInventory();

}
