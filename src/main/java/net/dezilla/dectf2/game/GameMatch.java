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
import org.bukkit.ChunkSnapshot;
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
	private int scoreToWin = 0;
	private int yBottom = -64;
	private Map<Integer, Location> teamSpawns = new HashMap<Integer, Location>();//this var is only used between sign parse and team creations, use GameTeam#getSpawn()
	private Map<Integer, Material> spawnMat = new HashMap<Integer, Material>();
	private boolean blockparsed = false;
	
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
				sourceZip = files[(int) (Math.random()*(files.length))];
		}
		//At this point, if no valid map is set, big rip
		if(sourceZip == null || !sourceZip.exists()) {
			throw new FileNotFoundException("No map found in configured folder. Make sure map zips are available.");
		}
		
		name = sourceZip.getName();
	}
	
	public void Load(WorldRunnable onLoad) {
		scoreboardNotif("Loading "+name);
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
				scoreboardNotif("Creating world");
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
				world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
				spawn = world.getSpawnLocation();
				scoreboardNotif("Parsing signs");
				parseSigns();
				createTeams();
				if(mode.equalsIgnoreCase("ctf"))
					game = new CTFGame(this);
				else if(mode.equalsIgnoreCase("zones"))
					{}//TODO
				else if(mode.equalsIgnoreCase("tdm"))
					{}//TODO
				if(scoreToWin <= 0) {
					scoreToWin = game.getDefaultScoreToWin();
				}
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
					if(state == GameState.PREGAME) {
						state = GameState.INGAME;
						timer.setSeconds(timeLimit);
						game.gameStart();
						timer.unpause();
						for(Player p : Bukkit.getOnlinePlayers()) {
							if(p.getGameMode() == GameMode.SURVIVAL) 
								respawnPlayer(GamePlayer.get(p));
						}
					}
					else if(state == GameState.INGAME) {
						state = GameState.POSTGAME;
						timer.setSeconds(20);
						game.unregister();
						timer.unpause();
						for(Player p : Bukkit.getOnlinePlayers()) {
							if(p.getGameMode() == GameMode.SURVIVAL) 
								respawnPlayer(GamePlayer.get(p));
						}
					}
					else if(state == GameState.POSTGAME) {
						GameMatch.previousMatch = this;
						try {
							GameMatch m = new GameMatch(null);
							//trying to load a duplicate will cause a crash. This will send players to default world but at least avoid a crash
							if(m.getSourceZip().equals(sourceZip))
								unload();
							m.Load((world) -> {
								for(Player p : Bukkit.getOnlinePlayers())
									m.addPlayer(GamePlayer.get(p));
								if(isLoaded()) {
									unload();
									currentMatch = m;
								}
							});
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				});
				gameLoaded = true;
				currentMatch = this;
				onLoad.run(world);
				//This task is going to grab all spawn blocks and team-colored blocks. This is performed while match is in pre-game
				Bukkit.getScheduler().runTaskAsynchronously(GameMain.getInstance(), () -> {
					int max = world.getMaxHeight();
					int min = world.getMinHeight();
					for(Chunk c : world.getLoadedChunks()) {
						ChunkSnapshot cs = c.getChunkSnapshot();
						for(int x = 0; x < 16 ; x++) {
							for(int z = 0; z < 16; z++) {
								for(int y = min; y < max; y++) {
									Material m = cs.getBlockType(x, y, z);
									for(GameTeam t : teams) {
										if(t.isSpawnMaterial(m))
											t.addSpawnBlock(c.getBlock(x, y, z));
										if(t.isTeamColor(m))
											t.addColorBlock(c.getBlock(x, y, z));
									}
								}
							}
						}
					}
					blockparsed=true;
				});
			});
		});
	}
	
	//currently used for debug
	private void scoreboardNotif(String msg) {
		List<String> l = new ArrayList<String>();
		l.add("DeCTF2");
		l.add(msg);
		for(Player p : Bukkit.getOnlinePlayers())
			GamePlayer.get(p).updateScoreboardDisplay(l);
	}
	
	public void unload() {
		timer.unregister();
		gameLoaded = false;
		game.unregister();
		if(currentMatch.equals(this))
			currentMatch = null;
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
		player.getPlayer().setGameMode(GameMode.SURVIVAL);
		respawnPlayer(player);
	}
	
	public void respawnPlayer(GamePlayer player) {
		player.getPlayer().setHealth(20.0);
		player.getPlayer().setFoodLevel(20);
		player.getPlayer().getInventory().clear();
		GameTeam team = getTeam(player);
		if(state != GameState.INGAME || team == null) {
			player.getPlayer().teleport(spawn);
		} else {
			player.getKit().setInventory();
			player.getPlayer().teleport(team.getSpawn());
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
										spawnMat.put(team, loc.getBlock().getRelative(BlockFace.DOWN).getType());
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
			if(spawnMat.containsKey(i))
				t.setSpawnMaterial(spawnMat.get(i));
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
	
	public GameTeam getTeam(int id) {
		if(teams.size()>=id-1 && id>=0)
			return teams.get(id);
		return null;
	}
	
	public GameTeam getTeam(String name) {
		for(GameTeam t : teams)
			if(t.getTeamName().equalsIgnoreCase(name))
				return t;
		return null;
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
	
	public boolean isBlockParsed() {
		return blockparsed;
	}
	
	public File getSourceZip() {
		return sourceZip;
	}
	
	public List<String> preGameDisplay() {
		List<String> list = new ArrayList<String>();
		if(waitingForPlayers) 
			list.add("Waiting for players ("+Bukkit.getOnlinePlayers().size()+"/"+GameConfig.playersToStart+")");
		else
			list.add("Starts in "+timer.getTimeLeftDisplay());
		list.add(ChatColor.BOLD+"Gamemode");
		list.add(ChatColor.GOLD+" "+game.getGamemodeName());
		list.add(ChatColor.BOLD+"Map");
		list.add(ChatColor.GOLD+" "+name);
		list.add(" by "+ChatColor.GOLD+author);
		list.add(ChatColor.BOLD+"Online");
		list.add(ChatColor.GOLD+" "+Bukkit.getOnlinePlayers().size());
		if(!blockparsed)
			list.add(ChatColor.GOLD+"Blocks parsing...");
		list.add(""+ChatColor.GRAY+ChatColor.ITALIC+GameConfig.serverName);
		return list;
	}
	
	public List<String> postGameDisplay() {
		List<String> list = new ArrayList<String>();
		list.add("Next game in "+timer.getTimeLeftDisplay());
		list.add(ChatColor.BOLD+"Scores");
		for(GameTeam t : getTeams()) 
			list.add(" "+t.getColoredTeamName()+ChatColor.RESET+" "+t.getScore());
		list.add(ChatColor.RESET+" ");
		list.add("DeCTF2 is still in");
		list.add("development. Enjoy");
		list.add("your stay =)");
		list.add(""+ChatColor.GRAY+ChatColor.ITALIC+GameConfig.serverName);
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
