package net.dezilla.dectf2;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.dezilla.dectf2.commands.*;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.kits.BaseKit;
import net.dezilla.dectf2.kits.HeavyKit;
import net.dezilla.dectf2.kits.TestyKit;
import net.dezilla.dectf2.listeners.EventListener;
import net.dezilla.dectf2.listeners.GuiListener;
import net.dezilla.dectf2.listeners.SpongeListener;
import net.dezilla.dectf2.util.GameConfig;

public class GameMain extends JavaPlugin{
	
	static GameMain instance;
	
	public static GameMain getInstance() {
		return instance;
	}
	
	private List<Class<? extends BaseKit>> kits = new ArrayList<Class<? extends BaseKit>>();
	private Map<String, Class<? extends BaseKit>> kitMap = new HashMap<String, Class<? extends BaseKit>>();
	
	@Override
	public void onLoad() {
		instance = this;
		kits.add(HeavyKit.class);
		kits.add(TestyKit.class);
		for(Class<? extends BaseKit> c : kits) {
			try {
				BaseKit k = c.getConstructor(new Class[] {GamePlayer.class, int.class}).newInstance(null, 0);
				kitMap.put(k.getName().toLowerCase(), c);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onEnable() {
		// Listeners
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		getServer().getPluginManager().registerEvents(new GuiListener(), this);
		if(GameConfig.launchSponge)
			getServer().getPluginManager().registerEvents(new SpongeListener(), this);
		//Commands
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

			bukkitCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
			
			KitCommand kitCommand = new KitCommand();
			List<String> kitNames = new ArrayList<String>();
			for(Entry<String, Class<? extends BaseKit>> e : kitMap.entrySet()) {
				kitNames.add(e.getKey());
			}
			kitCommand.addAliases(kitNames);
			
			List<Command> commands = Arrays.asList(
					new TestCommand(),
					new SwitchCommand(),
					new TimerCommand(),
					new TeamCommand(),
					new FlagCommand(),
					new VoteCommand(),
					kitCommand);
			commandMap.registerAll("dectf2", commands);
		} catch(Exception e) {
			e.printStackTrace();
		}
		//Initial Match
		try {
			GameMatch m = new GameMatch(null);
			m.Load((world) -> {
				for(Player p : Bukkit.getOnlinePlayers())
					m.addPlayer(GamePlayer.get(p));
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, Class<? extends BaseKit>> kitMap(){
		return kitMap;
	}
}
