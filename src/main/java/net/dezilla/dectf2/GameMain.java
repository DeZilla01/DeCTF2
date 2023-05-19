package net.dezilla.dectf2;

import java.io.FileNotFoundException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.dezilla.dectf2.game.GameMatch;
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
		try {
			GameMatch m = new GameMatch(null);
			m.Load((world) -> {
				for(Player p : Bukkit.getOnlinePlayers())
					p.teleport(m.getSpawn());
			});
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
