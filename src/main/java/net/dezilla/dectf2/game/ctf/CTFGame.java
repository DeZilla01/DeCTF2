package net.dezilla.dectf2.game.ctf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameBase;
import net.dezilla.dectf2.game.GameCallout;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.ctf.CTFFlag.FlagType;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ObjectiveLocation;
import net.dezilla.dectf2.util.RestrictArea;
import net.md_5.bungee.api.ChatColor;

public class CTFGame extends GameBase implements Listener{
	private static double FLAG_BUILDING_RESTRICT_RADIUS = 15;
	private GameMatch match;
	private Map<GameTeam, CTFFlag> flags = new HashMap<GameTeam, CTFFlag>();
	private int onTickTaskId = 0;
	Map<GamePlayer, GameTimer> stealDelays = new HashMap<GamePlayer, GameTimer>();
	
	public CTFGame(GameMatch match) {
		this.match = match;
		Bukkit.getPluginManager().registerEvents(this, GameMain.getInstance());
		onTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> onTick(), 0, 1);
	}
	
	@Override
	public void unregister() {
		if(!unregistered) {
			HandlerList.unregisterAll(this);
			Bukkit.getScheduler().cancelTask(onTickTaskId);
			unregistered = true;
		}	
	}
	
	private void onTick() {
		for(Player p : match.getWorld().getPlayers()) {
			GamePlayer gp = GamePlayer.get(p);
			CTFFlag held = getHeldFlag(gp);
			for(GameTeam team : match.getTeams()) {
				if(!flags.containsKey(team))
					continue;
				CTFFlag flag = flags.get(team);
				if(!flag.isHome() && !flag.isDropped())
					continue;
				if(held != null)
					continue;
				if(!flag.inStealRange(gp) || stealDelays.containsKey(gp))
					continue;
					
				//pick flag from home
				if(flag.isHome()) {
					if(gp.isInvisible() || gp.getTeam().equals(flag.getTeam()))
						continue;
					GameTimer timer = new GameTimer(-1);
					timer.onTick((t) -> {
						if(GameMatch.currentMatch == null || GameMatch.currentMatch.getGameState() != GameState.INGAME) {
							t.unregister();
							stealDelays.remove(gp);
						}
						if(t.getTicks() >= gp.getKit().getStealDelay()) {
							t.unregister();
							stealDelays.remove(gp);
							gp.incrementStats("steals", 1);
							flag.setCarrier(gp);
							flag.getLocation().getWorld().strikeLightningEffect(flag.getLocation());
							for(Player pl : Bukkit.getOnlinePlayers()) {
								GamePlayer.get(pl).notify(gp.getColoredName()+ChatColor.RESET+" has taken "+flag.getTeam().getColoredTeamName()+ChatColor.RESET+" "+flag.getFlagType().flag());
							}
							return;
						}
						if(!flag.inStealRange(gp)||!flag.isHome()) {
							t.unregister();
							stealDelays.remove(gp);
						}
					});
					stealDelays.put(gp, timer);
				}
				//pick flag from dropped
				else {
					//prevent player dropping the flag from instant re-pickup (else it's kinda janky)
					if(gp.equals(flag.getLastCarrier()) && flag.getResetTimer().getSeconds()==GameConfig.flagReset)
						continue;
					if(p.isInvisible())
						continue;
					if(flag.getTeam().equals(gp.getTeam()))
						continue;
					flag.setCarrier(gp);
					flag.getLocation().getWorld().strikeLightningEffect(flag.getLocation());
					for(Player pl : Bukkit.getOnlinePlayers()) {
						GamePlayer.get(pl).notify(gp.getColoredName()+ChatColor.RESET+" has taken "+flag.getTeam().getColoredTeamName()+ChatColor.RESET+" "+flag.getFlagType().flag());
					}
					break;
				}
			}
		}
	}
	
	@Override
	public void gameStart() {
		for(Entry<String, Location> e : match.signConfigs().entrySet()) {
			String s = e.getKey();
			if(s.contains("=")) {
				String[] a = s.split("=");
				if(a[0].equalsIgnoreCase("flag")) {
					try {
						int id = Integer.parseInt(a[1]);
						GameTeam team = match.getTeams()[id];
						CTFFlag flag = new CTFFlag(team, e.getValue(), GameConfig.flagType);
						flags.put(team, flag);
						match.addCallout(new GameCallout(e.getValue(), team, "Flag"));
						RestrictArea area = new RestrictArea(flag.getLocation(), FLAG_BUILDING_RESTRICT_RADIUS);
						area.setReason("Cannot build too close to the flag");
						match.addRestrictedArea(area);
					}catch(Exception ex) {}
				}
			}
		}
		for(GameTeam team : match.getTeams()) {
			if(!flags.containsKey(team)) {
				System.out.println("Warning, no flag configured for team "+team.getTeamName());
				CTFFlag flag = new CTFFlag(team, team.getSpawn(), GameConfig.flagType);
				flags.put(team, flag);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		GamePlayer p = GamePlayer.get(event.getPlayer());
		CTFFlag held = getHeldFlag(p);
		if(p.isSpawnProtected() && held != null) {
			p.setCustomDamageCause(CustomDamageCause.SPAWN_WITH_FLAG);
			p.getPlayer().damage(9999);
			held.resetFlag();
		}
		for(Entry<GameTeam, CTFFlag> entry : flags.entrySet()) {
			CTFFlag f = entry.getValue();
			if(p.getTeam().equals(entry.getKey())) {
				if(held != null && entry.getValue().inStealRange(p) && entry.getValue().isHome()) {
					p.incrementStats("captures", 1);
					held.resetFlag();
					for(Player pl : Bukkit.getOnlinePlayers()) {
						GamePlayer.get(pl).notify(held.getTeam().getColoredTeamName()+ChatColor.RESET+" "+held.getFlagType().flag()+" has been captured");
					}
					f.getLocation().getWorld().strikeLightningEffect(f.getLocation());
					p.getTeam().incrementScore(1);
				}
				if(f.isDropped() && f.inStealRange(p)) {
					f.resetFlag();
					p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
					for(Player pl : Bukkit.getOnlinePlayers()) {
						GamePlayer.get(pl).notify(f.getTeam().getColoredTeamName()+ChatColor.RESET+" "+f.getFlagType().flag()+" has been recovered");
					}
					p.incrementStats("recoveries", 1);
				}
				continue;
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		GamePlayer p = GamePlayer.get(event.getPlayer());
		CTFFlag held = getHeldFlag(p);
		Item item = event.getItemDrop();
		if(held != null && held.isFlag(item.getItemStack())) {
			held.dropFlag(item.getLocation().add(0, 1, 0), item.getVelocity());
			for(Player pl : Bukkit.getOnlinePlayers()) {
				GamePlayer.get(pl).notify(p.getColoredName()+ChatColor.RESET+" has dropped "+held.getTeam().getColoredTeamName()+ChatColor.RESET+" "+held.getFlagType().flag());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		GamePlayer p = GamePlayer.get(event.getPlayer());
		CTFFlag f = getHeldFlag(p);
		if(f != null) {
			f.dropFlag(p.getLocation().add(0,2,0), new Vector(0,0,0));
			for(Player pl : Bukkit.getOnlinePlayers()) {
				GamePlayer.get(pl).notify(p.getColoredName()+ChatColor.RESET+" has dropped "+f.getTeam().getColoredTeamName()+ChatColor.RESET+" "+f.getFlagType().flag());
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onDeath(PlayerDeathEvent event) {
		GamePlayer p = GamePlayer.get(event.getEntity());
		CTFFlag held = getHeldFlag(p);
		Location l = p.getLocation();
		if(held != null) {
			held.dropFlag(l.add(0,2,0), new Vector(0,0,0));
			for(Player pl : Bukkit.getOnlinePlayers()) {
				GamePlayer.get(pl).notify(p.getColoredName()+ChatColor.RESET+" has dropped "+held.getTeam().getColoredTeamName()+ChatColor.RESET+" "+held.getFlagType().flag());
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onItemMerge(ItemMergeEvent event) {
		if(event.getEntity().getItemStack().getType().toString().contains("WOOL")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Block b = event.getClickedBlock();
		if(b == null || event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		for(Entry<GameTeam, CTFFlag> entry : flags.entrySet()) {
			CTFFlag f = entry.getValue();
			if(f.getFlagType() == FlagType.CHEST && f.getHomeChest() != null && f.getHomeChest().getBlock().equals(b)) {
				event.getPlayer().openInventory(f.getHomeChest().getInventory());
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		GamePlayer p = GamePlayer.get((Player) event.getWhoClicked());
		for(Entry<GameTeam, CTFFlag> entry : flags.entrySet()) {
			CTFFlag f = entry.getValue();
			if(f.getFlagType() == FlagType.CHEST && f.getHomeChest() != null && p.getPlayer().getOpenInventory().getTopInventory().equals(f.getHomeChest().getInventory())) {
				event.setCancelled(true);
				CTFFlag held = this.getHeldFlag(p);
				if(held != null && f.getTeam().equals(p.getTeam()) && f.isHome()) {
					ItemStack is = event.getCurrentItem();
					if(is != null && held.getFlagItem().getType() == is.getType()) {
						p.incrementStats("captures", 1);
						held.resetFlag();
						for(Player pl : Bukkit.getOnlinePlayers()) {
							GamePlayer.get(pl).notify(held.getTeam().getColoredTeamName()+ChatColor.RESET+" "+held.getFlagType().flag()+" has been captured");
						}
						f.getLocation().getWorld().strikeLightningEffect(f.getLocation());
						p.getTeam().incrementScore(1);
					}
				}
				if(event.getCurrentItem() != null && event.getCurrentItem().getType() == f.getFlagItem().getType() && f.isHome()) {
					if(p.getTeam().equals(f.getTeam())) {
						p.notify("You cannot steal your team's flag");
					} else if(p.isInvisible()) {
						p.notify("You cannot steal the flag while invisible");
					} else if(held != null) {
						p.notify("You already have a flag to capture");
					} else {
						p.incrementStats("steals", 1);
						f.setCarrier(p);
						f.getLocation().getWorld().strikeLightningEffect(f.getLocation());
						for(Player pl : Bukkit.getOnlinePlayers()) {
							GamePlayer.get(pl).notify(p.getColoredName()+ChatColor.RESET+" has taken "+f.getTeam().getColoredTeamName()+ChatColor.RESET+" "+f.getFlagType().flag());
						}
					}
					Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> p.getPlayer().closeInventory());
				}
			}
		}
	}
	
	public CTFFlag getHeldFlag(GamePlayer player) {
		for(Entry<GameTeam, CTFFlag> entry : flags.entrySet()) {
			CTFFlag f = entry.getValue();
			if(f.getCarrier() != null && f.getCarrier().equals(player))
				return f;
		}
		return null;
	}
	
	public void updateFlagColors() {
		for(Entry<GameTeam, CTFFlag> entry : flags.entrySet()) {
			if(entry.getKey().getColor() != entry.getValue().getColor())
				entry.getValue().changeColor(entry.getKey().getColor());
		}
	}
	
	public CTFFlag getFlag(GameTeam team) {
		return flags.get(team);
	}

	@Override
	public String getGamemodeName() {
		return "Capture the flag";
	}
	
	@Override
	public String getGamemodeKey() {
		return "ctf";
	}
	
	@Override
	public int getDefaultScoreToWin() {
		return 3;
	}

	@Override
	public List<String> getScoreboardDisplay(GamePlayer player) {
		List<String> display = new ArrayList<String>();
		display.add("Ends in "+match.getTimer().getTimeLeftDisplay());
		GameTeam t = match.getTeam(player);
		if(t != null) {
			display.addAll(teamDisplay(t, true));
		}
		for(GameTeam team : match.getTeams()) {
			if(team.equals(t))
				continue;
			display.addAll(teamDisplay(team, false));
		}
		display.add(ChatColor.BOLD+"Your Stats");
		display.add(ChatColor.GOLD+" Kills "+ChatColor.RESET+player.getStats("kills"));
		display.add(ChatColor.GOLD+" Deaths "+ChatColor.RESET+player.getStats("deaths"));
		display.add(ChatColor.GOLD+" Streak "+ChatColor.RESET+player.getStats("streak"));
		display.add(ChatColor.GOLD+" Recoveries "+ChatColor.RESET+player.getStats("recoveries"));
		display.add(ChatColor.GOLD+" Captures "+ChatColor.RESET+player.getStats("captures"));
		display.add(""+ChatColor.GRAY+ChatColor.ITALIC+GameConfig.serverName);
		return display;
	}
	
	private List<String> teamDisplay(GameTeam team, boolean yourTeam){
		List<String> display = new ArrayList<String>();
		CTFFlag f = flags.get(team);
		if(match.getTeamAmount()<=2) {
			if(yourTeam)
				display.add(ChatColor.BOLD+team.getTeamName()+ChatColor.RESET+" - Your Team");
			else
				display.add(ChatColor.BOLD+team.getTeamName());
			display.add(team.getColor().getChatColor()+" Captures "+ChatColor.RESET+team.getScore()+"/"+match.getScoreToWin());
			display.add(team.getColor().getChatColor()+" "+f.getFlagType().flag()+" "+ChatColor.RESET+(f.isHome() ? f.getFlagType().home() : (f.getCarrier() != null ? "Taken" : "Dropped")));
			if(f.isHome())
				display.add(team.getColor().getChatColor()+"");
			else if (f.getCarrier()!= null)
				display.add(ChatColor.GRAY+" Held by "+f.getCarrier().getColoredName());
			else 
				display.add(ChatColor.GRAY+" Resets in "+f.getResetTimer().getSeconds());
		} else if (match.getTeamAmount()<=4) {
			if(yourTeam)
				display.add(ChatColor.BOLD+team.getTeamName()+ChatColor.RESET+" - Your Team");
			else
				display.add(ChatColor.BOLD+team.getTeamName());
			String scores = team.getColor().getChatColor()+" Cap "+ChatColor.RESET+team.getScore()+"/"+match.getScoreToWin();
			scores+=team.getColor().getChatColor()+" "+f.getFlagType().flag()+" ";
			if(f.isHome())
				scores+=ChatColor.WHITE+f.getFlagType().home();
			else if (f.getCarrier()!=null)
				scores+=f.getCarrier().getColoredName();
			else
				scores+=ChatColor.WHITE+"Dropped";
			display.add(scores);
		} else {
			String n = team.getTeamName();
			if(n.length()>=2)
				n = n.substring(0, 2);
			String stats = ""+ChatColor.BOLD+team.getColor().getChatColor()+n;
			stats+=ChatColor.RESET+" "+team.getScore()+"/"+match.getScoreToWin();
			stats+=team.getColor().getChatColor()+" "+f.getFlagType().flag()+" ";
			if(f.isHome())
				stats+=ChatColor.WHITE+f.getFlagType().home();
			else if (f.getCarrier()!=null)
				stats+=f.getCarrier().getColoredName();
			else
				stats+=ChatColor.WHITE+"Dropped";
			display.add(stats);
		}
		
		return display;
	}

	@Override
	public boolean hasObjectiveLocations() {
		return true;
	}

	@Override
	public List<ObjectiveLocation> getObjectiveLocations() {
		List<ObjectiveLocation> list = new ArrayList<ObjectiveLocation>();
		for(Entry<GameTeam, CTFFlag> e : flags.entrySet()) {
			String name = e.getKey().getColoredTeamName()+ChatColor.RESET+"'s "+e.getValue().getFlagType().flag();
			ObjectiveLocation o = new ObjectiveLocation(e.getValue().getLocation(), name, false);
			list.add(o);
		}
		return list;
	}
}
