package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import net.dezilla.dectf2.GamePlayer;

public class Ballista extends BaseStructure{

	public Ballista(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		placeBlocks();
	}
	
	private void placeBlocks() {
		ArmorStand g = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		g.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
		g.setAI(false);
		g.setRightArmPose(new EulerAngle(270,350,0));
		///summon armor_stand ~ ~ ~ {ShowArms:1b,ArmorItems:[{},{},{},{id:"diamond_helmet",Count:1b}],HandItems:[{id:"crossbow",Count:1b},{}],Pose:{RightArm:[270f,350f,0f]}}
	}

	@Override
	public boolean canPlace(Location location) {
		return true;
	}

}
