package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import net.dezilla.dectf2.GamePlayer;

public class Entry extends BaseStructure{
	Block plate = null;
	ArmorStand display = null;
	Exit exit = null;

	public Entry(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		destroyable = true;
		placeBlocks();
	}
	
	@Override
	public void onTick() {
		if(exit == null || exit.isDead()) {
			String name = "No Exit";
			if(display.getCustomName().equals(name))
				display.setCustomName(name);
			return;
		}
		
	}
	
	private void placeBlocks() {
		Block b = location.getBlock().getRelative(BlockFace.DOWN);
		previousMaterial.add(b.getType());
		b.setType(owner.getTeam().getColor().wool());
		blocks.add(b);
		plate = b.getRelative(BlockFace.UP);
		plate.setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		blocks.add(plate);
		display = (ArmorStand) location.getWorld().spawnEntity(plate.getLocation().add(.5,.1,.5), EntityType.ARMOR_STAND);
		display.setVisible(false);
		display.setMarker(true);
		display.setInvulnerable(true);
		display.setGravity(false);
		display.setCustomNameVisible(true);
		display.setCustomName("Entry");
		entities.add(display);
		dead = false;
	}
	
	public void setExit(Exit exit) {
		this.exit = exit;
	}

	@Override
	public boolean canPlace(Location location) {
		Block b = location.getBlock();
		if(b.getType() != Material.AIR || b.getRelative(BlockFace.UP).getType() != Material.AIR)
			return false;
		return !areaRestricted(b);
	}

}
