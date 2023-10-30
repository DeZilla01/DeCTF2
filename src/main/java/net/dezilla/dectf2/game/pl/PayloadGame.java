package net.dezilla.dectf2.game.pl;

import java.util.List;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameBase;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.util.ObjectiveLocation;

public class PayloadGame extends GameBase{
	GameMatch match;
	
	public PayloadGame(GameMatch match) {
		this.match = match;
	}

	@Override
	public void unregister() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getGamemodeName() {
		return "Payload";
	}

	@Override
	public String getGamemodeKey() {
		return "pl";
	}

	@Override
	public int getDefaultScoreToWin() {
		return 999;
	}

	@Override
	public List<String> getScoreboardDisplay(GamePlayer player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasObjectiveLocations() {
		return false;
	}

	@Override
	public List<ObjectiveLocation> getObjectiveLocations() {
		// TODO Auto-generated method stub
		return null;
	}

}
