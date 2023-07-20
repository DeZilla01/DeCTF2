package net.dezilla.dectf2.game.ctf;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameMatch.GameState;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameColor;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class CTFFlag {
	GameTeam team;
	GameColor color;
	FlagType type;
	Location homeLocation;
	boolean flagIsHome = true;
	Entity homeEntity = null;
	GamePlayer carrier = null;
	GamePlayer lastCarrier = null;
	Entity flagEntity = null;
	ArmorStand sheepHolder = null;
	Chest homeChest = null;
	Material flagPostMaterial = null;
	ItemStack carrierHelmet = null;
	int woolTaskId = 0;
	int sheepTaskId = 0;
	GameTimer resetTimer = null;
	boolean bannerGrounded = false;
	boolean dmgWarned = false;
	
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
				Location l = homeLocation.clone().add(0,-1,0);
				sheepHolder = (ArmorStand) homeLocation.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
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
					Directional d = (Directional) b.getBlockData();
					d.setFacing(Util.getFacing(homeLocation.clone()));
					b.setBlockData(d);
					homeChest = (Chest) b.getState();
				}
				homeChest.getInventory().setItem(13, new ItemStack(color.spawnBlock()));
				break;
			}
		}
		if(flagEntity != null && GameConfig.flagGlow) {
			flagEntity.setGlowing(true);
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
				as.setMarker(true);
				as.setVisible(false);
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
				Location l = location.clone().add(0,.5,0);
				if(colored)
					item = location.getWorld().dropItem(l, new ItemStack(color.wool()));
				else
					item = location.getWorld().dropItem(l, new ItemStack(Material.WHITE_WOOL));
				item.setVelocity(new Vector(0,0,0));
				item.setGravity(false);
				item.setPickupDelay(999999999);
				item.setUnlimitedLifetime(true);
				return item;
			}
			case CHEST:{
				Item item;
				Location l = location.clone().add(0,.5,0);
				item = location.getWorld().dropItem(l, new ItemStack(color.spawnBlock()));
				item.setVelocity(new Vector(0,0,0));
				item.setGravity(false);
				item.setPickupDelay(999999999);
				item.setUnlimitedLifetime(true);
				return item;
			}
			case SHEEP:{
				Sheep sheep = (Sheep) location.getWorld().spawnEntity(location, EntityType.SHEEP);
				if(colored)
					sheep.setColor(color.dyeColor());
				else 
					sheep.setColor(DyeColor.WHITE);
				sheep.setInvulnerable(true);
				return sheep;
			}
		}
		return null;
	}
	
	private void unsetCarrier() {
		if(carrier==null)
			return;
		lastCarrier = carrier;
		if(!carrier.getPlayer().isOnline()) {
			carrier = null;
			return;
		}
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
		if(carrier.getPlayer().hasPotionEffect(PotionEffectType.GLOWING))
			carrier.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
		final GamePlayer pl = carrier;
		Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> {
			if(!pl.getPlayer().isOnline())
				return;
			for(ItemStack item : pl.getPlayer().getInventory().getContents()) {
				if(item!=null && ItemBuilder.getData(item) != null && ItemBuilder.getData(item).equals("flag"))
					pl.getPlayer().getInventory().remove(item);
			}
		});
		carrier = null;
	}
	
	public Location getLocation() {
		if(flagIsHome)
			return homeLocation.clone();
		else if(carrier != null)
			return carrier.getPlayer().getLocation();
		else if(flagEntity instanceof ArmorStand && bannerGrounded)
			return flagEntity.getLocation().add(0,.9,0);
		else
			return flagEntity.getLocation();
	}
	
	public void changeColor(GameColor color) {
		this.color = color;
		if(isHome())
			resetFlag();
		else if(isDropped()) {
			if(flagEntity instanceof ArmorStand) {
				ArmorStand as = (ArmorStand) flagEntity;
				as.getEquipment().setHelmet(getFlagItem());
			} else if(flagEntity instanceof Sheep) {
				Sheep s = (Sheep) flagEntity;
				s.setColor(color.dyeColor());
			} else if(flagEntity instanceof Item) {
				Item i = (Item) flagEntity;
				i.setItemStack(getFlagItem());
			}
		}
		else if(carrier != null && type == FlagType.BANNER)
			carrier.getPlayer().getInventory().setHelmet(new ItemStack(color.banner()));
	}
	
	public FlagType getFlagType() {
		return type;
	}
	
	public void setFlagType(FlagType type) {
		this.type = type;
		if(type!=FlagType.CHEST && homeChest != null) {
			homeChest.getBlock().setType(flagPostMaterial);
			homeChest = null;
		}
		if(isHome())
			resetFlag();
	}
	
	public boolean isHome() {
		return flagIsHome;
	}
	
	public boolean isDropped() {
		return !flagIsHome && carrier==null;
	}
	
	public boolean inStealRange(GamePlayer player) {
		if(type == FlagType.CHEST && isHome())
			return false;
		return getLocation().distance(player.getPlayer().getLocation()) <= GameConfig.flagStealRadius;
	}
	
	public GameColor getColor() {
		return color;
	}
	
	public GameTeam getTeam() {
		return team;
	}
	
	public Chest getHomeChest() {
		return homeChest;
	}
	
	@Deprecated
	public boolean isFlagMaterial(Material material) {
		if(material == color.banner() || material == color.wool() || material == color.spawnBlock())
			return true;
		return false;
	}
	
	public boolean isFlag(ItemStack item) {
		return ItemBuilder.getData(item).equals("flag");
	}
	
	public ItemStack getFlagItem() {
		Material m = color.banner();
		if(type == FlagType.WOOL)
			m = color.wool();
		else if(type == FlagType.SHEEP) {
			ItemStack s;
			switch (color) {
            	case BLACK:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzI2NTIwODNmMjhlZDFiNjFmOWI5NjVkZjFhYmYwMTBmMjM0NjgxYzIxNDM1OTUxYzY3ZDg4MzY0NzQ5ODIyIn19fQ==");break;
            	case BLUE:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDllYzIyODE4ZDFmYmZjODE2N2ZiZTM2NzI4YjI4MjQwZTM0ZTE2NDY5YTI5MjlkMDNmZGY1MTFiZjJjYTEifX19");break;
            	case BROWN:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTU1YWQ2ZTVkYjU2OTJkODdmNTE1MTFmNGUwOWIzOWZmOWNjYjNkZTdiNDgxOWE3Mzc4ZmNlODU1M2I4In19fQ==");break;
            	case CYAN:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZmNmM3ZTdmZDUxNGNlMGFjYzY4NTkzMjI5ZTQwZmNjNDM1MmI4NDE2NDZlNGYwZWJjY2NiMGNlMjNkMTYifX19");break;
            	case DARK_GRAY:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI4N2ViNTAxMzkxZjI3NTM4OWYxNjZlYzlmZWJlYTc1ZWM0YWU5NTFiODhiMzhjYWU4N2RmN2UyNGY0YyJ9fX0=");break;
            	case DARK_GREEN:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTAxNzIxNWM3ZjhkYjgyMDQwYWEyYzQ3Mjk4YjY2NTQxYzJlYjVmN2Y5MzA0MGE1ZDIzZDg4ZjA2ODdkNGIzNCJ9fX0=");break;
            	case LIGHT_BLUE:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM4YTk3YTM4ODU2NTE0YTE2NDEzZTJjOTk1MjEyODlmNGM1MzYzZGM4MmZjOWIyODM0Y2ZlZGFjNzhiODkifX19");break;
            	case GREEN:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJhMjQ0OGY1OGE0OTEzMzI0MzRlODVjNDVkNzg2ZDg3NDM5N2U4MzBhM2E3ODk0ZTZkOTI2OTljNDJiMzAifX19");break;
            	case MAGENTA:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTgzNjU2NWM3ODk3ZDQ5YTcxYmMxODk4NmQxZWE2NTYxMzIxYTBiYmY3MTFkNDFhNTZjZTNiYjJjMjE3ZTdhIn19fQ==");break;
            	case ORANGE:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjA5ODM5N2EyNzBiNGMzZDJiMWU1NzRiOGNmZDNjYzRlYTM0MDkwNjZjZWZlMzFlYTk5MzYzM2M5ZDU3NiJ9fX0=");break;
            	case PINK:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmFjNzRhMmI5YjkxNDUyZTU2ZmExZGRhNWRiODEwNzc4NTZlNDlmMjdjNmUyZGUxZTg0MWU1Yzk1YTZmYzVhYiJ9fX0=");break;
            	case PURPLE:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWU1Mjg2N2FmZWYzOGJiMTRhMjZkMTQyNmM4YzBmMTE2YWQzNDc2MWFjZDkyZTdhYWUyYzgxOWEwZDU1Yjg1In19fQ==");break;
            	case RED:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM5YWY0NzdlYjYyNzgxNWY3MjNhNTY2MjU1NmVjOWRmY2JhYjVkNDk0ZDMzOGJkMjE0MjMyZjIzZTQ0NiJ9fX0=");break;
            	case GRAY:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2UxYWM2ODM5OTNiZTM1NTEyZTFiZTMxZDFmNGY5OGU1ODNlZGIxNjU4YTllMjExOTJjOWIyM2I1Y2NjZGMzIn19fQ==");break;
            	case YELLOW:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjZhNDExMmRmMWU0YmNlMmE1ZTI4NDE3ZjNhYWZmNzljZDY2ZTg4NWMzNzI0NTU0MTAyY2VmOGViOCJ9fX0=");break;
            		//white
            	default:
            		s = Util.createTexturedHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMxZjljY2M2YjNlMzJlY2YxM2I4YTExYWMyOWNkMzNkMThjOTVmYzczZGI4YTY2YzVkNjU3Y2NiOGJlNzAifX19");break;
			}
			return ItemBuilder.of(s).name(team.getTeamName()+" "+type.fName).data("flag").get();
		}
		else if(type == FlagType.CHEST)
			m = color.spawnBlock();
		return ItemBuilder.of(m).name(team.getTeamName()+" "+type.fName).data("flag").get();
	}
	
	public void setCarrier(GamePlayer player) {
		carrier = player;
		if(GameConfig.flagGlow)
			player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 0));
		switch(type) {
			case BANNER:{
				ItemStack h = player.getPlayer().getEquipment().getHelmet();
				if(h != null)
					carrierHelmet = h.clone();
				player.getPlayer().getEquipment().setHelmet(new ItemStack(color.banner()));
				if(flagEntity!=null && !flagEntity.isDead())
					flagEntity.remove();
				if(isHome())
					homeEntity = spawnEntity(homeLocation, false);
				player.getPlayer().getInventory().addItem(getFlagItem());
				break;
			}
			case WOOL:{
				if(flagEntity!=null && !flagEntity.isDead())
					flagEntity.remove();
				if(isHome())
					homeEntity = spawnEntity(homeLocation, false);
				player.getPlayer().getInventory().addItem(getFlagItem());
				woolTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> {
					if(carrier == null || 
							!carrier.getPlayer().isOnline() || 
							carrier.getPlayer().isDead() || 
							GameMatch.currentMatch == null || 
							GameMatch.currentMatch.getGameState() != GameState.INGAME ||
							type != FlagType.WOOL) {
						Bukkit.getScheduler().cancelTask(woolTaskId);
						woolTaskId = 0;
						return;
					}
					Location l = carrier.getPlayer().getLocation().add(0,2.5,0);
					Item i = l.getWorld().dropItem(l, new ItemStack(color.wool()));
					i.setVelocity(i.getVelocity().multiply(new Vector(.3,2,.3)));
					i.setGlowing(true);
					Bukkit.getScheduler().scheduleSyncDelayedTask(GameMain.getInstance(), () -> i.remove(), 12);
				}, 0, 4);
				break;
			}
			case SHEEP:{
				if(flagEntity != null && !flagEntity.isDead() && !(flagEntity instanceof Sheep)) {
					flagEntity.remove();
				}
				if(flagEntity==null || flagEntity.isDead())
					flagEntity = spawnEntity(player.getLocation(), true);
				Sheep sheep = (Sheep) flagEntity;
				sheep.setLeashHolder(player.getPlayer());
				if(isHome()) {
					homeEntity = spawnEntity(homeLocation, false);
					Sheep s = (Sheep) homeEntity;
					s.setLeashHolder(sheepHolder);
				}
				player.getPlayer().getInventory().addItem(getFlagItem());
				sheepTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> {
					if(carrier == null || 
							!carrier.getPlayer().isOnline() ||
							player.getPlayer().isDead() ||
							GameMatch.currentMatch == null ||
							GameMatch.currentMatch.getGameState() != GameState.INGAME ||
							type != FlagType.SHEEP) {
						Bukkit.getScheduler().cancelTask(sheepTaskId);
						sheepTaskId = 0;
						return;
					}
					if(flagEntity == null || flagEntity.isDead())
						flagEntity = spawnEntity(carrier.getLocation(), true);
					Sheep s = (Sheep) flagEntity;
					if(!s.isLeashed()) {
						s.teleport(carrier.getLocation());
						s.setLeashHolder(carrier.getPlayer());
					}
				}, 1, 1);
				break;
			}
			case CHEST:{
				if(flagEntity != null && !flagEntity.isDead()) {
					flagEntity.remove();
					flagEntity=null;
				}
				homeChest.getInventory().setItem(13, null);
				player.getPlayer().getInventory().addItem(getFlagItem());
				break;
			}
		}
		GameTimer timer = new GameTimer(-1);
		dmgWarned = false;
		timer.onTick((t) -> {
			if(carrier == null || carrier.getPlayer().isDead() || GameMatch.currentMatch == null || GameMatch.currentMatch.getGameState() != GameState.INGAME) {
				t.unregister();
				return;
			}
			if(t.getTicks() % 300 == 0) {
				if(!dmgWarned) {
					carrier.getPlayer().sendMessage("The "+type.fName+" is poisoning you");
					dmgWarned = true;
				}
				carrier.setCustomDamageCause(CustomDamageCause.FLAG_POISON);
				carrier.getPlayer().damage(3);
			}
		});
		flagIsHome = false;
	}
	
	public void dropFlag(Location location, Vector velocity) {
		unsetCarrier();
		bannerGrounded = false;
		if(type == FlagType.BANNER) {
			flagEntity = spawnEntity(location, true);
			ArmorStand as = (ArmorStand) flagEntity;
			as.setMarker(false);
			as.setGravity(true);
			as.setSmall(true);
			as.setVelocity(velocity);
		}
		else if(type == FlagType.WOOL) {
			flagEntity = spawnEntity(location, true);
			Item i = (Item) flagEntity;
			i.setGravity(true);
			i.setVelocity(velocity);
		}
		else if(type == FlagType.CHEST) {
			flagEntity = spawnEntity(location, true);
			Item i = (Item) flagEntity;
			i.setGravity(true);
			i.setVelocity(velocity);
		}
		else if(type == FlagType.SHEEP) {
			if(flagEntity == null || flagEntity.isDead())
				flagEntity = spawnEntity(location, true);
			Sheep s = (Sheep) flagEntity;
			s.setLeashHolder(null);
		}
		if(flagEntity != null && GameConfig.flagGlow) {
			flagEntity.setGlowing(true);
		}
		resetTimer = new GameTimer(GameConfig.flagReset);
		resetTimer.unpause();
		resetTimer.onTick((timer) -> {
			if(!isDropped()) {
				timer.unregister();
				return;
			}
			if(flagEntity.isDead()) {
				resetFlag();
				timer.unregister();
			}
			if(flagEntity instanceof ArmorStand && !bannerGrounded) {
				ArmorStand as = (ArmorStand) flagEntity;
				if(as.isOnGround()) {
					as.setGravity(false);
					as.teleport(as.getLocation().add(0,-.9,0));
					as.setMarker(true);
					bannerGrounded = true;
				}
			}
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
	
	public GamePlayer getLastCarrier() {
		return lastCarrier;
	}
	
	public static enum FlagType {
		BANNER("Flag", "Home"),
		WOOL("Flag", "Home"),
		CHEST("Flag", "Chest"),
		SHEEP("Sheep", "Home");
		String fName;
		String hName;
		FlagType(String flagName, String homeName){
			fName = flagName;
			hName = homeName;
		}
		public String home() {
			return hName;
		}
		public String flag() {
			return fName;
		}
	}
	
}
