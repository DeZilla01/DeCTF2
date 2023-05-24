package net.dezilla.dectf2.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameColor;

public class GameTeam {
	private int id;
	private GameColor teamColor;
	private String teamName;
	private List<GamePlayer> players = new ArrayList<GamePlayer>();
	private Location spawn;
	
	
	public GameTeam(int id, GameColor color, Location spawn) {
		this.id = id;
		teamColor = color;
		teamName = color.getName();
		this.spawn = spawn;
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
