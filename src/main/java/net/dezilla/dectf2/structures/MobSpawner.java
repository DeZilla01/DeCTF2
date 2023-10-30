package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import net.dezilla.dectf2.GamePlayer;

public class MobSpawner extends BaseStructure{
	
	CreatureSpawner spawner = null;

	public MobSpawner(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		destroyable = true;
		removedOnDeath = true;
		removeOnSpawnProtection = true;
		placeBlocks();
	}
	
	private void placeBlocks() {
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
		dead = false;
	}

	@Override
	public boolean canPlace(Location location) {
		return true;
	}

}
