package net.dezilla.dectf2.game.zc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameTeam;
import net.md_5.bungee.api.ChatColor;

public class Zone {
	static BlockFace[] faces = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
	
	double captureRate = .01; //Progress per second per players on point;
	List<Block> blocks = new ArrayList<Block>();
	Location location;
	String name;
	Material zoneMaterial;
	boolean captured = false;
	GameTeam owningTeam = null;
	double captureProgress = 0;
	
	public Zone(Location location, String name) {
		this.location = location.clone();
		this.name = name;
		this.zoneMaterial = location.getBlock().getType();
		grabBlocks();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isCaptured() {
		return captured;
	}
	
	public void setCaptured(boolean value) {
		captured = value;
	}
	
	public GameTeam getOwningTeam() {
		return owningTeam;
	}
	
	public double getCaptureProgress() {
		return captureProgress;
	}
	
	public void setCaptureProgress(double value) {
		captureProgress = value;
	}
	
	public double getCaptureRate() {
		return captureRate;
	}
	
	public void setCaptureRate(double rate) {
		captureRate = rate;
	}
	
	public Material getZoneMaterial() {
		return zoneMaterial;
	}
	
	public void setZoneMaterial(Material material) {
		zoneMaterial = material;
	}
	
	public Location getLocation() {
		return location;
	}
	
	//This is meant to be executed every seconds.
	public void update() {
		boolean teamOnPoint = false;
		boolean noOneOnPoint = true;
		List<GamePlayer> playersOnPoint = new ArrayList<GamePlayer>();
		double toAdd = 0;
		Map<GameTeam, Integer> tieBreaker = new HashMap<GameTeam, Integer>();
		for(Player p : location.getWorld().getPlayers()) {
			if(!isStandingOnZone(p))
				continue;
			GamePlayer player = GamePlayer.get(p);
			GameTeam team = player.getTeam();
			if(team == null)
				continue;
			playersOnPoint.add(player);
			noOneOnPoint = false;
			if(owningTeam == null) {
				if(!tieBreaker.containsKey(team))
					tieBreaker.put(team, 0);
				tieBreaker.put(team, tieBreaker.get(team)+1);
				continue;
			}
			if(!teamOnPoint && team.equals(owningTeam))
				teamOnPoint = true;
			if(team.equals(owningTeam))
				toAdd += captureRate;
			else
				toAdd -= captureRate;
		}
		if(owningTeam == null && !tieBreaker.isEmpty()) {
			GameTeam bestTeam = null;
			int bestScore = 0;
			for(Entry<GameTeam, Integer> e : tieBreaker.entrySet()) {
				if(e.getValue() == bestScore)
					bestTeam = null;
				if(e.getValue()>bestScore) {
					bestTeam = e.getKey();
					bestScore = e.getValue();
				}
			}
			if(bestTeam != null) {
				owningTeam = bestTeam;
				update(); //Redoing the process now that a team owns the point
				return;
			}
		} else {
			if(!captured && !teamOnPoint)
				toAdd-=captureRate;
			else if(captured && noOneOnPoint)
				toAdd+=captureRate;
			captureProgress += toAdd;
			if(captureProgress>1) {
				if(!captured) {
					captured = true;
					for(GamePlayer p : playersOnPoint) {
						if(p.getTeam().equals(owningTeam))
							p.incrementStats("zone_capture", 1);
					}
					location.getWorld().strikeLightningEffect(location);
					for(Player p : location.getWorld().getPlayers()) {
						GamePlayer pl = GamePlayer.get(p);
						pl.notify(owningTeam.getColoredTeamName()+ChatColor.RESET+" has captured "+name);
					}
				}
				captureProgress=1;
			}
			else if(captureProgress < 0) {
				if(owningTeam != null) {
					for(GamePlayer p : playersOnPoint) {
						if(!p.getTeam().equals(owningTeam))
							p.incrementStats("zone_uncapture", 1);
					}
					if(captured) {
						location.getWorld().strikeLightningEffect(location);
						for(Player p : location.getWorld().getPlayers()) {
							GamePlayer pl = GamePlayer.get(p);
							pl.notify(owningTeam.getColoredTeamName()+ChatColor.RESET+" lost "+name);
						}
						captured = false;
					}
					owningTeam = null;
				}
				captureProgress = 0;
			}
		}
		updateBlocks();
		if(captured) {
			owningTeam.incrementScore(1);
		}
	}
	
	public boolean isStandingOnZone(Player player) {
		Block b = player.getLocation().add(0,-1,0).getBlock();
		if(b.getType() == Material.AIR)
			b = b.getRelative(BlockFace.DOWN);
		return blocks.contains(b);
	}
	
	public void updateBlocks() {
		if(owningTeam == null) {
			for(Block b : blocks) {
				if(b.getType() != zoneMaterial)
					b.setType(zoneMaterial);
			}
			return;
		}
		Material teamMaterial = getTeamMaterial();
		for(Block b : blocks) {
			double a = ((double)blocks.indexOf(b)) / blocks.size();
			if(a>=captureProgress) {
				if(b.getType() != zoneMaterial)
					b.setType(zoneMaterial);
			} else {
				if(b.getType() != teamMaterial)
					b.setType(teamMaterial);
			}
		}
	}
	
	private Material getTeamMaterial() {
		if(owningTeam == null)
			return zoneMaterial;
		if(zoneMaterial.toString().endsWith("_WOOL"))
			return owningTeam.getColor().wool();
		if(zoneMaterial.toString().endsWith("_GLAZED_TERRACOTTA"))
			return owningTeam.getColor().glazedTerracotta();
		if(zoneMaterial.toString().endsWith("_TERRACOTTA"))
			return owningTeam.getColor().terracotta();
		if(zoneMaterial.toString().endsWith("_CARPET"))
			return owningTeam.getColor().carpet();
		if(zoneMaterial.toString().endsWith("_CONCRETE"))
			return owningTeam.getColor().concrete();
		if(zoneMaterial.toString().endsWith("_CONCRETE_POWDER"))
			return owningTeam.getColor().concretePowder();
		if(zoneMaterial.toString().endsWith("_STAINED_GLASS"))
			return owningTeam.getColor().stainedGlass();
		if(zoneMaterial.toString().endsWith("_STAINED_GLASS_PANE"))
			return owningTeam.getColor().stainedGlassPane();
		if(zoneMaterial.toString().endsWith("_SHULKER_BOX"))
			return owningTeam.getColor().shulkerBox();
		return owningTeam.getSpawnMaterial();
	}
	
	private void grabBlocks() {
		List<Block> toCheck = new ArrayList<Block>();
		toCheck.add(location.getBlock());
		while(!toCheck.isEmpty()) {
			Block b = toCheck.get(0);
			for(BlockFace f : faces) {
				Block bl = b.getRelative(f);
				if(bl.getType() == zoneMaterial && !toCheck.contains(bl) && !blocks.contains(bl))
					toCheck.add(bl);
			}
			blocks.add(b);
			toCheck.remove(0);
		}
	}

}
