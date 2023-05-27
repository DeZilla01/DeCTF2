package net.dezilla.dectf2.game;

import java.util.List;

import net.dezilla.dectf2.GamePlayer;

public abstract class GameBase {
	protected boolean unregistered = false;
	
	public abstract void unregister();
	
	public abstract void gameStart();
	
	public abstract String getGamemodeName();
	
	public abstract List<String> getScoreboardDisplay(GamePlayer player);

}
