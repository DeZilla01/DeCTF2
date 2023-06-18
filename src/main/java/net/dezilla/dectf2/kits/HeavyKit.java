package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class HeavyKit extends BaseKit {

	public HeavyKit(GamePlayer player) {
		super(player);
	}

	@Override
	public void setInventory() {
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
	public ItemStack getIcon() {
		return new ItemStack(Material.DIAMOND_SWORD);
	}

}
