package net.dezilla.dectf2.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;

import net.dezilla.dectf2.GamePlayer;

public class ShieldUtil {
	
	public static ItemStack getShield(GamePlayer player) {
		DyeColor team = DyeColor.WHITE;
		if(player.getTeam() != null) {
			team = player.getTeam().getColor().dyeColor();
		}
		if(player.getName().equals("DeZilla")) {
			List<Pattern> patterns = new ArrayList<Pattern>();
			patterns.add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM));
			patterns.add(new Pattern(DyeColor.WHITE, PatternType.TRIANGLE_BOTTOM));
			patterns.add(new Pattern(DyeColor.BLACK, PatternType.CREEPER));
			patterns.add(new Pattern(DyeColor.BLACK, PatternType.FLOWER));
			patterns.add(new Pattern(team, PatternType.BORDER));
			patterns.add(new Pattern(DyeColor.BROWN, PatternType.CIRCLE_MIDDLE));
			patterns.add(new Pattern(DyeColor.WHITE, PatternType.CROSS));
			patterns.add(new Pattern(DyeColor.BROWN, PatternType.SKULL));
			patterns.add(new Pattern(DyeColor.WHITE, PatternType.TRIANGLES_BOTTOM));
			patterns.add(new Pattern(team, PatternType.TRIANGLE_TOP));
			patterns.add(new Pattern(team, PatternType.TRIANGLE_TOP));
			patterns.add(new Pattern(team, PatternType.STRIPE_TOP));
			
			return ItemBuilder.of(Material.SHIELD).unbreakable().shieldColor(DyeColor.PINK).shieldPatterns(patterns).get();
		}
		return ItemBuilder.of(Material.SHIELD).unbreakable().shieldColor(team).get();
	}

}
