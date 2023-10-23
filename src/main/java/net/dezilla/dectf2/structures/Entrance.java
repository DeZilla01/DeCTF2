package net.dezilla.dectf2.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameCallout;
import net.dezilla.dectf2.game.GameMatch;

public class Entrance extends BaseStructure{
	static int TELEPORTING_DELAY = 60;
	
	Block plate = null;
	ArmorStand display = null;
	Exit exit = null;

	public Entrance(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		removeOnSpawnProtection = true;
		destroyable = true;
		placeBlocks();
	}
	
	@Override
	public void onTick() {
		if(exit == null || exit.isDead()) {
			String name = "No Exit";
			if(!display.getCustomName().equals(name))
				display.setCustomName(name);
			if(!delays.isEmpty())
				delays.clear();
			return;
		}
		Location l = exit.getExitLocation();
		String name = "Teleporting to "+l.getBlockX()+" "+l.getBlockY()+" "+l.getBlockZ();
		if(GameMatch.currentMatch != null) {
			GameCallout c = GameMatch.currentMatch.getCalloutNear(l);
			if(c != null)
				name = "Teleporting to "+c.getName();
		}
		if(!display.getCustomName().equals(name))
			display.setCustomName(name);
		List<GamePlayer> toRemove = new ArrayList<GamePlayer>();
		for(Entry<GamePlayer, Integer> e : delays.entrySet()) {
			if(!e.getKey().getLocation().getBlock().equals(plate)) {
				toRemove.add(e.getKey());
				e.getKey().notify("Teleportation cancelled");
				continue;
			}
			delays.put(e.getKey(), e.getValue()-1);
			if(e.getValue() <= 0) {
				exit.teleport(e.getKey().getPlayer());
				toRemove.add(e.getKey());
				e.getKey().notify("");
			} else {
				int i = (e.getValue()/20)+1;
				e.getKey().notify("Teleporting in "+i+" second(s)");
			}
		}
		for(GamePlayer gp : toRemove) {
			delays.remove(gp);
		}
	}
	
	Map<GamePlayer, Integer> delays = new HashMap<GamePlayer, Integer>();
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(exit == null || exit.isDead())
			return;
		if(!event.getPlayer().getLocation().getBlock().equals(plate))
			return;
		GamePlayer gp = GamePlayer.get(event.getPlayer());
		if(!owner.getTeam().equals(gp.getTeam())) {
			gp.notify("You can't use enemy teleporters you dumb fuck");
			return;
		}
		if(!delays.containsKey(gp))
			delays.put(gp, TELEPORTING_DELAY);
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
