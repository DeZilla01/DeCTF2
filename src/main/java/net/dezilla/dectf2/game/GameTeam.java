package net.dezilla.dectf2.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.GameColor;

public class GameTeam {
	private int id;
	private GameColor teamColor;
	private String teamName;
	private List<GamePlayer> players = new ArrayList<GamePlayer>();
	
	
	public GameTeam(int id, GameColor color) {
		this.id = id;
		teamColor = color;
		teamName = color.getName();
	}
	
	public int size() {
		cleanPlayerList();
		return players.size();
	}
	
	public int getId() {
		return id;
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
