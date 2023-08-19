package net.dezilla.dectf2.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimPattern;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.ItemBuilder;

public class MageKit extends BaseKit{

	public MageKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setChestplate(ItemBuilder.of(Material.LEATHER_CHESTPLATE).unbreakable().leatherColor(Color.PURPLE).armorTrim(TrimPattern.VEX, color().getTrimMaterial()).get());
		inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).unbreakable().leatherColor(Color.PURPLE).armorTrim(TrimPattern.VEX, color().getTrimMaterial()).get());
		inv.setBoots(ItemBuilder.of(Material.LEATHER_BOOTS).unbreakable().leatherColor(Color.PURPLE).armorTrim(TrimPattern.VEX, color().getTrimMaterial()).get());
		
	}

	@Override
	public String getName() {
		return "Mage";
	}

	@Override
	public String getVariation() {
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.DIAMOND_HOE);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default"};
		return variations;
	}

}
