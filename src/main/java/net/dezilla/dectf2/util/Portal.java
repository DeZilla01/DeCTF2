package net.dezilla.dectf2.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import net.dezilla.dectf2.GameMain;

public class Portal implements Listener{
	Location location;
	PortalFacing facing;
	Location destination;
	List<Block> blocks = new ArrayList<Block>();
	
	public Portal(Location location, PortalFacing facing, Location destination) {
		this.location = location;
		this.facing = facing;
		this.destination = destination;
		grabBlocks();
		Bukkit.getServer().getPluginManager().registerEvents(this, GameMain.getInstance());
		Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
			for(Block b : blocks) {
				if(facing == PortalFacing.UP_DOWN)
					b.setType(Material.END_PORTAL);
				else
					b.setType(Material.NETHER_PORTAL);
				if(facing == PortalFacing.NORTH_SOUTH) {
					Orientable o = (Orientable) b.getBlockData();
					o.setAxis(Axis.X);
					b.setBlockData(o);
				}
				else if(facing == PortalFacing.EAST_WEST) {
					Orientable o = (Orientable) b.getBlockData();
					o.setAxis(Axis.Z);
					b.setBlockData(o);
				}
			}
		},1);
	}
	
	public void remove() {
		HandlerList.unregisterAll(this);
	}
	
	public Location getLocation() {
		return location;
	}
	
	public Location getDestination() {
		return destination;
	}
	
	public void setDestination(Location destination) {
		this.destination = destination;
	}
	
	public PortalFacing getFacing() {
		return facing;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!event.getTo().getWorld().equals(location.getWorld()))
			return;
		Block b = event.getTo().getBlock();
		if(b.getType() != Material.NETHER_PORTAL && b.getType() != Material.END_PORTAL)
			return;
		if(blocks.contains(b)) {
			event.getPlayer().teleport(destination);
			event.getPlayer().playSound(event.getPlayer(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
		}
	}
	
	private void grabBlocks() {
		List<Block> toCheck = new ArrayList<Block>();
		toCheck.add(location.getBlock());
		while(!toCheck.isEmpty()) {
			Block b = toCheck.get(0);
			if(facing != PortalFacing.NORTH_SOUTH) {
				Block n = b.getRelative(BlockFace.NORTH);
				Block s = b.getRelative(BlockFace.SOUTH);
				if(n.getType() == Material.AIR && !blocks.contains(n) && !toCheck.contains(n))
					toCheck.add(n);
				if(s.getType() == Material.AIR && !blocks.contains(s) && !toCheck.contains(s))
					toCheck.add(s);
			}
			if(facing != PortalFacing.EAST_WEST) {
				Block e = b.getRelative(BlockFace.EAST);
				Block w = b.getRelative(BlockFace.WEST);
				if(e.getType() == Material.AIR && !blocks.contains(e) && !toCheck.contains(e))
					toCheck.add(e);
				if(w.getType() == Material.AIR && !blocks.contains(w) && !toCheck.contains(w))
					toCheck.add(w);
			}
			if(facing != PortalFacing.UP_DOWN) {
				Block u = b.getRelative(BlockFace.UP);
				Block d = b.getRelative(BlockFace.DOWN);
				if(u.getType() == Material.AIR && !blocks.contains(u) && !toCheck.contains(u))
					toCheck.add(u);
				if(d.getType() == Material.AIR && !blocks.contains(d) && !toCheck.contains(d))
					toCheck.add(d);
			}
			blocks.add(b);
			toCheck.remove(b);
			//safety break, incase theres no proper frame
			if(blocks.size() >= 80)
				break;
		}
	}
	
	public static PortalFacing BlocktoPortalFace(BlockFace face) {
		switch(face) {
			case EAST: case EAST_NORTH_EAST: case EAST_SOUTH_EAST: case WEST: case WEST_NORTH_WEST: case WEST_SOUTH_WEST:
				return PortalFacing.EAST_WEST;
			case NORTH: case NORTH_EAST: case NORTH_NORTH_EAST: case NORTH_NORTH_WEST: case NORTH_WEST:
			case SOUTH: case SOUTH_EAST: case SOUTH_SOUTH_EAST: case SOUTH_SOUTH_WEST: case SOUTH_WEST:
				return PortalFacing.NORTH_SOUTH;
			default:
				return PortalFacing.UP_DOWN;
		
		}
	}
	
	public static enum PortalFacing{
		NORTH_SOUTH,
		EAST_WEST,
		UP_DOWN;
	}

}
