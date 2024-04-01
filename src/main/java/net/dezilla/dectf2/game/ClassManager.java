package net.dezilla.dectf2.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.kits.BaseKit;
import net.dezilla.dectf2.util.KitLimitException;

// -1 = No Limit
// 0 = Disabled
public class ClassManager {
	Map<String, KitManager> kits = new HashMap<String, KitManager>();
	
	public ClassManager() {
		for(Entry<String,Class<? extends BaseKit>> e : GameMain.getInstance().kitMap().entrySet()) {
			KitManager m = new KitManager(e.getValue());
			kits.put(m.getKitName(), m);
		}
	}
	
	public KitManager getManager(String kitName) {
		if(!kits.containsKey(kitName))
			return null;
		return kits.get(kitName);
	}
	
	public void canUseKitCheck(GamePlayer player, BaseKit kit) throws KitLimitException {
		int kitAmount = 0;
		int varAmount = 0;
		
		KitManager manager = getManager(kit.getName().toLowerCase());
		if(manager.getLimit() == -1 && manager.getLimit(kit.getVariation().toLowerCase()) == -1)
			return;
		if(manager.getLimit() == 0)
			throw new KitLimitException("This class is disabled");
		if(manager.getLimit(kit.getVariation().toLowerCase()) == 0)
			throw new KitLimitException("This variation is disabled");
		
		Map<String, Map<String, Integer>> bigMap = player.getTeam().getKitUsage();
		if(bigMap.containsKey(kit.getName().toLowerCase())) {
			for(Entry<String, Integer> e : bigMap.get(kit.getName().toLowerCase()).entrySet()) {
				kitAmount+=e.getValue();
			}
			if(bigMap.get(kit.getName().toLowerCase()).containsKey(kit.getVariation().toLowerCase())) {
				varAmount = bigMap.get(kit.getName().toLowerCase()).get(kit.getVariation().toLowerCase());
			}
		}
		
		if(kitAmount >= manager.getLimit())
			throw new KitLimitException("Your team has reached the limit for this class ("+kitAmount+"/"+manager.getLimit()+")");
		if(varAmount >= manager.getLimit(kit.getVariation().toLowerCase()))
			throw new KitLimitException("Your team has reached the limit for this variation ("+varAmount+"/"+manager.getLimit(kit.getVariation().toLowerCase())+")");
	}
	
	public static class KitManager{
		Class<? extends BaseKit> kit;
		String kitName;
		Map<String, Integer> limits = new HashMap<String, Integer>();
		int limit = -1;
		
		public KitManager(Class<? extends BaseKit> kit){
			this.kit = kit;
			try {
				GamePlayer p = null;//somehow this works.... don't ask
				BaseKit k = kit.getConstructor(GamePlayer.class).newInstance(p);
				kitName = k.getName().toLowerCase();
				for(String s : k.getVariations()) {
					limits.put(s, -1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		public String getKitName() {
			return kitName;
		}
		
		public int getLimit() {
			return limit;
		}
		
		public int getLimit(String variation) {
			if(!limits.containsKey(variation))
				return 0;
			return limits.get(variation);
		}
		
		public Map<String, Integer> getLimits(){
			return new HashMap<String, Integer>(limits);
		}
		
		public void setLimit(int limit) {
			this.limit = limit;
		}
		
		public void setLimit(String variation, int limit) {
			limits.put(variation, limit);
		}
	}
}
