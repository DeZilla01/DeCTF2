package net.dezilla.dectf2.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameMatch;
import net.md_5.bungee.api.ChatColor;

public class MapPreview {
	File file;
	String name = "";
	String author = "";
	String gamemode = "";
	ItemStack icon = new ItemStack(Material.PAPER);
	int teams = 2;
	boolean selectable = true;
	
	public MapPreview(File file) {
		this.file = file;
		name = file.getName().replace(".zip", "");
		loadFromJSON();
	}
	
	public MapPreview(String file) {
		this.file = new File(Util.getGameMapFolder(), file);
		name = file.replace(".zip", "");
		loadFromJSON();
	}
	
	public File getFile() {
		return file;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNameAndMode(boolean colored) {
		return ChatColor.WHITE+"["+ChatColor.GOLD+gamemode.toUpperCase()+ChatColor.WHITE+"] "+(colored ? ChatColor.GOLD: ChatColor.WHITE)+name;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getGamemodeKey() {
		return gamemode;
	}
	
	public boolean isSelectable() {
		return selectable;
	}
	
	public String getGamemodeName() {
		switch(gamemode.toLowerCase()) {
			case "tdm": return "Team Deathmatch";
			case "ctf": return "Capture the Flag";
			case "zc" : return "Zone Control";
			case "dl": case "delivery": return "Delivery";
			case "arena": return "Arena";
			case "pl": case "payload": return "Payload";
		}
		return gamemode + "(unknow)";
	}
	
	public ItemStack getIcon() {
		return icon.clone();
	}
	
	public int getTeamAmount() {
		return teams;
	}
	
	private void loadFromJSON() {
		File jsonFile = new File(Util.getGameMapFolder(), "maps.json");
		if(!jsonFile.exists())
			return;
		JSONParser jsonParser = new JSONParser();
		try(FileReader reader = new FileReader(jsonFile)){
			JSONObject json = (JSONObject) jsonParser.parse(reader);
			if(!json.containsKey(file.getName()))
				return;
			JSONObject mapjson = (JSONObject) json.get(file.getName());
			try {name = (String) mapjson.get("name");}catch(Exception e) {}
			try {author = (String) mapjson.get("author");}catch(Exception e) {}
			try {gamemode = (String) mapjson.get("gamemode");}catch(Exception e) {}
			try {teams = (int) mapjson.get("teams");}catch(Exception e) {}
			try {icon = new ItemStack(Material.valueOf((String) mapjson.get("icon")));}catch(Exception e) {}
			try {selectable = (boolean) mapjson.get("selectable");}catch(Exception e) {}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void saveData(GameMatch match) {
		File jsonFile = new File(Util.getGameMapFolder(), "maps.json");
		if(!jsonFile.exists()) {
			JSONObject j = new JSONObject();
			try(FileWriter f = new FileWriter(jsonFile)){
				f.write(j.toJSONString());
				f.flush();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}try {
			JSONParser jsonParser = new JSONParser();
			FileReader reader = new FileReader(jsonFile);
			JSONObject json = (JSONObject) jsonParser.parse(reader);
			JSONObject mapjson = new JSONObject();
			mapjson.put("name", match.getMapName());
			mapjson.put("author", match.getMapAuthor());
			mapjson.put("gamemode", match.getGame().getGamemodeKey());
			mapjson.put("icon", match.getMapIcon().getType().toString());
			mapjson.put("teams", match.getTeamAmount());
			mapjson.put("selectable", match.isSelectable());
			json.put(match.getSourceZip().getName(), mapjson);
			try(FileWriter writer = new FileWriter(jsonFile)){
				writer.write(json.toString());
				writer.flush();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
