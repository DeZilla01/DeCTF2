package net.dezilla.dectf2.util;

import org.bukkit.Location;

public class ObjectiveLocation {
	Location location;
	String name;
	boolean staticObjective;
	
	public ObjectiveLocation(Location loc, String name, boolean isStatic) {
		this.location = loc;
		this.name = name;
		this.staticObjective = isStatic;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isStatic() {
		return staticObjective;
	}

}
