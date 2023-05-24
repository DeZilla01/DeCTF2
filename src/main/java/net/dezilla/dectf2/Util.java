package net.dezilla.dectf2;

import java.io.File;
import java.io.FileFilter;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.game.GameMatch;
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
	    float pitch = loc.getPitch();
	    for(;pitch < 0; pitch += 360F);
	    pitch %= 360F;
	    int pitchdir = Math.round(pitch/90F) % 4;

	    switch(pitchdir)
	    {
	        case 1:
	            return BlockFace.UP;
	        case 3:
	            return BlockFace.DOWN;
	        default:
	            break;
	    }

	    float yaw = loc.getYaw();
	    for(;yaw < 0; yaw += 360F);
	    yaw %= 360F;
	    int yawdir = Math.round(yaw / 90F) % 4;
	    switch(yawdir)
	    {
	        case 0:
	            return BlockFace.SOUTH;
	        case 1:
	            return BlockFace.WEST;
	        case 2:
	            return BlockFace.NORTH;
	        case 3:
	            return BlockFace.EAST;
	    }
	    return BlockFace.SELF;
	}
}
