package net.dezilla.dectf2.game.ctf;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.util.GameColor;
import net.dezilla.dectf2.util.GameConfig;

public class CTFFlag {
	GameTeam team;
	GameColor color;
	FlagType type;
	Location homeLocation;
	boolean flagIsHome = true;
	Entity homeEntity = null;
	GamePlayer carrier = null;
	Entity flagEntity = null;
	ArmorStand sheepHolder = null;
	Chest homeChest = null;
	Material flagPostMaterial = null;
	ItemStack carrierHelmet = null;
	int woolTaskId = 0;
	GameTimer resetTimer = null;
	
	public CTFFlag(GameTeam team, Location home, FlagType type) {
		this.team = team;
		this.color = team.getColor();
		this.homeLocation = home;
		this.type = type;
		this.flagPostMaterial = home.getBlock().getRelative(BlockFace.DOWN).getType();
		resetFlag();
	}
	
	public void resetFlag() {
		if(homeEntity != null && !homeEntity.isDead()) {
			homeEntity.remove();
			homeEntity = null;
		}
		if(flagEntity != null && !flagEntity.isDead()) {
			flagEntity.remove();
			flagEntity = null;
		}
		if(sheepHolder != null && !sheepHolder.isDead()) {
			sheepHolder.remove();
			sheepHolder = null;
		}
		if(carrier != null) {
			unsetCarrier();
		}
		switch(type) {
			case BANNER: case WOOL:{
				flagEntity = spawnEntity(homeLocation, true);
				break;
			} 
			case SHEEP:{
				flagEntity = spawnEntity(homeLocation, true);
				sheepHolder = (ArmorStand) homeLocation.getWorld().spawnEntity(homeLocation, EntityType.ARMOR_STAND);
				sheepHolder.setVisible(false);
				sheepHolder.setGravity(false);
				sheepHolder.setInvulnerable(true);
				for(EquipmentSlot es : EquipmentSlot.values()) {
					sheepHolder.addEquipmentLock(es, LockType.ADDING_OR_CHANGING);
					sheepHolder.addEquipmentLock(es, LockType.REMOVING_OR_CHANGING);
				}
				((Sheep) flagEntity).setLeashHolder(sheepHolder);
				break;
			}
			case CHEST:{
				if(homeChest == null) {
					Block b = homeLocation.getBlock().getRelative(BlockFace.DOWN);
					b.setType(Material.CHEST);
					Rotatable r = ((Rotatable) b.getBlockData());
					r.setRotation(Util.getFacing(homeLocation.clone()));
					homeChest = (Chest) b.getState();
				}
				homeChest.getInventory().setItem(13, new ItemStack(color.spawnBlock()));
				break;
			}
		}
		flagIsHome = true;
	}
	
	private Entity spawnEntity(Location location, boolean colored) {
		switch(type) {
			case BANNER:{
				Location l = location.clone().add(0,-1.85,0);
				l = l.add(Util.getFacing(l).getDirection().multiply(.27));
				ArmorStand as = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
				as.setVisible(false);
				as.setGravity(false);
				as.setInvulnerable(true);
				for(EquipmentSlot es : EquipmentSlot.values()) {
					as.addEquipmentLock(es, LockType.ADDING_OR_CHANGING);
					as.addEquipmentLock(es, LockType.REMOVING_OR_CHANGING);
				}
				if(colored)
					as.getEquipment().setHelmet(new ItemStack(color.banner()));
				else
					as.getEquipment().setHelmet(new ItemStack(Material.WHITE_BANNER));
				return as;
			}
			case WOOL:{
				Item item;
				if(colored)
					item = location.getWorld().dropItem(location, new ItemStack(color.wool()));
				else
					item = location.getWorld().dropItem(location, new ItemStack(Material.WHITE_WOOL));
				item.setVelocity(new Vector(0,0,0));
				item.setGravity(false);
				item.setPickupDelay(999999999);
				item.setUnlimitedLifetime(true);
				return item;
			}
			case CHEST:{
				return null;
			}
			case SHEEP:{
				Sheep sheep = (Sheep) location.getWorld().spawnEntity(location, EntityType.SHEEP);
				if(colored)
					sheep.setColor(color.dyeColor());
				sheep.setInvulnerable(true);
				return sheep;
			}
		}
		return null;
	}
	
