package net.dezilla.dectf2.structures;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.util.Minion;

public class MobSpawner extends BaseStructure{
	static int MOB_SPAWN_DELAY = 120;
	static int MOB_MAX_SPAWN = 5;
	static int MOB_SPAWN_RADIUS = 4;
	
	CreatureSpawner spawner = null;
	List<Block> spawnAreas = new ArrayList<Block>();
	int delay = MOB_SPAWN_DELAY;
	List<Minion> minions = new ArrayList<Minion>();

	public MobSpawner(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		destroyable = true;
		removedOnDeath = true;
		removeOnSpawnProtection = true;
		spawnAreas();
		if(spawnAreas.isEmpty())
			throw new CannotBuildException("No room for spawning minions");
		placeBlocks();
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() == null || !blocks.contains(event.getClickedBlock()))
			return;
		if(event.getItem() == null || !event.getItem().getType().toString().contains("_SPAWN_EGG"))
			return;
		if(!event.getPlayer().equals(owner.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		event.setCancelled(true);
		try {
			EntityType type = EntityType.valueOf(event.getItem().getType().toString().replace("_SPAWN_EGG", ""));
			spawner.setSpawnedType(type);
			spawner.update();
		}catch(Exception e) {}
		
	}
	
	@Override
	public void remove() {
		super.remove();
		for(Minion m : minions)
			m.remove();
	}
	
	@Override
	public void onTick() {
		delay--;
		if(delay<=0) {
			removeDeadMinions();
			if(minions.size() >= MOB_MAX_SPAWN) {
				return;
			}
			Block b = spawnAreas.get((int)(Math.random()*spawnAreas.size()));
			while(!Util.air(b)) {
				spawnAreas.remove(b);
				if(spawnAreas.isEmpty()) {
					remove();
					return;
				}
				b = spawnAreas.get((int)(Math.random()*spawnAreas.size()));
			}
			Location l = b.getLocation().add(.5,0,.5);
			Minion m = new Minion(spawner.getSpawnedType(), owner.getTeam(), l, owner);
			minions.add(m);
			delay = MOB_SPAWN_DELAY;
		}
	}
	
	private void removeDeadMinions() {
		List<Minion> toRemove = new ArrayList<Minion>();
		for(Minion m : minions) {
			if(m.isDead())
				toRemove.add(m);
		}
		for(Minion m : toRemove) {
			minions.remove(m);
		}
	}
	
	private void placeBlocks() throws CannotBuildException {
		Block b = location.getBlock();
		addBlock(b);
		b.setType(Material.NETHER_BRICK_FENCE);
		Block under = b.getRelative(BlockFace.DOWN);
		addBlock(under);
		under.setType(owner.getTeam().getColor().wool());
		Block s = b.getRelative(BlockFace.UP);
		addBlock(s);
		s.setType(Material.SPAWNER);
		spawner = (CreatureSpawner) s.getState();
		spawner.setSpawnedType(EntityType.ZOMBIE);
		spawner.setSpawnCount(0);
		spawner.update();
	}
	
	private void spawnAreas() {
		for(int x = -MOB_SPAWN_RADIUS; x <= MOB_SPAWN_RADIUS; x++) {
			for(int z = -MOB_SPAWN_RADIUS; z <= MOB_SPAWN_RADIUS; z++) {
				Block b = location.clone().add(x, 0, z).getBlock();
				if(Util.air(b)) {
					int c = 0;
					while(Util.air(b.getRelative(BlockFace.DOWN)) && c<3) {
						b = b.getRelative(BlockFace.DOWN);
						c++;
					}
					spawnAreas.add(b);
				}
				else if(!Util.air(b)) {
					int c = 0;
					b = b.getRelative(BlockFace.UP);
					while(!Util.air(b) && c<3) {
						b = b.getRelative(BlockFace.UP);
						c++;
					}
					if(c<3) {
						spawnAreas.add(b);
					}
				}
			}
		}
	}

	@Override
	public boolean canPlace(Location location) {
		return true;
	}

}
