package net.dezilla.dectf2.game.ctf;

import org.bukkit.Location;

import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.util.GameColor;

public class CTFFlag {
	GameTeam team;
	GameColor color;
	
	public CTFFlag(GameTeam team, Location home) {
		this.team = team;
		this.color = team.getColor();
	}
}
