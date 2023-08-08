package net.dezilla.dectf2.util;

import org.bukkit.Color;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.trim.TrimMaterial;

import net.md_5.bungee.api.ChatColor;

public enum GameColor {
	WHITE("White", ChatColor.WHITE, "WHITE", DyeColor.WHITE, Color.WHITE, TrimMaterial.QUARTZ, org.bukkit.ChatColor.WHITE), //0
	ORANGE("Orange", ChatColor.GOLD, "ORANGE", DyeColor.ORANGE, Color.ORANGE, TrimMaterial.GOLD, org.bukkit.ChatColor.GOLD), //1
	MAGENTA("Magenta", ChatColor.LIGHT_PURPLE, "MAGENTA", DyeColor.MAGENTA, Color.fromRGB(255, 85, 255), TrimMaterial.AMETHYST, org.bukkit.ChatColor.LIGHT_PURPLE), //2
	LIGHT_BLUE("Light Blue", ChatColor.AQUA, "LIGHT_BLUE", DyeColor.LIGHT_BLUE, Color.AQUA, TrimMaterial.DIAMOND, org.bukkit.ChatColor.AQUA), //3
	YELLOW("Yellow", ChatColor.YELLOW, "YELLOW", DyeColor.YELLOW, Color.YELLOW, TrimMaterial.GOLD, org.bukkit.ChatColor.YELLOW), //4
	GREEN("Green", ChatColor.GREEN, "LIME", DyeColor.LIME, Color.LIME, TrimMaterial.EMERALD, org.bukkit.ChatColor.GREEN), //5
	PINK("Pink", ChatColor.of(new java.awt.Color(250, 134, 196)), "PINK", DyeColor.PINK, Color.fromRGB(250, 134, 196), TrimMaterial.AMETHYST, org.bukkit.ChatColor.LIGHT_PURPLE), //6
	DARK_GRAY("Dark Gray", ChatColor.DARK_GRAY, "GRAY", DyeColor.GRAY, Color.GRAY, TrimMaterial.NETHERITE, org.bukkit.ChatColor.DARK_GRAY), //7
	GRAY("Gray", ChatColor.GRAY, "LIGHT_GRAY", DyeColor.LIGHT_GRAY, Color.SILVER, TrimMaterial.IRON, org.bukkit.ChatColor.GRAY), //8
	CYAN("Cyan", ChatColor.DARK_AQUA, "CYAN", DyeColor.CYAN, Color.TEAL, TrimMaterial.DIAMOND, org.bukkit.ChatColor.DARK_AQUA), //9
	PURPLE("Purple", ChatColor.DARK_PURPLE, "PURPLE", DyeColor.PURPLE, Color.PURPLE, TrimMaterial.AMETHYST, org.bukkit.ChatColor.DARK_PURPLE), //10
	BLUE("Blue", ChatColor.BLUE, "BLUE", DyeColor.BLUE, Color.fromRGB(85, 85, 255), TrimMaterial.LAPIS, org.bukkit.ChatColor.BLUE), //11
	BROWN("Brown", ChatColor.of(new java.awt.Color(150, 75, 0)), "BROWN", DyeColor.BROWN, Color.fromRGB(150, 75, 0), TrimMaterial.COPPER, org.bukkit.ChatColor.GOLD), //12
	DARK_GREEN("Dark Green", ChatColor.DARK_GREEN, "GREEN", DyeColor.GREEN, Color.GREEN, TrimMaterial.EMERALD, org.bukkit.ChatColor.DARK_GREEN), //13
	RED("Red", ChatColor.RED, "RED", DyeColor.RED, Color.fromRGB(255, 85, 85), TrimMaterial.REDSTONE, org.bukkit.ChatColor.RED), //14
	BLACK("Black", ChatColor.BLACK, "BLACK", DyeColor.BLACK, Color.BLACK, TrimMaterial.NETHERITE, org.bukkit.ChatColor.BLACK), //15
	CRIMSON("Crimson", ChatColor.DARK_RED, "RED", DyeColor.RED, Color.MAROON, TrimMaterial.REDSTONE, org.bukkit.ChatColor.DARK_RED), //16
	DARK_BLUE("Dark Blue", ChatColor.DARK_BLUE, "BLUE", DyeColor.BLUE, Color.fromRGB(0, 0, 170), TrimMaterial.LAPIS, org.bukkit.ChatColor.DARK_BLUE), //17
	CUSTOM("Custom", ChatColor.WHITE, "WHITE", DyeColor.WHITE, Color.WHITE, TrimMaterial.QUARTZ, org.bukkit.ChatColor.WHITE); //18
	
	public static GameColor custom(String name, ChatColor chatColor, String materialColor, DyeColor dyeColor, Color color, TrimMaterial trimMaterial, org.bukkit.ChatColor bChatColor) {
		GameColor c = GameColor.CUSTOM;
		c.set(name, chatColor, materialColor, dyeColor, color, trimMaterial, bChatColor);
		return c;
	}
	
	String name;
	ChatColor chatcolor;
	String prefix;
	String materialName;
	DyeColor dye;
	Color bukkitColor;
	TrimMaterial trimMaterial;
	org.bukkit.ChatColor bukkitChatColor;
	
	GameColor(String name, ChatColor chatcolor, String materialName, DyeColor dye, Color bukkitColor, TrimMaterial trimMaterial, org.bukkit.ChatColor bukkitChatColor) {
		this.name = name;
		this.chatcolor = chatcolor;
		this.prefix = chatcolor+"";
		this.materialName = materialName;
		this.dye = dye;
		this.bukkitColor = bukkitColor;
		this.trimMaterial = trimMaterial;
		this.bukkitChatColor = bukkitChatColor;
	}
	
	private void set(String name, ChatColor chatColor, String materialColor, DyeColor dyeColor, Color color, TrimMaterial trimMaterial, org.bukkit.ChatColor bChatColor) {
		this.name = name;
		this.chatcolor = chatColor;
		this.prefix = chatcolor+"";
		this.materialName = materialColor;
		this.dye = dyeColor;
		this.bukkitColor = color;
		this.trimMaterial = trimMaterial;
		this.bukkitChatColor = bChatColor;
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
	
	public String getMaterialName() {
		return materialName;
	}
	
	public Color getBukkitColor() {
		return bukkitColor;
	}
	
	public TrimMaterial getTrimMaterial() {
		return trimMaterial;
	}
	
	public org.bukkit.ChatColor getBukkitChatColor(){
		return bukkitChatColor;
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
	
	public Material[] coloredMaterials() {
		Material[] a = {wool(), terracotta(), banner(), bed(), candle(), candleCake(), carpet(), concrete(), concretePowder(), glazedTerracotta(), shulkerBox(), stainedGlass(), stainedGlassPane(), wallBanner()};
		return a;
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
				return Material.STRIPPED_CHERRY_WOOD;
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
			case CRIMSON:
				return Material.RED_NETHER_BRICKS;
			case DARK_BLUE:
				return Material.OXIDIZED_COPPER;
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
			GameColor.DARK_GRAY, //team 15
			GameColor.CRIMSON}; //team 16
	
	public static GameColor[] defaultColorOrder() {
		return colorOrder;
	}
}
