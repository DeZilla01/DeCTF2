package net.dezilla.dectf2.kits;

import java.sql.Timestamp;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.structures.CannotBuildException;
import net.dezilla.dectf2.structures.MedicWeb;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.Minion;
import net.dezilla.dectf2.util.ShieldUtil;

public class MedicKit extends BaseKit{
	private static int MEDIC_WEB_REGEN = 120;
	private static int MEDIC_HP_REGEN = 120;
	private static int MEDIC_WEB_AMOUNT = 8;
	private static int MEDIC_RANGED_WEB_AMOUNT = 4;
	private static int MEDIC_HEAL_COOLDOWN = 15; //in seconds
	private static ItemStack webItem = ItemBuilder.of(Material.SNOWBALL).name("Medic Web").data("medicweb").get();
	
	private boolean ranged = false;
	private int ticksWebRegen = MEDIC_WEB_REGEN;
	private int ticksHpRegen = MEDIC_HP_REGEN;

	public MedicKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory(boolean resetStats) {
		super.setInventory(resetStats);
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setHelmet(ItemBuilder.of(Material.GOLDEN_HELMET).name("Medic Helmet").unbreakable().armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
		inv.setChestplate(ItemBuilder.of(Material.GOLDEN_CHESTPLATE).name("Medic Chestplate").unbreakable().armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		inv.setLeggings(ItemBuilder.of(Material.GOLDEN_LEGGINGS).name("Medic Leggings").unbreakable().armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
		inv.setBoots(ItemBuilder.of(Material.GOLDEN_BOOTS).name("Medic Boots").unbreakable().armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
		
		if(ranged) {
			inv.setItem(0, ItemBuilder.of(Material.CROSSBOW).unbreakable().enchant(Enchantment.QUICK_CHARGE, 1).name("Medic Crossbow").get());
			inv.setItem(1, ItemBuilder.of(Material.GOLDEN_SWORD).unbreakable().name("Medic Sword").data("heal_sword").get());
			inv.setItem(2, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(3).get());
			inv.setItem(3, ItemBuilder.of(webItem.clone()).amount(MEDIC_RANGED_WEB_AMOUNT).get());
			inv.setItem(4, ItemBuilder.of(Material.ARROW).amount(64).get());
		}
		else {
			inv.setItem(0, ItemBuilder.of(Material.GOLDEN_SWORD).unbreakable().name("Medic Sword").data("heal_sword").get());
			inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(6).get());
			inv.setItem(2, ItemBuilder.of(webItem.clone()).amount(MEDIC_WEB_AMOUNT).get());
			inv.setItemInOffHand(ShieldUtil.getShield(player));
		}
		addToolItems();
		player.applyInvSave();
	}
	
	@Override
	public void onTick() {
		if(GameMatch.currentMatch == null || GameMatch.currentMatch.getGameState() != GameState.INGAME)
			return;
		int amount = 0;
		for(ItemStack i : player.getPlayer().getInventory().getContents()) {
			if(i == null || i.getType() == Material.AIR)
				continue;
			if(ItemBuilder.getData(i) != null && ItemBuilder.getData(i).equals("medicweb")) {
				amount += i.getAmount();
			}
		}
		if((amount<MEDIC_WEB_AMOUNT && !ranged) || (ranged && amount<MEDIC_RANGED_WEB_AMOUNT)) {
			if(ticksWebRegen<=0) {
				player.getPlayer().getInventory().addItem(webItem.clone());
				ticksWebRegen = MEDIC_WEB_REGEN;
			}
			else
				ticksWebRegen--;
		} else
			ticksWebRegen = MEDIC_WEB_REGEN;
		//passive regen
		double max = player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		if(player.getPlayer().getHealth() >= max)
			ticksHpRegen = MEDIC_HP_REGEN;
		else if (ticksHpRegen <= 0){
			if(player.getPlayer().getHealth()+1 >= max)
				player.getPlayer().setHealth(max);
			else
				player.getPlayer().setHealth(player.getPlayer().getHealth()+1);
			ticksHpRegen = MEDIC_HP_REGEN;
		} else
			ticksHpRegen--;
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getItem() != null && event.getItem().getType() == Material.SNOWBALL && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if(player.getPlayer().getCooldown(Material.SNOWBALL) == 0) {
				Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> player.getPlayer().setCooldown(Material.SNOWBALL, 5));
			}
		}
	}
	
	@EventHandler
	public void onArrowHit(EntityDamageByEntityEvent event) {
		if(!ranged || event.getDamager().getType() != EntityType.ARROW)
			return;
		GamePlayer p = Util.getOwner(event.getDamager());
		if(p == null || !p.equals(player) || !(event.getEntity() instanceof LivingEntity))
			return;
		Minion m = Minion.get((LivingEntity) event.getEntity());
		if(m != null) {
			if(m.getTeam().equals(player.getTeam()))
				m.heal(8);
			return;
		}
		if(event.getEntity().getType() != EntityType.PLAYER)
			return;
		GamePlayer target = GamePlayer.get((Player) event.getEntity());
		if(target.getTeam().equals(player.getTeam()))
			Util.heal(target.getPlayer(), 8);
	}
	
	@EventHandler
	public void onHitOtherPlayer(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player) || !((Player)event.getDamager()).equals(player.getPlayer()))
			return;
		if(!(event.getEntity() instanceof Player))
			return;
		if(player.getPlayer().getInventory().getItemInMainHand() == null || !ItemBuilder.dataMatch(player.getPlayer().getInventory().getItemInMainHand(), "heal_sword"))
			return;
		GamePlayer target = GamePlayer.get((Player) event.getEntity());
		if(target.getTeam() != null && player.getTeam() != null && target.getTeam().equals(player.getTeam())) {
			Timestamp now = new Timestamp(new Date().getTime());
			if(target.isFullHp()) {
				if(Util.regenPlayer(target))
					player.getPlayer().playSound(player.getPlayer(), Sound.ENTITY_SLIME_HURT_SMALL, 1, 1);
				else
					player.notify(target.getName()+"'s inventory is on cooldown. ("+target.getRegenTickDelay()/20+" seconds)");
			}
			else if(now.getTime() - target.getLastHeal().getTime() >= MEDIC_HEAL_COOLDOWN*1000) {
				target.setCustomDamageCause(CustomDamageCause.MEDIC_HEAL);
				target.getPlayer().damage(0);
				target.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 4));
				target.setLastHeal(now);
			}
			else {
				long timeLeft = (MEDIC_HEAL_COOLDOWN*1000)-(now.getTime()-target.getLastHeal().getTime());
				timeLeft /= 1000;
				player.notify(target.getName()+" cannot be healed for another "+timeLeft+" seconds.");
			}
		}
	}
	
	@EventHandler
	public void onWebHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Snowball) {
			Snowball s = (Snowball) event.getEntity();
			if(s.getShooter()!= null && s.getShooter().equals(player.getPlayer())) {
				Block b = event.getEntity().getLocation().getBlock();
				if(event.getHitBlock() != null)
					b = event.getHitBlock().getRelative(event.getHitBlockFace());
				try {
					new MedicWeb(player, b.getLocation());
				} catch(CannotBuildException e) {}
			}
		}
	}
	
	@Override
	public void setEffects() {
		super.setEffects();
		player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
		player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, PotionEffect.INFINITE_DURATION, 0));
	}

	@Override
	public String getName() {
		return "Medic";
	}
	@Override
	public String getVariation() {
		if(ranged)
			return "Ranged";
		return "Default";
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("ranged"))
			ranged = true;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.GOLDEN_SWORD);
	}
	
	@Override
	public ItemStack getIcon(String variation) {
		if(variation.equalsIgnoreCase("ranged"))
			return new ItemStack(Material.CROSSBOW);
		return getIcon();
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default", "ranged"};
		return variations;
	}
	
	@Override
	public ItemStack[] getFancyDisplay() {
		return new ItemStack[] {
				new ItemStack(Material.NETHER_STAR),
				new ItemStack(Material.GOLDEN_CHESTPLATE),
				new ItemStack(Material.COBWEB),
				new ItemStack(Material.GOLDEN_HELMET),
				new ItemStack(Material.GOLDEN_SWORD),
				new ItemStack(Material.GOLDEN_HELMET),
				new ItemStack(Material.COBWEB),
				new ItemStack(Material.GOLDEN_CHESTPLATE),
				new ItemStack(Material.NETHER_STAR)
		};
	}

}
