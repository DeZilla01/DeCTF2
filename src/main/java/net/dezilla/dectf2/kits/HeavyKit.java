package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameConfig;

public class HeavyKit extends BaseKit {

	public HeavyKit(GamePlayer player) {
		super(player);
	}

	@Override
	public void setInventory() {
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.clear();
		inv.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
		inv.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		inv.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		inv.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		inv.setItem(0, new ItemStack(Material.DIAMOND_SWORD));
		inv.setItem(1, new ItemStack(GameConfig.foodMaterial, 3));
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
