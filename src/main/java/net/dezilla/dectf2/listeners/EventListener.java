package net.dezilla.dectf2.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.md_5.bungee.api.ChatColor;

public class EventListener implements Listener{

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(GameMatch.currentMatch != null) {
			GameMatch.currentMatch.addPlayer(GamePlayer.get(event.getPlayer()));
		} else {
			GameMatch.waitingForNextMatch.add(GamePlayer.get(event.getPlayer()));
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onDamage(EntityDamageEvent event) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null)
			return;
		if(event.getEntityType() == EntityType.PLAYER) {
			if(match.getGameState() == GameState.PREGAME || match.getGameState() == GameState.POSTGAME)
				event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onDamageEntity(EntityDamageByEntityEvent event) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null || match.getGameState() != GameState.INGAME)
			return;
		if(event.getEntityType() == EntityType.PLAYER) {
			GamePlayer victim = GamePlayer.get((Player) event.getEntity());
			GameTeam victimTeam = match.getTeam(victim);
			//spawn protection
			if(victimTeam.isSpawnBlock(victim.getPlayer().getLocation().add(0,-1,0).getBlock())) {
				event.setCancelled(true);
				return;
			}
		}
		if(event.getEntityType() == EntityType.PLAYER && event.getDamager() instanceof Player) {
			GamePlayer victim = GamePlayer.get((Player) event.getEntity());
			GamePlayer damager = GamePlayer.get((Player) event.getDamager());
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
				break;
			case FIRE:
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
				break;
			case LAVA:
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
				break;
			case SONIC_BOOM:
				break;
			case STARVATION:
				break;
			case SUFFOCATION:
				break;
			case SUICIDE:
				break;
			case THORNS:
				break;
			case VOID:
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
		if(match == null || match.getGameState() != GameState.INGAME)
			return;
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
	
	@EventHandler(ignoreCancelled=true)
	public void onInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if(block == null || event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		List<Material> dontTouchThat = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL);
		if(dontTouchThat.contains(block.getType()) || block.getType().toString().contains("SHULKER_BOX")) {
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
}
