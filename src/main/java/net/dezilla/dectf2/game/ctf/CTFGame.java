package net.dezilla.dectf2.game.ctf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameBase;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.ctf.CTFFlag.FlagType;
import net.dezilla.dectf2.util.GameConfig;

public class CTFGame extends GameBase implements Listener{
	private GameMatch match;
	private Map<GameTeam, CTFFlag> flags = new HashMap<GameTeam, CTFFlag>();
	private int onTickTaskId = 0;
	
	public CTFGame(GameMatch match) {
		this.match = match;
		Bukkit.getPluginManager().registerEvents(this, GameMain.getInstance());
		onTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> onTick(), 0, 1);
	}
	
	@Override
	public void unregister() {
		if(!unregistered) {
			HandlerList.unregisterAll(this);
			Bukkit.getScheduler().cancelTask(onTickTaskId);
			unregistered = true;
		}	
	}
	
	private void onTick() {
	}
	
	@Override
	public void gameStart() {
		for(Entry<String, Location> e : match.signConfigs().entrySet()) {
			String s = e.getKey().replace("[", "");
			s = s.replace("]", "");
			if(s.contains("=")) {
				String[] a = s.split("=");
				if(a[0].equalsIgnoreCase("flag")) {
					try {
						int id = Integer.parseInt(a[1]);
						GameTeam team = match.getTeams()[id];
						CTFFlag flag = new CTFFlag(team, e.getValue(), GameConfig.flagType);
						flags.put(team, flag);
					}catch(Exception ex) {}
				}
			}
		}
		for(GameTeam team : match.getTeams()) {
			if(!flags.containsKey(team)) {
				System.out.println("Warning, no flag configured for team "+team.getTeamName());
				CTFFlag flag = new CTFFlag(team, team.getSpawn(), GameConfig.flagType);
				flags.put(team, flag);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		GamePlayer p = GamePlayer.get(event.getPlayer());
		for(Entry<GameTeam, CTFFlag> entry : flags.entrySet()) {
			if(p.getTeam().equals(entry.getKey())) {
				CTFFlag held = getHeldFlag(p);
				if(held != null && entry.getValue().inStealRange(p)) {
					p.incrementStats("captures", 1);
					held.resetFlag();
					p.getTeam().incrementScore(1);
				}
				continue;
			}
			CTFFlag f = entry.getValue();
			if(f.isHome() || f.isDropped()) {
				if(f.inStealRange(p))
					f.setCarrier(p);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		GamePlayer p = GamePlayer.get(event.getPlayer());
		CTFFlag held = getHeldFlag(p);
		Item item = event.getItemDrop();
		if(held != null && held.isFlagMaterial(item.getItemStack().getType())) {
			held.dropFlag(item.getLocation().add(0, 1, 0), item.getVelocity());
		}
	}
	
	public CTFFlag getHeldFlag(GamePlayer player) {
		for(Entry<GameTeam, CTFFlag> entry : flags.entrySet()) {
			CTFFlag f = entry.getValue();
			if(f.getCarrier() != null && f.getCarrier().equals(player))
				return f;
		}
		return null;
	}

	@Override
	public String getGamemodeName() {
		return "Capture the flag";
	}
	
	@Override
	public int getDefaultScoreToWin() {
		return 3;
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
		display.add(ChatColor.GOLD+" Recoveries "+ChatColor.RESET+player.getStats("recoveries"));
		display.add(ChatColor.GOLD+" Captures "+ChatColor.RESET+player.getStats("captures"));
		display.add(""+ChatColor.GRAY+ChatColor.ITALIC+"dezilla.net");
		return display;
	}
	
	private List<String> teamDisplay(GameTeam team, boolean yourTeam){
		List<String> display = new ArrayList<String>();
		CTFFlag f = flags.get(team);
		if(yourTeam)
			display.add(ChatColor.BOLD+team.getTeamName()+ChatColor.RESET+" - Your Team");
		else
			display.add(ChatColor.BOLD+team.getTeamName());
		display.add(team.getColor().getChatColor()+" Captures "+ChatColor.RESET+team.getScore()+"/"+match.getScoreToWin());
		display.add(team.getColor().getChatColor()+" Flag "+ChatColor.RESET+(f.isHome() ? "Home" : (f.getCarrier() != null ? "Taken" : "Dropped")));
		if(f.isHome())
			display.add(team.getColor().getChatColor()+"");
		else if (f.getCarrier()!= null)
			display.add(ChatColor.GRAY+" Held by "+f.getCarrier().getPlayer().getDisplayName());
		else 
			display.add(ChatColor.GRAY+" Resets in "+f.getResetTimer().getSeconds());
		return display;
	}
}
