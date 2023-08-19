package net.dezilla.dectf2.util;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

import net.dezilla.dectf2.GameMain;
import net.md_5.bungee.api.ChatColor;

//Inspired by Brawl's ItemBuilder
public class ItemBuilder {
	private static NamespacedKey key = new NamespacedKey(GameMain.getInstance(), "data");
	
	public static ItemBuilder of(Material material) {
		return new ItemBuilder(material);
	}
	
	public static ItemBuilder of(ItemStack item) {
		return new ItemBuilder(item);
	}
	
	public static String getData(ItemStack item) {
		ItemMeta m = item.getItemMeta();
		return m.getPersistentDataContainer().get(key, PersistentDataType.STRING);
	}
	
	public static boolean dataMatch(ItemStack item, String data) {
		if(item == null || item.getType() == Material.AIR || getData(item) == null)
			return false;
		return getData(item).equals(data);
	}
	
	ItemStack item;
	ItemMeta meta;
	
	public ItemBuilder(Material material) {
		item = new ItemStack(material);
		meta = item.getItemMeta();
	}
	
	public ItemBuilder(ItemStack item) {
		this.item = item;
		meta = item.getItemMeta();
	}
	
	public ItemStack get() {
		item.setItemMeta(meta);
		return item;
	}
	
	public ItemBuilder name(String name) {
		meta.setDisplayName(ChatColor.RESET+name);
		return this;
	}
	
	public ItemBuilder desc(String desc) {
		meta.setLore(Arrays.asList(desc));
		return this;
	}
	
	public ItemBuilder desc(List<String> desc) {
		meta.setLore(desc);
		return this;
	}
	
	public ItemBuilder data(String data) {
		meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
		return this;
	}
	
	public ItemBuilder unbreakable() {
		meta.setUnbreakable(true);
		return this;
	}
	
	public ItemBuilder unbreakable(boolean value) {
		meta.setUnbreakable(value);
		return this;
	}
	
	public ItemBuilder amount(int amount) {
		item.setAmount(amount);
		return this;
	}
	
	public ItemBuilder damageModifier(double modifier) {
		//meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("test", modifier, AttributeModifier.Operation.ADD_NUMBER));
		meta.getAttributeModifiers(EquipmentSlot.HAND).put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("damage", modifier, AttributeModifier.Operation.ADD_NUMBER));
		return this;
	}
	
	public ItemBuilder enchant(Enchantment ench, int lvl) {
		if(meta.hasEnchant(ench))
			meta.removeEnchant(ench);
		meta.addEnchant(ench, lvl, true);
		return this;
	}
	
	public ItemBuilder unenchant(Enchantment ench) {
		if(meta.hasEnchant(ench))
			meta.removeEnchant(ench);
		return this;
	}
	
	public ItemBuilder armorTrim(TrimPattern pattern, TrimMaterial material) {
		if(meta instanceof ArmorMeta) {
			((ArmorMeta) meta).setTrim(new ArmorTrim(material, pattern));
		}
		return this;
	}
	
	public ItemBuilder leatherColor(Color color) {
		if(!(meta instanceof LeatherArmorMeta))
			return this;
		LeatherArmorMeta m = (LeatherArmorMeta) meta;
		m.setColor(color);
		meta = m;
		return this;
	}
	
	public ItemBuilder shieldColor(DyeColor dye) {
		if(!(meta instanceof BlockStateMeta))
			return this;
		BlockStateMeta bs = (BlockStateMeta) meta;
		if(!(bs.getBlockState() instanceof Banner))
			return this;
		Banner b = (Banner) bs.getBlockState();
		b.setBaseColor(dye);
		bs.setBlockState(b);
		meta = bs;
		return this;
	}
	
	public ItemBuilder shieldPatterns(List<Pattern> patterns) {
		if(!(meta instanceof BlockStateMeta))
			return this;
		BlockStateMeta bs = (BlockStateMeta) meta;
		if(!(bs.getBlockState() instanceof Banner))
			return this;
		Banner b = (Banner) bs.getBlockState();
		b.setPatterns(patterns);
		bs.setBlockState(b);
		meta = bs;
		return this;
	}

}
