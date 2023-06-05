package net.dezilla.dectf2.listeners;

import java.util.Arrays;
import java.util.List;

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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.util.DamageCause;

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
			DamageCause d = new DamageCause(victim, damager, damager.getPlayer().getInventory().getItemInMainHand());
			victim.setLastDamage(d);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent event) {
		GameMatch match = GameMatch.currentMatch;
		if(match==null)
			return;
		GamePlayer p = GamePlayer.get(event.getEntity());
		p.incrementStats("deaths", 1);
		event.setDeathMessage(null);
		if(p.getLastDamage()!= null) {
			event.setDeathMessage(p.getLastDamage().getDeathMessage());
			p.getLastDamage().getDamager().incrementStats("kills", 1);
			p.getLastDamage().getDamager().incrementStats("streak", 1);
			p.setLastDamage(null);
		}
		p.setDeathLocation(p.getLocation());
		match.respawnPlayer(p);
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
				p.getPlayer().damage(999);
				p.getPlayer().sendMessage("Don't walk in the enemy's spawn, dumbass");
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
