package net.dezilla.dectf2.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import net.dezilla.dectf2.structures.CannotBuildException;
import net.dezilla.dectf2.structures.PyroFire;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.Minion;

public class PyroKit extends BaseKit{
	static double PYRO_SPEED = .11;
	static double LIGHT_SPEED = .13;
	static int PYRO_BOW_FIRE = 100;
	static int PYRO_FRENZY_BOW_FIRE = 250;
	static double PYRO_FRENZY_MANA_DRAIN = .01;
	static double PYRO_FRENZY_MANA_GAIN_RATE = .02;
	static double LIGHT_MANA_USE = .08;
	static double LIGHT_MANA_RECHARGE = 0.003;
	
	boolean frenzy = false;
	boolean light = false;
	float frenzyMana = 0;
	float lightMana = 1;
	boolean frenzyMode = false;
	List<PyroFire> fires = new ArrayList<PyroFire>();
	List<Arrow> chargedArrows = new ArrayList<Arrow>();

	public PyroKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory(boolean resetStats) {
		super.setInventory(resetStats);
		PlayerInventory inv = player.getPlayer().getInventory();
		//armor
		inv.setHelmet(ItemBuilder.of(Material.LEATHER_HELMET).name("Pyro Helmet").unbreakable().armorTrim(TrimPattern.SHAPER, color().getTrimMaterial()).data("pyro_armor").get());
		inv.setChestplate(ItemBuilder.of(Material.IRON_CHESTPLATE).name("Pyro Chestplate").unbreakable().data("pyro_armor").get());
		inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).name("Pyro Leggings").unbreakable().armorTrim(TrimPattern.TIDE, color().getTrimMaterial()).data("pyro_armor").get());
		inv.setBoots(ItemBuilder.of(Material.LEATHER_BOOTS).name("Pyro Boots").unbreakable().armorTrim(TrimPattern.TIDE, color().getTrimMaterial()).data("pyro_armor").get());
		//items
		if(!light)
			inv.setItemInOffHand(ItemBuilder.of(Material.BOW).name("Pyro Bow").unbreakable().enchant(Enchantment.ARROW_INFINITE, 1).get());
		if(frenzy)
			inv.setItem(0, ItemBuilder.of(Material.DIAMOND_AXE).unbreakable().name("Pyro Axe").damageModifier(7.5).data("pyro_axe").get());
		else if(light)
			inv.setItem(0, ItemBuilder.of(Material.IRON_AXE).unbreakable().name("Pyro Axe").damageModifier(3).get());
		else
			inv.setItem(0, ItemBuilder.of(Material.GOLDEN_AXE).unbreakable().name("Pyro Axe").damageModifier(4.5).get());
		if(light)
			inv.setItemInOffHand(ItemBuilder.of(Material.FLINT_AND_STEEL).name("Pyro Flint & Steel").data("light_flint").unbreakable().get());
		else
			inv.setItem(2, ItemBuilder.of(Material.FLINT_AND_STEEL).name("Pyro Flint & Steel").data("pyro_flint").unbreakable().get());
		if(!light)
			inv.setItem(9, new ItemStack(Material.ARROW));
		inv.addItem(ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(3).get());
		addToolItems();
		player.applyInvSave();
		if(resetStats) {
			frenzyMana = 0;
			player.getPlayer().setCooldown(Material.FLINT_AND_STEEL, 0);
		}
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
		if(light) {
			lightMana+=LIGHT_MANA_RECHARGE;
			if(lightMana>1)
				lightMana=1;
			player.getPlayer().setExp(lightMana);
		}
	}
	
	@EventHandler
	public void onShoot(EntityShootBowEvent event) {
		if(!(event.getEntity() instanceof Player))
			return;
		Player p = (Player) event.getEntity();
		if(!p.equals(player.getPlayer()))
			return;
		if(event.getForce()==1 && event.getProjectile() instanceof Arrow) {
			chargedArrows.add((Arrow) event.getProjectile());
		}
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getItem() != null && event.getItem().getType() == Material.FLINT_AND_STEEL) {
			event.setCancelled(true);
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getPlayer().getCooldown(Material.FLINT_AND_STEEL)==0) {
				Block block = event.getClickedBlock().getRelative(event.getBlockFace());
				if(ItemBuilder.dataMatch(event.getItem(), "pyro_flint")) {
					for(int x = -1 ; x <= 1 ; x++) {
						for(int z = -1 ; z <= 1 ; z++) {
							Block b = block.getLocation().add(x, 0, z).getBlock();
							if(b.getType() != Material.AIR)
								return;
							try {
								PyroFire f = new PyroFire(player, b.getLocation());
								fires.add(f);
							} catch(CannotBuildException e) {}
						}
					}
					player.getPlayer().setCooldown(Material.FLINT_AND_STEEL, 300);
				} else if(ItemBuilder.dataMatch(event.getItem(), "light_flint")) {
					if(lightMana>LIGHT_MANA_USE) {
						try {
							PyroFire f = new PyroFire(player, block.getLocation());
							fires.add(f);
							lightMana -= LIGHT_MANA_USE;
						}catch(CannotBuildException e) {}
					}
				}
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
		if(event.getDamager() instanceof Arrow) {
			GamePlayer p = Util.getOwner(event.getDamager());
			if(p != null && p.equals(player)) {
				if(frenzy && !frenzyMode) {
					frenzyMana+=event.getDamage()*PYRO_FRENZY_MANA_GAIN_RATE;
					if(frenzyMana>1)
						frenzyMana=1;
					player.getPlayer().setExp(frenzyMana);
				}
			}
		}
		if(!(event.getDamager() instanceof Player) || !((Player)event.getDamager()).equals(player.getPlayer()))
			return;
		if(!(event.getEntity() instanceof LivingEntity))
			return;
		LivingEntity victim = (LivingEntity) event.getEntity();
		if(!frenzy && victim.getFireTicks()>0 && !victim.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
			event.setDamage(event.getDamage()*3.5);
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
		if(event.getEntity() instanceof Arrow && chargedArrows.contains(event.getEntity())) {
			Location l = event.getEntity().getLocation();
			for(LivingEntity e : l.getWorld().getLivingEntities()) {
				if(e.getLocation().distance(l) > 4)
					continue;
				if(e.getType() == EntityType.PLAYER) {
					GamePlayer victim = GamePlayer.get((Player) e);
					if(sameTeam(victim))
						continue;
					float modifier = .5f;
					if(event.getHitEntity() != null && event.getHitEntity().equals(e))
						modifier = 1;
					setOnFire(e, modifier);
				} else {
					Minion m = Minion.get(e);
					if(m == null)
						continue;
					if(sameTeam(e))
						continue;
					float modifier = .5f;
					if(event.getHitEntity() != null && event.getHitEntity().equals(e))
						modifier = 1;
					setOnFire(e, modifier);
				}
			}
			l.getWorld().createExplosion(l, 0);
			chargedArrows.remove(event.getEntity());
		}
	}
	
	private void setOnFire(LivingEntity e, float modifier) {
		if(e.getType() == EntityType.PLAYER) {
			GamePlayer victim = GamePlayer.get((Player) e);
			victim.setLastAttacker(player);
		}
		final float M = modifier;
		final int fireTicks = (frenzy ? PYRO_FRENZY_BOW_FIRE : PYRO_BOW_FIRE);
		Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> e.setFireTicks((int) (M*fireTicks)));
	}
	
	public boolean isPyroFire(Block block) {
		List<PyroFire> toRemove = new ArrayList<PyroFire>();
		for(PyroFire f : fires) {
			if(f.isDead()) {
				toRemove.add(f);
				continue;
			}
			if(f.isStructure(block))
				return true;
		}
		for(PyroFire f : toRemove) {
			fires.remove(f);
		}
		return false;
	}
	
	private void updateFrenzyMode() {
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
		if(light)
			return "Light";
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.FLINT_AND_STEEL);
	}
	
	@Override
	public ItemStack getIcon(String variation) {
		if(variation.equalsIgnoreCase("frenzy"))
			return new ItemStack(Material.DIAMOND_AXE);
		if(variation.equalsIgnoreCase("light"))
			return new ItemStack(Material.IRON_AXE);
		return new ItemStack(Material.GOLDEN_AXE);
	}
	
	@Override
	public double getMovementSpeed() {
		if(light)
			return LIGHT_SPEED;
		return PYRO_SPEED;
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("frenzy")) 
			frenzy = true;
		else if(variation.equalsIgnoreCase("light"))
			light = true;
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default", "frenzy", "light"};
		return variations;
	}
	
	@Override
	public ItemStack[] getFancyDisplay() {
		return new ItemStack[] {
				new ItemStack(Material.NETHER_STAR),
				new ItemStack(Material.FLINT_AND_STEEL),
				new ItemStack(Material.FLINT_AND_STEEL),
				new ItemStack(Material.BOW),
				new ItemStack(Material.GOLDEN_AXE),
				new ItemStack(Material.BOW),
				new ItemStack(Material.FLINT_AND_STEEL),
				new ItemStack(Material.FLINT_AND_STEEL),
				new ItemStack(Material.NETHER_STAR)
		};
	}

}
