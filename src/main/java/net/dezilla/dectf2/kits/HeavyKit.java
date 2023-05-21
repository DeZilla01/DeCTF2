package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.dezilla.dectf2.GamePlayer;

public class HeavyKit extends BaseKit {

	public HeavyKit(GamePlayer player) {
		super(player);
	}

	@Override
	public void setInventory() {
		PlayerInventory inv = player.getInventory();
		inv.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
		inv.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		inv.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		inv.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		inv.setItem(0, new ItemStack(Material.DIAMOND_SWORD));
	}

}
