package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.trim.TrimPattern;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.util.ItemBuilder;

public class MageKit extends BaseKit{
	private static double MAGE_DMG_SPELL_MODIFIER = .8;
	private static ItemStack MAGE_LIGHTNING_HEAD = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNkMTQ1NjFiYmQwNjNmNzA0MjRhOGFmY2MzN2JmZTljNzQ1NjJlYTM2ZjdiZmEzZjIzMjA2ODMwYzY0ZmFmMSJ9fX0=");

	public MageKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setChestplate(ItemBuilder.of(Material.LEATHER_CHESTPLATE).unbreakable().leatherColor(Color.PURPLE).armorTrim(TrimPattern.VEX, color().getTrimMaterial()).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).get());
		inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).unbreakable().leatherColor(Color.PURPLE).armorTrim(TrimPattern.VEX, color().getTrimMaterial()).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).get());
		inv.setBoots(ItemBuilder.of(Material.LEATHER_BOOTS).unbreakable().leatherColor(Color.PURPLE).armorTrim(TrimPattern.VEX, color().getTrimMaterial()).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).get());
		inv.setItem(0, ItemBuilder.of(Material.DIAMOND_HOE).data("dmg_spell").name("Damage Spell").unbreakable().get());
		inv.setItem(1, ItemBuilder.of(Material.WOODEN_HOE).data("fire_spell").name("Flame Spell").unbreakable().get());
		inv.setItem(2, ItemBuilder.of(Material.STONE_HOE).data("lightning_spell").name("Lightning Spell").unbreakable().get());
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(ItemBuilder.dataMatch(event.getItem(), "dmg_spell")) {
			Arrow arrow = player.getPlayer().launchProjectile(Arrow.class, Util.inFront(player.getPlayer(), 4.5));
			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
				if(arrow != null && !arrow.isDead()) {
					dmgEffect(arrow.getLocation());
					arrow.remove();
				}
			}, 5);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "fire_spell")) {
			LargeFireball fireball = player.getPlayer().launchProjectile(LargeFireball.class, Util.inFront(player.getPlayer(), 2));
			fireball.setGravity(true);
			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
				if(fireball != null && !fireball.isDead()) {
					flameImpact(fireball.getLocation());
					fireball.getLocation().getWorld().createExplosion(fireball.getLocation(), 0);
					fireball.remove();
				}
			}, 10);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "lightning_spell")) {
			Snowball light = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), .5));
			light.setItem(MAGE_LIGHTNING_HEAD.clone());
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
		if(event.getEntityType() == EntityType.FIREBALL) {
			flameImpact(event.getEntity().getLocation());
		}
		if(event.getEntityType() == EntityType.SNOWBALL) {
			Snowball s = (Snowball) event.getEntity();
			if(s.getItem() != null && s.getItem().getType() == Material.PLAYER_HEAD) {
				s.getLocation().getWorld().strikeLightningEffect(s.getLocation());
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onEntityDmgEntity(EntityDamageByEntityEvent event) {
		GamePlayer p = Util.getOwner(event.getEntity());
		if(p == null || !p.equals(player))
			return;
		if(event.getEntity() instanceof Arrow) {
			event.setDamage(event.getDamage()*MAGE_DMG_SPELL_MODIFIER);
		}
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
	
	private void flameImpact(Location location) {
		for(Player p : location.getWorld().getPlayers()) {
			if(p.getLocation().distance(location) < 3) {
				if(sameTeam(GamePlayer.get(p)))
					continue;
				p.setFireTicks(100);
			}
		}
	}

	@Override
	public String getName() {
		return "test1";
	}

	@Override
	public String getVariation() {
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.DIAMOND_HOE);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default"};
		return variations;
	}

}
