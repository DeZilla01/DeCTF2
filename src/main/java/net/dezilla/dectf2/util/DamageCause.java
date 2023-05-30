package net.dezilla.dectf2.util;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import net.dezilla.dectf2.GamePlayer;

public class DamageCause {
	GamePlayer damager;
	GamePlayer victim;
	String deathCause = "";
	ItemStack itemHeld;
	
	//This is lazily put together for now, but I'll work on it I swear.
	public DamageCause(GamePlayer victim, GamePlayer damager, ItemStack item) {
		this.damager = damager;
		this.victim = victim;
		this.itemHeld = item;
	}
	
	public String getDeathMessage() {
		return victim.getColoredName() + ChatColor.RESET + "has been killed by " + damager.getColoredName();
	}
	
	public boolean isDamagerOnline() {
		return damager.getPlayer().isOnline();
	}
	
	public GamePlayer getDamager() {
		return damager;
	}
}
