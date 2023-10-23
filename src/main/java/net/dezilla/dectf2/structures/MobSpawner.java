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
		previousMaterial.add(b.getType());
		b.setType(Material.NETHER_BRICK_FENCE);
		blocks.add(b);
		Block under = b.getRelative(BlockFace.DOWN);
		previousMaterial.add(under.getType());
		under.setType(owner.getTeam().getColor().wool());
		blocks.add(under);
		Block s = b.getRelative(BlockFace.UP);
		previousMaterial.add(s.getType());
		s.setType(Material.SPAWNER);
		blocks.add(s);
		spawner = (CreatureSpawner) s.getState();
		spawner.setSpawnedType(EntityType.ZOMBIE);
		spawner.setSpawnCount(0);
	}

	@Override
	public boolean canPlace(Location location) {
		// TODO Auto-generated method stub
		return false;
	}

}
