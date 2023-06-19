package net.dezilla.dectf2.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.dezilla.dectf2.gui.GuiItem;
import net.dezilla.dectf2.gui.GuiPage;

public class GuiListener implements Listener{

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if(!(event.getWhoClicked() instanceof Player))
			return;
		Player player = (Player) event.getWhoClicked();
		if(event.getCurrentItem()!= null) {
			GuiPage page = GuiPage.getPage(player);
			if(page == null)
				return;
			GuiItem item = page.getGuiItem(event.getCurrentItem());
			if(item == null)
				return;
			event.setCancelled(true);
			item.run(event);
			event.setCurrentItem(item.getItem());
		}
	}
}
