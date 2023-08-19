package net.dezilla.dectf2.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.GamePlayer.PlayerChatType;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.tdm.TDMGame;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.LuckPermsStuff;
import net.md_5.bungee.api.ChatColor;

public class EventListener implements Listener{

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(GameMatch.currentMatch != null) {
			GameMatch.currentMatch.addPlayer(GamePlayer.get(event.getPlayer()));
		} else {
			GameMatch.waitingForNextMatch.add(GamePlayer.get(event.getPlayer()));
		}
		GamePlayer p = GamePlayer.get(event.getPlayer());
		String msg = p.getColoredName() + ChatColor.GRAY + ChatColor.ITALIC + " has joined the game.";
		if(GameConfig.joinMessages)
			event.setJoinMessage(msg);
		else
			event.setJoinMessage(null);
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		GamePlayer p = GamePlayer.get(event.getPlayer());
		String msg = p.getColoredName() + ChatColor.GRAY + ChatColor.ITALIC + " has left the game.";
		if(GameConfig.joinMessages)
			event.setQuitMessage(msg);
		else
			event.setQuitMessage(null);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onDamage(EntityDamageEvent event) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null)
			return;
		if(event.getEntityType() == EntityType.PLAYER) {
			if(match.getGameState() == GameState.PREGAME || match.getGameState() == GameState.POSTGAME) {
				event.setCancelled(true);
				return;
			}
			GamePlayer victim = GamePlayer.get((Player) event.getEntity());
			GameTeam victimTeam = match.getTeam(victim);
			//spawn protection
			if(victimTeam.isSpawnBlock(victim.getPlayer().getLocation().add(0,-1,0).getBlock()) && event.getDamage() < 999) {
				event.setCancelled(true);
				return;
			}
			//falldmg block
			Block standingOn = victim.getLocation().add(0,-1,0).getBlock();
			if(event.getCause() == DamageCause.FALL && (standingOn.getType() == Material.SOUL_SAND || standingOn.getType() == Material.HAY_BLOCK)) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onDamageEntity(EntityDamageByEntityEvent event) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null || match.getGameState() != GameState.INGAME)
			return;
		GamePlayer damager = Util.getOwner(event.getDamager());
		//invis
		if(damager != null && damager.isInvisible()) {
			event.setCancelled(true);
			return;
		}
		if(event.getEntityType() == EntityType.PLAYER && damager != null) {
			GamePlayer victim = GamePlayer.get((Player) event.getEntity());
			//friendly fire
			if(victim.getTeam().equals(damager.getTeam())) {
				event.setCancelled(true);
				return;
			}
			damager.incrementDamageDealt(event.getFinalDamage());
			if(match.getGame() instanceof TDMGame && damager.getTeam() != null) {
				TDMGame g = (TDMGame) match.getGame();
				g.addDamage(damager.getTeam(), event.getFinalDamage());
			}
			
			victim.setLastAttacker(damager);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent event) {
		GameMatch match = GameMatch.currentMatch;
		if(match==null)
			return;
		GamePlayer p = GamePlayer.get(event.getEntity());
		p.incrementStats("deaths", 1);
		GamePlayer killer = p.getLastAttacker();
		if(killer != null) {
			killer.incrementStats("kills", 1);
			killer.incrementStats("streak", 1);
			p.setLastAttacker(null);
			if(match.getGame() instanceof TDMGame && killer.getTeam() != null) {
				TDMGame game = (TDMGame) match.getGame();
				game.addKill(killer.getTeam());
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> match.respawnPlayer(p));
		//Death Message
		String msg = p.getColoredName()+ChatColor.RESET+" ";
		DamageCause cause = p.getPlayer().getLastDamageCause().getCause();
		List<String> list = new ArrayList<String>();
		boolean notByKiller = false;
		switch(cause) {
			case BLOCK_EXPLOSION:
				list.add("exploded into pieces");
				break;
			case CONTACT:
				list.add("kissed the wrong block");
				notByKiller=true;
				break;
			case CRAMMING:
				list.add("was squished too much");
				break;
			case CUSTOM:
				CustomDamageCause customCause = p.getCustomDamageCause();
				if(customCause == null)
					break;
				switch(customCause) {
					case ENEMY_SPAWN:
						list.add("walked in the wrong spawn");
						notByKiller = true;
						break;
					case FLAG_POISON:
						list.add("died from flag poisoning");
						notByKiller = true;
						break;
					case SPAWN_WITH_FLAG:
						list.add("decided that he would bring the flag to his spawn");
						notByKiller = true;
						break;
					case KIT_SWITCH:
						list.add("switched kit");
						notByKiller = true;
						break;
					case SHIELDED_DAMAGE:
						list.add("was pierced");
						list.add("was killed");
						list.add("was slain");
						list.add("relied too much on his shield and got killed");
						break;
					case ARCHER_HEADSHOT:
						list.add("was headshoted");
						list.add("got snipped");
						break;
					case NINJA_TELEPORT:
						list.add("teleported to his doom");
						list.add("teleported to death");
						list.add("died from teleportation");
						list.add("pearled to his death");
						notByKiller=true;
					default:
						break;
				}
				break;
			case DRAGON_BREATH:
				list.add("was roasted in dragon's breath");
				break;
			case DROWNING:
				list.add("drowned");
				notByKiller = true;
				break;
			case DRYOUT:
				list.add("died from dehydration");
				notByKiller = true;
				break;
			case ENTITY_ATTACK:
				list.add("was slain");
				list.add("was killed");
				if(killer != null && (killer.getPlayer().getInventory().getItemInMainHand() == null || killer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR)) {
					list.clear();
					list.add("was punched");
					list.add("was fisted");
				}
				break;
			case ENTITY_EXPLOSION:
				list.add("exploded into pieces");
				list.add("was blown up");
				break;
			case ENTITY_SWEEP_ATTACK:
				list.add("was sweeped");
				break;
			case FALL:
				list.add("fell from too high");
				list.add("forgot he can't fly");
				list.add("broke his legs");
				notByKiller = true;
				break;
			case FALLING_BLOCK:
				list.add("didn't look above");
				list.add("was killed by a block from above");
				notByKiller = true;
				break;
			case FIRE:
				list.add("burned to death");
				notByKiller = true;
				break;
			case FIRE_TICK:
				break;
			case FLY_INTO_WALL:
				break;
			case FREEZE:
				break;
			case HOT_FLOOR:
				break;
			case KILL:
				list.add("was killed by console");
				list.add("was killed by the magic of administrator power");
				notByKiller = true;
				break;
			case LAVA:
				list.add("swimmed in lava");
				list.add("mistook lava for water");
				notByKiller = true;
				break;
			case LIGHTNING:
				break;
			case MAGIC:
				break;
			case MELTING:
				break;
			case POISON:
				break;
			case PROJECTILE:
				list.add("was shot");
				break;
			case SONIC_BOOM:
				break;
			case STARVATION:
				break;
			case SUFFOCATION:
				break;
			case SUICIDE:
				list.add("killed himself");
				list.add("commited sudoku");
				notByKiller = true;
				break;
			case THORNS:
				break;
			case VOID:
				list.add("felled off the map");
				list.add("felled in the void");
				notByKiller=true;
				break;
			case WITHER:
				break;
			case WORLD_BORDER:
				break;
			default:
				break;
		}
		if(list.isEmpty())
			list.add("died");
		msg+=list.get((int) (Math.random()*list.size()));
		if(killer != null) {
			if(notByKiller)
				msg+=" while running from ";
			else
				msg+=" by ";
			msg+=killer.getColoredName()+ChatColor.RESET+".";
			killer.getPlayer().sendMessage(msg);
		} else
			msg+=".";
		p.getPlayer().sendMessage(msg);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onMove(PlayerMoveEvent event) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null)
			return;
		if(match.getGameState() != GameState.INGAME) {
			if(event.getTo().getBlockY() < match.getWorld().getMinHeight())
				match.respawnPlayer(GamePlayer.get(event.getPlayer()));
			return;
		}
		GamePlayer p = GamePlayer.get(event.getPlayer());
		GameTeam t = match.getTeam(p);
		if(t == null)
			return;
		Block b = p.getPlayer().getLocation().add(0,-1,0).getBlock();
		if(t.isSpawnBlock(b) && !p.isSpawnProtected()){
			p.setSpawnProtection();
		}
		for(GameTeam team : match.getTeams()) {
			if(team.equals(t))
				continue;
			if(team.isSpawnBlock(b)) {
				p.setCustomDamageCause(CustomDamageCause.ENEMY_SPAWN);
				p.getPlayer().damage(9999);
			}
		}
	}
	
	@EventHandler
	public void onGamemodeChange(PlayerGameModeChangeEvent event) {
		if(event.getNewGameMode() == GameMode.ADVENTURE) {
			event.getPlayer().setGameMode(GameMode.SURVIVAL);
			return;
		}
		GameMatch match = GameMatch.currentMatch;
		if(match == null)
			return;
		if(event.getNewGameMode() == GameMode.SURVIVAL) {
			match.respawnPlayer(GamePlayer.get(event.getPlayer()));
			return;
		}
		if(event.getNewGameMode() == GameMode.CREATIVE) {
			//idk
		}
			
	}
	
	@EventHandler
	public void onServerListPing(ServerListPingEvent e) {
		if(!GameConfig.displayServerListMotd)
			return;
		GameMatch match = GameMatch.currentMatch;
		if(match == null || !match.isLoaded()) {
			e.setMotd(GameConfig.serverName + " DeCTF2 "+GameMain.getInstance().getDescription().getVersion());
			return;
		}
		String status = (match.getGameState() == GameState.PREGAME ? "Pre-game" : "Post-game");
		String scores = "";
		if(match.getGameState() == GameState.INGAME) {
			status = "Time left: "+ChatColor.GOLD+match.getTimer().getTimeLeftDisplay()+" ";
			scores = ChatColor.WHITE+"Scores ";
			for(GameTeam team : match.getTeams()) {
				scores += team.getColor().getPrefix()+team.getScore()+"/"+match.getScoreToWin()+" ";
			}
		}
		
		String motd = "";
		motd+=ChatColor.WHITE+"Mode: "+ChatColor.GOLD+match.getGame().getGamemodeName()+ChatColor.WHITE+" - "+status+"\n";
		motd+=ChatColor.WHITE+"Map: "+ChatColor.GOLD+match.getMapName()+ChatColor.WHITE+" by "+ChatColor.GOLD+match.getMapAuthor()+" "+scores;
		e.setMotd(motd);
		
	}
	
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event) {
		if(event.getEntityType() == EntityType.CHICKEN && event.getSpawnReason() == SpawnReason.EGG)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onArrowHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Arrow) {
			Arrow a = (Arrow) event.getEntity();
			Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {if(!a.isDead()) a.remove();});
		}
	}
	
	static List<Material> dontTouchThat = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL, Material.ENDER_CHEST, 
			Material.FURNACE, Material.CRAFTING_TABLE, Material.HOPPER);
	@EventHandler(ignoreCancelled=true)
	public void onInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if(block == null || event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		if(dontTouchThat.contains(block.getType()) || block.getType().toString().contains("SHULKER_BOX") || block.getType().toString().contains("SIGN")) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if(event.getEntityType() == EntityType.PLAYER)
			event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onItemPickup(EntityPickupItemEvent event) {
		if(event.getEntityType() != EntityType.PLAYER)
			return;
		Player p = (Player) event.getEntity();
		if(p.getGameMode() == GameMode.CREATIVE)
			return;
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onItemDrop(PlayerDropItemEvent event) {
		if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		ChatColor color = ChatColor.WHITE;
		GamePlayer p = GamePlayer.get(event.getPlayer());
		if(p.getTeam() != null)
			color = p.getTeam().getColor().getChatColor();
		String prefix = "";
		if(GameMain.hasLuckPerms()) {
			prefix = LuckPermsStuff.getPrefix(p.getPlayer());
		}
		if(prefix == null)
			prefix = "";
		event.setFormat("§7"+prefix+"%1$s"+color+" §l» §r§6/"+(p.getChatType() == PlayerChatType.GLOBAL ? "a" : "t")+"§r %2$s");
		if(p.getChatType() == PlayerChatType.TEAM) {
			event.setCancelled(true);
			List<Player> recipients = new ArrayList<Player>();
			for(Player pl : Bukkit.getOnlinePlayers()) {
				GamePlayer gp = GamePlayer.get(pl);
				if(gp.getTeam() == null) {
					recipients.add(pl);
					continue;
				}
				if(p.getTeam() == null)
					continue;
				if(gp.getTeam().equals(p.getTeam()))
					recipients.add(pl);
			}
			String msg = "§7"+prefix+p.getPlayer().getDisplayName()+color+" §l» §r§6/t§r "+event.getMessage();
			for(Player pl : recipients) {
				pl.sendMessage(msg);
			}
			GameMain.getInstance().getLogger().info(msg);
		}
	}
}
