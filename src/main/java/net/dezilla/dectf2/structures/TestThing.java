package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.util.GameColor;

public class TestThing extends BaseStructure{

	public TestThing(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		placeBlocks();
	}
	
	private void placeBlocks() {
		Block b = location.getBlock();
		addBlock(b);
		b.setType(Material.STONE);
		b = b.getRelative(BlockFace.UP);
		addBlock(b);
		b.setType(Material.STONE);
		ArmorStand a = (ArmorStand) location.getWorld().spawnEntity(b.getLocation().add(.5,.5,.5), EntityType.ARMOR_STAND);
		entities.add(a);
		a.setVisible(false);
		a.setMarker(true);
		a.setGravity(false);
		a.setCustomName("Structure Test");
		a.setCustomNameVisible(true);
		a.setInvulnerable(true);
		dead = false;
	}
	
	@Override
	public void onTick() {
		GameTeam t = new GameTeam(-1, GameColor.WHITE);
		if(owner != null)
			t = owner.getTeam();
		Player p = null;
		for(Player pl : location.getWorld().getPlayers()) {
			GamePlayer gp = GamePlayer.get(pl);
			if(gp.getTeam().equals(t))
				continue;
			if(p == null) {
				p = pl;
				continue;
			}
			if(location.distance(pl.getLocation()) < location.distance(p.getLocation()))
				p = pl;
		}
		if(p != null) {
			ArmorStand a = (ArmorStand) entities.get(0);
			a.launchProjectile(Snowball.class, Util.getVectorToLoc(a.getEyeLocation(), p.getLocation().add(0,.5,0), 1));
		}
	}

	@Override
	public boolean canPlace(Location location) {
		Block b = location.getBlock();
		return b.getType() == Material.AIR && b.getRelative(BlockFace.UP).getType() == Material.AIR && !areaRestricted(b) && !spawnArea(b);
	}
		
}
