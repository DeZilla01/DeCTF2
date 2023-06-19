package net.dezilla.dectf2.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.dezilla.dectf2.GamePlayer;

public class GameMapVote {
	List<String> zips;
	Map<GamePlayer, Integer> votes = new HashMap<GamePlayer, Integer>();
	
	public GameMapVote(List<String> zipNames) {
		zips = new ArrayList<String>(zipNames);
	}
	
	public int size() {
		return zips.size();
	}
	
	public void vote(GamePlayer player, int vote) {
		votes.put(player, vote);
	}
	
	public List<String> getZipList(){
		return new ArrayList<String>(zips);
	}
	
	public String getWinner() {
		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		for(Entry<GamePlayer, Integer> e : votes.entrySet()) {
			int i = e.getValue();
			if(!counts.containsKey(i))
				counts.put(i, 0);
			counts.put(i, counts.get(i)+1);
		}
		int highMap = 0;
		int highCount = 0;
		for(Entry<Integer, Integer> e : counts.entrySet()) {
			if(e.getValue() > highCount) {
				highMap = e.getKey();
				highCount = e.getValue();
			}
		}
		return zips.get(highMap);
	}
}
