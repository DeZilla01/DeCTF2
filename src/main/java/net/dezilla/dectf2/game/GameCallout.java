package net.dezilla.dectf2.game;

import org.bukkit.Location;

import net.dezilla.dectf2.util.GameConfig;
import net.md_5.bungee.api.ChatColor;

public class GameCallout {
	private Location location;
	private GameTeam team;
	private int teamId;
	private String name;
	
	public GameCallout(Location location, GameTeam teamSide, String name) {
		this.location = location.clone();
		this.team = teamSide;
		this.name = name;
		if(team != null)
			this.teamId = team.getId();
	}
	
	public GameCallout(Location location, String name) {
		teamId=-1;
		team = null;
		this.location = location.clone();
		this.name = name;
	}
	
	public GameCallout(Location location, int teamId, String name) {
		this.location = location.clone();
		this.teamId = teamId;
		this.team = null;
		this.name = name;
	}
	
	public String getName() {
		String s = "";
		if(team != null) {
			s+=team.getColoredTeamName()+" "+ChatColor.RESET;
		}
		s+=name;
		return s;
	}
	
	public void setTeam(GameTeam team) {
		this.team = team;
		if(team != null)
			teamId = team.getId();
	}
	
	public GameTeam getTeam() {
		return team;
	}
	
	public int getTeamId() {
		return teamId;
	}
	
	public boolean isNear(Location loc) {
		if(!location.getWorld().equals(loc.getWorld()))
			return false;
		return location.distance(loc) <= GameConfig.calloutNameRadius;
	}
	
	public Location getLocation() {
		return location.clone();
	}

}
