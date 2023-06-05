package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameConfig;

public abstract class BaseKit implements Listener{
	
	GamePlayer player;
	
	public BaseKit(GamePlayer player) {
		this.player = player;
		Bukkit.getServer().getPluginManager().registerEvents(this, GameMain.getInstance());
	}
	
	public void unregister() {
		HandlerList.unregisterAll(this);
	}
	
	public abstract void setInventory();
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		Player p = player.getPlayer();
		if(!event.getPlayer().equals(player))
			return;
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			//Steak
			if(event.getItem()!=null && event.getItem().getType() == GameConfig.foodMaterial) {
				event.setCancelled(true);
				double currentHp = p.getHealth();
				double maxHp = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				if(currentHp == maxHp)
					return;
				ItemStack item = event.getItem();
				if(item.getAmount()==1)
					p.getInventory().remove(item);
				else
					item.setAmount(item.getAmount()-1);
				if(currentHp+8>=maxHp)
					p.setHealth(maxHp);
				else
					p.setHealth(currentHp+8);
				p.playSound(p, Sound.ENTITY_PLAYER_BURP, 1, 1);
			}
		}
	}

}
