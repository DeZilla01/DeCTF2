package net.dezilla.dectf2.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

//Used to restric structures in specific areas
public class RestrictArea {
	Location location;
	double radius;
	
	public RestrictArea(Location location, double radius) {
		this.location = location;
		this.radius = radius;
	}
	
	public RestrictArea(Block block, double radius) {
		this.location = block.getLocation().add(.5,.5,.5);
		this.radius = radius;
	}
	
	public boolean inRadius(Location loc) {
		return location.distance(loc)<radius;
	}
}
