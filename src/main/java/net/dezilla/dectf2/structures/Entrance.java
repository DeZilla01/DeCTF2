package net.dezilla.dectf2.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameCallout;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.ctf.CTFFlag;
import net.dezilla.dectf2.game.ctf.CTFGame;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Entrance extends BaseStructure{
	static int TELEPORTING_DELAY = 60;
	
	Block plate = null;
	ArmorStand display = null;
	Exit exit = null;

	public Entrance(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		removeOnSpawnProtection = true;
		destroyable = true;
		placeBlocks();
	}
	
	@Override
	public void onTick() {
		if(exit == null || exit.isDead()) {
			String name = "No Exit";
			if(!display.getCustomName().equals(name))
				display.setCustomName(name);
			if(!delays.isEmpty())
				delays.clear();
			return;
		}
		Location l = exit.getExitLocation();
		String name = "Teleporting to "+l.getBlockX()+" "+l.getBlockY()+" "+l.getBlockZ();
		if(GameMatch.currentMatch != null) {
			GameCallout c = GameMatch.currentMatch.getCalloutNear(l);
			if(c != null)
				name = "Teleporting to "+c.getName();
		}
		if(!display.getCustomName().equals(name))
			display.setCustomName(name);
		List<GamePlayer> toRemove = new ArrayList<GamePlayer>();
		for(Entry<GamePlayer, Integer> e : delays.entrySet()) {
			if(!e.getKey().getLocation().getBlock().equals(plate)) {
				toRemove.add(e.getKey());
				e.getKey().getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Teleportation cancelled"));
				continue;
			}
			delays.put(e.getKey(), e.getValue()-1);
			if(e.getValue() <= 0) {
				exit.teleport(e.getKey().getPlayer());
				toRemove.add(e.getKey());
				e.getKey().getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
			} else {
				int i = (e.getValue()/20)+1;
				e.getKey().getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Teleporting in "+i+" second(s)"));
			}
		}
		for(GamePlayer gp : toRemove) {
			delays.remove(gp);
		}
	}
	
	Map<GamePlayer, Integer> delays = new HashMap<GamePlayer, Integer>();
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(exit == null || exit.isDead())
			return;
		if(!event.getPlayer().getLocation().getBlock().equals(plate))
			return;
		GamePlayer gp = GamePlayer.get(event.getPlayer());
		if(!owner.getTeam().equals(gp.getTeam())) {
			gp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("You can't use enemy teleporters you dumb fuck"));
			return;
		}
		if(GameMatch.currentMatch != null && GameMatch.currentMatch.getGame() instanceof CTFGame) {
			CTFGame game = (CTFGame) GameMatch.currentMatch.getGame();
			CTFFlag flag = game.getHeldFlag(gp);
			if(flag != null) {
				gp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("So you want to teleport away with the flag heh? Well not today bitch!"));
				return;
			}
		}
		if(!delays.containsKey(gp))
			delays.put(gp, TELEPORTING_DELAY);
	}
	
	private void placeBlocks() throws CannotBuildException {
		Block b = location.getBlock().getRelative(BlockFace.DOWN);
		plate = b.getRelative(BlockFace.UP);
		addBlock(plate);
		addBlock(b);
		b.setType(owner.getTeam().getColor().wool());
		plate.setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		display = (ArmorStand) location.getWorld().spawnEntity(plate.getLocation().add(.5,.1,.5), EntityType.ARMOR_STAND);
		display.setVisible(false);
		display.setMarker(true);
		display.setInvulnerable(true);
		display.setGravity(false);
		display.setCustomNameVisible(true);
		display.setCustomName("Entry");
		entities.add(display);
	}
	
	public void setExit(Exit exit) {
		this.exit = exit;
	}

	@Override
	public boolean canPlace(Location location) {
		Block b = location.getBlock();
		if(!Util.air(b) || !Util.air(b.getRelative(BlockFace.UP)) || !Util.air(b.getRelative(BlockFace.UP).getRelative(BlockFace.UP)))
			return false;
		return true;
	}
	
	@Override
	public boolean bypassRestrictedAreas() {
		return false;
	}

}
