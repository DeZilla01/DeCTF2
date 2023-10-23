package net.dezilla.dectf2.game.zc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameBase;
import net.dezilla.dectf2.game.GameCallout;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ObjectiveLocation;
import net.dezilla.dectf2.util.RestrictArea;
import net.md_5.bungee.api.ChatColor;

public class ZonesGame extends GameBase implements Listener{
	private static String pattern1 = Pattern.quote("{") + "(.*)" + Pattern.quote("}");
	
	private GameMatch match;
	private int onTickTaskId = 0;
	private List<Zone> zones = new ArrayList<Zone>();
	private int tickCount = 1;
	
	public ZonesGame(GameMatch match){
		this.match = match;
		Bukkit.getPluginManager().registerEvents(this, GameMain.getInstance());
		onTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> onTick(), 0, 1);
		for(Sign s : match.signConfigz()) {
			for(Side side : Side.values()) {
				for(String line : s.getSide(side).getLines()) {
					if(line.matches(pattern1)) {
						String value = line.replaceAll(pattern1, "$1").toLowerCase();
						if(value.equalsIgnoreCase("zone")) {
							String name = Util.grabConfigText(s);
							Location loc = s.getLocation().add(0,-1,0);
							Zone z = new Zone(loc, name);
							for(Side ss : Side.values()) {
								int c = 0;
								for(String ll : s.getSide(ss).getLines()) {
									if(ll.isEmpty()) {
										c++;
										continue;
									}
									try {
										double d = Double.parseDouble(ll);
										z.setCaptureRate(d);
										s.getSide(ss).setLine(c, "");
										z.setName(Util.grabConfigText(s));
										break;
									}catch(Exception e) {}
									c++;
								}
							}
							for(Block b : z.blocks) {
								Location l = b.getLocation().add(.5,1,.5);
								match.addRestrictedArea(new RestrictArea(l, 1));
							}
							zones.add(z);
							match.addCallout(new GameCallout(loc, name));
						}
					}
				}
			}
		}
	}
	
	private void onTick() {
		if(match.getGameState() != GameState.INGAME)
			return;
		if(tickCount % 20 == 0) {
			for(Zone z : zones) {
				z.update();
			}
			boolean allCaptured = true;
			GameTeam winning = null;
			for(Zone z : zones) {
				if(!z.isCaptured()) {
					allCaptured = false;
					break;
				}
				if(winning == null)
					winning = z.getOwningTeam();
				else if(!winning.equals(z.getOwningTeam())) {
					allCaptured = false;
					break;
				}
			}
			if(allCaptured) {
				winning.setScore(match.getScoreToWin());
				match.endGame();
			}
			tickCount=1;
		} else {
			tickCount++;
		}
	}
	
	public List<Zone> getZones(){
		return new ArrayList<Zone>(zones);
	}

	@Override
	public void unregister() {
		if(!unregistered) {
			HandlerList.unregisterAll(this);
			Bukkit.getScheduler().cancelTask(onTickTaskId);
			unregistered = true;
		}	
	}

	@Override
	public void gameStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getGamemodeName() {
		return "Zone Control";
	}

	@Override
	public String getGamemodeKey() {
		return "zc";
	}

	@Override
	public int getDefaultScoreToWin() {
		return 1000;
	}

	@Override
	public List<String> getScoreboardDisplay(GamePlayer player) {
		List<String> list = new ArrayList<String>();
		list.add("Ends in "+match.getTimer().getTimeLeftDisplay());
		for(Zone z : zones) {
			if(z.isCaptured()) {
				ChatColor c = z.getOwningTeam().getColor().getChatColor();
				list.add(""+c+ChatColor.BOLD+z.getName()+c+" - "+((int) (z.getCaptureProgress()*100))+"%");
			} else {
				list.add(ChatColor.BOLD+z.getName()+ChatColor.RESET+" - "+((int) (z.getCaptureProgress()*100))+"%");
			}
			String bar = "";
			for(int i = 0; i < GameConfig.captureBarSize ; i++) {
				double a = ((double) i)/GameConfig.captureBarSize;
				if(z.getOwningTeam() != null && z.getCaptureProgress()>a)
					bar+=z.getOwningTeam().getColor().getChatColor()+"▒";
				else
					bar+=ChatColor.WHITE+"▒";
			}
			list.add(bar);
		}
		list.addAll(getScores());
		list.add(ChatColor.BOLD+"Your Stats");
		list.add(ChatColor.GOLD+" Kills "+ChatColor.RESET+player.getStats("kills"));
		list.add(ChatColor.GOLD+" Deaths "+ChatColor.RESET+player.getStats("deaths"));
		list.add(ChatColor.GOLD+" Streak "+ChatColor.RESET+player.getStats("streak"));
		list.add(ChatColor.GOLD+" Captures "+ChatColor.RESET+player.getStats("zone_capture"));
		list.add(ChatColor.GOLD+" Uncaptures "+ChatColor.RESET+player.getStats("zone_uncapture"));
		list.add(""+ChatColor.GRAY+ChatColor.ITALIC+GameConfig.serverName);
		return list;
	}
	
	private List<String> getScores(){
		List<String> list = new ArrayList<String>();
		int count = 1;
		String s = "";
		for(GameTeam team : match.getTeams()) {
			s+=team.getColoredTeamName()+ChatColor.RESET+" "+team.getScore();
			if(count % 2 == 0) {
				list.add(s);
				s = "";
			} else {
				s+=" ";
			}
		}
		if(!s.isEmpty())
			list.add(s);
		return list;
	}

	@Override
	public boolean hasObjectiveLocations() {
		return true;
	}

	@Override
	public List<ObjectiveLocation> getObjectiveLocations() {
		List<ObjectiveLocation> list = new ArrayList<ObjectiveLocation>();
		for(Zone z : zones) {
			list.add(new ObjectiveLocation(z.getLocation(), z.getName(), true));
		}
		return list;
	}

}
