package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.util.ASBuilder;

public class SpeedPad extends BaseStructure {
	static int SPEED_TIME = 120;
	static int SPEED_AMP = 2;
	
	ArmorStand display = null;
	Block plate = null;
	public SpeedPad(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		removeOnSpawnProtection = true;
		destroyable = true;
		placeBlocks();
	}
	
	private void placeBlocks() throws CannotBuildException {
		Block base = location.getBlock().getRelative(BlockFace.DOWN);
		plate = base.getRelative(BlockFace.UP);
		addBlock(base);
		addBlock(plate);
		base.setType(owner.getTeam().getColor().concrete());
		plate.setType(Material.STONE_PRESSURE_PLATE);
		display = ASBuilder.create(plate.getLocation().add(.5,.1,.5)).gravity(false).visible(false).invulnerable(false).marker(true).display("Speed Pad").get();
		entities.add(display);
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		GamePlayer gp = GamePlayer.get(event.getPlayer());
		if(!gp.getTeam().equals(owner.getTeam()))
			return;
		if(gp.getPlayer().hasPotionEffect(PotionEffectType.SPEED))
			return;
		if(gp.getLocation().getBlock().equals(plate)) {
			gp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SPEED_TIME, SPEED_AMP-1));
		}
	}
	
	@Override
	public boolean canPlace(Location location) {
		Block b = location.getBlock();
		if(!Util.air(b) || !Util.air(b.getRelative(BlockFace.UP)) || !Util.air(b.getRelative(BlockFace.UP).getRelative(BlockFace.UP)))
			return false;
		return !areaRestricted(b);
	}

}
