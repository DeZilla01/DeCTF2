package net.dezilla.dectf2.util;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

import net.dezilla.dectf2.GameMain;

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
	
	public ItemBuilder armorTrim(TrimPattern pattern, TrimMaterial material) {
		if(meta instanceof ArmorMeta) {
			((ArmorMeta) meta).setTrim(new ArmorTrim(material, pattern));
		}
		return this;
	}

}
