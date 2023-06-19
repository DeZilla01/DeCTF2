package net.dezilla.dectf2.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.util.InventoryRunnable;

public class GuiItem {
	
	private static NamespacedKey key = new NamespacedKey(GameMain.getInstance(), "guiitem");
	private static int ids = Integer.MIN_VALUE;
	
	public static NamespacedKey getKey() {return key;}
	
	private ItemStack item;
	private InventoryRunnable runnable= null;
	private int id;
	
	public GuiItem(ItemStack item) {
		id = ids++;
		this.item = item.clone();
		ItemMeta meta = this.item.getItemMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, id);
		this.item.setItemMeta(meta);
		if(ids == Integer.MAX_VALUE)
			ids = Integer.MIN_VALUE;
	}
	
	public int getId() {
		return id;
	}
	
	public GuiItem setRun(InventoryRunnable runnable) {
		this.runnable = runnable;
		return this;
	}
	
	public void run(InventoryClickEvent event) {
		if(runnable != null) {
			runnable.run(event);
		}
	}
	
	public GuiItem setItem(ItemStack item) {
		this.item = item.clone();
		ItemMeta meta = this.item.getItemMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, id);
		this.item.setItemMeta(meta);
		return this;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public boolean isDead() {
		if(item.getAmount()==0)
			return true;
		return false;
	}
}
