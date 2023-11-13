package net.dezilla.dectf2.structures;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;

public abstract class BaseStructure implements Listener {
	static List<BaseStructure> STRUCTURES = new ArrayList<BaseStructure>();
	
	public static BaseStructure getStructure(Block block) {
		List<BaseStructure> toRemove = new ArrayList<BaseStructure>();
		for(BaseStructure s : STRUCTURES) {
			if(s.isDead()) {
				toRemove.add(s);
				continue;
			}
			if(s.isStructure(block))
				return s;
		}
		for(BaseStructure s : toRemove)
			STRUCTURES.remove(s);
		return null;
	}
	
	public static BaseStructure getStructure(Entity entity) {
		List<BaseStructure> toRemove = new ArrayList<BaseStructure>();
		for(BaseStructure s : STRUCTURES) {
			if(s.isDead()) {
				toRemove.add(s);
				continue;
			}
			if(s.isStructure(entity))
				return s;
		}
		for(BaseStructure s : toRemove)
			STRUCTURES.remove(s);
		return null;
	}
	
	GamePlayer owner;
	Location location;
	List<Block> blocks = new ArrayList<Block>();
	List<Material> previousMaterial = new ArrayList<Material>();
	List<BlockData> previousData = new ArrayList<BlockData>();
	List<Entity> entities = new ArrayList<Entity>();
	boolean dead = true;
	int onTickTaskID = 0;
	boolean removedOnDeath = false;
	boolean removeOnSpawnProtection = false;
	boolean destroyable = false;
	boolean minionCanAttack = false;
	
	
	public BaseStructure(GamePlayer owner, Location location) throws CannotBuildException {
		this.owner = owner;
		this.location = location;
		if(spawnArea(location.getBlock()))
			throw new CannotBuildException("Ye can't place shit on spawn u dumdum");
		if(!structureCheck(location.getBlock()))
			throw new CannotBuildException("Ye can't build on another structure u idiot");
		if(!canPlace(location)) {
			GameMatch match = GameMatch.currentMatch;
			if(match != null) {
				String s = match.getRestrictionReason(location);
				if(s != null)
					throw new CannotBuildException(s);
			}
			throw new CannotBuildException("Ye can't place fucking shit here mate");
		}
		onTickTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> {
			onTick();
			if(removeOnSpawnProtection && owner != null && owner.isSpawnProtected())
				remove();
			if(GameMatch.currentMatch != null && GameMatch.currentMatch.getGameState() != GameState.INGAME)
				remove();
		}, 1, 1);
		Bukkit.getServer().getPluginManager().registerEvents(this, GameMain.getInstance());
		STRUCTURES.add(this);
		dead = false;
	}
	
	public void onTick() {}
	
	public abstract boolean canPlace(Location location);
	
	public boolean isStructure(Block block) {
		return blocks.contains(block);
	}
	
	public boolean isStructure(Entity entity) {
		return entities.contains(entity);
	}
	
	public GamePlayer getOwner() {
		return owner;
	}
	
	protected void addBlock(Block block) throws CannotBuildException {
		if(!singleBlockStructureCheck(block)) {
			this.remove();
			throw new CannotBuildException("Ye can't build on another structure u dumbfuck");
		}
		previousMaterial.add(block.getType());
		previousData.add(block.getBlockData());
		blocks.add(block);
	}
	
	public boolean minionCanAttack(ArmorStand entity) {
		if(!minionCanAttack)
			return false;
		return entities.contains(entity);
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if(owner == null || !removedOnDeath)
			return;
		GamePlayer gp = GamePlayer.get(event.getEntity());
		if(!gp.equals(owner))
			return;
		remove();
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if(owner == null || !removedOnDeath)
			return;
		GamePlayer gp = GamePlayer.get(event.getPlayer());
		if(!gp.equals(owner))
			return;
		remove();
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(!blocks.contains(event.getBlock()) || !destroyable)
			return;
		if(owner != null && owner.getTeam() != null) {
			GamePlayer gp = GamePlayer.get(event.getPlayer());
			if(owner.getTeam().equals(gp.getTeam()) && !owner.equals(gp))
				return;
		}
		remove();
	}
	
	public void remove() {
		for(Entity e : entities) {
			if(!e.isDead())
				e.remove();
		}
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTask(onTickTaskID);
		for(Block b : blocks) {
			if(previousMaterial.size()>blocks.indexOf(b))
				b.setType(previousMaterial.get(blocks.indexOf(b)));
			else
				b.setType(Material.AIR);
			if(previousData.size() >blocks.indexOf(b))
				b.setBlockData(previousData.get(blocks.indexOf(b)));
		}
		dead = true;
		if(STRUCTURES.contains(this))
			STRUCTURES.remove(this);
	};
	
	public boolean isDead() {
		return dead;
	}
	
	public static boolean areaRestricted(Block b) {
		GameMatch match = GameMatch.currentMatch;
		if(match == null)
			return false;
		return match.isAreaRestricted(b.getLocation().add(.5,.5,.5));
	}
	
	public static boolean structureCheck(Block b) {
		return getStructure(b.getRelative(BlockFace.DOWN)) == null && getStructure(b) == null;
	}
	
	public static boolean singleBlockStructureCheck(Block b) {
		return getStructure(b) == null;
	}
	
	public static boolean spawnArea(Block b) {
		if(GameMatch.currentMatch == null)
			return false;
		GameMatch m = GameMatch.currentMatch;
		for(GameTeam t : m.getTeams()) {
			if(t.isSpawnBlock(b) || t.isSpawnBlock(b.getRelative(BlockFace.DOWN)))
				return true;
		}
		return false;
	}

}
