package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.dezilla.dectf2.GamePlayer;

public class TestyKit extends BaseKit{

	public TestyKit(GamePlayer player, int variation) {
		super(player, variation);
	}

	@Override
	public void setInventory() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Testy";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.COMMAND_BLOCK);
	}

	@Override
	public void setAttributes() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getVariations() {
		// TODO Auto-generated method stub
		return null;
	}

}
