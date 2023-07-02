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
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

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
