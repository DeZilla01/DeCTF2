package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;

public class IceBox extends BaseStructure{
	
	int tickLeft = 60;
	LivingEntity victim = null;
	Location center = null;

	public IceBox(GamePlayer owner, LivingEntity victim) throws CannotBuildException {
		super(owner, victim.getLocation());
		this.victim = victim;
		this.center = victim.getLocation().getBlock().getLocation().add(.5,0,.5);
		Block b = center.getBlock().getRelative(BlockFace.DOWN);
		for(int x = -1 ; x <= 1 ; x++) {
			for(int y = 0; y <= 3; y++) {
				for(int z = -1; z <= 1; z++) {
					if((y == 1 || y == 2) && (x == 0 && z == 0))
						continue;
					Block block = b.getLocation().add(x, y, z).getBlock();
					if(!Util.air(block))
						continue;
					if(!structureCheck(block))
						continue;
					addBlock(block);
					block.setType(Material.ICE);
				}
			}
		}
		victim.setFreezeTicks(tickLeft*2);
	}
	
	@Override
	public void remove() {
		super.remove();
		center.getWorld().playSound(center, Sound.BLOCK_GLASS_BREAK, 1, 1);
	}
	
	@Override
	public void onTick() {
		if(tickLeft<=0) {
			remove();
			return;
		}
		if(victim.getLocation().distance(center)>.5)
			victim.teleport(center);
		tickLeft--;
	}

	@Override
	public boolean canPlace(Location location) {
		return true;
	}

}
