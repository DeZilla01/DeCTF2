package net.dezilla.dectf2.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.listeners.events.SpongeLaunchEvent;

public class SpongeListener implements Listener{
	static final double X_INTENSITY = 3;
	static final double Y_INTENSITY = 1;
	
	@EventHandler(ignoreCancelled=true)
	public void onSponge(PlayerMoveEvent event) {
		if(isLaunched(event.getPlayer()) || event.getPlayer().getGameMode() == GameMode.SPECTATOR)
			return;
		Material mat = event.getTo().getBlock().getRelative(BlockFace.DOWN).getType();
		if(mat == Material.SPONGE || mat == Material.WET_SPONGE) {
			Block b = event.getTo().getBlock();
			launch(event.getPlayer(), getLaunchVector(b));
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onDamage(EntityDamageEvent event) {
		if(event.getEntityType() == EntityType.PLAYER && event.getCause() == DamageCause.FALL) {
			Player p = (Player) event.getEntity();
			if(cancelFall.contains(p)) {
				event.setCancelled(true);
				cancelFall.remove(p);
			}
		}
	}
	
	private static List<SpongeLaunchEvent> spongeEvents = new ArrayList<SpongeLaunchEvent>();
	private static List<Player> cancelFall = new ArrayList<Player>();
	
	public static boolean isLaunched(Player player) {
		cleanList();
		for(SpongeLaunchEvent i : spongeEvents) {
			if(i.getPlayer().equals(player))
				return true;
		}
		return false;
	}
	
	private static void cleanList() {
		List<SpongeLaunchEvent> toRemove = new ArrayList<SpongeLaunchEvent>();
		for(SpongeLaunchEvent i : spongeEvents) {
			if(i.isComplete())
				toRemove.add(i);
		}
		for(SpongeLaunchEvent i : toRemove) {
			spongeEvents.remove(i);
		}
	}
	
	public static void launch(Player player, Vector vector) {
		SpongeLaunchEvent event = new SpongeLaunchEvent(player, vector);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled())
			return;
		spongeEvents.add(event);
		int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> {
			Player p = event.getPlayer();
			
			if(!p.isOnline() || p.isDead()) {
				Bukkit.getScheduler().cancelTask(event.getTaskId());
				event.setComplete(true);
				return;
			}
			
			Vector v = new Vector();
			double x = event.getX();
			double y = event.getY();
			double z = event.getZ();
			
			if(x<=-1) {
				v.setX(-X_INTENSITY);
				event.incrementVector(1, 0, 0);
			} else if(x>=1) {
				v.setX(X_INTENSITY);
				event.incrementVector(-1, 0, 0);
			}
			if(y<=-1) {
				v.setY(-Y_INTENSITY);
				event.incrementVector(0, 1, 0);
			} else if(y>=1) {
				v.setY(Y_INTENSITY);
				event.incrementVector(0, -1, 0);
			}
			if(z<=-1) {
				v.setZ(-X_INTENSITY);
				event.incrementVector(0, 0, 1);
			} else if(z>=1) {
				v.setZ(X_INTENSITY);
				event.incrementVector(0, 0, -1);
			}
			
			if(v.getX() == 0 && v.getY() == 0 && v.getZ() == 0) {
				Bukkit.getScheduler().cancelTask(event.getTaskId());
				event.setComplete(true);
				if(!cancelFall.contains(p)) {
					GameTimer timer = new GameTimer(-1);
					cancelFall.add(p);
					timer.unpause();
					timer.onTick((t) -> {
						if(!cancelFall.contains(p)) {
							t.unregister();
							return;
						}
						if(Util.onGround(p)) {
							Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
								if(cancelFall.contains(p)) {
									cancelFall.remove(p);
								}
								t.unregister();
							}, 1);
						}
					});
				}
			}
			p.setVelocity(v);
		}, 0, 1);
		event.setTaskId(taskId);
	}
	
	private static Vector getLaunchVector(Block block) {
		Block b = block.getRelative(BlockFace.DOWN, 2);
		double x = stackedSponges(b, BlockFace.WEST) - stackedSponges(b, BlockFace.EAST);
		double z = stackedSponges(b, BlockFace.NORTH) - stackedSponges(b, BlockFace.SOUTH);
		double y = stackedSponges(b, null) + 1;
		Vector v = new Vector(x, y, z).multiply(10);
		return v;
	}
	
	private static double stackedSponges(Block block, BlockFace face) {
		Block b = block;
		if(face!=null)
			b = b.getRelative(face);
		int i = 0;
		while(b.getType()==Material.SPONGE||b.getType()==Material.WET_SPONGE) {
			i++;
			b = b.getRelative(BlockFace.DOWN);
		}
		return i;
	}
}
