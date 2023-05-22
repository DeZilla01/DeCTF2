package net.dezilla.dectf2.game.ctf;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.game.GameBase;
import net.dezilla.dectf2.game.GameMatch;

public class CTFGame extends GameBase implements Listener{
	private GameMatch match;
	
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
		
	}

	@Override
	public String getGamemodeName() {
		return "Capture the flag";
	}
}
