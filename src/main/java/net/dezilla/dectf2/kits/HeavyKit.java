package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class HeavyKit extends BaseKit {
	private static double attackSpeed = 40;
	private static double movementSpeed = .09;
	
	private int variation = 0;

	public HeavyKit(GamePlayer player, int variation) {
		super(player, variation);
		this.variation = variation;
	}

	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.clear();
		inv.setHelmet(ItemBuilder.of(Material.DIAMOND_HELMET).name("Heavy Helmet").unbreakable().get());
		inv.setChestplate(ItemBuilder.of(Material.DIAMOND_CHESTPLATE).name("Heavy Chestplate").unbreakable().get());
		inv.setLeggings(ItemBuilder.of(Material.DIAMOND_LEGGINGS).name("Heavy Leggings").unbreakable().get());
		inv.setBoots(ItemBuilder.of(Material.DIAMOND_BOOTS).name("Heavy Boots").unbreakable().get());
		inv.setItem(0, ItemBuilder.of(Material.DIAMOND_SWORD).name("Heavy Sword").unbreakable().get());
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Heavy Steak").amount(3).get());
	}
	
	@Override
	public String getName() {
		return "Heavy";
	}
	
	@Override
	public void setAttributes() {
		player.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(attackSpeed);
		player.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(movementSpeed);
	}
	
	@Override
	public void setEffects() {
		
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.DIAMOND_SWORD);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"Default", "Powerup"};
		return variations;
	}
}
