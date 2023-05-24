package net.dezilla.dectf2.game.ctf;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.game.GameBase;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.ctf.CTFFlag.FlagType;

public class CTFGame extends GameBase implements Listener{
	private GameMatch match;
	private Map<GameTeam, CTFFlag> flags = new HashMap<GameTeam, CTFFlag>();
	
	public CTFGame(GameMatch match) {
		this.match = match;
		Bukkit.getPluginManager().registerEvents(this, GameMain.getInstance());
	}
	
	@Override
	public void unregister() {
		HandlerList.unregisterAll(this);
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
						CTFFlag flag = new CTFFlag(team, e.getValue(), FlagType.BANNER);
						flags.put(team, flag);
					}catch(Exception ex) {}
				}
			}
		}
		for(GameTeam team : match.getTeams()) {
			if(!flags.containsKey(team)) {
				System.out.println("Warning, no flag configured for team "+team.getTeamName());
				CTFFlag flag = new CTFFlag(team, team.getSpawn(), FlagType.BANNER);
				flags.put(team, flag);
			}
		}
	}

	@Override
	public String getGamemodeName() {
		return "Capture the flag";
	}
}
