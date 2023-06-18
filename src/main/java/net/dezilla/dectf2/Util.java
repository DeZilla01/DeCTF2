package net.dezilla.dectf2;

import java.io.File;
import java.io.FileFilter;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

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
}
