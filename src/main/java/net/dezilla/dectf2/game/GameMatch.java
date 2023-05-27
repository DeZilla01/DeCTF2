package net.dezilla.dectf2.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.util.WorldRunnable;
import net.dezilla.dectf2.game.ctf.CTFGame;
import net.dezilla.dectf2.util.GameColor;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.UnzipUtility;

public class GameMatch {
	private static int GAMEID = 0;
	public static GameMatch currentMatch = null;
	public static GameMatch previousMatch = null;
	public static GameMatch nextMatch;
	public static List<GamePlayer> waitingForNextMatch = new ArrayList<GamePlayer>();
	
	
	private int gameId = GAMEID++;
	private GameBase game = null;
	private File sourceZip = null;
	private File gameFolder = null;
	private boolean gameLoaded = false;
	private World world = null;
	private Location spawn = null;
	private Map<String, Location> signConfig = new HashMap<String, Location>();
	private String name = "";
	private String author = "";
	private String mode = "tdm";
	private int teamAmount = 2;
	private int timeLimit = 1200; //20 mins default
	private List<GameTeam> teams = new ArrayList<GameTeam>();
	private GameState state = GameState.PREGAME;
	private GameTimer timer = new GameTimer(30);
	private boolean waitingForPlayers = true;
	private int scoreToWin = 3;
	private Map<Integer, Location> teamSpawns = new HashMap<Integer, Location>();//this var is only used between sign parse and team creations, use GameTeam#getSpawn()
	
	public GameMatch(String levelName) throws FileNotFoundException {
		//Set chosen level
		if(levelName != null)
			sourceZip = new File(Util.getGameMapFolder().getPath()+levelName);
		//Set to default level if not valid or null
		if(sourceZip == null || !sourceZip.exists()) {
			if(GameConfig.defaultMap!=null)
				sourceZip = new File(Util.getGameMapFolder().getPath()+GameConfig.defaultMap);
		}
		//Incase default level is not configured or not valid, choose a random map
		if(sourceZip == null || !sourceZip.exists()) {
			File[] files = Util.getWorldList();
			if(files.length!=0)
				sourceZip = files[(int) (Math.random()*(files.length-1))];
		}
		//At this point, if no valid map is set, big rip
		if(sourceZip == null || !sourceZip.exists()) {
			throw new FileNotFoundException("No map found in configured folder. Make sure map zips are available.");
		}
		
		name = sourceZip.getName();
	}
	
	public void Load(WorldRunnable onLoad) {
		Bukkit.getScheduler().runTaskAsynchronously(GameMain.getInstance(), () -> {
			//Create folder for the game
			gameFolder = Util.CreateMatchFolder(gameId);
			//unzip the world
			try {
				UnzipUtility.unzip(sourceZip.getPath(), gameFolder.getPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {
				WorldCreator wc = new WorldCreator(gameFolder.getPath());
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
				spawn = world.getSpawnLocation();
				parseSigns();
				createTeams();
				if(mode.equalsIgnoreCase("ctf"))
					game = new CTFGame(this);
				else if(mode.equalsIgnoreCase("zones"))
					{}//TODO
				else if(mode.equalsIgnoreCase("tdm"))
					{}//TODO
				timer.onSecond((timer) -> {
					if(state == GameState.PREGAME && waitingForPlayers) {
						if(Bukkit.getOnlinePlayers().size() >= GameConfig.playersToStart) {
							waitingForPlayers = false;
							timer.unpause();
						}
					}
					
					for(Player p : Bukkit.getOnlinePlayers())
						GamePlayer.get(p).updateScoreboardDisplay();
				});
				timer.onEnd((timer) -> {
					state = GameState.INGAME;
					timer.setSeconds(timeLimit);
					game.gameStart();
					timer.unpause();
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.getGameMode() == GameMode.SURVIVAL) 
							respawnPlayer(GamePlayer.get(p));
					}
					timer.onEnd((postTimer) -> {
						state = GameState.POSTGAME;
						timer.setSeconds(20);
						game.unregister();
						postTimer.unpause();
						for(Player p : Bukkit.getOnlinePlayers()) {
							if(p.getGameMode() == GameMode.SURVIVAL) 
								respawnPlayer(GamePlayer.get(p));
						}
						postTimer.onEnd((endTimer) -> {
							unload();
							GameMatch.currentMatch = null;
							try {
								GameMatch m = new GameMatch(null);
								m.Load((world) -> {
									for(Player p : Bukkit.getOnlinePlayers())
										m.addPlayer(GamePlayer.get(p));
								});
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						});
					});
				});
				gameLoaded = true;
				currentMatch = this;
				onLoad.run(world);
			});
		});
	}
	
