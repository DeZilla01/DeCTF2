package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class EngineerKit extends BaseKit{

	public EngineerKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setHelmet(ItemBuilder.of(Material.IRON_HELMET).unbreakable().name("Engineer Helmet").get());
		inv.setChestplate(ItemBuilder.of(Material.LEATHER_CHESTPLATE).unbreakable().name("Engineer Chestplate").get());
		inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).unbreakable().name("Engineer Leggings").get());
		inv.setBoots(ItemBuilder.of(Material.IRON_BOOTS).unbreakable().name("Engineer Boots").get());
		inv.setItem(0, ItemBuilder.of(Material.DIAMOND_PICKAXE).unbreakable().name("Engineer Pickaxe").enchant(Enchantment.DAMAGE_ALL, 0).get());
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(4).get());
		inv.setItem(2, ItemBuilder.of(Material.WOODEN_SWORD).name("Engineer Sword").unbreakable().get());
		inv.setItem(3, ItemBuilder.of(Material.DISPENSER).name("Turret").get());
		inv.setItem(4, ItemBuilder.of(Material.CAKE).name("Dispenser").get());
		inv.setItem(5, ItemBuilder.of(Material.STONE_PRESSURE_PLATE).name("Entrance").get());
		inv.setItem(6, ItemBuilder.of(Material.LIGHT_WEIGHTED_PRESSURE_PLATE).name("Exit").get());
	}

	@Override
	public String getName() {
		return "Engineer";
	}

	@Override
	public String getVariation() {
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.DISPENSER);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default"};
		return variations;
	}

}
