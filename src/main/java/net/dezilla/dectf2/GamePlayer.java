package net.dezilla.dectf2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;

public class GamePlayer {
	
	private static List<GamePlayer> PLAYERS = new ArrayList<GamePlayer>();
	
	public static GamePlayer get(Player player) {
		for(GamePlayer i : PLAYERS) {
			if(i.getPlayer().equals(player))
				return i;
			if(i.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				i.updatePlayer(player);
				return i;
			}
		}
		return new GamePlayer(player);
	}
	
	private Player player;
	private Scoreboard score;
	private Map<String, Integer> stats = new HashMap<String, Integer>();
	
	private GamePlayer(Player player) {
		this.player = player;
		PLAYERS.add(this);
		score = Bukkit.getScoreboardManager().getNewScoreboard();
		
		applyScoreboard();
		
		updateScoreboardDisplay();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	private void updatePlayer(Player player) {
		this.player = player;
		applyScoreboard();
		updateScoreboardDisplay();
	}
	
	public void updateScoreboardTeams() {
		GameMatch match = GameMatch.currentMatch;
		if(match == null)
			return;
		for(GameTeam t : match.getTeams()) {
			Team team = score.getTeam(""+t.getId());
			if(team==null) {
				team = score.registerNewTeam(""+t.getId());
				team.setPrefix(t.getColor().getPrefix());
				team.setColor(t.getColor().getChatColor());
			}
			for(GamePlayer p : t.getPlayers()) {
				if(!team.hasEntry(p.getPlayer().getName()))
					team.addEntry(p.getPlayer().getName());
			}
		}
	}
	
	public void updateScoreboardDisplay() {
		GameMatch match = GameMatch.currentMatch;
		Objective display = score.getObjective("display");
		if(display != null)
			display.unregister();
		display = score.registerNewObjective("display", Criteria.DUMMY, "display");
		display.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		List<String> displayList = new ArrayList<String>();
		displayList.add("DeCTF2");
		displayList.add("No game found");
		
		if(match != null && match.getGameState() == GameState.PREGAME) {
			displayList = match.preGameDisplay();
		} else if(match != null && match.getGameState() == GameState.INGAME) {
			displayList = match.getGame().getScoreboardDisplay(this);
		} else if(match != null && match.getGameState() == GameState.POSTGAME) {
			displayList.clear();
			displayList.add("DeCTF2");
			displayList.add("postgame display");
			displayList.add(match.getTimer().getTimeLeftDisplay());
		}
		display.setDisplayName(displayList.get(0));
		displayList.remove(0);
		
		int s = displayList.size()-1;
		for(String i : displayList)
			display.getScore(i).setScore(s--);
	}
	
	public void setStats(String key, int amount) {
		stats.put(key, amount);
	}
	
	public void incrementStats(String key, int amount) {
		stats.put(key, getStats(key)+amount);
	}
	
	public int getStats(String key) {
		if(!stats.containsKey(key))
			stats.put(key, 0);
		return stats.get(key);
	}
	
	public void applyScoreboard() {
		player.setScoreboard(score);
	}

}
