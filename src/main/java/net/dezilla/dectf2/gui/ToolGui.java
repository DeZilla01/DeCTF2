package net.dezilla.dectf2.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.ItemBuilder;
import net.md_5.bungee.api.ChatColor;

public class ToolGui extends GuiPage{

	public ToolGui(Player player) {
		super(1, player);
		this.setName("Tools");
		this.addItems();
	}
	
	private void refresh() {
		this.clear();
		this.addItems();
		this.display();
	}
	
	private void addItems() {
		GamePlayer p = GamePlayer.get(getPlayer());
		GuiItem compass = new GuiItem(ItemBuilder.of(Material.COMPASS).name("Objective Tracker: "+(p.useTracker() ? ChatColor.GREEN+"True" : ChatColor.RED+"False")).get());
		compass.setRun((event) -> {
			p.setTracker(!p.useTracker());
			refresh();
		});
		setItem(0,0,compass);
		GuiItem pointer = new GuiItem(ItemBuilder.of(Material.GOLDEN_CARROT).name("Pointer: "+(p.usePointer() ? ChatColor.GREEN+"True" : ChatColor.RED+"False")).get());
		pointer.setRun((event) -> {
			p.setPointer(!p.usePointer());
			refresh();
		});
		setItem(0,1,pointer);
		GuiItem spyglass = new GuiItem(ItemBuilder.of(Material.SPYGLASS).name("Spyglass: "+(p.useSpyglass() ? ChatColor.GREEN+"True" : ChatColor.RED+"False")).get());
		spyglass.setRun((event) -> {
			p.setSpyglass(!p.useSpyglass());
			refresh();
		});
		setItem(0,2,spyglass);
		GuiItem kitselect = new GuiItem(ItemBuilder.of(Material.NETHER_STAR).name("Kit Selector: "+(p.useKitSelector() ? ChatColor.GREEN+"True" : ChatColor.RED+"False")).get());
		kitselect.setRun((event) -> {
			p.setKitSelector(!p.useKitSelector());
			refresh();
		});
		setItem(0,3,kitselect);
	}

}
