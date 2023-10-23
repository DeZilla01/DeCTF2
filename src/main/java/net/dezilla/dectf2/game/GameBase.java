package net.dezilla.dectf2.game;

import java.util.List;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.ObjectiveLocation;

public abstract class GameBase {
	protected boolean unregistered = false;
	
	public abstract void unregister();
	
	public abstract void gameStart();
	
	public abstract String getGamemodeName();
	
	public abstract String getGamemodeKey();
	
	public abstract int getDefaultScoreToWin();
	
	public abstract List<String> getScoreboardDisplay(GamePlayer player);
	
	public abstract boolean hasObjectiveLocations();
	
	public abstract List<ObjectiveLocation> getObjectiveLocations();

}
