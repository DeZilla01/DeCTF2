package net.dezilla.dectf2.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.Util;
import net.md_5.bungee.api.ChatColor;

public class MapManagerWorld {
	private static List<MapManagerWorld> WORLDS = new ArrayList<MapManagerWorld>();
	
	public static MapManagerWorld get(World world) {
		for(MapManagerWorld w : WORLDS) {
			if(w.getWorld() != null && w.getWorld().equals(world))
				return w;
		}
		return null;
	}
	
	World world;
	boolean loaded = false;
	File sourceZip;
	
	public MapManagerWorld() {
		WORLDS.add(this);
	}
	
	public void load(File sourceZip, WorldRunnable onLoaded) {
		this.sourceZip = sourceZip;
		Bukkit.getScheduler().runTaskAsynchronously(GameMain.getInstance(), () -> {
			//Create folder for the game
			File worldFolder = Util.CreateFolder(sourceZip.getName().replace(".zip", ""));
			//unzip the world
			try {
				//UnzipUtility.unzip(sourceZip.getPath(), worldFolder.getPath());
				ZipUtility.unzipMap(sourceZip, worldFolder);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {
				WorldCreator wc = new WorldCreator(worldFolder.getPath());
				world = Bukkit.getServer().createWorld(wc);
				world.setAutoSave(false);
				world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
				world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
				world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
				world.setGameRule(GameRule.KEEP_INVENTORY, true);
				world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
				world.setGameRule(GameRule.DO_FIRE_TICK, false);
				world.setGameRule(GameRule.MOB_GRIEFING, false);
				world.setGameRule(GameRule.NATURAL_REGENERATION, false);
				world.setGameRule(GameRule.DO_VINES_SPREAD, false);
				world.setGameRule(GameRule.DISABLE_RAIDS, true);
				world.setGameRule(GameRule.DO_MOB_LOOT, false);
				world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
				world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
				world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
				parseSigns();
				loaded = true;
				onLoaded.run(world);
			});
		});
	}
	
