package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class PyroKit extends BaseKit{
	static double PYRO_SPEED = .11;
	static int PYRO_BOW_FIRE = 100;
	static int PYRO_FRENZY_BOW_FIRE = 200;
	static double PYRO_FRENZY_MANA_DRAIN = .01;
	static double PYRO_FRENZY_MANA_GAIN_RATE = .005;
	
	boolean frenzy = false;
	boolean bowCharged = false;
	float frenzyMana = 0;
	boolean frenzyMode = false;

	public PyroKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setHelmet(ItemBuilder.of(Material.LEATHER_HELMET).name("Pyro Helmet").unbreakable().armorTrim(TrimPattern.SHAPER, color().getTrimMaterial()).data("pyro_armor").get());
		inv.setChestplate(ItemBuilder.of(Material.IRON_CHESTPLATE).name("Pyro Chestplate").unbreakable().data("pyro_armor").get());
		inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).name("Pyro Leggings").unbreakable().armorTrim(TrimPattern.TIDE, color().getTrimMaterial()).data("pyro_armor").get());
		inv.setBoots(ItemBuilder.of(Material.LEATHER_BOOTS).name("Pyro Boots").unbreakable().armorTrim(TrimPattern.TIDE, color().getTrimMaterial()).data("pyro_armor").get());
		inv.setItemInOffHand(ItemBuilder.of(Material.BOW).name("Pyro Bow").unbreakable().enchant(Enchantment.ARROW_INFINITE, 1).get());
		if(frenzy)
			inv.setItem(0, ItemBuilder.of(Material.DIAMOND_AXE).unbreakable().name("Pyro Axe").damageModifier(9).data("pyro_axe").get());
		else
			inv.setItem(0, ItemBuilder.of(Material.GOLDEN_AXE).unbreakable().name("Pyro Axe").damageModifier(7).get());
		inv.setItem(1, ItemBuilder.of(Material.FLINT_AND_STEEL).name("Pyro Flint & Steel").unbreakable().get());
		inv.setItem(9, new ItemStack(Material.ARROW));
		inv.addItem(ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(3).get());
		frenzyMana = 0;
	}
	
	@Override
	public void onTick() {
		if(frenzy && frenzyMode) {
			frenzyMana-=PYRO_FRENZY_MANA_DRAIN;
			if(frenzyMana<0) {
				frenzyMode=false;
				frenzyMana=0;
				updateFrenzyMode();
			}
			player.getPlayer().setExp(frenzyMana);
		}
	}
	
	@EventHandler
	public void onShoot(EntityShootBowEvent event) {
		if(!(event.getEntity() instanceof Player))
			return;
		Player p = (Player) event.getEntity();
		if(!p.equals(player.getPlayer()))
			return;
		if(event.getForce()==1)
			bowCharged = true;
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getItem() != null && event.getItem().getType() == Material.FLINT_AND_STEEL) {
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getPlayer().getCooldown(Material.FLINT_AND_STEEL)==0) {
				Block b = event.getClickedBlock().getRelative(event.getBlockFace());
				if(b.getType() != Material.AIR)
					return;
				b.setType(Material.FIRE);
				GameTimer timer = new GameTimer(2);
				timer.unpause();
				timer.onEnd((t) -> {
					if(b.getType() == Material.FIRE)
						b.setType(Material.AIR);
					timer.unregister();
				});
				player.getPlayer().setCooldown(Material.FLINT_AND_STEEL, 20);
			}
		}
		if(frenzy && event.getItem() != null && event.getItem().getType() == Material.DIAMOND_AXE && !frenzyMode && frenzyMana==1 && player.getPlayer().isSneaking()) {
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
				frenzyMode = true;
				player.getLocation().getWorld().playSound(player.getPlayer(), Sound.ENTITY_BLAZE_DEATH, 1, 1);
				player.getLocation().getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 10);
				updateFrenzyMode();
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onHitOtherPlayer(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player) || !((Player)event.getDamager()).equals(player.getPlayer()))
			return;
		if(!(event.getEntity() instanceof LivingEntity))
			return;
		LivingEntity victim = (LivingEntity) event.getEntity();
		if(!frenzy && victim.getFireTicks()>0 && !victim.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
			event.setDamage(event.getDamage()*4);
		}
		else if(frenzy && frenzyMode) {
			if(victim.getFireTicks()>0 && !victim.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))
				event.setDamage(event.getDamage()*8);
			else {
				event.setDamage(event.getDamage()*1.5);
				victim.setFireTicks(100);
			}
		}
		else if(frenzy && !frenzyMode) {
			if(victim.getFireTicks()>0 && !victim.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))
				event.setDamage(event.getDamage()*1.5);
			frenzyMana+=event.getDamage()*PYRO_FRENZY_MANA_GAIN_RATE;
			if(frenzyMana>1)
				frenzyMana=1;
			player.getPlayer().setExp(frenzyMana);
		}
	}
	
	@EventHandler
	public void onArrowHit(ProjectileHitEvent event) {
		GamePlayer p = Util.getOwner((Entity) event.getEntity().getShooter());
		if(p == null || !p.getPlayer().equals(player.getPlayer()))
			return;
		if(bowCharged) {
			Location l = event.getEntity().getLocation();
			for(Player pl : l.getWorld().getPlayers()) {
				GamePlayer victim = GamePlayer.get(pl);
				if(victim.getTeam() != null && player.getTeam() != null && victim.getTeam().equals(player.getTeam()))
					continue;
				if(victim.getPlayer().getLocation().distance(l) > 4)
					continue;
				float modifier = .5f;
				if(event.getHitEntity() != null && event.getHitEntity().equals(pl))
					modifier = 1;
				victim.setLastAttacker(player);
				final float M = modifier;
				final int fireTicks = (frenzy ? PYRO_FRENZY_BOW_FIRE : PYRO_BOW_FIRE);
				Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> pl.setFireTicks((int) (M*fireTicks)));
			}
			l.getWorld().createExplosion(l, 0);
			bowCharged = false;
		}
	}
	
	private void updateFrenzyMode() {
		int c = 0;
		PlayerInventory inv = player.getPlayer().getInventory();
		for(ItemStack is : inv.getContents()) {
			if(is==null)
				continue;
			if(ItemBuilder.dataMatch(is, "pyro_axe")) {
				if(frenzyMode)
					ItemBuilder.of(is).enchant(Enchantment.DAMAGE_ALL, 1).get();
				else
					ItemBuilder.of(is).unenchant(Enchantment.DAMAGE_ALL).get();
			}
			if(ItemBuilder.dataMatch(is, "pyro_armor")) {
				if(frenzyMode)
					ItemBuilder.of(is).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).get();
				else
					ItemBuilder.of(is).unenchant(Enchantment.PROTECTION_ENVIRONMENTAL).get();
			}
			c++;
		}
	}

	@Override
	public String getName() {
		return "Pyro";
	}
	
	@Override
	public String getVariation() {
		if(frenzy)
			return "Frenzy";
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.FLINT_AND_STEEL);
	}
	
	@Override
	public double getMovementSpeed() {
		return PYRO_SPEED;
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("frenzy")) 
			frenzy = true;
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default", "frenzy"};
		return variations;
	}

}
