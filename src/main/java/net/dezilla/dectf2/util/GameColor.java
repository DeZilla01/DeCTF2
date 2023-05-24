package net.dezilla.dectf2.util;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

public enum GameColor {
	WHITE("White", ChatColor.WHITE, "WHITE", DyeColor.WHITE),
	ORANGE("Orange", ChatColor.GOLD, "ORANGE", DyeColor.ORANGE),
	MAGENTA("Magenta", ChatColor.LIGHT_PURPLE, "MAGENTA", DyeColor.MAGENTA),
	LIGHT_BLUE("Light Blue", ChatColor.AQUA, "LIGHT_BLUE", DyeColor.LIGHT_BLUE),
	YELLOW("Yellow", ChatColor.YELLOW, "YELLOW", DyeColor.YELLOW),
	GREEN("Green", ChatColor.GREEN, "LIME", DyeColor.LIME),
	PINK("Pink", ChatColor.LIGHT_PURPLE, "PINK", DyeColor.PINK),
	DARK_GRAY("Dark Gray", ChatColor.DARK_GRAY, "GRAY", DyeColor.GRAY),
	GRAY("Gray", ChatColor.GRAY, "LIGHT_GRAY", DyeColor.LIGHT_GRAY),
	CYAN("Cyan", ChatColor.DARK_AQUA, "CYAN", DyeColor.CYAN),
	PURPLE("Purple", ChatColor.DARK_PURPLE, "PURPLE", DyeColor.PURPLE),
	BLUE("Blue", ChatColor.BLUE, "BLUE", DyeColor.BLUE),
	BROWN("Brown", ChatColor.GOLD, "BROWN", DyeColor.BROWN),
	DARK_GREEN("Dark Green", ChatColor.DARK_GREEN, "GREEN", DyeColor.GREEN),
	RED("Red", ChatColor.RED, "RED", DyeColor.RED),
	BLACK("Black", ChatColor.BLACK, "BLACK", DyeColor.BLACK);
	
	String name;
	ChatColor chatcolor;
	String prefix;
	String materialName;
	DyeColor dye;
	
	GameColor(String name, ChatColor chatcolor, String materialName, DyeColor dye) {
		this.name = name;
		this.chatcolor = chatcolor;
		this.prefix = chatcolor+"";
		this.materialName = materialName;
		this.dye = dye;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public ChatColor getChatColor() {
		return chatcolor;
	}
	
	public DyeColor dyeColor() {
		return dye;
	}
	
	public Material wool() {
		return Material.valueOf(materialName+"_WOOL");
	}
	
	public Material terracotta() {
		return Material.valueOf(materialName+"_TERRACOTTA");
	}
	
	public Material banner() {
		return Material.valueOf(materialName+"_BANNER");
	}
	
	public Material bed() {
		return Material.valueOf(materialName+"_BED");
	}
	
	public Material candle() {
		return Material.valueOf(materialName+"_CANDLE");
	}
	
	public Material candleCake() {
		return Material.valueOf(materialName+"_CANDLE_CAKE");
	}
	
	public Material carpet() {
		return Material.valueOf(materialName+"_CARPET");
	}
	
	public Material concrete() {
		return Material.valueOf(materialName+"_CONCRETE");
	}
	
	public Material concretePowder() {
		return Material.valueOf(materialName+"_CONCRETE_POWDER");
	}
	
	public Material dye() {
		return Material.valueOf(materialName+"_DYE");
	}
	
	public Material glazedTerracotta() {
		return Material.valueOf(materialName+"_GLAZED_TERRACOTTA");
	}
	
	public Material shulkerBox() {
		return Material.valueOf(materialName+"_SHULKER_BOX");
	}
	
	public Material stainedGlass() {
		return Material.valueOf(materialName+"_STAINED_GLASS");
	}
	
	public Material stainedGlassPane() {
		return Material.valueOf(materialName+"_STAINED_GLASS_PANE");
	}
	
	public Material wallBanner() {
		return Material.valueOf(materialName+"_WALL_BANNER");
	}
	
	//Default spawn block material for spawn recolor or chest flag
	public Material spawnBlock() {
		switch(this) {
			case WHITE:
				return Material.QUARTZ_BLOCK;
			case ORANGE:
				return Material.RED_SANDSTONE;
			case MAGENTA:
				return Material.PURPUR_BLOCK;
			case LIGHT_BLUE:
				return Material.DIAMOND_BLOCK;
			case YELLOW:
				return Material.GOLD_BLOCK;
			case GREEN:
				return Material.EMERALD_BLOCK;
			case PINK:
				return Material.BRAIN_CORAL_BLOCK;
			case DARK_GRAY:
				return Material.DEEPSLATE;
			case GRAY:
				return Material.CLAY;
			case CYAN:
				return Material.WARPED_WART_BLOCK;
			case PURPLE:
				return Material.AMETHYST_BLOCK;
			case BLUE:
				return Material.LAPIS_BLOCK;
			case BROWN:
				return Material.PODZOL;
			case DARK_GREEN:
				return Material.MOSS_BLOCK;
			case RED:
				return Material.NETHERRACK;
			case BLACK:
				return Material.OBSIDIAN;
		}
		return Material.STONE;
	}
	
	//For team creation.
	private final static GameColor[] colorOrder = {
			GameColor.RED, //team 0
			GameColor.BLUE, //team 1
			GameColor.YELLOW, //team 2
			GameColor.GREEN, //team 3
			GameColor.PURPLE, //team 4
			GameColor.ORANGE, //team 5
			GameColor.MAGENTA, //team 6
			GameColor.CYAN, //team 7
			GameColor.WHITE, //team 8
			GameColor.DARK_GREEN, //team 9
			GameColor.LIGHT_BLUE, //team 10
			GameColor.PINK, //team 11
			GameColor.GRAY, //team 12
			GameColor.BROWN, //team 13
			GameColor.BLACK, //team 14
			GameColor.DARK_GRAY}; //team 15
	
	public static GameColor[] defaultColorOrder() {
		return colorOrder;
	}
}
