package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.ASBuilder;

public class Cannon extends BaseStructure{

	ArmorStand barrel = null;
	ArmorStand tnt = null;
	public Cannon(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		destroyable = true;
		placeBlocks();
	}
	
	private void placeBlocks() throws CannotBuildException {
		Block post = location.getBlock();
		addBlock(post);
		post.setType(Material.NETHER_BRICK_FENCE);
		barrel = ASBuilder.create(post.getLocation().add(.5,-.4,.5)).visible(false).gravity(false).invulnerable().lockEquip().helmet(Material.COMPOSTER).get();
		tnt = ASBuilder.create(post.getLocation().add(.5,-.1,.5)).visible(false).gravity(false).marker().invulnerable().small().helmet(Material.TNT).get();
		entities.add(barrel);
		entities.add(tnt);
	}
	
	@EventHandler
	public void onTouchMyCannon(PlayerInteractAtEntityEvent event) {
		System.out.println("A");
		if(event.getRightClicked() == null || event.getRightClicked().getType() != EntityType.ARMOR_STAND || !event.getRightClicked().equals(barrel))
			return;
		System.out.println("B");
		GamePlayer gp = GamePlayer.get(event.getPlayer());
		if(!gp.getTeam().equals(owner.getTeam()))
			return;
		System.out.println("C");
		barrel.addPassenger(gp.getPlayer());
	}

	@Override
	public boolean canPlace(Location location) {
		return true;
	}
	
	@Override
	public boolean bypassRestrictedAreas() {
		return false;
	}

}
