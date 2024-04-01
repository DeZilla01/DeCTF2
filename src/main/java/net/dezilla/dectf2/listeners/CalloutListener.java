package net.dezilla.dectf2.listeners;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockIterator;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameCallout;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.util.GameColor;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class CalloutListener implements Listener{
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!ItemBuilder.dataMatch(event.getItem(), "pointer"))
			return;
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		GamePlayer p = GamePlayer.get(event.getPlayer());
		Block b = p.getPlayer().getTargetBlock(Util.airListSet(), (int) (GameConfig.calloutRadius-1));
		if(Util.air(b)) {
			p.setLastItemDrop(null);
			return;
		}
		BlockFace f = getTargetFace(p.getPlayer(), b);
		Location l = b.getLocation();
		switch(f) {
			case EAST: l.add(1,.5,.5);break;
			case WEST: l.add(0,.5,.5);break;
			case NORTH: l.add(.5,.5,0);break;
			case SOUTH: l.add(.5,.5,1);break;
			case DOWN: l.add(.5,0,.5);break;
			case UP: l.add(.5,1,.5);break;
			default:{}
		}
		playCallout(p, l);
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		GamePlayer p = GamePlayer.get(event.getPlayer());
		Timestamp now = new Timestamp(System.currentTimeMillis());
		if(p.getLastItemDrop() == null) {
			p.setLastItemDrop(now);
			return;
		}
		Timestamp last = p.getLastItemDrop();
		if(now.getTime()-last.getTime() < 500) {
			Block b = p.getPlayer().getTargetBlock(Util.airListSet(), (int) (GameConfig.calloutRadius-1));
			if(Util.air(b)) {
				p.setLastItemDrop(null);
				return;
			}
			BlockFace f = getTargetFace(p.getPlayer(), b);
			Location l = b.getLocation();
			switch(f) {
				case EAST: l.add(1,.5,.5);break;
				case WEST: l.add(0,.5,.5);break;
				case NORTH: l.add(.5,.5,0);break;
				case SOUTH: l.add(.5,.5,1);break;
				case DOWN: l.add(.5,0,.5);break;
				case UP: l.add(.5,1,.5);break;
				default:{}
			}
			playCallout(p, l);
			p.setLastItemDrop(null);
		}
		else
			p.setLastItemDrop(now);
	}
	
	private Map<GamePlayer, Timestamp> lastCall = new HashMap<GamePlayer, Timestamp>();
	private static final int CALLOUT_DELAY = 5000;
	
	private void playCallout(GamePlayer player, Location location) {
		Timestamp now = new Timestamp(new Date().getTime());
		if(lastCall.containsKey(player) && now.getTime()-lastCall.get(player).getTime() < CALLOUT_DELAY) {
			return;
		}
		GameTeam team = null;
		GameMatch match = GameMatch.currentMatch;
		if(match != null && match.getTeam(player) != null)
			team = match.getTeam(player);
		List<Player> playersToNotify = new ArrayList<Player>();
		if(match == null)
			playersToNotify.addAll(Bukkit.getOnlinePlayers());
		else if(team == null) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(match.getTeam(GamePlayer.get(p)) == null)
					playersToNotify.add(p);
			}
		}
		else {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(match.getTeam(GamePlayer.get(p)) != null && match.getTeam(GamePlayer.get(p)).equals(team))
					playersToNotify.add(p);
			}
		}
		for(Player p : playersToNotify) {
			if(p.getLocation().distance(location) > GameConfig.calloutRadius)
				continue;
			p.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 3, 1);
			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
				if(p.isOnline())
					p.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 3, 1);
			}, 3);
			GameColor c = GameColor.WHITE;
			if(team != null)
				c = team.getColor();
			final GameColor C = c;
			GameTimer t = new GameTimer(3);
			t.unpause();
			t.onTick((timer) -> {
				if(timer.getTicks() % 5 == 0)
					showParticles(location, p, C);
			});
			t.onEnd((timer) -> timer.unregister());
			String where = null;
			if(match != null) {
				GameCallout call = match.getCalloutNear(location);
				if(call != null)
					where = call.getName();
			}
			if(where == null) {
				where = location.getBlockX()+" "+location.getBlockY()+" "+location.getBlockZ();
			}
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(player.getColoredName()+ChatColor.RESET+" pointed at "+where));
		}
		lastCall.put(player, now);
	}
	
	private static BlockFace getTargetFace(Player player, Block block) {
		Iterator<Block> itr = new BlockIterator(player, 50);
		Block previous = player.getEyeLocation().getBlock();
		while(itr.hasNext()) {
			Block b = itr.next();
			if(b.equals(block))
				return b.getFace(previous);
			else
				previous = b;
		}
		return null;
	}
	
	private static void showParticles(Location location, Player player, GameColor color) {
		for (int i = 0; i < 10; i++) {
	        for (int j = 0; j < 5; j++) {
	            double r = 0.8;
	            double phi = 2 * Math.PI * (i / 10f);
	            double theta = Math.PI * (j / 5f);

	            double x = r * Math.cos(phi) * Math.sin(theta);
	            double z = r * Math.sin(phi) * Math.sin(theta);
	            double y = r * Math.cos(theta);

	            Location l = location.clone().add(x, y, z);
	            
	            player.spawnParticle(Particle.REDSTONE, l, 1, 0, 0, 0, 1, new Particle.DustOptions(color.getBukkitColor(), 1f));
	        }
	    }
	}
}
