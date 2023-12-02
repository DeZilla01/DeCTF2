package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Cake;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;

public class Dispenser extends BaseStructure{
	static int DISPENSER_REGEN = 100;
	
	Block cake = null;
	int cakelvl = 6; //6 == empty, 0 == full
	int regen = DISPENSER_REGEN;

	public Dispenser(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		removeOnSpawnProtection = true;
		destroyable = true;
		placeBlocks();
	}
	
	private void placeBlocks() throws CannotBuildException {
		Block b = location.getBlock();
		addBlock(b);
		b.setType(owner.getTeam().getColor().stainedGlass());
		cake = b.getRelative(BlockFace.UP);
		addBlock(cake);
		cake.setType(Material.CAKE);
		updateCake();
	}
	
	@Override
	public void onTick() {
		if(cakelvl == 0) {
			regen = DISPENSER_REGEN;
			return;
		}
		if(regen <= 0) {
			cakelvl--;
			updateCake();
			regen = DISPENSER_REGEN;
			return;
		}
		regen --;
	}
	
	@EventHandler
	public void onTouchTheCake(PlayerInteractEvent event) {
		if(event.getClickedBlock() == null || !blocks.contains(event.getClickedBlock()))
			return;
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() != Material.CAKE)
			return;
		event.setCancelled(true);
		GamePlayer p = GamePlayer.get(event.getPlayer());
		if(cakelvl == 6) {
			p.notify("Dispenser is empty");
			return;
		}
		if(p.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() <= p.getPlayer().getHealth()) {
			if(!Util.regenPlayer(p)) {
				int delay = (p.getRegenTickDelay()/20);
				p.notify("You must wait "+delay+" second(s) to regenerate your inventory");
				return;
			}
		} else if(!p.getPlayer().hasPotionEffect(PotionEffectType.REGENERATION)){
			p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 3));
		} else
			return;
		cakelvl++;
		updateCake();
	}

	@Override
	public boolean canPlace(Location location) {
		Block b = location.getBlock();
		if(!Util.air(b) || !Util.air(b.getRelative(BlockFace.UP)) || !Util.air(b.getRelative(BlockFace.UP).getRelative(BlockFace.UP)))
			return false;
		return true;
	}
	
	@Override
	public boolean bypassRestrictedAreas() {
		return false;
	}
	
	private void updateCake() {
		Cake c = (Cake) cake.getBlockData();
		if(c.getBites() != cakelvl) {
			c.setBites(cakelvl);
			cake.setBlockData(c);
		}
	}

}
