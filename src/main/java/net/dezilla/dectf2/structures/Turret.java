package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import net.dezilla.dectf2.GamePlayer;

public class Turret extends BaseStructure{

	public Turret(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		
	}

	@Override
	public boolean canPlace(Location location) {
		Block b = location.getBlock();
		if(b.getType() != Material.AIR && b.getRelative(BlockFace.UP).getType() != Material.AIR && b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType() != Material.AIR)
			return false;
		return !areaRestricted(b);
	}

}
