package net.dezilla.dectf2;

import java.util.ArrayList;
import java.util.List;

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
		
		if(match == null) {
			score.clearSlot(DisplaySlot.SIDEBAR);
		} else if(match.getGameState() == GameState.PREGAME) {
			List<String> pregame = match.preGameDisplay();
			display.setDisplayName(pregame.get(0));
			pregame.remove(0);
			
			int s = pregame.size()-1;
			for(String i : pregame)
				display.getScore(i).setScore(s--);
		}
	}
	
	public void applyScoreboard() {
		player.setScoreboard(score);
	}

}
