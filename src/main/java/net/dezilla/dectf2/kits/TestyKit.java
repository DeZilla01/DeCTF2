package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class TestyKit extends BaseKit{
	private static double attackSpeed = 40;
	private static double movementSpeed = .3;

	public TestyKit(GamePlayer player) {
		super(player);
	}

	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.clear();
		inv.setItem(0, ItemBuilder.of(Material.DEBUG_STICK).name("test").unbreakable().get());
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(64).get());
	}

	@Override
	public String getName() {
		return "Testy";
	}
	
	@Override
	public String getVariation() {
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.COMMAND_BLOCK);
	}

	@Override
	public void setAttributes() {
		player.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(attackSpeed);
		player.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(movementSpeed);
	}

	@Override
	public String[] getVariations() {
		return new String[] {"Default"};
	}

}
