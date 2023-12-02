package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Banner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.tdm.TDMGame;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameColor;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.Minion;

public abstract class BaseKit implements Listener{
	protected static double defaultAttackSpeed = 40;
	protected static double defaultMovementSpeed = .12;
	private int tickTaskId = -1;
	private boolean unregistered = true;
	private boolean armorLocked = true;
	
	GamePlayer player;
	
	public abstract String getName();
	
	public abstract String getVariation();
	
	public abstract ItemStack getIcon();
	
	public ItemStack getIcon(String variation) {
		return getIcon();
	}
	
	public abstract String[] getVariations();
	
	public void setVariation(String variation) {};
	
	public abstract ItemStack[] getFancyDisplay();
	
	public BaseKit(GamePlayer player) {
		this.player = player;
		//player can be null, mainly to access kit name, icons, desc, etc...
		if(player != null) {
			register();
		}
	}
	
	public void register() {
		if(unregistered) {
			Bukkit.getServer().getPluginManager().registerEvents(this, GameMain.getInstance());
			tickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> {
				if(!player.getPlayer().isOnline() || player.getPlayer().isDead() || !player.getKit().equals(this)) {
					unregister();
					return;
				}
				onTick();
			}, 1, 1);
			unregistered = false;
		}
	}
	
	public void onTick() {
		
	}
	
	public void unregister() {
		if(!unregistered) {
			HandlerList.unregisterAll(this);
			Bukkit.getScheduler().cancelTask(tickTaskId);
			unregistered = true;
		}
	}
	
	public boolean isUnregistered() {
		return unregistered;
	}
	
	public void setInventory(boolean resetStats) {
		register();
		player.getPlayer().getInventory().clear();
		player.getPlayer().setItemOnCursor(null);
		setAttributes();
		setEffects();
		setLevel();
	}
	
	public void setAttributes() {
		player.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(getAttackSpeed());
		player.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getMovementSpeed());
	}
	
	public void setEffects() {
		for(PotionEffect e : player.getPlayer().getActivePotionEffects())
			player.getPlayer().removePotionEffect(e.getType());
		if(player.isInvisible())
			player.setInvisible(false);
	}
	
	public void setLevel() {
		player.getPlayer().setExp(0);
		player.getPlayer().setLevel(0);
	}
	
	public double getAttackSpeed() {
		return defaultAttackSpeed;
	}
	
	public double getMovementSpeed() {
		return defaultMovementSpeed;
	}
	
	public int getStealDelay() {
		return GameConfig.stealDelay;
	}
	
	public void updateColor() {
		GameColor color = GameColor.WHITE;
		GameTeam team = player.getTeam();
		if(team != null)
			color = team.getColor();
		for(ItemStack i : player.getPlayer().getInventory().getContents()) {
			if(i == null || i.getType() == Material.AIR)
				continue;
			ItemMeta m = i.getItemMeta();
			if(m instanceof ArmorMeta) {
				ArmorMeta a = (ArmorMeta) m;
				if(a.hasTrim()) {
					a.setTrim(new ArmorTrim(color.getTrimMaterial(), a.getTrim().getPattern()));
					i.setItemMeta(a);
				}
			}
			if(m instanceof BlockStateMeta) {
				BlockStateMeta b = (BlockStateMeta) m;
				if(b.getBlockState() instanceof Banner) {
					Banner ba = (Banner) b.getBlockState();
					ba.setBaseColor(color.dyeColor());
					b.setBlockState(ba);
					i.setItemMeta(b);
				}
			}
		}
	}
	
	protected void addToolItems() {
		PlayerInventory inv = player.getPlayer().getInventory();
		if(player.useTracker() && GameMatch.currentMatch != null && GameMatch.currentMatch.getGame().hasObjectiveLocations()) {
			inv.addItem(ItemBuilder.of(Material.COMPASS).name("Pointing to "+player.getObjectiveLocationName()).data("objective_tracker").get());
		}
		if(player.usePointer()) {
			inv.addItem(ItemBuilder.of(Material.GOLDEN_CARROT).name("Pointer").data("pointer").get());
		}
		if(player.useSpyglass()) {
			inv.addItem(ItemBuilder.of(Material.SPYGLASS).name("Spyglass").get());
		}
		if(player.useKitSelector()) {
			inv.addItem(ItemBuilder.of(Material.NETHER_STAR).name("Kit Selector").data("kit_selector").get());
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onArrowPassthrough(ProjectileHitEvent event) {
		GamePlayer gp = Util.getOwner(event.getEntity());
		if(gp == null || !gp.equals(player))
			return;
		if(event.getHitEntity() == null)
			return;
		GamePlayer hit = Util.getOwner(event.getHitEntity());
		if(hit == null || !sameTeam(hit))
			return;
		event.setCancelled(true);
		Location l = event.getEntity().getLocation().add(event.getEntity().getVelocity());
		Projectile p = (Projectile) l.getWorld().spawnEntity(l, event.getEntityType());
		p.setVelocity(event.getEntity().getVelocity());
		p.setShooter(player.getPlayer());
		event.getEntity().remove();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onSteakUse(PlayerInteractEvent event) {
		Player p = player.getPlayer();
		if(!event.getPlayer().equals(p))
			return;
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			//Steak
			if(event.getItem()!=null && event.getItem().getType() == GameConfig.foodMaterial) {
				event.setCancelled(true);
				double currentHp = p.getHealth();
				double maxHp = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				if(currentHp == maxHp)
					return;
				ItemStack item = event.getItem();
				if(item.getAmount()==1) {
					
					if(item.equals(p.getInventory().getItemInOffHand())) {
						p.getInventory().setItemInOffHand(null);
					}
					else
						p.getInventory().remove(item);
				}
				else
					item.setAmount(item.getAmount()-1);
				if(currentHp+8>=maxHp)
					p.setHealth(maxHp);
				else
					p.setHealth(currentHp+8);
				p.playSound(p, Sound.ENTITY_PLAYER_BURP, 1, 1);
			}
		}
	}
	
	@EventHandler
	public void onTrackerUse(PlayerInteractEvent event) {
		Player p = player.getPlayer();
		if(!event.getPlayer().equals(p))
			return;
		if(!ItemBuilder.dataMatch(event.getItem(), "objective_tracker"))
			return;
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			player.toggleObjectiveLocation();
			ItemBuilder.of(event.getItem()).name("Pointing to "+player.getObjectiveLocationName()).get();
		}
	}
	
	long lastAttack = 0;
	@EventHandler
	public void onDamageEntity(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player) || !((Player) event.getEntity()).equals(player.getPlayer()))
			return;
		long now = GameMain.getServerTick();
		ItemStack offhand = player.getPlayer().getEquipment().getItemInOffHand();
		if(event.getDamager() != null && player.getPlayer().isHandRaised() && offhand != null && offhand.getType() == Material.SHIELD && event.getCause() != DamageCause.CUSTOM && now-lastAttack >= 10) {
			event.setCancelled(true);
			GamePlayer killer = Util.getOwner(event.getDamager());
			if(killer != null)
				player.setLastAttacker(killer);
			player.setCustomDamageCause(CustomDamageCause.SHIELDED_DAMAGE);
			double dmg = event.getDamage() - (Util.getDamageReduced(player.getPlayer())*event.getDamage());
			player.getPlayer().damage(dmg*.5);
			killer.incrementDamageDealt(dmg*.5);
			if(GameMatch.currentMatch != null && killer.getTeam() != null && GameMatch.currentMatch.getGame() instanceof TDMGame) {
				TDMGame game = (TDMGame) GameMatch.currentMatch.getGame();
				game.addDamage(killer.getTeam(), dmg*.5);
			}
			Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {
				player.getPlayer().setVelocity(Util.getKnockback(player.getPlayer(), event.getDamager()));
				player.getPlayer().playSound(player.getPlayer(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
			});
		}
		if(now-lastAttack >= 10)
			lastAttack = now;
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onTouchTheArmor(InventoryClickEvent event) {
		if(event.getSlotType() == SlotType.ARMOR && armorLocked) {
			event.setCancelled(true);
		}
	}
	
	protected GameColor color() {
		if(player != null) {
			GameTeam team = player.getTeam();
			if(team != null)
				return team.getColor();
		}
		return GameColor.WHITE;
	}
	
	protected boolean canAttack(LivingEntity entity) {
		if(entity instanceof Player) {
			GamePlayer bp = GamePlayer.get((Player) entity);
			if(bp.getTeam() != null && player.getTeam() != null && bp.getTeam().equals(player.getTeam()))
				return false;
			if(bp.isSpawnProtected())
				return false;
		} else {
			Minion m = Minion.get(entity);
			if(m == null)
				return false;
			if(m.getTeam() != null && player.getTeam() != null && m.getTeam().equals(player.getTeam()))
				return false;
		}
		return true;
	}
	
	protected boolean sameTeam(GamePlayer p) {
		return (p.getTeam() != null && player.getTeam() != null && p.getTeam().equals(player.getTeam()));
	}
	
	protected boolean sameTeam(LivingEntity e) {
		if(e.getType() == EntityType.PLAYER)
			return sameTeam(GamePlayer.get((Player) e));
		Minion m = Minion.get(e);
		if(m == null)
			return false;
		return m.getTeam().equals(player.getTeam());
	}

}
