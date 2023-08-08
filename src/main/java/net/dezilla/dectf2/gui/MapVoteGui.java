package net.dezilla.dectf2.gui;

import java.util.Arrays;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMapVote;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.MapPreview;
import net.md_5.bungee.api.ChatColor;

public class MapVoteGui extends GuiPage{
	GameMapVote mapVote;

	public MapVoteGui(Player player, GameMapVote mapVote) {
		super(6, player);
		this.mapVote = mapVote;
		this.setName("Vote for the next map");
		int row = 0;
		int col = 0;
		for(MapPreview m : mapVote.getMapList()) {
			ItemStack icon = ItemBuilder.of(m.getIcon()).name(m.getNameAndMode(true)).desc(""+ChatColor.RESET+ChatColor.WHITE+"Author: "+ChatColor.GOLD+m.getAuthor()).get();
			if(m.getTeamAmount()!=2)
				icon = ItemBuilder.of(icon).desc(Arrays.asList(""+ChatColor.RESET+ChatColor.WHITE+"Author: "+ChatColor.GOLD+m.getAuthor(), ChatColor.RESET+""+ChatColor.GOLD+m.getTeamAmount()+ChatColor.RESET+" Teams")).get();
			GuiItem item = new GuiItem(icon);
			item.setRun((event) -> {
				mapVote.vote(GamePlayer.get(player), mapVote.getMapList().indexOf(m));
				player.sendMessage("You have voted for "+m.getName());
				player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
				player.closeInventory();
			});
			setItem(row, col++, item);
			if(col>8) {
				row++;
				col=0;
			}
		}
		this.removeEmptyRows();
		this.centerRow(row);
	}

}
