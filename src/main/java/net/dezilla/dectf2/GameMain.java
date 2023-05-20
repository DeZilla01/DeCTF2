package net.dezilla.dectf2;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.dezilla.dectf2.commands.SwitchCommand;
import net.dezilla.dectf2.commands.TestCommand;
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
		// Listeners
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		if(GameConfig.launchSponge)
			getServer().getPluginManager().registerEvents(new SpongeListener(), this);
		//Commands
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

			bukkitCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
			
			List<Command> commands = Arrays.asList(
					new TestCommand(),
					new SwitchCommand());
			commandMap.registerAll("dectf2", commands);
		} catch(Exception e) {
			e.printStackTrace();
		}
		//Initial Match
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
