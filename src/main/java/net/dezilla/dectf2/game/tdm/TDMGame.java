package net.dezilla.dectf2.game.tdm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameBase;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ObjectiveLocation;
import net.md_5.bungee.api.ChatColor;

public class TDMGame extends GameBase implements Listener{
	private GameMatch match;
	private Map<GameTeam, Double> dmgScore = new HashMap<GameTeam, Double>();
	
	public TDMGame(GameMatch match) {
		this.match = match;
		Bukkit.getPluginManager().registerEvents(this, GameMain.getInstance());
	}
	
	@Override
	public void gameStart() {
		//Well this is a really basic gamemode, not much to write =/
		for(GameTeam t : match.getTeams()) {
			dmgScore.put(t, 0.0);
		}
	}
	
	@Override
	public void unregister() {
		HandlerList.unregisterAll(this);
	}
	
	public void addKill(GameTeam team) {
		team.incrementScore(1);
	}
	
	public void addDamage(GameTeam team, double amount) {
		if(!dmgScore.containsKey(team))
			dmgScore.put(team, 0.0);
		dmgScore.put(team, dmgScore.get(team)+amount);
	}

	@Override
	public String getGamemodeName() {
		return "Team Deathmatch";
	}
	
	@Override
	public String getGamemodeKey() {
		return "tdm";
	}
	
	@Override
	public int getDefaultScoreToWin() {
		return 150;
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onDamageEntity(EntityDamageByEntityEvent event) {
		GamePlayer damager = Util.getOwner(event.getDamager());
		if(damager == null)
			return;
		if(!dmgScore.containsKey(damager.getTeam()))
			dmgScore.put(damager.getTeam(), 0.0);
		dmgScore.put(damager.getTeam(), dmgScore.get(damager.getTeam())+event.getFinalDamage());
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		GamePlayer victim = GamePlayer.get(event.getEntity());
		GamePlayer killer = victim.getLastAttacker();
		if(killer == null)
			return;
		killer.getTeam().incrementScore(1);
	}

	@Override
	public List<String> getScoreboardDisplay(GamePlayer player) {
		List<String> display = new ArrayList<String>();
		display.add("Ends in "+match.getTimer().getTimeLeftDisplay());
		GameTeam t = match.getTeam(player);
		if(t != null) {
			display.addAll(teamDisplay(t, true));
		}
		for(GameTeam team : match.getTeams()) {
			if(team.equals(t))
				continue;
			display.addAll(teamDisplay(team, false));
		}
		display.add(ChatColor.BOLD+"Your Stats");
		display.add(ChatColor.GOLD+" Kills "+ChatColor.RESET+player.getStats("kills"));
		display.add(ChatColor.GOLD+" Deaths "+ChatColor.RESET+player.getStats("deaths"));
		display.add(ChatColor.GOLD+" Streak "+ChatColor.RESET+player.getStats("streak"));
		display.add(ChatColor.GOLD+" Damage "+ChatColor.RESET+String.format("%.2f", player.getDamageDealt()));
		display.add(""+ChatColor.GRAY+ChatColor.ITALIC+GameConfig.serverName);
		return display;
	}
	
	private List<String> teamDisplay(GameTeam team, boolean yourTeam){
		List<String> display = new ArrayList<String>();
		if(!dmgScore.containsKey(team))
			dmgScore.put(team, 0.0);
		if(yourTeam)
			display.add(ChatColor.BOLD+team.getTeamName()+ChatColor.RESET+" - Your Team");
		else
			display.add(ChatColor.BOLD+team.getTeamName());
		display.add(team.getColor().getChatColor()+" Kills "+ChatColor.RESET+team.getScore()+"/"+match.getScoreToWin());
		display.add(team.getColor().getChatColor()+" Damage "+ChatColor.RESET+String.format("%.2f", dmgScore.get(team)));
		display.add(team.getColor().getChatColor()+"");
		
		return display;
	}

	@Override
	public boolean hasObjectiveLocations() {
		return false;
	}

	@Override
	public List<ObjectiveLocation> getObjectiveLocations() {
		return null;
	}

	
}
