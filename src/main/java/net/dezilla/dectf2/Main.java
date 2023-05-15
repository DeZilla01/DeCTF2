package net.dezilla.dectf2;

import org.bukkit.plugin.java.JavaPlugin;

import net.dezilla.dectf2.listeners.EventListener;

public class Main extends JavaPlugin{
	
	static Main instance;
	
	public static Main getInstance() {
		return instance;
	}
	@Override
	public void onLoad() {
		instance = this;
	}
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new EventListener(), this);
	}
}
