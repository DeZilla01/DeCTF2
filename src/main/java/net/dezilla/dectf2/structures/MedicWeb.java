package net.dezilla.dectf2.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import net.dezilla.dectf2.GamePlayer;

public class MedicWeb extends BaseStructure{
	
	int tickLeft = 40;

	public MedicWeb(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		destroyable = true;
		Block b = location.getBlock();
		previousMaterial.add(b.getType());
		b.setType(Material.COBWEB);
		blocks.add(b);
		dead=false;
	}
	
	@Override
	public void onTick() {
		if(tickLeft<=0) {
			remove();
			return;
		}
		tickLeft--;
	}

	@Override
	public boolean canPlace(Location location) {
		return true;
	}

}
