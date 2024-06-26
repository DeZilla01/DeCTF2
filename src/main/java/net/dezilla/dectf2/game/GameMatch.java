package net.dezilla.dectf2.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
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
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.util.WorldRunnable;
import net.dezilla.dectf2.util.ZipUtility;
import net.md_5.bungee.api.ChatColor;
import net.dezilla.dectf2.game.ctf.CTFGame;
import net.dezilla.dectf2.game.pl.PayloadGame;
import net.dezilla.dectf2.game.tdm.TDMGame;
import net.dezilla.dectf2.game.zc.ZonesGame;
import net.dezilla.dectf2.gui.MapVoteGui;
import net.dezilla.dectf2.util.GameColor;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.LabyUtil;
import net.dezilla.dectf2.util.MapPreview;
import net.dezilla.dectf2.util.Portal;
import net.dezilla.dectf2.util.RestrictArea;

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
	@Deprecated
	private Map<String, Location> signConfig = new HashMap<String, Location>();
	private List<Sign> signConfigs = new ArrayList<Sign>();
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
	private Map<Integer, List<Location>> teamSpawns = new HashMap<Integer, List<Location>>();//this var is only used between sign parse and team creations, use GameTeam#getSpawn()
	private Map<Integer, Material> spawnMat = new HashMap<Integer, Material>();//this var is only used between sign parse and team creations
	private Map<Integer, GameColor> teamColor = new HashMap<Integer, GameColor>();//this var is only used between sign parse and team creations
	private Map<Integer, String> teamName = new HashMap<Integer, String>();//this var is only used between sign parse and team creations
	private boolean blockparsed = false;
	private GameMapVote mapVote = null;
	private List<GameCallout> callouts = new ArrayList<GameCallout>();
	private ItemStack mapIcon = new ItemStack(Material.PAPER);
	private List<RestrictArea> restrictedAreas = new ArrayList<RestrictArea>();
	private List<Portal> portals = new ArrayList<Portal>();
	private boolean selectable = true;
	
	public GameMatch(String levelName) throws FileNotFoundException {
		//Set chosen level
		if(levelName != null)
			sourceZip = new File(Util.getGameMapFolder().getPath()+File.separator+levelName);
		//Set to default level if not valid or null
		if(sourceZip == null || !sourceZip.exists()) {
			if(GameConfig.defaultMap!=null)
				sourceZip = new File(Util.getGameMapFolder().getPath()+File.separator+GameConfig.defaultMap);
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
				//UnzipUtility.unzip(sourceZip.getPath(), gameFolder.getPath());
				ZipUtility.unzipMap(sourceZip, gameFolder);
			} catch (IOException e) {
				e.printStackTrace();
				return;
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
				world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
				world.setGameRule(GameRule.TNT_EXPLOSION_DROP_DECAY, false);
				world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
				world.setDifficulty(Difficulty.EASY);
				spawn = world.getSpawnLocation();
				scoreboardNotif("Parsing signs");
				parseSigns();
				createTeams();
				if(mode.equalsIgnoreCase("ctf"))
					game = new CTFGame(this);
				else if(mode.equalsIgnoreCase("zc"))
					game = new ZonesGame(this);
				else if(mode.equalsIgnoreCase("pl"))
					game = new PayloadGame(this);
				else 
					game = new TDMGame(this); //fallback gamemode incase mode is not valid
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
						startGame();
					}
					else if(state == GameState.INGAME) {
						endGame();
					}
					else if(state == GameState.POSTGAME) {
						endPostGame();
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
				//This task will save map information in "maps.json" to be used without loading the map
				Bukkit.getScheduler().runTaskAsynchronously(GameMain.getInstance(), () -> {
					MapPreview.saveData(this);
				});
			});
		});
	}
	
	public void startGame() {
		state = GameState.INGAME;
		timer.setSeconds(timeLimit);
		game.gameStart();
		timer.unpause();
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getGameMode() == GameMode.SURVIVAL) 
				respawnPlayer(GamePlayer.get(p));
		}
	}
	
	public void endGame() {
		state = GameState.POSTGAME;
		timer.setSeconds(20);
		game.unregister();
		timer.unpause();
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getGameMode() == GameMode.SURVIVAL) 
				respawnPlayer(GamePlayer.get(p));
		}
		createMapVote(true);
	}
	
	public void endPostGame() {
		GameMatch.previousMatch = this;
		try {
			GameMatch m;
			if(GameMatch.nextMatch == null)
				m = new GameMatch(mapVote.getWinner());
			else
				m = GameMatch.nextMatch;
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
		nextMatch = null;
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
		for(Portal p : portals)
			p.remove();
		File worldFolder = world.getWorldFolder();
		if(currentMatch.equals(this))
			currentMatch = null;
		for(Player p : world.getPlayers()) 
			p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
		Bukkit.getServer().unloadWorld(world, false);
		Util.deleteFolder(worldFolder);
	}
	
	//When joining the server or switching match
	public void addPlayer(GamePlayer player) {
		boolean addedToPreviousTeam = false;
		if(previousMatch != null && previousMatch.getTeam(player) != null && previousMatch.getTeamAmount() == this.getTeamAmount()) {
			addedToPreviousTeam = addPlayerToTeam(player, previousMatch.getTeam(player).getId());
		} 
		if(!addedToPreviousTeam)
			addPlayerToRandomTeam(player);
		player.getPlayer().setGameMode(GameMode.SURVIVAL);
		Location l = spawn.clone();
		l.setY(world.getMinHeight());
		player.getPlayer().setBedSpawnLocation(l, true);
		respawnPlayer(player);
		Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
			if(LabyUtil.playerUseLaby(player.getPlayer()))
				LabyUtil.updateLabyStatus(player.getPlayer(), game.getGamemodeName());
		}, 2);
	}
	
	public void createMapVote(boolean openGui) {
		List<File> files = Arrays.asList(Util.getWorldList());
		List<String> choices = new ArrayList<String>();
		int max = files.size();
		while(choices.size() < GameConfig.mapVoteAmount && choices.size() < max) {
			File f = files.get((int) (Math.random()*files.size()));
			if(choices.contains(f.getName()))
				continue;
			if(!new MapPreview(f.getName()).isSelectable()) {
				max--;//TODO rewrite this bit
				continue;
			}
			choices.add(f.getName());
		}
		List<MapPreview> selected = new ArrayList<MapPreview>();
		for(String s : choices)
			selected.add(new MapPreview(s));
		mapVote = new GameMapVote(selected);
		if(openGui) {
			for(Player p : world.getPlayers()) {
				new MapVoteGui(p, mapVote).display();
			}
		}
	}
	
	public void respawnPlayer(GamePlayer player) {
		player.getPlayer().setHealth(20.0);
		player.getPlayer().setFoodLevel(20);
		player.getPlayer().getInventory().clear();
		player.setInversionTicks(0);
		GameTeam team = getTeam(player);
		if(state != GameState.INGAME || team == null) {
			if(state == GameState.PREGAME)
				setPregameInventory(player);
			else if(state == GameState.POSTGAME)
				setPostgameInventory(player);
			player.getPlayer().teleport(spawn);
		} else {
			player.setSpawnProtection();
			player.getKit().setInventory(true);
			player.getPlayer().teleport(team.getSpawn());
		}
	}
	
	private void setPregameInventory(GamePlayer player) {
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setItem(4, ItemBuilder.of(Material.NETHER_STAR).name("Kit Selector").data("kit_selector").get());
		inv.setItem(3, ItemBuilder.of(player.getTeam().getColor().wool()).name("Switch Team").data("switch_team").get());
		inv.setItem(5, ItemBuilder.of(Material.CLOCK).name("Tools").data("tool_select").get());
		inv.setHeldItemSlot(4);
	}
	
	private void setPostgameInventory(GamePlayer player) {
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setItem(4, ItemBuilder.of(Material.EMERALD).name("Vote for the next map").data("map_vote").get());
		inv.setHeldItemSlot(4);
	}
	
	private void parseSigns() {
		int spawnx = world.getSpawnLocation().getChunk().getX();
		int spawnz = world.getSpawnLocation().getChunk().getZ();
		String pattern1 = Pattern.quote("{") + "(.*)" + Pattern.quote("}");
		String pattern2 = Pattern.quote("{") + "(.*)=(.*)" + Pattern.quote("}");
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
					List<String> lines = new ArrayList<String>();
					lines.addAll(Arrays.asList(sign.getSide(Side.FRONT).getLines()));
					lines.addAll(Arrays.asList(sign.getSide(Side.BACK).getLines()));
					for(String i : lines) {
						if(i.matches(pattern1) || i.matches(pattern2)) {
							removeSign = true;
							String line = i.replaceAll(pattern1, "$1").toLowerCase();
							signConfig.put(line, loc);
							signConfigs.add(sign);
							if(i.matches(pattern2)) {
								String key = i.replaceAll(pattern2, "$1").toLowerCase();
								String value = i.replaceAll(pattern2, "$2").toLowerCase();
								//Team Amount
								if(key.equals("teams")) {
									try {
										teamAmount = Integer.parseInt(value);
										if(teamAmount<1)
											teamAmount=1;
										else if(teamAmount>16)
											teamAmount=16;
									}catch(Exception e) {}
								}
								//Time Limit
								else if(key.equals("time")) {
									try {
										timeLimit = Integer.parseInt(value);
									}catch(Exception e) {}
								}
								//Score to win
								else if(key.equals("score")) {
									try {
										scoreToWin = Integer.parseInt(value);
									}catch(Exception e) {}
								}
								//Game Mode
								else if(key.equals("mode")) {
									mode = value;
								}
								//Team Spawns
								else if(key.equals("spawn")) {
									try {
										int team = Integer.parseInt(value);
										if(!teamSpawns.containsKey(team))
											teamSpawns.put(team, new ArrayList<Location>());
										teamSpawns.get(team).add(loc);
										spawnMat.put(team, loc.getBlock().getRelative(BlockFace.DOWN).getType());
									}catch(Exception e) {}
								}
								//Callout
								else if(key.equals("callout")) {
									try {
										int team = Integer.parseInt(value);
										callouts.add(new GameCallout(loc, team, Util.grabConfigText(sign)));
									}catch(Exception e) {}
								}
								//Team Color
								else if(key.startsWith("color")) {
									try {
										int team = Integer.parseInt(key.replace("color", ""));
										GameColor color;
										try {
											color = GameColor.values()[Integer.parseInt(value)];
										}catch(Exception e) {
											color = GameColor.valueOf(value.toUpperCase());
										}
										teamColor.put(team, color);
									}catch(Exception e) {}
								}
								//Team Name
								else if(key.equals("name")) {
									try {
										int team = Integer.parseInt(value);
										teamName.put(team, Util.grabConfigText(sign));
									}catch(Exception e) {}
								}
								continue;
							}
							//Main spawnpoint (pre/post game spawn)
							if(line.equals("spawn")) {
								spawn = loc;
							}
							//Map Name
							else if(line.equals("name")) {
								name = Util.grabConfigText(sign);
							}
							//Map Author
							else if(line.equals("author")) {
								author = Util.grabConfigText(sign);
							}
							//Selectable
							else if(line.equals("selectable")) {
								try {
									selectable = Boolean.parseBoolean(Util.grabConfigText(sign));
								} catch(Exception e) {}
							}
							//Neutral Callout (not team sided)
							else if(line.equals("callout")) {
								callouts.add(new GameCallout(loc, Util.grabConfigText(sign)));
							}
							//Map icon
							else if(line.equals("icon")) {
								try {
									mapIcon = new ItemStack(Material.valueOf(Util.grabConfigText(sign).toUpperCase()));
								} catch(Exception e) {}
							}
							//restrict block
							else if(line.equals("restrict")) {
								double radius = 3;
								try {
									radius = Double.parseDouble(Util.grabConfigText(sign));
								}catch(Exception e) {}
								RestrictArea area = new RestrictArea(sign.getBlock(), radius);
								restrictedAreas.add(area);
							}
							//portal
							else if(line.equals("portal")) {
								List<Double> numbers = Util.grabNumbers(sign);
								if(numbers.size()>=3) {
									Location dest = new Location(sign.getWorld(), numbers.get(0), numbers.get(1), numbers.get(2));
									if(numbers.size()>=5) {
										double yaw = numbers.get(3);
										double pitch = numbers.get(4);
										dest.setYaw((float) yaw);
										dest.setPitch((float) pitch);
									}
									portals.add(new Portal(loc, Portal.BlocktoPortalFace(face), dest));
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
			if(teamColor.containsKey(i))
				c = teamColor.get(i);
			
			GameTeam t = new GameTeam(i, c);
			
			if(!teamSpawns.containsKey(i))
				System.out.println("Missing configuration for spawn "+t.getTeamName());
			else {
				for(Location l : teamSpawns.get(i)) {
					t.addSpawn(l);
					callouts.add(new GameCallout(l, t, "Spawn"));
				}
			}
			if(teamName.containsKey(i)) {
				t.setTeamName(teamName.get(i));
			}
			if(spawnMat.containsKey(i))
				t.setSpawnMaterial(spawnMat.get(i));
			teams.add(t);
			for(GameCallout call : callouts) {
				if(call.getTeamId() == t.getId())
					call.setTeam(t);
			}
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
	
	public void addCallout(GameCallout callout) {
		callouts.add(callout);
	}
	
	public GameCallout getCalloutNear(Location loc) {
		GameCallout call = null;
		double distance = 99999;
		for(GameCallout c : callouts) {
			if(c.isNear(loc)) {
				double d = c.getLocation().distance(loc);
				if(d < distance) {
					call = c;
					distance = d;
				}
			}
		}
		return call;
	}
	
	public void addRestrictedArea(RestrictArea area) {
		restrictedAreas.add(area);
	}
	
	public boolean isAreaRestricted(Location loc) {
		if(!loc.getWorld().equals(world))
			return true;
		for(RestrictArea a : restrictedAreas) {
			if(a.inRadius(loc)) {
				return true;
			}
		}
		return false;
	}
	
	public String getRestrictionReason(Location loc) {
		if(!loc.getWorld().equals(world))
			return null;
		for(RestrictArea a : restrictedAreas) {
			if(a.inRadius(loc)) {
				return a.getReason();
			}
		}
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
	
	public boolean isSelectable() {
		return selectable;
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
	
	public List<Sign> signConfigz(){
		return signConfigs;
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
	
	public GameMapVote getMapVote() {
		return mapVote;
	}
	
	public World getWorld() {
		return world;
	}
	
	public ItemStack getMapIcon() {
		return mapIcon;
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
		if(mapVote != null) {
			list.add(ChatColor.BOLD+"Next Map "+ChatColor.RESET+"- /vote");
			for(MapPreview m : mapVote.getMapList())
				list.add(" "+m.getNameAndMode(false)+" "+ChatColor.AQUA+mapVote.getVotes(m.getFile().getName()));
		}
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