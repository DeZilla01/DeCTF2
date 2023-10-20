package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Cake;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;

public class Dispenser extends BaseStructure{
	static int DISPENSER_REGEN = 100;
	
	ArmorStand display = null;
	Block cake = null;
	int cakelvl = 6; //6 == empty, 0 == full
	int regen = DISPENSER_REGEN;

	public Dispenser(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		destroyable = true;
		placeBlocks();
	}
	
	private void placeBlocks() {
		Block b = location.getBlock();
		b.setType(owner.getTeam().getColor().stainedGlass());
		blocks.add(b);
		cake = b.getRelative(BlockFace.UP);
		cake.setType(Material.CAKE);
		blocks.add(cake);
		updateCake();
		display = (ArmorStand) location.getWorld().spawnEntity(b.getLocation().add(.5,1.3,.5), EntityType.ARMOR_STAND);
		display.setMarker(true);
		display.setGravity(false);
		display.setInvisible(true);
		display.setInvulnerable(true);
		display.setCustomNameVisible(true);
		display.setCustomName("Dispenser Test");
		entities.add(display);
		dead = false;
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
			p.notify("dispenser is empty, sucks to be you lol");
			return;
		}
		if(p.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() <= p.getPlayer().getHealth()) {
			if(!Util.regenPlayer(p)) {
				p.notify("wait u dumb fuck");
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
		if(b.getType() != Material.AIR || b.getRelative(BlockFace.UP).getType() != Material.AIR || b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType() != Material.AIR)
			return false;
		return !areaRestricted(b);
	}
	
	private void updateCake() {
		Cake c = (Cake) cake.getBlockData();
		if(c.getBites() != cakelvl) {
			c.setBites(cakelvl);
			cake.setBlockData(c);
		}
	}

}
