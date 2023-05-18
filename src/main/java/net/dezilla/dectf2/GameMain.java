package net.dezilla.dectf2;

import org.bukkit.plugin.java.JavaPlugin;

import net.dezilla.dectf2.listeners.EventListener;
import net.dezilla.dectf2.listeners.SpongeListener;
import net.dezilla.dectf2.util.GameConfig;

public class GameMain extends JavaPlugin{
	
	static GameMain instance;
	
	public static GameMain getInstance() {
		return instance;
	}
	@Override
	public void onLoad() {
		instance = this;
	}
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		if(GameConfig.launchSponge)
			getServer().getPluginManager().registerEvents(new SpongeListener(), this);
	}
}
