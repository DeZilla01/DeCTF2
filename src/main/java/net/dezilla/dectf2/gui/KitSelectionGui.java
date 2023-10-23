package net.dezilla.dectf2.gui;

import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.kits.BaseKit;
import net.dezilla.dectf2.util.ItemBuilder;

public class KitSelectionGui extends GuiPage{
	private static ItemStack pane = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("").get();
	BaseKit kit = null;

	public KitSelectionGui(Player player) {
		super(6, player);
		setItems();
	}
	
	private void refresh() {
		this.clear();
		this.setItems();
		this.display();
	}
	
	private void setItems() {
		this.setAmountOfRows(6);
		int row = 0;
		int col = 0;
		for(int i = 0; i <9; i++) {
			setItem(row, col++, new GuiItem(pane.clone()));
			if(col>8) {
				row++;
				col=0;
			}
		}
		if(kit != null) {
			//back button
			GuiItem backItem = new GuiItem(ItemBuilder.of(Material.REPEATER).name("Back").get());
			backItem.setRun(event -> {
				kit = null;
				refresh();
			});
			this.setName(kit.getName());
			setItem(0,0,backItem);
			for(ItemStack i : kit.getFancyDisplay()) {
				setItem(row, col++, new GuiItem(ItemBuilder.of(i).name("").get()));
				if(col>8)
					break;
			}
			col=0;
			row++;
			for(int i = 0; i <9; i++) {
				setItem(row, col++, new GuiItem(pane.clone()));
				if(col>8) {
					row++;
					col=0;
				}
			}
			for(String alt : kit.getVariations()) {
				ItemStack icon = ItemBuilder.of(kit.getIcon(alt)).name(alt).get();
				GuiItem item = new GuiItem(icon);
				item.setRun(event -> {
					GamePlayer p = GamePlayer.get(getPlayer());
					p.getPlayer().closeInventory();
					p.setKit(kit.getClass(), alt);
				});
				setItem(row, col++, item);
				if(col>8) {
					row++;
					col=0;
				}
			}
			this.centerRow(row);
			this.removeEmptyRows();
			return;
		}
		try {
			this.setName("Kit Selection");
			for(Entry<String,Class<? extends BaseKit>> e : GameMain.getInstance().kitMap().entrySet()) {
				GamePlayer p = null;//somehow this works.... don't ask
				BaseKit k = e.getValue().getConstructor(GamePlayer.class).newInstance(p);
				ItemStack icon = ItemBuilder.of(k.getIcon()).name(k.getName()).get();
				GuiItem item = new GuiItem(icon);
				item.setRun(event -> {
					kit = k;
					refresh();
				});
				setItem(row, col++, item);
				if(col>8) {
					row++;
					col=0;
				}
			}
			this.removeEmptyRows();
			this.centerRow(row);
		}catch(Exception e) {e.printStackTrace();}
	}

}
