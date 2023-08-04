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
import net.dezilla.dectf2.commands.mapmanager.*;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.kits.ArcherKit;
import net.dezilla.dectf2.kits.BaseKit;
import net.dezilla.dectf2.kits.HeavyKit;
import net.dezilla.dectf2.kits.MedicKit;
import net.dezilla.dectf2.kits.SoldierKit;
import net.dezilla.dectf2.kits.TestyKit;
import net.dezilla.dectf2.listeners.CalloutListener;
import net.dezilla.dectf2.listeners.EventListener;
import net.dezilla.dectf2.listeners.GuiListener;
import net.dezilla.dectf2.listeners.MapManagerListener;
import net.dezilla.dectf2.listeners.SpongeListener;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.MapManagerWorld;

public class GameMain extends JavaPlugin{
	
	static GameMain instance;
	
	public static GameMain getInstance() {
		return instance;
	}
	
	static long serverTick = 0;
	
	public static long getServerTick() {
		return serverTick;
	}
	
	private List<Class<? extends BaseKit>> kits = new ArrayList<Class<? extends BaseKit>>();
	private Map<String, Class<? extends BaseKit>> kitMap = new HashMap<String, Class<? extends BaseKit>>();
	
	@Override
	public void onLoad() {
		instance = this;
		kits.add(HeavyKit.class);
		kits.add(TestyKit.class);
		kits.add(SoldierKit.class);
		kits.add(ArcherKit.class);
		kits.add(MedicKit.class);
		for(Class<? extends BaseKit> c : kits) {
			try {
				BaseKit k = c.getConstructor(new Class[] {GamePlayer.class}).newInstance(GamePlayer.get(null));
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
		//Sponge
		if(GameConfig.launchSponge)
			getServer().getPluginManager().registerEvents(new SpongeListener(), this);
		//Callouts
		getServer().getPluginManager().registerEvents(new CalloutListener(), this);
		//Map Manager
		if(GameConfig.mapManager) {
			getServer().getPluginManager().registerEvents(new MapManagerListener(), this);
			try {
				final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

				bukkitCommandMap.setAccessible(true);
				CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
				
				List<Command> commands = Arrays.asList(
						new LoadMapCommand(),
						new WorldCommand(),
						new UnloadCommand(),
						new ParseSignsCommand(),
						new SaveCommand(),
						new SignFindCommand(),
						new BrawlToDectfCommand(),
						new SetWorldSpawnCommand());
				commandMap.registerAll("dectf2", commands);
			} catch(Exception e) {
				e.printStackTrace();
			}
			GameTimer timer = new GameTimer(-1);
			timer.onSecond((t) -> {
				for(Player p : Bukkit.getOnlinePlayers()) {
					GamePlayer pl = GamePlayer.get(p);
					MapManagerWorld w = MapManagerWorld.get(p.getWorld());
					List<String> display = new ArrayList<String>();
					display.add("Map Manager Mode");
					display.add("Welcome to Map");
					display.add("Manager Mode.");
					display.add("To get started, ");
					display.add("use /loadmap");
					if(w != null) {
						display = w.getDisplay();
					}
					pl.updateScoreboardDisplay(display);
				}
			});
			return;
		}
		// Listeners
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		getServer().getPluginManager().registerEvents(new GuiListener(), this);
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
					new ChangeMapCommand(),
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
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, () -> serverTick++, 1, 1);
	}
	
	public Map<String, Class<? extends BaseKit>> kitMap(){
		return kitMap;
	}
}
