package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.tdm.TDMGame;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameColor;
import net.dezilla.dectf2.util.GameConfig;

public abstract class BaseKit implements Listener{
	protected static double defaultAttackSpeed = 40;
	protected static double defaultMovementSpeed = .12;
	
	GamePlayer player;
	
	public abstract String getName();
	
	public abstract ItemStack getIcon();
	
	public abstract String[] getVariations();
	
	public void setVariation(String variation) {};
	
	public BaseKit(GamePlayer player) {
		this.player = player;
		//player can be null, mainly to access kit name, icons, desc, etc...
		if(player != null)
			Bukkit.getServer().getPluginManager().registerEvents(this, GameMain.getInstance());
	}
	
	public void unregister() {
		HandlerList.unregisterAll(this);
	}
	
	public void setInventory() {
		setAttributes();
		setEffects();
		setLevel();
	}
	
	public void setAttributes() {
		player.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(getAttackSpeed());
		player.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getMovementSpeed());
	}
	
	public void setEffects() {}
	
	public void setLevel() {
		player.getPlayer().setExp(0);
		player.getPlayer().setLevel(0);
	}
	
	public double getAttackSpeed() {
		return defaultAttackSpeed;
	}
	
	public double getMovementSpeed() {
		return defaultMovementSpeed;
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		Player p = player.getPlayer();
		if(!event.getPlayer().equals(p))
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
	
	long lastAttack = 0;
	@EventHandler
	public void onDamageEntity(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player) || !((Player) event.getEntity()).equals(player.getPlayer()))
			return;
		long now = GameMain.getServerTick();
		if(event.getDamager() != null && player.getPlayer().isBlocking() && event.getCause() != DamageCause.CUSTOM && now-lastAttack >= 10) {
			event.setCancelled(true);
			GamePlayer killer = Util.getOwner(event.getDamager());
			if(killer != null)
				player.setLastAttacker(killer);
			player.setCustomDamageCause(CustomDamageCause.SHIELDED_DAMAGE);
			double dmg = event.getDamage() - (Util.getDamageReduced(player.getPlayer())*event.getDamage());
			player.getPlayer().damage(dmg*.5);
			killer.incrementDamageDealt(dmg*.5);
			if(GameMatch.currentMatch != null && killer.getTeam() != null && GameMatch.currentMatch.getGame() instanceof TDMGame) {
				TDMGame game = (TDMGame) GameMatch.currentMatch.getGame();
				game.addDamage(killer.getTeam(), dmg*.5);
			}
			Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {
				player.getPlayer().setVelocity(Util.getKnockback(player.getPlayer(), event.getDamager()));
				player.getPlayer().playSound(player.getPlayer(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
			});
		}
		if(now-lastAttack >= 10)
			lastAttack = now;
	}
	
	protected GameColor color() {
		if(player != null) {
			GameTeam team = player.getTeam();
			if(team != null)
				return team.getColor();
		}
		return GameColor.WHITE;
	}

}
