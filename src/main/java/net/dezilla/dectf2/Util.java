package net.dezilla.dectf2;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.dezilla.dectf2.util.GameConfig;

public class Util {
	
	static FileFilter zipFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return file.getName().endsWith(".zip");
		}
	};
	
	public static File[] getWorldList() {
		File worldFolder = getGameMapFolder();
		return worldFolder.listFiles(zipFileFilter);
	}
	
	public static File getGameMapFolder() {
		File folder = new File(GameConfig.mapFolder+File.separator);
		if(!folder.exists() && !folder.isDirectory())
			folder.mkdir();
		return folder;
	}
	
	public static File CreateMatchFolder(int id) {
		File folder = new File(GameConfig.gameFolderName+String.valueOf(id));
		if(!folder.exists() || !folder.isDirectory())
			folder.mkdir();
		else {
			deleteFolder(folder);
			folder.mkdir();
		}
		return folder;
	}
	
	public static File CreateFolder(String name) {
		File folder = new File(name);
		if(!folder.exists() || !folder.isDirectory())
			folder.mkdir();
		else {
			deleteFolder(folder);
			folder.mkdir();
		}
		return folder;
	}
	
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
	
	public static float getYaw(Vector vector) {
		return (float) Math.toDegrees(Math.atan2(-vector.getX(), vector.getZ()));
	}
	
	public static BlockFace getFacing(Location loc) {
		float yaw = loc.getYaw();
		while(yaw>180)
			yaw-=360;
		while(yaw<-180)
			yaw+=360;
		if(yaw<-135)
			return BlockFace.NORTH;
		if(yaw<-45)
			return BlockFace.EAST;
		if(yaw<45)
			return BlockFace.SOUTH;
		if(yaw<135)
			return BlockFace.WEST;
		return BlockFace.NORTH;
		
	}
	
	//used for shields
	public static Vector getKnockback(LivingEntity victim, Entity attacker) {
		double x = attacker.getLocation().getX() - victim.getLocation().getX();
		double z = attacker.getLocation().getZ() - victim.getLocation().getZ();
		double distance = Math.sqrt((x*x)+(z*z));
		x = (.28/distance)*-x;
		z = (.28/distance)*-z;
		return new Vector(x, .34, z);
	}
	
	public static Vector inFront(Player player, double distance) {
		float yaw = player.getEyeLocation().getYaw();
		float pitch = player.getEyeLocation().getPitch();
		while(yaw<-180)
			yaw+=360;
		while(yaw>180)
			yaw-=360;
		double a = pitch/90;
		if(a<0)
			a*=-1;
		a=(a-1)*-1;
		double x= distance * Math.sin(Math.PI * 2 * yaw / 360) * a;
		double z = -distance * Math.cos(Math.PI * 2 * yaw / 360) * a;
		double y = distance * Math.sin(Math.PI * 2 * pitch / 360);
		return new Vector(x, y, z);
		
	}
	
	public static double getDamageReduced(Player player) {
		List<ItemStack> armorItems = new ArrayList<ItemStack>();
		armorItems.add(player.getInventory().getHelmet());
		armorItems.add(player.getInventory().getChestplate());
		armorItems.add(player.getInventory().getLeggings());
		armorItems.add(player.getInventory().getBoots());
		while(armorItems.contains(null))
			armorItems.remove(null);
		int armorPoint = 0;
		double protection = 0.0;
		for(ItemStack item : armorItems) {
			switch(item.getType()) {
				case LEATHER_HELMET: case LEATHER_BOOTS: case GOLDEN_BOOTS: case CHAINMAIL_BOOTS:
					armorPoint+=1;break;
				case GOLDEN_HELMET: case CHAINMAIL_HELMET: case IRON_HELMET: case TURTLE_HELMET:
				case LEATHER_LEGGINGS: case IRON_BOOTS:
					armorPoint+=2;break;
				case DIAMOND_HELMET: case NETHERITE_HELMET: case LEATHER_CHESTPLATE:
				case GOLDEN_LEGGINGS: case DIAMOND_BOOTS: case NETHERITE_BOOTS:
					armorPoint+=3;break;
				case CHAINMAIL_LEGGINGS:
					armorPoint+=4;break;
				case GOLDEN_CHESTPLATE: case CHAINMAIL_CHESTPLATE: case IRON_LEGGINGS:
					armorPoint+=5;break;
				case IRON_CHESTPLATE: case DIAMOND_LEGGINGS: case NETHERITE_LEGGINGS:
					armorPoint+=6;break;
				case DIAMOND_CHESTPLATE: case NETHERITE_CHESTPLATE:
					armorPoint+=8;break;
				default: {}
			}
			if(item.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)) {
				player.sendMessage(""+item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL));
				protection+=(item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL)+1)*4;
			}
		}
		if(protection != 0)
			protection/=100;
		double result = 0.0;
		while(armorPoint!=0) {
			if(result==0)
				result+=.02;
			else
				result+=.04;
			armorPoint--;
		}
		double a = 1.0-result;
		result += (a*protection);
		return result;
    }
	
	public static GamePlayer getOwner(Entity entity) {
		if(entity instanceof Arrow) {
			Arrow a = (Arrow) entity;
			if(a.getShooter() != null && a.getShooter() instanceof Entity)
				entity = (Entity) a.getShooter();
		}
		if(entity instanceof Snowball) {
			Snowball s = (Snowball) entity;
			if(s.getShooter() != null && s.getShooter() instanceof Entity)
				entity = (Entity) s.getShooter();
		}
		if(entity instanceof Egg) {
			Egg e = (Egg) entity;
			if(e.getShooter() != null && e.getShooter() instanceof Entity)
				entity = (Entity) e.getShooter();
		}
		if(entity instanceof Player) {
			Player p = (Player) entity;
			return GamePlayer.get(p);
		}
		return null;
	}
	
	public static String grabConfigText(Sign sign) {
		String pattern1 = Pattern.quote("{") + "(.*)" + Pattern.quote("}");
		String pattern2 = Pattern.quote("{") + "(.*)=(.*)" + Pattern.quote("}");
		List<String> lines = new ArrayList<String>();
		lines.addAll(Arrays.asList(sign.getSide(Side.FRONT).getLines()));
		lines.addAll(Arrays.asList(sign.getSide(Side.BACK).getLines()));
		String s = "";
		for(String i : lines) {
			if(i.matches(pattern1) || i.matches(pattern2))
				continue;
			s+=i;
		}
		return s;
	}
	
	public static ItemStack createTexturedHead(String texture) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();

		mutateItemMeta(meta, texture);
		head.setItemMeta(meta);

		return head;
	}
	
	private static Method metaSetProfileMethod;
	private static Field metaProfileField;
	
	private static void mutateItemMeta(SkullMeta meta, String b64) {
		//This is not my code. I stole it from another plugin. I'm a terrible person
		try {
			if (metaSetProfileMethod == null) {
				metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
				metaSetProfileMethod.setAccessible(true);
			}
			metaSetProfileMethod.invoke(meta, makeProfile(b64));
		} catch (Exception ex) {
			// if in an older API where there is no setProfile method,
			// we set the profile field directly.
			try {
				if (metaProfileField == null) {
					metaProfileField = meta.getClass().getDeclaredField("profile");
					metaProfileField.setAccessible(true);
				}
				metaProfileField.set(meta, makeProfile(b64));

			} catch (NoSuchFieldException | IllegalAccessException ex2) {
				ex2.printStackTrace();
			}
		}
	}
	
	private static GameProfile makeProfile(String b64) {
		//This is not my code I stole it from another plugin. I'm a terrible person
		// random uuid based on the b64 string
		UUID id = new UUID(
				b64.substring(b64.length() - 20).hashCode(),
				b64.substring(b64.length() - 10).hashCode()
		);
		GameProfile profile = new GameProfile(id, "aaaaa");
		profile.getProperties().put("textures", new Property("textures", b64));
		return profile;
	}
	
	
	
}
