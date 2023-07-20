package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimPattern;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class HeavyKit extends BaseKit {
	private static double attackSpeed = 40;
	private static double movementSpeed = .12;
	
	private boolean powerup = false;

	public HeavyKit(GamePlayer player) {
		super(player);
	}

	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.clear();
		inv.setHelmet(ItemBuilder.of(Material.DIAMOND_HELMET).name("Heavy Helmet").unbreakable().armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
		inv.setChestplate(ItemBuilder.of(Material.DIAMOND_CHESTPLATE).name("Heavy Chestplate").unbreakable().armorTrim(TrimPattern.HOST, color().getTrimMaterial()).get());
		inv.setLeggings(ItemBuilder.of(Material.DIAMOND_LEGGINGS).name("Heavy Leggings").unbreakable().armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		inv.setBoots(ItemBuilder.of(Material.DIAMOND_BOOTS).name("Heavy Boots").unbreakable().armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		inv.setItem(0, ItemBuilder.of(Material.DIAMOND_SWORD).name("Heavy Sword").unbreakable().get());
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Heavy Steak").amount(3).get());
		inv.setItemInOffHand(new ItemStack(Material.SHIELD));
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