	public void unload(boolean save) {
		if(!loaded)
			return;
		for(Player p : world.getPlayers()) {
			p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
		}
		File folder = world.getWorldFolder();
		Bukkit.unloadWorld(world, save);
		loaded = false;
		WORLDS.remove(this);
		if(save) {
			try {
				//UnzipUtility.zip(folder.list(), sourceZip.getPath());
				ZipUtility.zip(folder, sourceZip);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Util.deleteFolder(folder);
	}
	
	private boolean spawn = false;
	private Location spawnLocation = null;
	private int teamAmount = 2;
	private String name = null;
	private String author = null;
	private int time = 1200;
	private int score = -1;
	private String mode = "tdm";
	private boolean[] teamSpawn = new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
	private boolean[] teamFlag = new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
	
	public void parseSigns() {
		spawn = false;
		spawnLocation = null;
		teamAmount = 2;
		name = null;
		author = null;
		time = 1200;
		score = -1;
		mode = "tdm";
		teamSpawn = new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		teamFlag = new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		int spawnx = world.getSpawnLocation().getChunk().getX();
		int spawnz = world.getSpawnLocation().getChunk().getZ();
		String pattern1 = Pattern.quote("{") + "(.*)" + Pattern.quote("}");
		String pattern2 = Pattern.quote("{") + "(.*)=(.*)" + Pattern.quote("}");
		
		for(int x = spawnx-16; x <= spawnx+16; x++) {
			for(int z = spawnz-16; z <= spawnz+16; z++) {
				if(!world.isChunkGenerated(x, z))
					continue;
				Chunk c = world.getChunkAt(x, z);
				for(BlockState s : c.getTileEntities()) {
					if(!(s instanceof Sign))
						continue;
					Sign sign = (Sign) s;
					List<String> lines = new ArrayList<String>();
					lines.addAll(Arrays.asList(sign.getSide(Side.FRONT).getLines()));
					lines.addAll(Arrays.asList(sign.getSide(Side.BACK).getLines()));
					for(String i : lines) {
						if(i.matches(pattern2)) {
							String key = i.replaceAll(pattern2, "$1");
							String value = i.replaceAll(pattern2, "$2");
							if(key.equalsIgnoreCase("teams")) {
								try {
									teamAmount = Integer.parseInt(value);
									if(teamAmount<1)
										teamAmount=1;
									else if(teamAmount>16)
										teamAmount=16;
								} catch(Exception e) {}
								continue;
							}
							if(key.equalsIgnoreCase("time")) {
								try {
									time = Integer.parseInt(value);
								} catch(Exception e) {}
								continue;
							}
							if(key.equalsIgnoreCase("score")) {
								try {
									score = Integer.parseInt(value);
								} catch(Exception e) {}
								continue;
							}
							if(key.equalsIgnoreCase("mode")) {
								mode = value;
								continue;
							}
							if(key.equalsIgnoreCase("spawn")) {
								try {
									int v = Integer.parseInt(value);
									teamSpawn[v] = true;
								} catch(Exception e) {}
								continue;
							}
							if(key.equalsIgnoreCase("flag")) {
								try {
									int v = Integer.parseInt(value);
									teamFlag[v] = true;
								} catch(Exception e) {}
								continue;
							}
						}
						else if(i.matches(pattern1)) {
							String key = i.replaceAll(pattern1, "$1");
							String value = Util.grabConfigText(sign);
							if(key.equalsIgnoreCase("spawn")) {
								spawn = true;
								spawnLocation = sign.getLocation().add(.5,0,.5);
								continue;
							}
							if(key.equalsIgnoreCase("name")) {
								name = value;
								continue;
							}
							if(key.equalsIgnoreCase("author")) {
								author = value;
								continue;
							}
						}
					}
				}
			}
		}
		if(score == -1) {
			switch(mode) {
				case "ctf":
					score = 3;break;
				default: 
					score = 0;break;
			}
		}
	}
	
	public List<String> getDisplay(){
		List<String> display = new ArrayList<String>();
		display.add(world.getName());
		if(name == null)
			display.add(ChatColor.RED+"Name not set");
		else
			display.add(ChatColor.GOLD+name);
		if(author==null)
			display.add(ChatColor.RED+"Author not set");
		else
			display.add("by "+ChatColor.GOLD+author);
		switch(mode.toLowerCase()) {
			case "tdm":
				display.add("Mode: "+ChatColor.GOLD+"Team Deathmatch");break;
			case "zc":
				display.add("Mode: "+ChatColor.GOLD+"Zone Control");break;
			case "ctf":
				display.add("Mode: "+ChatColor.GOLD+"Capture the Flag");break;
			case "dl":
				display.add("Mode: "+ChatColor.GOLD+"Delivery");break;
			case "arena":
				display.add("Mode: "+ChatColor.GOLD+"Arena");break;
			case "pl":
				display.add("Mode: "+ChatColor.GOLD+"Payload");break;
			default:
				display.add("Mode: "+ChatColor.RED+mode+" (unknown mode)");
				display.add("If mode does not exist, ");
				display.add("will default to tdm.");
				break;
		}
		if(spawn)
			display.add(ChatColor.GOLD+"Spawn is set");
		else
			display.add(ChatColor.RED+"Spawn missing");
		display.add("Score: "+ChatColor.GOLD+score);
		display.add("Time: "+ChatColor.GOLD+time);
		for(int i = 0; i < teamAmount; i++) {
			GameColor c = GameColor.defaultColorOrder()[i];
			display.add(c.getName()+" spawn: "+(teamSpawn[i] ? ChatColor.GOLD+"set" : ChatColor.RED+"missing"));
		}
		if(mode.equalsIgnoreCase("ctf")) {
			for(int i = 0; i < teamAmount; i++) {
				GameColor c = GameColor.defaultColorOrder()[i];
				display.add(c.getName()+" flag: "+(teamFlag[i] ? ChatColor.GOLD+"set" : ChatColor.RED+"missing"));
			}
		}
		return display;
	}
	
	public World getWorld() {
		return world;
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public Location getSpawnLocation() {
		return spawnLocation;
	}

}
