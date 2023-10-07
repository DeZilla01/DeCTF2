package net.dezilla.dectf2.structures;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMatch;

public abstract class BaseStructure implements Listener {
	
	GamePlayer owner;
	Location location;
	List<Block> blocks = new ArrayList<Block>();
	List<Material> previousMaterial = new ArrayList<Material>();
	List<Entity> entities = new ArrayList<Entity>();
	boolean dead = true;
	int onTickTaskID = 0;
	boolean removedOnDeath = false;
	
	
	public BaseStructure(GamePlayer owner, Location location) throws CannotBuildException {
		this.owner = owner;
		this.location = location;
		if(!canPlace(location))
			throw new CannotBuildException("Ye can't place fucking shit here mate");
		onTickTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> onTick(), 1, 1);
		Bukkit.getServer().getPluginManager().registerEvents(this, GameMain.getInstance());
	}
	
	public void onTick() {}
	
	public abstract boolean canPlace(Location location);
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if(owner == null)
			return;
		GamePlayer gp = GamePlayer.get(event.getEntity());
		if(!gp.equals(owner))
			return;
		remove();
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if(owner == null)
			return;
		GamePlayer gp = GamePlayer.get(event.getPlayer());
		if(!gp.equals(owner))
			return;
		remove();
	}
	
	public void remove() {
		for(Entity e : entities) {
			if(!e.isDead())
				e.remove();
		}
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTask(onTickTaskID);
		dead = true;
	};
	
	public boolean isDead() {
		return dead;
	}
	
	public static boolean areaRestricted(Block b) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null)
			return false;
		if(!b.getLocation().getWorld().equals(match.getWorld()))
			return false;
		return match.isAreaRestricted(b.getLocation().add(.5,.5,.5));
	}

}
