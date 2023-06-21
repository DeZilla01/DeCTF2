package net.dezilla.dectf2.gui;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMapVote;
import net.dezilla.dectf2.util.ItemBuilder;

public class MapVoteGui extends GuiPage{
	GameMapVote mapVote;

	public MapVoteGui(Player player, GameMapVote mapVote) {
		super(6, player);
		this.mapVote = mapVote;
		this.setName("Vote for the next map");
		int row = 0;
		int col = 0;
		for(String z : mapVote.getZipList()) {
			ItemStack icon = ItemBuilder.of(Material.PAPER).name(z).get();
			GuiItem item = new GuiItem(icon);
			item.setRun((event) -> {
				mapVote.vote(GamePlayer.get(player), mapVote.getZipList().indexOf(z));
				player.sendMessage("You have voted for "+z);
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