	private void unsetCarrier() {
		if(carrier==null)
			return;
		if(type == FlagType.BANNER) {
			if(carrierHelmet != null) {
				carrier.getPlayer().getEquipment().setHelmet(carrierHelmet);
				carrierHelmet = null;
			}
			else
				carrier.getPlayer().getEquipment().setHelmet(null);
		}
		if(type == FlagType.WOOL && woolTaskId != 0) {
			Bukkit.getScheduler().cancelTask(woolTaskId);
			woolTaskId = 0;
		}
		carrier = null;
	}
	
	public Location getLocation() {
		if(flagIsHome)
			return homeLocation.clone();
		else if(carrier != null)
			return carrier.getPlayer().getLocation();
		else
			return flagEntity.getLocation();
	}
	
	public FlagType getFlagType() {
		return type;
	}
	
	public boolean isHome() {
		return flagIsHome;
	}
	
	public boolean isDropped() {
		return !flagIsHome && carrier==null;
	}
	
	public boolean inStealRange(GamePlayer player) {
		return getLocation().distance(player.getPlayer().getLocation()) <= GameConfig.flagStealRadius;
	}
	
	public GameColor getColor() {
		return color;
	}
	
	public boolean isFlagMaterial(Material material) {
		if(material == color.banner() || material == color.wool() || material == color.spawnBlock())
			return true;
		return false;
	}
	
	public void setCarrier(GamePlayer player) {
		carrier = player;
		flagIsHome = false;
		switch(type) {
			case BANNER:{
				ItemStack h = player.getPlayer().getEquipment().getHelmet();
				if(h != null)
					carrierHelmet = h.clone();
				player.getPlayer().getEquipment().setHelmet(new ItemStack(color.banner()));
				if(flagEntity!=null && !flagEntity.isDead())
					flagEntity.remove();
				homeEntity = spawnEntity(homeLocation, false);
				player.getPlayer().getInventory().addItem(new ItemStack(color.banner()));
				break;
			}
			case WOOL:{
				woolTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> {
					if(carrier == null || !carrier.getPlayer().isOnline() || carrier.getPlayer().isDead() || GameMatch.currentMatch == null || GameMatch.currentMatch.getGameState() != GameState.INGAME) {
						Bukkit.getScheduler().cancelTask(woolTaskId);
						woolTaskId = 0;
						return;
					}
					Location l = carrier.getPlayer().getLocation().add(0,2.5,0);
					Item i = l.getWorld().dropItem(l, new ItemStack(color.wool()));
					i.setVelocity(i.getVelocity().multiply(new Vector(.3,2,.3)));
					Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> i.remove(), 12);
				}, 0, 4);
				break;
			}
		}
	}
	
	public void dropFlag(Location location, Vector velocity) {
		unsetCarrier();
		if(type == FlagType.BANNER) {
			flagEntity = spawnEntity(location, true);
			ArmorStand as = (ArmorStand) flagEntity;
			as.setGravity(true);
			as.setSmall(true);
			as.setVelocity(velocity);
		}
		resetTimer = new GameTimer(16);
		resetTimer.unpause();
		resetTimer.onTick((timer) -> {
			if(!isDropped())
				timer.unregister();
		});
		resetTimer.onEnd((timer) -> {
			resetFlag();
			timer.unregister();
		});
	}
	
	public GameTimer getResetTimer() {
		return resetTimer;
	}
	
	public GamePlayer getCarrier() {
		return carrier;
	}
	
	public static enum FlagType {
		BANNER,
		WOOL,
		CHEST,
		SHEEP;
	}
	
}
