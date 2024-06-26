package net.dezilla.dectf2.kits;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.ShieldUtil;

public class ChemistKit extends BaseKit{
	static float MANA_REGEN = .00714f;
	
	float mana = 1;
	
	public ChemistKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory(boolean resetStats) {
		super.setInventory(resetStats);
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setHelmet(ItemBuilder.of(Material.LEATHER_HELMET).unbreakable().armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		inv.setChestplate(ItemBuilder.of(Material.GOLDEN_CHESTPLATE).unbreakable().armorTrim(TrimPattern.SILENCE, TrimMaterial.GOLD).get());
		inv.setLeggings(ItemBuilder.of(Material.GOLDEN_LEGGINGS).unbreakable().armorTrim(TrimPattern.SILENCE, TrimMaterial.GOLD).get());
		inv.setBoots(ItemBuilder.of(Material.LEATHER_BOOTS).unbreakable().armorTrim(TrimPattern.WILD, color().getTrimMaterial()).get());
		inv.setItem(0, ItemBuilder.of(Material.IRON_SWORD).unbreakable().name("Chemist Sword").get());
		inv.setItemInOffHand(ShieldUtil.getShield(player));
		inv.setItem(1, ItemBuilder.of(Material.SPLASH_POTION).potionEffect(PotionEffectType.HARM, 1, 0).potionColor(PotionEffectType.HARM.getColor()).name("Instant Damage").data("mana.5").amount(12).get());
		inv.setItem(2, ItemBuilder.of(Material.SPLASH_POTION).potionEffect(PotionEffectType.POISON, 120, 1).potionColor(PotionEffectType.POISON.getColor()).name("Poison II").data("mana.25").amount(8).get());
		inv.setItem(3, ItemBuilder.of(Material.SPLASH_POTION).potionEffect(PotionEffectType.JUMP, 100, 0).potionEffect(PotionEffectType.JUMP, 80, 1).potionColor(PotionEffectType.JUMP.getColor()).name("Jump Boost II").amount(3).get());
		inv.setItem(4, ItemBuilder.of(Material.SPLASH_POTION).potionEffect(PotionEffectType.FIRE_RESISTANCE, 3600, 0).potionEffect(PotionEffectType.INCREASE_DAMAGE, 3600, 0)
				.potionEffect(PotionEffectType.SPEED, 3600, 0).name("Buff Pot").potionColor(PotionEffectType.INCREASE_DAMAGE.getColor()).amount(5).get());
		inv.setItem(5, ItemBuilder.of(Material.SPLASH_POTION).potionEffect(PotionEffectType.HEAL, 0, 2).name("Instant Health III").potionColor(PotionEffectType.HEAL.getColor()).amount(5).get());
		inv.setItem(6, ItemBuilder.of(Material.SPLASH_POTION).potionEffect(PotionEffectType.REGENERATION, 320, 2).potionColor(PotionEffectType.REGENERATION.getColor()).name("Regeneration III").amount(5).get());
		addToolItems();
		player.applyInvSave();
		if(resetStats)
			mana = 1;
	}
	
	@Override
	public void onTick() {
		mana+=MANA_REGEN;
		if(mana>1)
			mana=1;
		player.getPlayer().setExp(mana);
	}
	
	@EventHandler
	public void onPotUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(event.getItem() == null || ItemBuilder.getData(event.getItem()) == null)
			return;
		String data = ItemBuilder.getData(event.getItem());
		if(data.startsWith("mana")) {
			try {
				float use = Float.parseFloat(data.replace("mana", ""));
				if(mana<use) {
					event.setCancelled(true);
				} else {
					mana-=use;
				}
			}catch(Exception e) {}
		}
	}

	@Override
	public String getName() {
		return "Chemist";
	}

	@Override
	public String getVariation() {
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.POTION);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default"};
		return variations;
	}
	
	@Override
	public ItemStack[] getFancyDisplay() {
		return new ItemStack[] {
				new ItemStack(Material.NETHER_STAR),
				ItemBuilder.of(Material.SPLASH_POTION).potionColor(PotionEffectType.HEAL.getColor()).get(),
				ItemBuilder.of(Material.SPLASH_POTION).potionColor(PotionEffectType.LUCK.getColor()).get(),
				ItemBuilder.of(Material.SPLASH_POTION).potionColor(PotionEffectType.FIRE_RESISTANCE.getColor()).get(),
				ItemBuilder.of(Material.SPLASH_POTION).potionColor(PotionEffectType.HARM.getColor()).get(),
				ItemBuilder.of(Material.SPLASH_POTION).potionColor(PotionEffectType.DAMAGE_RESISTANCE.getColor()).get(),
				ItemBuilder.of(Material.SPLASH_POTION).potionColor(PotionEffectType.CONDUIT_POWER.getColor()).get(),
				ItemBuilder.of(Material.SPLASH_POTION).potionColor(PotionEffectType.ABSORPTION.getColor()).get(),
				new ItemStack(Material.NETHER_STAR)
		};
	}

}
