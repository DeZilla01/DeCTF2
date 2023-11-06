package net.dezilla.dectf2.kits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.structures.CannotBuildException;
import net.dezilla.dectf2.structures.IceBox;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.Minion;
import net.md_5.bungee.api.ChatColor;

public class MageKit extends BaseKit{
	private static double MAGE_DMG_SPELL_MODIFIER = .8;
	private static ItemStack MAGE_LIGHTNING_HEAD = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNkMTQ1NjFiYmQwNjNmNzA0MjRhOGFmY2MzN2JmZTljNzQ1NjJlYTM2ZjdiZmEzZjIzMjA2ODMwYzY0ZmFmMSJ9fX0=");
	private static ItemStack FROZEN_HEAD = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTI2NDQwNzFiNmM3YmJhZTdiNWU0NWQ5ZjgyZjk2ZmZiNWVlOGUxNzdhMjNiODI1YTQ0NjU2MDdmMWM5YyJ9fX0=");
	private static ItemStack INVERSION_HEAD = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFiOWE0OWYzYmM3YTViMGI5YzRmOTUxODgxMTYyYjM1ZTkwYjdlMTE1ZDI0NmVhZTZlZDBiMDk3ZWY3YmVhIn19fQ==");
	private static Map<String, Float> SPELL_REGENS = new HashMap<String, Float>();
	private static int LIGHTNING_DMG = 6;
	private static int ICE_BLAST_DURATION = 50;
	private static double ICE_BLAST_RADIUS = 2.8;
	private static int MINION_CAP = 3;
	private static int BLOOD_HIT_AMOUNT = 4;
	private static double SHULKER_DMG = 5;
	private static int INVERSION_DURATION = 40;
	private static int SELF_INVERSION_DURATION = 60;
	private static int CURE_AREA_DURATION = 30;
	private static float CURE_AREA_RADIUS = 2.5f;
	private static float WITHER_SWORD_USAGE = .025f;
	private static Color DEFAULT_COLOR = Color.fromRGB(204, 102, 255);
	private static Color DARK_COLOR = Color.PURPLE;
	private static Color MYSTIC_COLOR = Color.fromRGB(191, 255, 220);
	private static Color DRAGON_COLOR = Color.BLACK;
	
	static {
		SPELL_REGENS.put("dmg_spell", .066f);
		SPELL_REGENS.put("fire_spell", .02f);
		SPELL_REGENS.put("lightning_spell", .01f);
		SPELL_REGENS.put("freeze_spell", .015f);
		SPELL_REGENS.put("heal_spell", .00625f);
		SPELL_REGENS.put("dark_dagger", .005f);
		SPELL_REGENS.put("blood_spell", .0125f);
		SPELL_REGENS.put("ice_spell", .00625f);
		SPELL_REGENS.put("shadow_spell", .00833f);
		SPELL_REGENS.put("skeleton_spell", .005f);
		SPELL_REGENS.put("inversion_spell", .013f);
		SPELL_REGENS.put("shulker_spell", .066f);
		SPELL_REGENS.put("cure_spell", .00625f);
		SPELL_REGENS.put("thorn_spell", .005f);
	}
	
	
	private boolean dark = false;
	private boolean mystic = false;
	private boolean dragon = false;
	private Map<String, Float> charges = new HashMap<String, Float>();
	private List<Minion> minions = new ArrayList<Minion>();
	private boolean witherSword = false;
	private List<ShulkerBullet> bullets = new ArrayList<ShulkerBullet>();

