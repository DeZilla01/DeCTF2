package net.dezilla.dectf2;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.game.GameBase;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.game.ctf.CTFGame;
import net.dezilla.dectf2.kits.BaseKit;
import net.dezilla.dectf2.kits.HeavyKit;
import net.dezilla.dectf2.kits.PyroKit;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.InvSave;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.ObjectiveLocation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class GamePlayer {
	private final static String[] filler = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};//used for scoreboards
	private static List<GamePlayer> PLAYERS = new ArrayList<GamePlayer>();
	private static ItemStack FROZEN_HEAD = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTI2NDQwNzFiNmM3YmJhZTdiNWU0NWQ5ZjgyZjk2ZmZiNWVlOGUxNzdhMjNiODI1YTQ0NjU2MDdmMWM5YyJ9fX0=");
	
	public static GamePlayer get(Player player) {
		if(player == null)
			return null;
		for(GamePlayer i : PLAYERS) {
			if(i.getPlayer().equals(player))
				return i;
			if(i.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				i.updatePlayer(player);
				return i;
			}
		}
		return new GamePlayer(player);
	}
	
	public static List<GamePlayer> getPlayers(){
		return new ArrayList<GamePlayer>(PLAYERS);
	}
	
	private Player player;
	private Scoreboard score;
	private Map<String, Integer> stats = new HashMap<String, Integer>();
	private double damageDealt = 0.0;
	private GamePlayer lastAttacker = null;
	private CustomDamageCause damageCause= null;
	private BaseKit kit;
	private boolean spawnProtection = false;
	private Timestamp lastItemDrop = null;
	private PlayerNotificationType notif = PlayerNotificationType.SUBTITLE;
	private boolean invisible = false;
	private PlayerChatType chatType = PlayerChatType.GLOBAL;
	private Timestamp lastHeal = new Timestamp(new Date().getTime());
	private Timestamp lastRegen = new Timestamp(new Date().getTime());
	private boolean onFire = false; //used for pyro
	private boolean fireImmunity = false; //used for pyro 
	private int inversionTicks = 0;
	List<InvertPosition> moveHistory = new ArrayList<InvertPosition>();
	private int objectiveIndex = 0;
	private int frozenTicks = 0;
	private boolean frozen = false;
	//tools
	private boolean objectiveTracker = true;
	private boolean spyglass = false;
	private boolean kitSelector = false;
	private boolean pointer = false;
	private List<InvSave> invSaves = new ArrayList<InvSave>();
	
	private GamePlayer(Player player) {
		this.player = player;
		PLAYERS.add(this);
		score = Bukkit.getScoreboardManager().getNewScoreboard();
		applyScoreboard();
		updateScoreboardDisplay();
		kit = new HeavyKit(this);
	}
	
	public void onTick() {
		if(player.getFireTicks()>0 && !isOnFire()) {
			setOnFire(true);
			onFire();
		} 
		else if(player.getFireTicks()<=0 && isOnFire()) {
			setOnFire(false);
			setFireImmunity(false);
		}
		if(inversionTicks>0 && !moveHistory.isEmpty()) {
			InvertPosition ip = moveHistory.get(moveHistory.size()-1);
			moveHistory.remove(ip);
			player.getPlayer().teleport(ip.getLocation());
			player.getPlayer().setVelocity(ip.getVelocity().multiply(-1));
			player.getPlayer().setFallDistance(0);
			inversionTicks -=1;
			player.getPlayer().playSound(getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 1);
		} else {
			InvertPosition ip = new InvertPosition(player.getLocation(), player.getPlayer().getVelocity());
			moveHistory.add(ip);
			if(moveHistory.size()>300)
				moveHistory.remove(0);
		}
		GameMatch match = GameMatch.currentMatch;
		if(match != null && match.getGame().hasObjectiveLocations()) {
			List<ObjectiveLocation> locs = match.getGame().getObjectiveLocations();
			if(locs.size()<=objectiveIndex)
				objectiveIndex = 0;
			if(!locs.isEmpty()) {
				player.setCompassTarget(locs.get(objectiveIndex).getLocation());
			}
		}
		if(!frozen && frozenTicks>0) {
			freeze();
		} else if(frozen && frozenTicks == 0) {
			unfreeze();
		}
		if(frozenTicks>0)
			frozenTicks--;
	}
	
	public void toggleObjectiveLocation() {
		objectiveIndex++;
	}
	
	public String getObjectiveLocationName() {
		GameMatch match = GameMatch.currentMatch;
		if(match != null && match.getGame().hasObjectiveLocations()) {
			List<ObjectiveLocation> locs = match.getGame().getObjectiveLocations();
			if(locs.size()<=objectiveIndex)
				objectiveIndex = 0;
			if(!locs.isEmpty()) {
				return locs.get(objectiveIndex).getName();
			}
		}
		return null;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	private void updatePlayer(Player player) {
		this.player = player;
		applyScoreboard();
		updateScoreboardDisplay();
	}
	
	public void updateScoreboardTeams() {
		GameMatch match = GameMatch.currentMatch;
		if(match == null)
			return;
		for(GameTeam t : match.getTeams()) {
			Team team = score.getTeam(""+t.getId());
			if(team==null) {
				team = score.registerNewTeam(""+t.getId());
				team.setColor(t.getColor().getBukkitChatColor());
				team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
			}
			if(team.getColor() != t.getColor().getBukkitChatColor()) {
				team.setColor(t.getColor().getBukkitChatColor());
			}
			for(GamePlayer p : t.getPlayers()) {
				if(!team.hasEntry(p.getPlayer().getName()))
					team.addEntry(p.getPlayer().getName());
			}
		}
	}
	
	public void updateScoreboardDisplay() {
		GameMatch match = GameMatch.currentMatch;
		
		List<String> displayList = new ArrayList<String>();
		displayList.add("DeCTF2");
		displayList.add("No game found");
		
		if(match != null && match.getGameState() == GameState.PREGAME) {
			displayList = match.preGameDisplay();
		} else if(match != null && match.getGameState() == GameState.INGAME) {
			displayList = match.getGame().getScoreboardDisplay(this);
		} else if(match != null && match.getGameState() == GameState.POSTGAME) {
			displayList = match.postGameDisplay();
		}
		updateScoreboardDisplay(displayList);
	}
	
	public void updateScoreboardDisplay(List<String> list) {
		Objective display = score.getObjective("display");
		if(display == null){
			display = score.registerNewObjective("display", Criteria.DUMMY, "display");
			display.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		
		List<String> displayList = new ArrayList<String>(list);
		
		display.setDisplayName(displayList.get(0));
		displayList.remove(0);
		
		int s = displayList.size()-1;
		if(s>15)
			s = 15;
		int count = 0;
		for(String f : filler) {
			if(count+1>displayList.size())
				score.resetScores(ChatColor.translateAlternateColorCodes('&', "&"+f+"&f"));
			count++;
		}
		for(String i : displayList) {
			Team t = score.getTeam("line"+s);
			if(t==null) {
				t = score.registerNewTeam("line"+s);
				t.addEntry(ChatColor.translateAlternateColorCodes('&', "&"+filler[s]+"&f"));
			}
			t.setSuffix(i);
			display.getScore(ChatColor.translateAlternateColorCodes('&', "&"+filler[s]+"&f")).setScore(s--);
			if(s<0)
				break;
		}
	}
	
	public void addInvSave(InvSave save) {
		InvSave old = null;
		for(InvSave s : invSaves) {
			if(s.getKit().equals(save.getKit()) && s.getVariation().equalsIgnoreCase(save.getVariation())) {
				old = s;
				break;
			}
		}
		if(old != null)
			invSaves.remove(old);
		invSaves.add(save);
	}
	
	public void applyInvSave() {
		for(InvSave s : invSaves) {
			if(kit.getClass().equals(s.getKit()) && kit.getVariation().equalsIgnoreCase(s.getVariation())) {
				s.apply(player.getInventory());
				break;
			}
		}
	}
	
	public boolean resetInvSave() {
		InvSave save = null;
		for(InvSave s : invSaves) {
			if(kit.getClass().equals(s.getKit()) && kit.getVariation().equalsIgnoreCase(s.getVariation())) {
				save = s;
				break;
			}
		}
		if(save != null) {
			invSaves.remove(save);
			return true;
		}
		else
			return false;
	}
	
	public void setStats(String key, int amount) {
		stats.put(key, amount);
	}
	
	public void incrementStats(String key, int amount) {
		stats.put(key, getStats(key)+amount);
	}
	
	public int getStats(String key) {
		if(!stats.containsKey(key))
			stats.put(key, 0);
		return stats.get(key);
	}
	
	public double getDamageDealt() {
		return damageDealt;
	}
	
	public void incrementDamageDealt(double amount) {
		damageDealt+=amount;
	}
	
	public boolean isSpawnProtected() {
		return spawnProtection;
	}
	
	public boolean isFullHp() {
		return player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() == player.getHealth();
	}
	
	public String getName() {
		return player.getDisplayName();
	}
	public Location getLocation() {
		return player.getLocation();
	}
	
	public Timestamp getLastItemDrop() {
		return lastItemDrop;
	}
	
	public void setLastItemDrop(Timestamp timestamp) {
		lastItemDrop = timestamp;
	}
	
	public void setChatType(PlayerChatType type) {
		chatType = type;
	}
	
	public PlayerChatType getChatType() {
		return chatType;
	}
	
	public boolean isOnFire() {
		return onFire;
	}
	
	public void setOnFire(boolean value) {
		onFire = value;
	}
	
	public boolean isFireImmune() {
		return fireImmunity;
	}
	
	public void setFireImmunity(boolean value) {
		fireImmunity = value;
	}
	
	public void setInversionTicks(int value) {
		inversionTicks = value;
	}
	
	public int getInversionTicks() {
		return inversionTicks;
	}
	
	public void resetInversionHistory() {
		moveHistory.clear();
	}
	
	public boolean useTracker() {
		return objectiveTracker;
	}
	
	public void setTracker(boolean value) {
		objectiveTracker = value;
	}
	
	public boolean useSpyglass() {
		return spyglass;
	}
	
	public void setSpyglass(boolean value) {
		spyglass = value;
	}
	
	public boolean useKitSelector() {
		return kitSelector;
	}
	
	public void setKitSelector(boolean value) {
		kitSelector = value;
	}
	
	public boolean usePointer() {
		return pointer;
	}
	
	public void setPointer(boolean value) {
		pointer = value;
	}
	
	public void setFrozenTicks(int value) {
		frozenTicks = value;
	}
	
	public int getFrozenTicks() {
		return frozenTicks;
	}
	
	public boolean isFrozen() {
		return frozen;
	}
	
	private ItemStack fHelmet = null;
	private ItemStack fChestplate = null;
	private ItemStack fLeggings = null;
	private ItemStack fBoots = null;
	
	private void freeze() {
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, -1, 50));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, -1, -5));
		player.setFreezeTicks(100);
		PlayerInventory inv = player.getInventory();
		fHelmet = inv.getHelmet();
		fChestplate = inv.getChestplate();
		fLeggings = inv.getLeggings();
		fBoots = inv.getBoots();
		inv.setHelmet(FROZEN_HEAD.clone());
		inv.setChestplate(ItemBuilder.of(Material.LEATHER_CHESTPLATE).unbreakable().leatherColor(Color.fromRGB(106, 142, 203)).armorTrim(TrimPattern.RAISER, TrimMaterial.IRON).get());
		inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).unbreakable().leatherColor(Color.fromRGB(106, 142, 203)).armorTrim(TrimPattern.RAISER, TrimMaterial.IRON).get());
		inv.setBoots(ItemBuilder.of(Material.LEATHER_BOOTS).unbreakable().leatherColor(Color.fromRGB(106, 142, 203)).armorTrim(TrimPattern.RAISER, TrimMaterial.IRON).get());
		frozen = true;
	}
	
	private void unfreeze() {
		if(player.hasPotionEffect(PotionEffectType.SLOW)) {
			PotionEffect eff = player.getPotionEffect(PotionEffectType.SLOW);
			if(eff.getAmplifier() == 50)
				player.removePotionEffect(PotionEffectType.SLOW);
		}
		if(player.hasPotionEffect(PotionEffectType.JUMP)) {
			PotionEffect eff = player.getPotionEffect(PotionEffectType.JUMP);
			if(eff.getAmplifier() == -5)
				player.removePotionEffect(PotionEffectType.JUMP);
		}
		PlayerInventory inv = player.getInventory();
		inv.setHelmet(fHelmet);
		inv.setChestplate(fChestplate);
		inv.setLeggings(fLeggings);
		inv.setBoots(fBoots);
		player.setFreezeTicks(0);
		frozen = false;
	}
	
	public void setInvisible(boolean value) {
		if(value) {
			//prevent going invisible when holding flag
			if(GameMatch.currentMatch != null) {
				GameBase game = GameMatch.currentMatch.getGame();
				if(game instanceof CTFGame) {
					CTFGame ctf = (CTFGame) game;
					if(ctf.getHeldFlag(this) != null)
						return;
				}
			}
			for(Player p : Bukkit.getOnlinePlayers()) {
				GamePlayer gp = GamePlayer.get(p);
				if(gp.getTeam() != null && getTeam() != null && gp.getTeam().equals(getTeam()))
					continue;
				p.hideEntity(GameMain.getInstance(), getPlayer());
			}
			getPlayer().setInvisible(true);
			invisible = true;
		} else {
			for(Player p : Bukkit.getOnlinePlayers()) {
				p.showEntity(GameMain.getInstance(), getPlayer());
			}
			getPlayer().setInvisible(false);
			invisible = false;
		}
	}
	
	public boolean isInvisible() {
		return invisible;
	}
	
	public void setLastHeal(Timestamp when) {
		lastHeal = when;
	}
	
	public Timestamp getLastHeal() {
		return lastHeal;
	}
	
	public void setLastRegen(Timestamp when) {
		lastRegen = when;
	}
	
	public Timestamp getLastRegen() {
		return lastRegen;
	}
	
	public int getRegenTickDelay() {
		Timestamp now = new Timestamp(new Date().getTime());
		int a = (int) (GameConfig.regenDelay - ((now.getTime() - lastRegen.getTime())/50));
		if(a<0)
			a=0;
		return a;
	}
	
	public void setSpawnProtection() {
		spawnProtection = true;
		GameTimer timer = new GameTimer(-1);
		timer.unpause();
		timer.onSecond((t) -> {
			double max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			double current = player.getHealth();
			if(current < max) {
				if(current+1<max)
					player.setHealth(current+1);
				else
					player.setHealth(max);
			}
		});
		timer.onTick((t) -> {
			GameTeam team = getTeam();
			if(team == null) {
				t.unregister();
				spawnProtection = false;
				return;
			}
			Block block = player.getLocation().add(0,-1,0).getBlock();
			if(!team.isSpawnBlock(block)) {
				t.unregister();
				spawnProtection = false;
				return;
			}
		});
	}
	
	public BaseKit getKit() {
		return kit;
	}
	
	public void setKit(Class<? extends BaseKit> kit) {
		setKit(kit, null);
	}
	
	public void setKit(Class<? extends BaseKit> kit, String variation) {
		try {
			BaseKit oldkit = this.kit;
			this.kit = kit.getConstructor(this.getClass()).newInstance(this);
			if(variation != null)
				this.kit.setVariation(variation);
			oldkit.unregister();
			GameMatch match = GameMatch.currentMatch;
			if(match != null && match.getGameState() == GameState.INGAME && player.getGameMode() == GameMode.SURVIVAL) {
				if(player.getHealth() < player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() && !isSpawnProtected()) {
					this.setCustomDamageCause(CustomDamageCause.KIT_SWITCH);
					player.damage(999);
				} else {
					match.respawnPlayer(this);
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public CustomDamageCause getCustomDamageCause() {
		return damageCause;
	}
	
	public void setCustomDamageCause(CustomDamageCause cause) {
		damageCause = cause;
	}
	
	public GamePlayer getLastAttacker() {
		return lastAttacker;
	}
	
	public void setLastAttacker(GamePlayer attacker) {
		lastAttacker = attacker;
	}
	
	public GameTeam getTeam() {
		if(GameMatch.currentMatch == null)
			return null;
		return GameMatch.currentMatch.getTeam(this);
	}
	
	public String getColoredName() {
		GameTeam t = getTeam();
		String prefix = ChatColor.WHITE+"";
		if(t != null)
			prefix = t.getColor().getPrefix();
		return prefix+player.getName();
	}
	
	public void applyScoreboard() {
		player.setScoreboard(score);
	}
	
	public PlayerNotificationType getNotificationType() {
		return notif;
	}
	
	public void setNotificationType(PlayerNotificationType type) {
		notif = type;
	}
	
	private void onFire() {
		GameTeam team = getTeam();
		if(team == null)
			return;
		List<Block> blocks = Util.get4x4Blocks(player.getLocation());
		for(GamePlayer p : GamePlayer.getPlayers()) {
			if(!p.getPlayer().isOnline())
				continue;
			if(p.getTeam() == null || !p.getTeam().equals(team))
				continue;
			if(p.getKit() == null || !(p.getKit() instanceof PyroKit))
				continue;
			PyroKit kit = (PyroKit) p.getKit();
			for(Block b : blocks)
				if(kit.isPyroFire(b)) {
					setFireImmunity(true);
				}
		}
	}
	
	public void notify(String msg) {
		switch(notif) {
			case ACTIONBAR:
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
				break;
			case SUBTITLE:
				player.sendTitle(ChatColor.RESET+" ", msg, 0, 50, 10);
				break;
			case CHAT:
				player.sendMessage(msg);
				break;
			case BOSSBAR:
				BossBar bar = Bukkit.createBossBar(msg, BarColor.WHITE, BarStyle.SOLID);
				bar.addPlayer(player);
				Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
					bar.removeAll();
				}, 150);
		}
	}
	
	public static enum PlayerNotificationType {
		SUBTITLE,
		ACTIONBAR,
		CHAT,
		BOSSBAR;
	}
	
	public static enum PlayerChatType {
		GLOBAL,
		TEAM;
	}
	
	public class InvertPosition {
		Location loc;
		Vector vel;
		
		public InvertPosition(Location location, Vector velocity) {
			loc = location;
			vel = velocity;
		}
		
		public Location getLocation() {
			return loc.clone();
		}
		
		public Vector getVelocity() {
			return vel.clone();
		}
	}

}
