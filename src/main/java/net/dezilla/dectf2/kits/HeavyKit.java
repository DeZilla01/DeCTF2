package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimPattern;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.ShieldUtil;

public class HeavyKit extends BaseKit {
	private static double powerupMovementSpeed = .08;
	private static double powerupGainPerTick = .005;
	private static double powerupSlowPerLevel = .0004;
	private static int powerupLevelCap = 8;
	
	private boolean powerup = false;
	private boolean tank = false;
	private ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD).name("Heavy Sword").unbreakable().get();
	private int level = 0;
	private float exp = 0;

	public HeavyKit(GamePlayer player) {
		super(player);
	}

	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.clear();
		if(tank) {
			sword = ItemBuilder.of(Material.STONE_SWORD).name("Heavy Sword").unbreakable().get();
			inv.setChestplate(ItemBuilder.of(Material.NETHERITE_CHESTPLATE).name("Heavy Chestplate").unbreakable().armorTrim(TrimPattern.HOST, color().getTrimMaterial()).get());
			inv.setLeggings(ItemBuilder.of(Material.NETHERITE_LEGGINGS).name("Heavy Leggings").unbreakable().armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		}
		else {
			inv.setChestplate(ItemBuilder.of(Material.DIAMOND_CHESTPLATE).name("Heavy Chestplate").unbreakable().armorTrim(TrimPattern.HOST, color().getTrimMaterial()).get());
			inv.setLeggings(ItemBuilder.of(Material.DIAMOND_LEGGINGS).name("Heavy Leggings").unbreakable().armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		}
		if(powerup) {
			inv.setHelmet(ItemBuilder.of(Material.CHAINMAIL_HELMET).name("Heavy Helmet").unbreakable().armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
			inv.setBoots(ItemBuilder.of(Material.CHAINMAIL_BOOTS).name("Heavy Boots").unbreakable().armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		} else {
			inv.setHelmet(ItemBuilder.of(Material.DIAMOND_HELMET).name("Heavy Helmet").unbreakable().armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
			inv.setBoots(ItemBuilder.of(Material.DIAMOND_BOOTS).name("Heavy Boots").unbreakable().armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		}
		inv.setItem(0, sword);
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(3).get());
		inv.setItemInOffHand(ShieldUtil.getShield(player));
		if(level != 0) {
			level /=2;
		}
		if(exp != 0) {
			exp=0;
		}
	}
	
	@Override
	public String getName() {
		return "Heavy";
	}
	
	@Override
	public String getVariation() {
		if(powerup)
			return "Powerup";
		if(tank)
			return "Tank";
		return "Default";
	}
	
	@Override
	public void onTick() {
		if(!powerup) 
			return;
		if(player.getPlayer().isSneaking() && level < powerupLevelCap) {
			exp += powerupGainPerTick - (powerupSlowPerLevel*level);
			if(exp>=1) {
				level++;
				exp=0;
				updateSword();
			}
		}
		else if(!player.getPlayer().isSneaking()) {
			if(level == 0 && exp == 0)
				return;
			exp -= powerupGainPerTick;
			if(exp < 0) {
				if(level != 0) {
					level--;
					exp=.9999f;
					updateSword();
				}
				else
					exp=0;
			}
		}
		player.getPlayer().setLevel(level);
		player.getPlayer().setExp(exp);
	}
	
	@Override
	public double getMovementSpeed() {
		if(powerup)
			return powerupMovementSpeed;
		else
			return defaultMovementSpeed;
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("powerup")) 
			powerup = true;
		if(variation.equalsIgnoreCase("tank"))
			tank = true;
	}
	
	private void updateSword() {
		if(sword.containsEnchantment(Enchantment.DAMAGE_ALL))
			sword.removeEnchantment(Enchantment.DAMAGE_ALL);
		if(level>0) {
			sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, level);
		}
		ItemStack[] is = player.getPlayer().getInventory().getContents();
		for(int i = 0; i < is.length ; i++) {
			if(is[i] == null)
				continue;
			if(is[i].getType() == sword.getType()) {
				player.getPlayer().getInventory().setItem(i, sword);
				break;
			}
		}
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.DIAMOND_SWORD);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default", "powerup", "tank"};
		return variations;
	}
	
	@Override
	public ItemStack[] getFancyDisplay() {
		return new ItemStack[] {
				new ItemStack(Material.DIAMOND_SWORD),
				new ItemStack(Material.DIAMOND_CHESTPLATE),
				new ItemStack(Material.DIAMOND_LEGGINGS),
				new ItemStack(Material.DIAMOND_HELMET),
				new ItemStack(GameConfig.foodMaterial),
				new ItemStack(Material.DIAMOND_BOOTS),
				new ItemStack(Material.DIAMOND_LEGGINGS),
				new ItemStack(Material.DIAMOND_CHESTPLATE),
				new ItemStack(Material.DIAMOND_SWORD)
		};
	}
	
}
