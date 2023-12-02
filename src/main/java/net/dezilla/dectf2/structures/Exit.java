package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;

public class Exit extends BaseStructure{
	Block plate = null;
	ArmorStand display = null;

	public Exit(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		removeOnSpawnProtection = true;
		destroyable = true;
		placeBlocks();
	}
	
	private void placeBlocks() throws CannotBuildException {
		Block b = location.getBlock().getRelative(BlockFace.DOWN);
		plate = b.getRelative(BlockFace.UP);
		addBlock(plate);
		addBlock(b);
		b.setType(owner.getTeam().getColor().wool());
		plate.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
		display = (ArmorStand) location.getWorld().spawnEntity(plate.getLocation().add(.5,.1,.5), EntityType.ARMOR_STAND);
		display.setVisible(false);
		display.setMarker(true);
		display.setInvulnerable(true);
		display.setGravity(false);
		display.setCustomNameVisible(true);
		display.setCustomName("Exit");
		entities.add(display);
	}
	
	public void teleport(LivingEntity entity) {
		Location l = plate.getLocation().add(.5,.1,.5);
		entity.teleport(l);
	}
	
	public Location getExitLocation() {
		return plate.getLocation().add(.5,.1,.5);
	}

	@Override
	public boolean canPlace(Location location) {
		Block b = location.getBlock();
		if(!Util.air(b) || !Util.air(b.getRelative(BlockFace.UP)) || !Util.air(b.getRelative(BlockFace.UP).getRelative(BlockFace.UP)))
			return false;
		return true;
	}
	
	@Override
	public boolean bypassRestrictedAreas() {
		return false;
	}

}
