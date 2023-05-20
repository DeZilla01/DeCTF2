package net.dezilla.dectf2.util;

import org.bukkit.ChatColor;

public enum GameColor {
	WHITE("White", ChatColor.WHITE),
	ORANGE("Orange", ChatColor.GOLD),
	MAGENTA("Magenta", ChatColor.LIGHT_PURPLE),
	LIGHT_BLUE("Light Blue", ChatColor.AQUA),
	YELLOW("Yellow", ChatColor.YELLOW),
	GREEN("Green", ChatColor.GREEN),
	PINK("Pink", ChatColor.LIGHT_PURPLE),
	DARK_GRAY("Dark Gray", ChatColor.DARK_GRAY),
	GRAY("Gray", ChatColor.GRAY),
	CYAN("Cyan", ChatColor.DARK_AQUA),
	PURPLE("Purple", ChatColor.DARK_PURPLE),
	BLUE("Blue", ChatColor.BLUE),
	BROWN("Brown", ChatColor.GOLD),
	DARK_GREEN("Dark Green", ChatColor.DARK_GREEN),
	RED("Red", ChatColor.RED),
	BLACK("Black", ChatColor.BLACK);
	
	String name;
	ChatColor chatcolor;
	String prefix;
	
	GameColor(String name, ChatColor chatcolor) {
		this.name = name;
		this.chatcolor = chatcolor;
		this.prefix = chatcolor+"";
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
