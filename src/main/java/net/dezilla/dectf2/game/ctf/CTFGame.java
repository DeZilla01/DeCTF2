package net.dezilla.dectf2.game.ctf;

import net.dezilla.dectf2.game.GameBase;
import net.dezilla.dectf2.game.GameMatch;

public class CTFGame extends GameBase{
	private GameMatch match;
	
	public CTFGame(GameMatch match) {
		this.match = match;
	}

	@Override
	public String getGamemodeName() {
		return "Capture the flag";
	}
}
