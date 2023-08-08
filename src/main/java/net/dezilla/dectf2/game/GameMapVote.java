package net.dezilla.dectf2.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.MapPreview;

public class GameMapVote {
	List<MapPreview> previews;
	Map<GamePlayer, Integer> votes = new HashMap<GamePlayer, Integer>();
	
	public GameMapVote(List<MapPreview> previews) {
		this.previews = previews;
	}
	
	public int size() {
		return previews.size();
	}
	
	public void vote(GamePlayer player, int vote) {
		votes.put(player, vote);
	}
	
	public List<MapPreview> getMapList(){
		return new ArrayList<MapPreview>(previews);
	}
	
	public int getVotes(String fileName) {
		int index = -4;
		for(MapPreview m : previews) {
			if(m.getFile().getName().equals(fileName)) {
				index = previews.indexOf(m);
				break;
			}
		}
		if(index==-1)
			return 0;
		int count = 0;
		for(Entry<GamePlayer, Integer> e : votes.entrySet())
			if(e.getValue() == index)
				count++;
		return count;
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
		return previews.get(highMap).getFile().getName();
	}
}