	public void unload() {
		timer.unregister();
		gameLoaded = false;
		game.unregister();
		for(Player p : world.getPlayers()) 
			p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
		Bukkit.getServer().unloadWorld(world, false);
	}
	
	//When joining the server or switching match
	public void addPlayer(GamePlayer player) {
		boolean addedToPreviousTeam = false;
		if(previousMatch != null && previousMatch.getTeam(player) != null) {
			addedToPreviousTeam = addPlayerToTeam(player, previousMatch.getTeam(player).getId());
		} 
		if(!addedToPreviousTeam)
			addPlayerToRandomTeam(player);
		GameTeam team = getTeam(player);
		if(state == GameState.INGAME) {
			player.getPlayer().teleport(team.getSpawn());
		} else {
			player.getPlayer().teleport(spawn);
		}
	}
	
	public void respawnPlayer(GamePlayer player) {
		player.getPlayer().setGameMode(GameMode.SURVIVAL);
		player.getPlayer().setHealth(20.0);
		GameTeam team = getTeam(player);
		if(state == GameState.INGAME && team != null) {
			player.getPlayer().teleport(team.getSpawn());
		} else {
			player.getPlayer().teleport(spawn);
		}
	}
	
	private void parseSigns() {
		int spawnx = world.getSpawnLocation().getChunk().getX();
		int spawnz = world.getSpawnLocation().getChunk().getZ();
		List<Block> toRemove = new ArrayList<Block>();
		for(int x = spawnx-16 ; x <= spawnx+16 ; x++) {
			for(int z = spawnz-16 ; z <= spawnz+16 ; z++) {
				if(!world.isChunkGenerated(x, z))
					continue;
				Chunk c = world.getChunkAt(x, z, false);
				for(BlockState s : c.getTileEntities()) {
					if(!(s instanceof Sign))
						continue;
					BlockFace face = BlockFace.NORTH;
					if(s.getBlockData() instanceof Rotatable) {
						Rotatable r = (Rotatable) s.getBlockData();
						face = r.getRotation();
					}
					Sign sign = (Sign) s;
					boolean removeSign = false;
					Location loc = sign.getLocation().add(0.5, 0, 0.5);
					loc.setYaw(Util.getYaw(face.getDirection()));
					for(String i : sign.getLines()) {
						if(i.startsWith("[") && i.endsWith("]")) {
							removeSign = true;
							String line = i.replace("[", "");
							line = line.replace("]", "");
							signConfig.put(line, loc);
							//spawn
							if(line.equalsIgnoreCase("spawn")) {
								spawn = loc;
							//name
							} else if(line.equalsIgnoreCase("name")) {
								String n = "";
								for(String ii : sign.getLines()) {
									if(ii.equalsIgnoreCase("[name]"))
										continue;
									n+=ii;
								}
								name = n;
							//author
							} else if(line.equalsIgnoreCase("author")) {
								String n = "";
								for(String ii : sign.getLines()) {
									if(ii.equalsIgnoreCase("[author]"))
										continue;
									n+=ii;
								}
								author = n;
							}
							if(line.contains("=")) {
								String[] a = line.split("=");
								if(a[0].equalsIgnoreCase("spawn")) {
									try {
										int team = Integer.parseInt(a[1]);
										teamSpawns.put(team, loc);
									}catch(Exception e) {}
								} else if(a[0].equalsIgnoreCase("time")) {
									try {
										timeLimit = Integer.parseInt(a[1]);
									}catch(Exception e) {}
								} else if(a[0].equalsIgnoreCase("score")) {
									try {
										scoreToWin = Integer.parseInt(a[1]);
									}catch(Exception e) {}
								} else if(a[0].equalsIgnoreCase("mode")) {
									mode = a[1];
								} else if(a[0].equalsIgnoreCase("teams")) {
									try {
										teamAmount = Integer.parseInt(a[1]);
										if(teamAmount>16)
											teamAmount = 16;
										else if(teamAmount<1)
											teamAmount = 1;
									} catch(Exception e){}
								}
							}
						}
					}
					if(removeSign)
						toRemove.add(s.getBlock());
				}
			}
		}
		for(Block b : toRemove)
			b.setType(Material.AIR);
	}
	private void createTeams() {
		for(int i = 0; i<teamAmount;i++) {
			GameColor c = GameColor.defaultColorOrder()[i];
			Location l = spawn.clone();
			boolean teamSpawnSet = false;
			if(teamSpawns.containsKey(i)) {
				l = teamSpawns.get(i);
				teamSpawnSet = true;
			}
			GameTeam t = new GameTeam(i, c, l);
			if(!teamSpawnSet)
				System.out.println("Missing configuration for spawn "+t.getTeamName());
			teams.add(t);
		}
	}
	
