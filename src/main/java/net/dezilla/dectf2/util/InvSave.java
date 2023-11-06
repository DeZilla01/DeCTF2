package net.dezilla.dectf2.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.dezilla.dectf2.kits.BaseKit;

public class InvSave {
	Class<? extends BaseKit> kit;
	String variation;
	String[] datas;
	Material[] types;
	
	public InvSave(Class<? extends BaseKit> kit, String variation, PlayerInventory inventory) {
		this.kit = kit;
		this.variation = variation;
		ItemStack[] contents = inventory.getContents();
		this.datas = new String[contents.length];
		this.types = new Material[contents.length];
		int c = 0;
		for(ItemStack i : contents) {
			if(i == null) {
				c++;
				continue;
			}
			String data = ItemBuilder.getData(i);
			if(data != null)
				datas[c] = data;
			types[c] = i.getType();
			c++;
		}
	}
	
	public boolean isKit(Class<? extends BaseKit> kit) {
		return this.kit.equals(kit);
	}
	
	public Class<? extends BaseKit> getKit(){
		return kit;
	}
	
	public boolean isVariation(String var) {
		return variation.equalsIgnoreCase(var);
	}
	
	public String getVariation() {
		return variation;
	}
	
	public void apply(PlayerInventory inv) {
		ItemStack[] contents = inv.getContents().clone();
		inv.clear();
		for(int i = 0; i < contents.length; i++) {
			Material mat = types[i];
			String data = datas[i];
			if(mat == null)
				continue;
			ItemStack item = null;
			int slot = 0;
			int c = -1;
			for(ItemStack is : contents) {
				c++;
				if(is == null)
					continue;
				if(is.getType() != mat)
					continue;
				if(data != null && !ItemBuilder.dataMatch(is, data))
					continue;
				item = is;
				slot = c;
				break;
			}
			if(item == null)
				continue;
			inv.setItem(i, item);
			contents[slot] = null;
		}
		int c = -1;
		for(ItemStack i : contents) {
			c++;
			if(i == null)
				continue;
			if(inv.getItem(c) == null)
				inv.setItem(c, i);
			else
				inv.addItem(i);
		}
	}
}
