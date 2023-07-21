package net.dezilla.dectf2;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.kits.BaseKit;
import net.dezilla.dectf2.kits.HeavyKit;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class GamePlayer {
	
	private static List<GamePlayer> PLAYERS = new ArrayList<GamePlayer>();
	
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
	
	private GamePlayer(Player player) {
		this.player = player;
		PLAYERS.add(this);
		score = Bukkit.getScoreboardManager().getNewScoreboard();
		applyScoreboard();
		updateScoreboardDisplay();
		kit = new HeavyKit(this);
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
				team.setPrefix(t.getColor().getPrefix());
				team.setColor(t.getColor().getChatColor());
				team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
			}
			if(team.getColor() != t.getColor().getChatColor()) {
				team.setPrefix(t.getColor().getPrefix());
				team.setColor(t.getColor().getChatColor());
			}
			for(GamePlayer p : t.getPlayers()) {
				if(!team.hasEntry(p.getPlayer().getName()))
					team.addEntry(p.getPlayer().getName());
			}
		}
	}
	
	public void updateScoreboardDisplay() {
		GameMatch match = GameMatch.currentMatch;
		Objective display = score.getObjective("display");
		if(display == null){
			display = score.registerNewObjective("display", Criteria.DUMMY, "display");
			display.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		
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
		display.setDisplayName(displayList.get(0));
		displayList.remove(0);
		
		List<String> oldDisplay = new ArrayList<String>();
		oldDisplay.addAll(score.getEntries());
		int s = displayList.size()-1;
		for(String i : displayList) {
			if(oldDisplay.contains(i) && display.getScore(i).getScore() == s) {
				s--;
				oldDisplay.remove(i);
				continue;
			}
			display.getScore(i).setScore(s--);
		}
		for(String i : oldDisplay)
			score.resetScores(i);
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
		
		List<String> oldDisplay = new ArrayList<String>();
		oldDisplay.addAll(score.getEntries());
		int s = displayList.size()-1;
		for(String i : displayList) {
			if(oldDisplay.contains(i) && display.getScore(i).getScore() == s) {
				s--;
				oldDisplay.remove(i);
				continue;
			}
			display.getScore(i).setScore(s--);
		}
		for(String i : oldDisplay)
			score.resetScores(i);
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
	
	public void notify(String msg) {
		switch(notif) {
			case ACTIONBAR:
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
				break;
			case SUBTITLE:
				player.sendTitle(ChatColor.RESET+"", msg, 0, 40, 10);
				break;
			case CHAT:
				player.sendMessage(msg);
				break;
		}
	}
	
	public static enum PlayerNotificationType {
		SUBTITLE,
		ACTIONBAR,
		CHAT;
	}

}
