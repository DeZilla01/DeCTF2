package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.ctf.CTFFlag;
import net.dezilla.dectf2.game.ctf.CTFGame;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.ShieldUtil;

public class SoldierKit extends BaseKit {
	private static float CLIMB_EXP_COST = .1f;
	private static float CLIMB_RECHARGE_PER_TICK = .003f;
	
	private static float CANNON_RECHARGE_PER_TICK = .003f;
	
	private static float SWORD_RECHARGE_PER_TICK = .005f;
	
	
	private boolean cannon = false;
	private boolean brawler = false;
	private float exp = 1f;
	private float sword1 = 1f;
	private float sword2 = 1f;
	private boolean airborne = false;

	public SoldierKit(GamePlayer player) {
		super(player);
	}

	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.clear();
		inv.setHelmet(ItemBuilder.of(Material.IRON_HELMET).name("Soldier Helmet").unbreakable().armorTrim(TrimPattern.SPIRE, color().getTrimMaterial()).get());
		inv.setChestplate(ItemBuilder.of(Material.IRON_CHESTPLATE).name("Soldier Chestplate").unbreakable().get());
		inv.setLeggings(ItemBuilder.of(Material.IRON_LEGGINGS).name("Soldier Leggings").unbreakable().get());
		inv.setBoots(ItemBuilder.of(Material.IRON_BOOTS).name("Soldier Boots").unbreakable().get());
		if(cannon) {
			inv.setItem(0,  ItemBuilder.of(Material.IRON_SWORD).name("Soldier Sword").data("sword1").unbreakable().get());
			inv.setItem(1,  ItemBuilder.of(Material.IRON_SWORD).name("Soldier Sword").data("sword2").unbreakable().get());
		} else  if(brawler){
			inv.setItem(0, ItemBuilder.of(Material.NETHERITE_SWORD).name("Soldier Sword").unbreakable().get());
		} else {
			inv.setItem(0, ItemBuilder.of(Material.IRON_SWORD).name("Soldier Sword").unbreakable().get());
		}
		inv.addItem(ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(3).get());
		inv.setItemInOffHand(ShieldUtil.getShield(player));
		exp = 1f;
		sword1 = 1f;
		sword2 = 1f;
	}
	
	@Override
	public void setLevel() {
		player.getPlayer().setExp(exp);
		player.getPlayer().setLevel(0);
	}
	
	@Override
	public void onTick() {
		//default
		if(!cannon) {
			if(exp < 1) {
				if(!brawler)
					exp+=CLIMB_RECHARGE_PER_TICK;
				else
					exp+=SWORD_RECHARGE_PER_TICK;
				if(exp>1)
					exp=1f;
				player.getPlayer().setExp(exp);
			}
		}
		else if(cannon) {
			if(sword1 < 1) {
				sword1+=CANNON_RECHARGE_PER_TICK;
				if(sword1>1)
					sword1=1f;
			}
			if(sword2 < 1) {
				sword2+=CANNON_RECHARGE_PER_TICK;
				if(sword2>1)
					sword2=1f;
			}
			ItemStack inHand = player.getPlayer().getInventory().getItemInMainHand();
			if(inHand != null && inHand.getType() != Material.AIR && ItemBuilder.getData(inHand) != null && ItemBuilder.getData(inHand).equals("sword1")) {
				if(player.getPlayer().isSneaking()) {
					sword1+=CANNON_RECHARGE_PER_TICK*2;
					if(sword1>1)
						sword1=1f;
				}
				player.getPlayer().setExp(sword1);
			}
			else if(inHand != null && inHand.getType() != Material.AIR && ItemBuilder.getData(inHand) != null && ItemBuilder.getData(inHand).equals("sword2")) {
				if(player.getPlayer().isSneaking()) {
					sword2+=CANNON_RECHARGE_PER_TICK*2;
					if(sword2>1)
						sword2=1f;
				}
				player.getPlayer().setExp(sword2);
			} else
				player.getPlayer().setExp(0);
			if(airborne) {
				if(player.getPlayer().isOnGround() || player.getLocation().add(0,-.1,0).getBlock().getType() != Material.AIR)
					airborne = false;
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onHitOtherPlayer(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player) || !((Player)event.getDamager()).equals(player.getPlayer()))
			return;
		if(!cannon)
			return;
		if(airborne && player.getPlayer().getVelocity().getY()<0) {
			event.setDamage(event.getDamage()*3);
			player.getPlayer().playSound(player.getPlayer(), Sound.BLOCK_ANVIL_LAND, 1, 1);
		}
	}
	
	@EventHandler
	public void onSwordUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.COAL_ORE)
			return;
		//default
		if(!cannon && !brawler && event.getItem() != null && event.getItem().getType() == Material.IRON_SWORD && event.getClickedBlock() != null &&
				event.getAction() == Action.RIGHT_CLICK_BLOCK && exp >= CLIMB_EXP_COST) {
			event.getPlayer().setVelocity(new Vector(0,.9,0));
			exp -= CLIMB_EXP_COST;
		}
		//brawler
		if(brawler && event.getItem() != null && event.getItem().getType() == Material.NETHERITE_SWORD && event.getClickedBlock() != null && 
				event.getAction() == Action.RIGHT_CLICK_BLOCK && exp >=1) {
			event.getPlayer().setVelocity(new Vector(0,1.2,0));
			exp = 0;
		}
		//cannon
		if(cannon && event.getItem() != null && ItemBuilder.getData(event.getItem()) != null && ItemBuilder.getData(event.getItem()).startsWith("sword") && 
				event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			double multiplyer = 1;
			if(GameMatch.currentMatch != null && GameMatch.currentMatch.getGame() instanceof CTFGame) {
				CTFGame game = (CTFGame) GameMatch.currentMatch.getGame();
				CTFFlag flag = game.getHeldFlag(player);
				if(flag != null)
					multiplyer = .6;
			}
			final double M = multiplyer;
			if(ItemBuilder.getData(event.getItem()).equals("sword1") && sword1 > .1) {
				player.getPlayer().getWorld().createExplosion(player.getLocation(), 0);
				Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {
					Vector v = Util.inFront(player.getPlayer(), 2.0*sword1).multiply(-M);
					player.getPlayer().setVelocity(player.getPlayer().getVelocity().add(v));
					Bukkit.getScheduler().runTaskLater(GameMain.getInstance(), () -> airborne=true, 1);
					sword1 = 0;
				});
			}
			if(ItemBuilder.getData(event.getItem()).equals("sword2") && sword2 > .1) {
				player.getPlayer().getWorld().createExplosion(player.getLocation(), 0);
				Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {
					Vector v = Util.inFront(player.getPlayer(), 2.0*sword2).multiply(-M);
					player.getPlayer().setVelocity(player.getPlayer().getVelocity().add(v));
					Bukkit.getScheduler().runTaskLater(GameMain.getInstance(), () -> airborne=true, 1);
					sword2 = 0;
				});
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onFallDmg(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player) || !((Player) event.getEntity()).equals(player.getPlayer()))
			return;
		if(event.getCause() == DamageCause.FALL)
			event.setCancelled(true);
	}
	
	@Override
	public String getName() {
		return "Soldier";
	}
	
	@Override
	public String getVariation() {
		if(cannon)
			return "Cannon";
		if(brawler)
			return "Brawler";
		return "Default";
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("cannon")) 
			cannon = true;
		if(variation.equalsIgnoreCase("brawler"))
			brawler = true;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.IRON_SWORD);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default", "cannon", "brawler"};
		return variations;
	}
	
}
