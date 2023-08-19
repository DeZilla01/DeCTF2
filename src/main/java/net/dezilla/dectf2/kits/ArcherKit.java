package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimPattern;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.ShieldUtil;

public class ArcherKit extends BaseKit{
	private static double HEADSHOT_DISTANCE = 30;
	
	private boolean crossbow = false;
	private boolean arbalist = false;

	public ArcherKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setHelmet(ItemBuilder.of(Material.CHAINMAIL_HELMET).name("Archer Helmet").unbreakable().armorTrim(TrimPattern.SHAPER, color().getTrimMaterial()).get());
		inv.setChestplate(ItemBuilder.of(Material.CHAINMAIL_CHESTPLATE).name("Archer Chestplate").unbreakable().get());
		inv.setLeggings(ItemBuilder.of(Material.CHAINMAIL_LEGGINGS).name("Archer Leggings").unbreakable().get());
		inv.setBoots(ItemBuilder.of(Material.CHAINMAIL_BOOTS).name("Archer Boots").unbreakable().get());
		if(crossbow)
			inv.setItemInOffHand(ItemBuilder.of(Material.CROSSBOW).name("Archer Crossbow").enchant(Enchantment.ARROW_KNOCKBACK, 1).unbreakable().get());
		else if(arbalist)
			inv.setItemInOffHand(ShieldUtil.getShield(player));
		else
			inv.setItemInOffHand(ItemBuilder.of(Material.BOW).name("Archer Bow").enchant(Enchantment.ARROW_KNOCKBACK, 1).unbreakable().get());
		if(arbalist) {
			inv.setItem(0, ItemBuilder.of(Material.STONE_SWORD).name("Archer Sword").enchant(Enchantment.DAMAGE_ALL, 1).unbreakable().get());
			inv.setItem(2, ItemBuilder.of(Material.CROSSBOW).name("Archer Crossbow").enchant(Enchantment.ARROW_DAMAGE, 1).unbreakable().get());
			inv.setItem(3, ItemBuilder.of(Material.CROSSBOW).name("Archer Crossbow").enchant(Enchantment.ARROW_DAMAGE, 1).unbreakable().get());
			inv.setItem(4, ItemBuilder.of(Material.CROSSBOW).name("Archer Crossbow").enchant(Enchantment.ARROW_DAMAGE, 1).unbreakable().get());
		}
		else
			inv.setItem(0, ItemBuilder.of(Material.STONE_SWORD).name("Archer Sword").unbreakable().get());
		inv.setItem(9, ItemBuilder.of(Material.ARROW).amount(64).get());
		inv.setItem(10, ItemBuilder.of(Material.ARROW).amount(64).get());
		if(arbalist)
			inv.addItem(ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(4).get());
		else
			inv.addItem(ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(3).get());
	}
	
	@EventHandler
	public void onShot(ProjectileHitEvent event) {
		if(!arbalist && event.getEntity() instanceof Arrow && ((Arrow) event.getEntity()).getShooter().equals(player.getPlayer()) ) {
			if(event.getHitEntity()!= null && event.getHitEntity() instanceof Player) {
				GamePlayer victim = GamePlayer.get((Player) event.getHitEntity());
				if(victim.getTeam() != null && player.getTeam() != null && victim.getTeam().equals(player.getTeam()))
					return;
				if(player.getLocation().distance(victim.getLocation()) >= HEADSHOT_DISTANCE) {
					victim.setLastAttacker(player);
					victim.setCustomDamageCause(CustomDamageCause.ARCHER_HEADSHOT);
					victim.getPlayer().damage(99);
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Archer";
	}
	
	@Override
	public String getVariation() {
		if(crossbow)
			return "Crossbow";
		if(arbalist)
			return "Arbalist";
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.BOW);
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("crossbow")) 
			crossbow = true;
		if(variation.equalsIgnoreCase("arbalist"))
			arbalist = true;
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default", "crossbow", "arbalist"};
		return variations;
	}

}
