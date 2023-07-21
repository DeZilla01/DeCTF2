package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class SoldierKit extends BaseKit {
	
	private boolean powerup = false;

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
		inv.setItem(0, ItemBuilder.of(Material.IRON_SWORD).name("Soldier Sword").unbreakable().get());
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(3).get());
		inv.setItemInOffHand(new ItemStack(Material.SHIELD));
	}
	
	@EventHandler
	public void onSwordUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getItem() != null && event.getItem().getType() == Material.IRON_SWORD && event.getClickedBlock() != null) {
			event.getPlayer().setVelocity(new Vector(0,.9,0));
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
	public void setEffects() {
		
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.IRON_SWORD);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default"};
		return variations;
	}
	
}
