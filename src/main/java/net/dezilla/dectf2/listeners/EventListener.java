package net.dezilla.dectf2.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.GamePlayer.PlayerChatType;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.kits.PyroKit;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.LuckPermsStuff;
import net.dezilla.dectf2.util.Minion;
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
		p.refreshVersionProtocol();
		if(p.getVersionProtocol() == 47)
			p.getPlayer().sendMessage(ChatColor.RED+"Warning, 1.8.X is not properly supported. It is recommended that you use a more modern version as you will likely encounter issues.");
		else if(p.getVersionProtocol()>0 && p.getVersionProtocol() < 47) 
			p.getPlayer().sendMessage(ChatColor.RED+"Warning, 1.7.X is not properly supported. It is recommended that you use a more modern version as you will likely encounter issues.");
		Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> p.checkLabyUse(), 1);
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
			//team fire
			if(event.getCause() == DamageCause.FIRE_TICK || event.getCause() == DamageCause.FIRE) {
				if(victim.isFireImmune()) {
					event.setCancelled(true);
					victim.getPlayer().setFireTicks(1);
					return;
				}
				if(!victim.isOnFire() && event.getCause() == DamageCause.FIRE) { //this could probably be optimized, but I'm lazy
					for(Block b : Util.get4x4Blocks(victim.getLocation())) {
						if(b.getType() == Material.FIRE) {
							for(Player p : victim.getLocation().getWorld().getPlayers()) {
								GamePlayer bp = GamePlayer.get(p);
								if(bp.getTeam() == null || victim.getTeam() == null || !bp.getTeam().equals(victim.getTeam()))
									continue;
								if(bp.getKit() instanceof PyroKit) {
									PyroKit k = (PyroKit) bp.getKit();
									if(k.isPyroFire(b)) {
										event.setCancelled(true);
										break;
									}
								}
							}
						}
					}
				}
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
			
			victim.setLastAttacker(damager);
		}
		else if(event.getEntity() instanceof LivingEntity && damager != null) {
			Minion m = Minion.get((LivingEntity) event.getEntity());
			if(m == null)
				return;
			if(m.getOwner() != null && m.getOwner().equals(damager))
				return;
			if(m.getTeam().equals(damager.getTeam())) {
				event.setCancelled(true);
			}
		}
	}
	Map<LivingEntity, Long> lastThorn = new HashMap<LivingEntity, Long>();
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onThorns(EntityDamageByEntityEvent e) {
		//This listener will convert luck effect to thorns
		if(!(e.getEntity() instanceof LivingEntity) || e.getDamager() == null || !(e.getDamager() instanceof LivingEntity))
			return;
		LivingEntity entity = (LivingEntity) e.getEntity();
		if(!entity.hasPotionEffect(PotionEffectType.LUCK))
			return;
		LivingEntity damager = (LivingEntity) e.getDamager();
		//this is a lazy fix to avoid server crash
		if(lastThorn.containsKey(damager) && lastThorn.get(damager) == GameMain.getServerTick())
			return;
		if(lastThorn.containsKey(entity) && lastThorn.get(entity) == GameMain.getServerTick())
			return;
		lastThorn.put(damager, GameMain.getServerTick());
		lastThorn.put(entity, GameMain.getServerTick());
		//if(damager.hasPotionEffect(PotionEffectType.LUCK) && entity.hasPotionEffect(PotionEffectType.LUCK))
		//	return;
		PotionEffect pot = entity.getPotionEffect(PotionEffectType.LUCK);
		double dmg = 2;
		dmg+=pot.getAmplifier();
		damager.damage(dmg, entity);
		damager.playEffect(EntityEffect.THORNS_HURT);
		if(damager.getType() == EntityType.PLAYER)
			((Player) damager).playSound(damager, Sound.ENCHANT_THORNS_HIT, 1, 1);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent event) {
		GameMatch match = GameMatch.currentMatch;
		if(match==null)
			return;
		GamePlayer p = GamePlayer.get(event.getEntity());
		p.incrementStats("deaths", 1);
		p.setStats("streak", 0);
		p.resetInversionHistory();
		p.setSpawnProtection();
		GamePlayer killer = p.getLastAttacker();
		if(killer != null) {
			killer.incrementStats("kills", 1);
			killer.incrementStats("streak", 1);
		}
		p.setInversionTicks(-1);
		p.setFrozenTicks(-1);
		try {
			LivingEntity dummy = (LivingEntity) p.getLocation().getWorld().spawnEntity(p.getLocation(), GameConfig.dummyType);
			dummy.setCustomNameVisible(true);
			dummy.setCustomName(p.getColoredName());
			dummy.damage(999);
			dummy.getEquipment().setHelmet(p.getPlayer().getInventory().getHelmet());
			dummy.getEquipment().setChestplate(p.getPlayer().getInventory().getChestplate());
			dummy.getEquipment().setLeggings(p.getPlayer().getInventory().getLeggings());
			dummy.getEquipment().setBoots(p.getPlayer().getInventory().getBoots());
			dummy.getEquipment().setItemInMainHand(p.getPlayer().getInventory().getItemInMainHand());
			dummy.getEquipment().setItemInOffHand(p.getPlayer().getInventory().getItemInOffHand());
		} catch(Exception e) {
			GameMain.getInstance().getLogger().info(ChatColor.RED+"!!!Dummy type is not living entity, pls change that in configs");
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
			match.respawnPlayer(p);
			p.setSpawnProtection();
			p.setLastAttacker(null);
		}, 4);
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
	
	@EventHandler(ignoreCancelled=true)
	public void onEffect(EntityPotionEffectEvent event) {
		if(event.getAction() == EntityPotionEffectEvent.Action.REMOVED || event.getAction() == EntityPotionEffectEvent.Action.CLEARED || !Util.isBadEffect(event.getNewEffect().getType()))
			return;
		GamePlayer p = null;
		Minion m = null;
		if(event.getEntityType() == EntityType.PLAYER) 
			p = GamePlayer.get((Player) event.getEntity());
		else if(event.getEntity() instanceof LivingEntity)
			m = Minion.get((LivingEntity) event.getEntity());
		Entity source = null;
		Location l = event.getEntity().getLocation();
		//we need to find the source areaeffectcloud, finding the closest first
		if(event.getCause() == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD) {
			for(AreaEffectCloud e : l.getWorld().getEntitiesByClass(AreaEffectCloud.class)) {
				if(source == null) {
					source = e;
					continue;
				}
				if(e.getLocation().distance(l) < source.getLocation().distance(l))
					source = e;
			}
		}
		if(event.getCause() == EntityPotionEffectEvent.Cause.POTION_SPLASH) {
			for(ThrownPotion e : l.getWorld().getEntitiesByClass(ThrownPotion.class)) {
				if(source == null) {
					source = e;
					continue;
				}
				if(e.getLocation().distance(l) < source.getLocation().distance(l))
					source = e;
			}
		}
		//if none is found, then cancel
		if(source == null)
			return;
		//we assume closest aec is the source of the effect, check who threw that effect and cancel if its from same team
		ProjectileSource projSource = null;
		if(source.getType() == EntityType.AREA_EFFECT_CLOUD)
			projSource = ((AreaEffectCloud) source).getSource();
		else if(source instanceof Projectile)
			projSource = ((Projectile) source).getShooter();
		if(projSource instanceof Player) {
			GamePlayer caster = GamePlayer.get((Player) projSource);
			if(caster.getTeam() == null)
				return;
			//cancel effect if it's from same team
			if(m != null && m.getTeam().equals(caster.getTeam()))
				event.setCancelled(true);
			if(p != null && p.getTeam() != null && p.getTeam().equals(caster.getTeam()))
				event.setCancelled(true);
			return;
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
			Material.FURNACE, Material.CRAFTING_TABLE, Material.HOPPER, Material.DISPENSER, Material.DROPPER);
	@EventHandler(ignoreCancelled=true)
	public void onInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if(GameMatch.currentMatch != null && GameMatch.currentMatch.getGameState() == GameState.PREGAME && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
			return;
		}
		if(block == null || event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		if(dontTouchThat.contains(block.getType()) || block.getType().toString().contains("SHULKER_BOX") || block.getType().toString().contains("SIGN") || 
				block.getType().toString().endsWith("_BED") || block.getType().toString().startsWith("POTTED_")) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onToolUse(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(ItemBuilder.dataMatch(event.getItem(), "kit_selector")) {
				event.getPlayer().performCommand("kit");
				event.setCancelled(true);
				return;
			}
			if(ItemBuilder.dataMatch(event.getItem(), "switch_team")) {
				event.getPlayer().performCommand("switch");
				ItemStack is = event.getItem();
				is.setType(GamePlayer.get(event.getPlayer()).getTeam().getColor().wool());
				ItemBuilder.of(is).name("Switch Team").data("switch_team").get();
				event.setCancelled(true);
				return;
			}
			if(ItemBuilder.dataMatch(event.getItem(), "map_vote")) {
				event.getPlayer().performCommand("vote");
				event.setCancelled(true);
				return;
			}
			if(ItemBuilder.dataMatch(event.getItem(), "tool_select")) {
				event.getPlayer().performCommand("tools");
				event.setCancelled(true);
				return;
			}
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
	
	@EventHandler(ignoreCancelled=true)
	public void onHangBreak(HangingBreakByEntityEvent event) {
		if(GameMatch.currentMatch == null)
			return;
		if(event.getRemover().getType() == EntityType.PLAYER) {
			Player p = (Player) event.getRemover();
			if(p.getGameMode() == GameMode.CREATIVE)
				return;
		}
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBoom(EntityExplodeEvent event) {
		if(event.getEntityType() == EntityType.PRIMED_TNT)
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