	public MageKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		Color armorColor = DEFAULT_COLOR;
		if(dark)
			armorColor = DARK_COLOR;
		else if(mystic)
			armorColor = MYSTIC_COLOR;
		else if(dragon)
			armorColor = DRAGON_COLOR;
		inv.setChestplate(ItemBuilder.of(Material.LEATHER_CHESTPLATE).unbreakable().leatherColor(armorColor).armorTrim(TrimPattern.VEX, color().getTrimMaterial()).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).get());
		inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).unbreakable().leatherColor(armorColor).armorTrim(TrimPattern.VEX, color().getTrimMaterial()).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).get());
		inv.setBoots(ItemBuilder.of(Material.LEATHER_BOOTS).unbreakable().leatherColor(armorColor).armorTrim(TrimPattern.VEX, color().getTrimMaterial()).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).get());
		if(dark) {
			inv.setItem(0, ItemBuilder.of(Material.STONE_SWORD).data("dark_dagger").name("Mage Dagger").unbreakable().get());
			inv.setItem(1, ItemBuilder.of(Material.NETHERITE_HOE).data("blood_spell").name("Blood Leach Spell").unbreakable().get());
			inv.setItem(2, ItemBuilder.of(Material.IRON_HOE).data("ice_spell").name("Ice Blast").unbreakable().get());
			inv.setItem(3, ItemBuilder.of(Material.WOODEN_HOE).data("shadow_spell").name("Shadow Spell").unbreakable().get());
			inv.setItem(4, ItemBuilder.of(Material.STONE_HOE).data("skeleton_spell").name("Skeleton Summon").unbreakable().get());
		} else if(mystic) {
			inv.setItem(0, ItemBuilder.of(Material.DIAMOND_HOE).data("shulker_spell").name("Damage Spell").unbreakable().get());
			inv.setItem(1, ItemBuilder.of(Material.IRON_HOE).data("inversion_spell").name("Inversion Spell").unbreakable().get());
			inv.setItem(3, ItemBuilder.of(Material.NETHERITE_HOE).data("thorn_spell").name("Thorns Spell").unbreakable().get());
			inv.setItem(2, ItemBuilder.of(Material.GOLDEN_HOE).data("cure_spell").name("Cure Spell").unbreakable().get());
		} else if(dragon) {
			inv.setItem(0, ItemBuilder.of(Material.IRON_NUGGET).data("dragon_fireball_spell").name("Dragon Fireball Spell").unbreakable().get());
			inv.setItem(1, ItemBuilder.of(Material.IRON_NUGGET).data("dragon_breath_spell").name("Dragon Breath Spell").unbreakable().get());
			inv.setItem(2, ItemBuilder.of(Material.IRON_NUGGET).data("heal_crystal_spell").name("Heal Crystal Spell").unbreakable().get());
			//dragon fireball
			//dragon breath
			//crystal heal
		} else {
			inv.setItem(0, ItemBuilder.of(Material.DIAMOND_HOE).data("dmg_spell").name("Damage Spell").unbreakable().get());
			inv.setItem(1, ItemBuilder.of(Material.WOODEN_HOE).data("fire_spell").name("Flame Spell").unbreakable().get());
			inv.setItem(2, ItemBuilder.of(Material.STONE_HOE).data("lightning_spell").name("Lightning Spell").unbreakable().get());
			inv.setItem(3, ItemBuilder.of(Material.IRON_HOE).data("freeze_spell").name("Freeze Spell").unbreakable().get());
			inv.setItem(4, ItemBuilder.of(Material.GOLDEN_HOE).data("heal_spell").name("Heal Spell").unbreakable().get());
		}
		addToolItems();
		player.applyInvSave();
		killMinions();
	}
	
	@Override
	public void unregister() {
		super.unregister();
		killMinions();
	}
	
	@Override
	public void onTick() {
		for(ItemStack i : player.getPlayer().getInventory().getContents()) {
			if(i == null || i.getType() == Material.AIR)
				continue;
			String key = ItemBuilder.getData(i);
			if(key == null)
				continue;
			if(!SPELL_REGENS.containsKey(key))
				continue;
			if(!charges.containsKey(key))
				charges.put(key, 1f);
			float charge = charges.get(key);
			if(key.equals("dark_dagger") && witherSword) {
				charge-=WITHER_SWORD_USAGE;
				if(charge<=0) {
					charge = 0;
					witherSword = false;
					updateWitherSword();
				}
			} else if(key.equals("inversion_spell") && !Util.onGround(player.getPlayer())) {
				//nothing lol
			} else
				charge+=SPELL_REGENS.get(key);
			if(charge>1)
				charge = 1;
			charges.put(key, charge);
			ItemBuilder.of(i).unbreakable(charge == 1 ? true : false).durability(charge).get();
		}
		ItemStack i = player.getPlayer().getInventory().getItemInMainHand();
		float charge = 0;
		if(i != null && ItemBuilder.getData(i) != null) {
			String key = ItemBuilder.getData(i);
			if(charges.containsKey(key))
				charge = charges.get(key);
		}
		player.getPlayer().setExp(charge);
		List<ShulkerBullet> deadBullets = new ArrayList<ShulkerBullet>();
		for(ShulkerBullet b : bullets) {
			if(b.isDead()) {
				deadBullets.add(b);
				continue;
			}
			if(b.getTarget() != null) {
				Location target = b.getTarget().getLocation().add(0,.8,0);
				Vector v = Util.getVectorToLoc(b.getLocation(), target, .1);
				b.setVelocity(b.getVelocity().add(v));
				continue;
			}
			for(LivingEntity e : b.getLocation().getWorld().getLivingEntities()) {
				if(e.getLocation().distance(b.getLocation()) < 3) {
					if(e.getType() == EntityType.PLAYER) {
						GamePlayer p = GamePlayer.get((Player) e);
						if(sameTeam(p))
							continue;
						b.setTarget(e);
						break;
					}
					Minion m = Minion.get(e);
					if(m == null)
						continue;
					if(m.getTeam().equals(player.getTeam()))
						continue;
					b.setTarget(e);
					break;
				}
			}
		}
		while(bullets.size()>2) {
			bullets.get(0).remove();
			bullets.remove(0);
		}
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK){
			if(ItemBuilder.dataMatch(event.getItem(), "inversion_spell")) {
				float charge = charges.get("inversion_spell");
				if(charge<1)
					return;
				charges.put("inversion_spell", 0f);
				player.setInversionTicks(SELF_INVERSION_DURATION);
			}
		}
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(ItemBuilder.dataMatch(event.getItem(), "dmg_spell")) {
			float charge = charges.get("dmg_spell");
			if(charge<1)
				return;
			charges.put("dmg_spell", 0f);
			Arrow arrow = player.getPlayer().launchProjectile(Arrow.class, Util.inFront(player.getPlayer(), 4.5));
			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
				if(arrow != null && !arrow.isDead()) {
					dmgEffect(arrow.getLocation());
					arrow.remove();
				}
			}, 5);
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "fire_spell")) {
			float charge = charges.get("fire_spell");
			if(charge<1)
				return;
			charges.put("fire_spell", 0f);
			EnderPearl fireball = player.getPlayer().launchProjectile(EnderPearl.class, Util.inFront(player.getPlayer(), 1.5));
			fireball.setItem(new ItemStack(Material.FIRE_CHARGE));
			fireball.setFireTicks(999);
			fireball.setVisualFire(true);
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "lightning_spell")) {
			float charge = charges.get("lightning_spell");
			if(charge<1)
				return;
			charges.put("lightning_spell", 0f);
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				strike(event.getClickedBlock().getLocation());
				return;
			}
			Snowball light = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), 1.8));
			light.setItem(MAGE_LIGHTNING_HEAD.clone());
			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
				if(!light.isDead()) {
					strike(light.getLocation());
					light.remove();
				}
			}, 6);
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "freeze_spell")) {
			float charge = charges.get("freeze_spell");
			if(charge<1)
				return;
			charges.put("freeze_spell", 0f);
			Snowball freeze = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), 2));
			freeze.setItem(new ItemStack(Material.ICE));
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "heal_spell")) {
			float charge = charges.get("heal_spell");
			if(charge<1)
				return;
			charges.put("heal_spell", 0f);
			ThrownPotion pot = player.getPlayer().launchProjectile(ThrownPotion.class, Util.inFront(player.getPlayer(), .5));
			pot.setItem(ItemBuilder.of(Material.SPLASH_POTION).potionEffect(PotionEffectType.REGENERATION, 100, 3).potionColor(PotionEffectType.REGENERATION.getColor()).get());
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "blood_spell")) {
			float charge = charges.get("blood_spell");
			if(charge<1)
				return;
			charges.put("blood_spell", 0f);
			Snowball blood = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), 1.8));
			blood.setItem(new ItemStack(Material.REDSTONE_BLOCK));
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "ice_spell")) {
			float charge = charges.get("ice_spell");
			if(charge<1)
				return;
			charges.put("ice_spell", 0f);
			Snowball ice = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), 1.5));
			ice.setItem(new ItemStack(Material.BLUE_ICE));
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "shadow_spell")) {
			float charge = charges.get("shadow_spell");
			if(charge<1)
				return;
			charges.put("shadow_spell", 0f);
			Snowball shadow = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), 1.5));
			shadow.setItem(new ItemStack(Material.COAL_BLOCK));
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "skeleton_spell")) {
			float charge = charges.get("skeleton_spell");
			if(charge<1)
				return;
			charges.put("skeleton_spell", 0f);
			cleanMinionList();
			if(minions.size()>= MINION_CAP) {
				minions.get(0).remove();
				minions.remove(0);
			}
			Snowball skel = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), .5));
			skel.setItem(new ItemStack(Material.WITHER_SKELETON_SKULL));
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "inversion_spell")) {
			float charge = charges.get("inversion_spell");
			if(charge<1)
				return;
			charges.put("inversion_spell", 0f);
			Snowball inver = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), 2));
			inver.setItem(INVERSION_HEAD.clone());
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "shulker_spell")) {
			float charge = charges.get("shulker_spell");
			if(charge<1)
				return;
			charges.put("shulker_spell", 0f);
			ShulkerBullet b = player.getPlayer().launchProjectile(ShulkerBullet.class, Util.inFront(player.getPlayer(), 1));
			bullets.add(b);
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "cure_spell")) {
			float charge = charges.get("cure_spell");
			if(charge<1)
				return;
			charges.put("cure_spell", 0f);
			Snowball cure = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), .5));
			cure.setItem(new ItemStack(Material.TURTLE_EGG));
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "thorn_spell")) {
			float charge = charges.get("thorn_spell");
			if(charge<1)
				return;
			charges.put("thorn_spell", 0f);
			ThrownPotion pot = player.getPlayer().launchProjectile(ThrownPotion.class, Util.inFront(player.getPlayer(), .5));
			pot.setItem(ItemBuilder.of(Material.SPLASH_POTION).potionEffect(PotionEffectType.LUCK, 160, 0).potionColor(PotionEffectType.LUCK.getColor()).get());
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "dark_dagger")) {
			float charge = charges.get("dark_dagger");
			if(charge<1)
				return;
			witherSword = true;
			updateWitherSword();
		}
		else if(ItemBuilder.dataMatch(event.getItem(), "dragon_fireball_spell")) {
			player.getPlayer().launchProjectile(DragonFireball.class, Util.inFront(player.getPlayer(), 1));
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		GamePlayer p = Util.getOwner((Entity) event.getEntity().getShooter());
		if(p == null || !p.getPlayer().equals(player.getPlayer()))
			return;
		//dmg spell
		if(event.getEntityType() == EntityType.ARROW) {
			dmgEffect(event.getEntity().getLocation());
		}
		//flame spell
		if(event.getEntityType() == EntityType.ENDER_PEARL) {
			flameImpact(event.getEntity().getLocation());
		}
		//shulker bullet
		if(event.getEntityType() == EntityType.SHULKER_BULLET) {
			if(event.getHitEntity() == null)
				return;
			event.setCancelled(true);
			GamePlayer pl = null;
			Minion m = null;
			if(event.getHitEntity().getType() == EntityType.PLAYER)
				pl = GamePlayer.get((Player) event.getHitEntity());
			else if(event.getHitEntity() instanceof LivingEntity)
				m = Minion.get((LivingEntity) event.getHitEntity());
			if(m != null && m.getTeam().equals(player.getTeam()))
				return;
			else if(pl != null && sameTeam(pl))
				return;
			else if(pl != null && pl.isSpawnProtected())
				return;
			if(m != null) {
				m.getEntity().damage(SHULKER_DMG);
			} else if(pl != null) {
				pl.setCustomDamageCause(CustomDamageCause.MAGE_SHULKER);
				pl.setLastAttacker(player);
				pl.getPlayer().damage(SHULKER_DMG, player.getPlayer());
			}
		}
		if(event.getEntityType() == EntityType.SNOWBALL) {
			Snowball s = (Snowball) event.getEntity();
			//lightning spell
			if(s.getItem() != null && s.getItem().getType() == Material.PLAYER_HEAD && !mystic) {
				strike(s.getLocation());
			}
			//freeze spell
			else if(s.getItem() != null && s.getItem().getType() == Material.ICE && event.getHitEntity() != null && event.getHitEntity() instanceof LivingEntity) {
				LivingEntity e = (LivingEntity) event.getHitEntity();
				if(!canAttack(e))
					return;
				freeze(event.getHitEntity());
			}
			//blood leach
			else if(s.getItem() != null && s.getItem().getType() == Material.REDSTONE_BLOCK && event.getHitEntity() != null && event.getHitEntity() instanceof LivingEntity) {
				LivingEntity e = (LivingEntity) event.getHitEntity();
				Minion m = Minion.get(e);
				if(m != null && m.getOwner().equals(player)) {
					blood(e);
					return;
				}
				if(!canAttack(e))
					return;
				blood(e);
			}
			//ice blast
			else if(s.getItem() != null && s.getItem().getType() == Material.BLUE_ICE) {
				iceBlast(s.getLocation());
			}
			//shadow spell
			else if(s.getItem() != null && s.getItem().getType() == Material.COAL_BLOCK) {
				AreaEffectCloud c = (AreaEffectCloud) s.getLocation().getWorld().spawnEntity(s.getLocation(), EntityType.AREA_EFFECT_CLOUD);
				c.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1), false);
				c.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0), false);
				c.addCustomEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0), false);
				c.setSource(player.getPlayer());
				c.setRadius(2.5f);
				c.setDuration(100);
				c.setColor(Color.BLACK);
				s.getLocation().getWorld().playSound(s.getLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1, 1);
			}
			//skeleton
			else if(s.getItem() != null && s.getItem().getType() == Material.WITHER_SKELETON_SKULL) {
				Minion m = new Minion(EntityType.WITHER_SKELETON, player.getTeam(), s.getLocation(), player);
				m.setName(player.getColoredName()+ChatColor.RESET+"'s Skeleton");
				m.addEffect(PotionEffectType.SPEED, -1, 1);
				m.getEntity().setHealth(6);
				minions.add(m);
				m.getLocation().getWorld().playSound(m.getLocation(), Sound.ENTITY_WITHER_HURT, .4f, .5f);
			}
			//inversion
			else if(s.getItem() != null && s.getItem().getType() == Material.PLAYER_HEAD && mystic) {
				invert(s.getLocation());
			}
			//cure
			else if(s.getItem() != null && s.getItem().getType() == Material.TURTLE_EGG) {
				cure(s.getLocation());
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onEntityDmgEntity(EntityDamageByEntityEvent event) {
		GamePlayer p = Util.getOwner(event.getDamager());
		if(p == null || !p.equals(player))
			return;
		if(event.getEntity() instanceof Arrow) {
			event.setDamage(event.getDamage()*MAGE_DMG_SPELL_MODIFIER);
		}
		if(!event.isCancelled() && event.getEntity() instanceof LivingEntity && event.getCause() == DamageCause.ENTITY_ATTACK && witherSword && player.getPlayer().getInventory().getItemInMainHand() != null 
				&& player.getPlayer().getInventory().getItemInMainHand().getType() == Material.NETHERITE_SWORD) {
			LivingEntity e = (LivingEntity) event.getEntity();
			e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 120, 0));
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onTeleport(PlayerTeleportEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getCause() == TeleportCause.ENDER_PEARL)
			event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onItemDamage(PlayerItemDamageEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		event.setCancelled(true);
	}
	
	private void dmgEffect(Location location) {
		Firework f = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		FireworkMeta fm = f.getFireworkMeta();
		FireworkEffect e = FireworkEffect.builder().withColor(Color.PURPLE, color().getBukkitColor()).build();
		fm.addEffect(e);
		f.setFireworkMeta(fm);
		f.setShooter(player.getPlayer());
		f.detonate();
	}
	
	private void killMinions() {
		for(Minion m : minions) {
			if(!m.isDead())
				m.remove();
		}
		minions.clear();
	}
	
	private void cleanMinionList() {
		for(Minion m : minions) {
			if(m.isDead()) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> minions.remove(m));
			}
		}
	}
	
	private void flameImpact(Location location) {
		location.getWorld().createExplosion(location, 0);
		for(Player p : location.getWorld().getPlayers()) {
			if(p.getLocation().distance(location) < 3) {
				if(sameTeam(GamePlayer.get(p)))
					continue;
				p.setFireTicks(100);
			}
		}
	}
	
	private void strike(Location location) {
		location.getWorld().strikeLightningEffect(location);
		for(Player p : location.getWorld().getPlayers()) {
			GamePlayer bp = GamePlayer.get(p);
			if(bp.getTeam() == null || player.getTeam() == null || player.getTeam().equals(bp.getTeam()))
				continue;
			if(bp.getLocation().distance(location) > 2)
				continue;
			Location l = location.clone();
			l.setY(bp.getLocation().getY() - .2);
			Vector v = bp.getLocation().toVector().subtract(l.toVector()).normalize().multiply(2);
			bp.getPlayer().setVelocity(v);
			bp.setCustomDamageCause(CustomDamageCause.MAGE_LIGHTNING);
			bp.setLastAttacker(player);
			bp.getPlayer().damage(LIGHTNING_DMG);
		}
	}
	
	private void iceBlast(Location loc) {
		BlockData bData = Material.ICE.createBlockData();
		for(int i = 0; i < 10; i++) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
				if(Bukkit.getWorld(loc.getWorld().getName()) == null)
					return;
				for(int ii = 0; ii < 4; ii++) {
					double x = ((Math.random()*2)-1)+loc.getX();
					double y = ((Math.random()*2)-.5)+loc.getY();
					double z = ((Math.random()*2)-1)+loc.getZ();
					Location l = new Location(loc.getWorld(), x, y, z);
					loc.getWorld().spawnParticle(Particle.FALLING_DUST, l, 10, bData);
				}
			}, i);
		}
		for(int i = 0; i<3; i++) {
			loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1, 1);
		}
		List<LivingEntity> victims = new ArrayList<LivingEntity>();
		for(Player p : loc.getWorld().getPlayers()) {
			GamePlayer gp = GamePlayer.get(p);
			if(this.sameTeam(gp) || p.getLocation().distance(loc) > ICE_BLAST_RADIUS || gp.isSpawnProtected())
				continue;
			victims.add(p);
		}
		for(Entity e : loc.getWorld().getEntities()) {
			if(!(e instanceof LivingEntity))
				continue;
			if(e.getLocation().distance(loc) > ICE_BLAST_RADIUS)
				continue;
			Minion m = Minion.get((LivingEntity) e);
			if(m == null)
				continue;
			if(m.getTeam().equals(player.getTeam()))
				continue;
			victims.add((LivingEntity) e);
		}
		for(LivingEntity e : victims) {
			if(e.getType() == EntityType.PLAYER) {
				GamePlayer gp = GamePlayer.get((Player) e);
				gp.setFrozenTicks(ICE_BLAST_DURATION);
				continue;
			}
			e.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ICE_BLAST_DURATION, 50));
			e.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ICE_BLAST_DURATION, -5));
			e.setFreezeTicks(ICE_BLAST_DURATION*2);
			EntityEquipment inv = e.getEquipment();
			ItemStack helmet = inv.getHelmet();
			ItemStack chestplate = inv.getChestplate();
			ItemStack leggings = inv.getLeggings();
			ItemStack boots = inv.getBoots();
			inv.setHelmet(FROZEN_HEAD.clone());
			inv.setChestplate(ItemBuilder.of(Material.LEATHER_CHESTPLATE).unbreakable().leatherColor(Color.fromRGB(106, 142, 203)).armorTrim(TrimPattern.RAISER, TrimMaterial.IRON).get());
			inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).unbreakable().leatherColor(Color.fromRGB(106, 142, 203)).armorTrim(TrimPattern.RAISER, TrimMaterial.IRON).get());
			inv.setBoots(ItemBuilder.of(Material.LEATHER_BOOTS).unbreakable().leatherColor(Color.fromRGB(106, 142, 203)).armorTrim(TrimPattern.RAISER, TrimMaterial.IRON).get());
			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
				if(e.isDead())
					return;
				/*if(e instanceof Player) {
					GamePlayer gp = GamePlayer.get((Player) e);
					if(gp.isSpawnProtected())
						return;
				}*/
				inv.setHelmet(helmet);
				inv.setChestplate(chestplate);
				inv.setLeggings(leggings);
				inv.setBoots(boots);
			}, ICE_BLAST_DURATION);
		}
	}
	
	private void freeze(Entity e) {
		if(!(e instanceof LivingEntity))
			return;
		try {
			new IceBox(player, (LivingEntity) e);
		} catch(CannotBuildException ex) {}
	}
	
	private void cure(Location loc) {
		AreaEffectCloud aec = (AreaEffectCloud) loc.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
		aec.setDuration(CURE_AREA_DURATION);
		aec.setRadius(CURE_AREA_RADIUS);
		aec.setColor(Color.fromRGB(191, 255, 220));
		loc.getWorld().playSound(loc, Sound.ENTITY_ALLAY_ITEM_TAKEN, 1, .3f);
		GameTimer timer = new GameTimer(-1);
		timer.unpause();
		timer.onTick((t) -> {
			if(t.getTicks()>CURE_AREA_DURATION) {
				t.unregister();
				return;
			}
			for(Player p : loc.getWorld().getPlayers()) {
				if(p.getLocation().distance(loc)>CURE_AREA_RADIUS)
					continue;
				if(p.getFireTicks()>0)
					p.setFireTicks(0);
				for(PotionEffect e : p.getActivePotionEffects()) {
					if(Util.isBadEffect(e.getType()))
						p.removePotionEffect(e.getType());
				}
				GamePlayer gp = GamePlayer.get(p);
				if(gp.getInversionTicks()>0)
					gp.setInversionTicks(0);
				if(gp.getFrozenTicks()>0)
					gp.setFrozenTicks(0);
				if(!p.hasPotionEffect(PotionEffectType.REGENERATION)) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 1));
				}
			}
		});
	}
	
	private void invert(Location l) {
		for(Player p : l.getWorld().getPlayers()) {
			GamePlayer victim = GamePlayer.get(p);
			if(sameTeam(victim) || victim.isSpawnProtected())
				continue;
			if(victim.getLocation().distance(l) < 2.5)
				victim.setInversionTicks(INVERSION_DURATION);
		}
		l.getWorld().playSound(l, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, .5f);
	}
	
	private void blood(LivingEntity e) {
		if(e instanceof Player) {
			GamePlayer p = GamePlayer.get((Player) e);
			for(int i = 0; i <=(BLOOD_HIT_AMOUNT-1)*11; i+=11) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
					if(!p.getPlayer().isOnline() || p.getPlayer().isDead() || !player.getPlayer().isOnline() || player.getPlayer().isDead() || p.isSpawnProtected())
						return;
					p.setCustomDamageCause(CustomDamageCause.MAGE_BLOOD_LEACH);
					p.setLastAttacker(player);
					p.getPlayer().damage(2);
					Util.heal(player.getPlayer(), 2);
					BlockData fallingDustData = Material.REDSTONE_BLOCK.createBlockData();
					for(double x = -.5; x<=.5; x++) {
						for(double z = -.5; z<=.5; z++) {
							p.getLocation().getWorld().spawnParticle(Particle.FALLING_DUST, p.getLocation().add(x,2,z), 10, fallingDustData);
						}
					}
				}, i);
			}
		} else {
			for(int i = 0; i <=(BLOOD_HIT_AMOUNT-1)*11; i+=11) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
					if(e.isDead() || !player.getPlayer().isOnline() || player.getPlayer().isDead())
						return;
					e.damage(2);
					Util.heal(player.getPlayer(), 2);
					BlockData fallingDustData = Material.REDSTONE_BLOCK.createBlockData();
					for(double x = -.5; x<=.5; x++) {
						for(double z = -.5; z<=.5; z++) {
							e.getLocation().getWorld().spawnParticle(Particle.FALLING_DUST, e.getLocation().add(x,2,z), 10, fallingDustData);
						}
					}
				}, i);
			}
		}
	}
	
	private void updateWitherSword() {
		PlayerInventory inv = player.getPlayer().getInventory();
		ItemStack sword = null;
		for(ItemStack i : inv.getContents()) {
			if(i == null)
				continue;
			if(ItemBuilder.dataMatch(i, "dark_dagger")) {
				sword = i;
				break;
			}
		}
		if(sword == null)
			return;
		if(witherSword)
			sword.setType(Material.NETHERITE_SWORD);
		else
			sword.setType(Material.STONE_SWORD);
		ItemBuilder.of(sword).unbreakable().get();
	}

	@Override
	public String getName() {
		return "Mage";
	}

	@Override
	public String getVariation() {
		if(dark)
			return "Dark";
		if(mystic)
			return "Mystic";
		if(dragon)
			return "Dragon";
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.DIAMOND_HOE);
	}
	
	@Override
	public ItemStack getIcon(String variation) {
		if(variation.equalsIgnoreCase("dark"))
			return ItemBuilder.of(Material.LEATHER_CHESTPLATE).leatherColor(DARK_COLOR).get();
		if(variation.equalsIgnoreCase("mystic"))
			return ItemBuilder.of(Material.LEATHER_CHESTPLATE).leatherColor(MYSTIC_COLOR).get();
		if(variation.equalsIgnoreCase("dragon"))
			return ItemBuilder.of(Material.LEATHER_CHESTPLATE).leatherColor(DRAGON_COLOR).get();
		return ItemBuilder.of(Material.LEATHER_CHESTPLATE).leatherColor(DEFAULT_COLOR).get();
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("dark"))
			dark = true;
		if(variation.equalsIgnoreCase("mystic"))
			mystic = true;
		if(variation.equalsIgnoreCase("dragon"))
			dragon = true;
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default", "dark", "mystic"};
		return variations;
	}
	
	@Override
	public ItemStack[] getFancyDisplay() {
		return new ItemStack[] {
				new ItemStack(Material.NETHER_STAR),
				new ItemStack(Material.NETHERITE_HOE),
				new ItemStack(Material.WOODEN_HOE),
				new ItemStack(Material.GOLDEN_HOE),
				new ItemStack(Material.DIAMOND_HOE),
				new ItemStack(Material.IRON_HOE),
				new ItemStack(Material.STONE_HOE),
				new ItemStack(Material.STONE_SWORD),
				new ItemStack(Material.NETHER_STAR)
		};
	}

}
