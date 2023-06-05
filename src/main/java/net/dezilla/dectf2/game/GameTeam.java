package net.dezilla.dectf2.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.ctf.CTFGame;
import net.dezilla.dectf2.util.GameColor;

public class GameTeam {
	private int id;
	private GameColor teamColor;
	private String teamName;
	private List<GamePlayer> players = new ArrayList<GamePlayer>();
	private Location spawn;
	private Material spawnMaterial;
	private int score = 0;
	private List<Block> spawnBlocks = new ArrayList<Block>();
	private List<Block> coloredBlocks = new ArrayList<Block>();
	
	
	public GameTeam(int id, GameColor color, Location spawn) {
		this.id = id;
		teamColor = color;
		teamName = color.getName();
		this.spawn = spawn;
		this.spawnMaterial = color.spawnBlock();
	}
	
	public int size() {
		cleanPlayerList();
		return players.size();
	}
	
	public int getId() {
		return id;
	}
	
	public Location getSpawn() {
		return spawn.clone();
	}
	
	public int incrementScore(int amount) {
		score+=amount;
		return score;
	}
	
	public void setScore(int amount) {
		score = amount;
	}
	
	public int getScore() {
		return score;
	}
	
	public Material getSpawnMaterial() {
		return spawnMaterial;
	}
	
	public void setSpawnMaterial(Material material) {
		spawnMaterial = material;
	}
	
	public void addSpawnBlock(Block block) {
		spawnBlocks.add(block);
	}
	
	public void addColorBlock(Block block) {
		coloredBlocks.add(block);
	}
	
	public boolean isSpawnMaterial(Material material) {
		return material == spawnMaterial;
	}
	
	public boolean isSpawnBlock(Block block) {
		GameMatch match = GameMatch.currentMatch;
		if(match==null)
			return false;
		if(match.isBlockParsed()) 
			return spawnBlocks.contains(block);
		return block.getType() == spawnMaterial;
	}
	
	public boolean isTeamColor(Material material) {
		if(material.toString().contains(teamColor.getMaterialName())) {
			for(Material m : teamColor.coloredMaterials()) {
				if(material==m) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void changeTeamColor(GameColor color) {
		GameColor oldColor = teamColor;
		teamColor = color;
		if(teamName.equalsIgnoreCase(oldColor.getName()))
			teamName = color.getName();
		spawnMaterial = color.spawnBlock();
		if(GameMatch.currentMatch != null) {
			GameMatch match = GameMatch.currentMatch;
			if(match.getGame() instanceof CTFGame) {
				CTFGame game = (CTFGame) match.getGame();
				game.updateFlagColors();
			}
		}
		for(Block b : spawnBlocks)
			b.setType(spawnMaterial);
		for(Block b : coloredBlocks) {
			if(b.getBlockData() instanceof Directional) {
				Directional d = (Directional) b.getBlockData();
				BlockFace f = d.getFacing();
				if(b.getType().toString().contains("_GLAZED_TERRACOTTA"))
					b.setType(color.glazedTerracotta());
				else if(b.getType().toString().contains("_SHULKER_BOX")) {
					ShulkerBox s = (ShulkerBox) b.getState();
					Inventory i = s.getInventory();
					b.setType(color.shulkerBox());
					s = (ShulkerBox) b.getState();
					s.getInventory().setContents(i.getContents());
					s.update();
				}
				else if(b.getType().toString().contains("_BED")) {
					Bed bed = (Bed) b.getBlockData();
					Part part = bed.getPart();
					b.setType(color.bed());
					bed = (Bed) b.getBlockData();
					bed.setPart(part);
				}
				else if(b.getType().toString().contains("_WALL_BANNER")) {
					Banner banner = (Banner) b.getState();
					List<Pattern> patterns = banner.getPatterns();
					b.setType(color.wallBanner());
					banner = (Banner) b.getState();
					banner.setPatterns(patterns);
					banner.update();
				}
				d = (Directional) b.getBlockData();
				d.setFacing(f);
			}
			if(b.getBlockData() instanceof Rotatable) {
				Rotatable r = (Rotatable) b.getBlockData();
				BlockFace f = r.getRotation();
				if(b.getType().toString().contains("_BANNER")) {
					Banner banner = (Banner) b.getState();
					List<Pattern> patterns = banner.getPatterns();
					b.setType(color.banner());
					banner = (Banner) b.getState();
					banner.setPatterns(patterns);
					banner.update();
				}
				r = (Rotatable) b.getBlockData();
				r.setRotation(f);
			}
			if(b.getBlockData() instanceof Lightable && b.getBlockData() instanceof Waterlogged) {
				Lightable light = (Lightable) b.getBlockData();
				Waterlogged water = (Waterlogged) b.getBlockData();
				boolean lit = light.isLit();
				boolean waterlogged = water.isWaterlogged();
				if(b.getType().toString().contains("_CANDLE")) {
					Candle c = (Candle) b.getBlockData();
					int amount = c.getCandles();
					b.setType(color.candle());
					c = (Candle) b.getBlockData();
					c.setCandles(amount);
				}
				light = (Lightable) b.getBlockData();
				water = (Waterlogged) b.getBlockData();
				light.setLit(lit);
				water.setWaterlogged(waterlogged);
			}
			if(b.getType().toString().contains("_WOOL"))
				b.setType(color.wool());
			else if(b.getType().toString().contains("_CARPET"))
				b.setType(color.carpet());
			else if(b.getType().toString().contains("_TERRACOTTA"))
				b.setType(color.terracotta());
			else if(b.getType().toString().contains("_CONCRETE_POWDER"))
				b.setType(color.concretePowder());
			else if(b.getType().toString().contains("_CONCRETE"))
				b.setType(color.concrete());
			else if(b.getType().toString().contains("_STAINED_GLASS_PANE"))
				b.setType(color.stainedGlassPane());
			else if(b.getType().toString().contains("_STAINED_GLASS"))
				b.setType(color.stainedGlass());
		}
	}
	
	public void addPlayer(GamePlayer player) {
		players.add(player);
		for(Player p : Bukkit.getOnlinePlayers()) {
			GamePlayer.get(p).updateScoreboardTeams();
		}
	}
	
	public void removePlayer(GamePlayer player) {
		if(players.contains(player))
			players.remove(player);
	}
	
	public boolean isInTeam(GamePlayer player) {
		cleanPlayerList();
		return players.contains(player);
	}
	
	public GamePlayer[] getPlayers() {
		return players.toArray(new GamePlayer[players.size()]);
	}
	
	public GameColor getColor() {
		return teamColor;
	}
	
	public String getTeamName() {
		return teamName;
	}
	
	public void setTeamName(String name) {
		teamName = name;
	}
	
	public String getColoredTeamName() {
		return teamColor.getPrefix()+teamName;
	}
	
	private void cleanPlayerList() {
		List<GamePlayer> toRemove = new ArrayList<GamePlayer>();
		for(GamePlayer gp : players) {
			if(!gp.getPlayer().isOnline())
				toRemove.add(gp);
		}
		for(GamePlayer gp : toRemove)
			players.remove(gp);
	}
}