	public void addPlayerToRandomTeam(GamePlayer player) {
		if(teamAmount==0 || !gameLoaded)
			return;
		GameTeam t = getTeam(player);
		if(t!=null) {
			t.removePlayer(player);
		}
		GameTeam lowTeam = teams.get(0);
		int lowest = teams.get(0).size();
		for(GameTeam team : teams)
			if(team.size()<lowest) {
				lowest = team.size();
				lowTeam = team;
			}
		lowTeam.addPlayer(player);
	}
	
	public void addPlayerToTeam(GamePlayer player, GameTeam team) {
		if(teamAmount==0 || !gameLoaded)
			return;
		GameTeam t = getTeam(player);
		if(t!=null) {
			t.removePlayer(player);
		}
		team.addPlayer(player);
	}
	
	//return false if teamId does not exists.
	public boolean addPlayerToTeam(GamePlayer player, int teamId) {
		if(teams.size()-1 < teamId)
			return false;
		addPlayerToTeam(player, teams.get(teamId));
		return true;
	}
	
	public GameTeam getTeam(GamePlayer player) {
		for(GameTeam t : teams) {
			if(t.isInTeam(player))
				return t;
		}
		return null;
	}
	
	public GameTeam[] getTeams() {
		return teams.toArray(new GameTeam[teams.size()]);
	}
	
	public boolean isLoaded() {
		return gameLoaded;
	}
	
	public Location getSpawn() {
		return spawn.clone();
	}
	
	public String getMapName() {
		return name;
	}
	
	public String getMapAuthor() {
		return author;
	}
	
	public int getTeamAmount() {
		return teamAmount;
	}
	
	public GameState getGameState() {
		return state;
	}
	
	public GameTimer getTimer() {
		return timer;
	}
	
	public boolean isWaitingForPlayers() {
		return waitingForPlayers;
	}
	
	public void setWaitingForPlayers(boolean value) {
		waitingForPlayers = value;
	}
	
	public Map<String, Location> signConfigs(){
		return signConfig;
	}
	
	public void setScoreToWin(int amount) {
		scoreToWin = amount;
	}
	
	public int getScoreToWin() {
		return scoreToWin;
	}
	
	public List<String> preGameDisplay() {
		List<String> list = new ArrayList<String>();
		if(waitingForPlayers) 
			list.add("Waiting for players ("+Bukkit.getOnlinePlayers().size()+"/"+GameConfig.playersToStart+")");
		else
			list.add("Starts in "+timer.getTimeLeftDisplay());
		list.add("Gamemode:");
		list.add(ChatColor.GRAY+" "+game.getGamemodeName());
		list.add("Map:");
		list.add(ChatColor.GRAY+" "+name);
		list.add(" by "+ChatColor.GRAY+author);
		list.add("Online:");
		list.add(ChatColor.GRAY+" "+Bukkit.getOnlinePlayers().size());
		list.add(""+ChatColor.GRAY+ChatColor.ITALIC+"dezilla.net");
		return list;
	}
	
	public GameBase getGame() {
		return game;
	}
	
	public static enum GameState{
		PREGAME,
		INGAME,
		POSTGAME;
	}
	
}
