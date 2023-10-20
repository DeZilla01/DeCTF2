package net.dezilla.dectf2.minion;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Mob;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import net.dezilla.dectf2.structures.BaseStructure;
import net.dezilla.dectf2.util.Minion;

public class PathfinderAttackStructure extends CustomPathfinder{
	Minion minion;
	
	public PathfinderAttackStructure(Minion m) {
		super((Mob) m.getEntity());
		this.minion = m;
	}

	@Override
	public boolean canStart() {
		if(entity.getTarget() != null)
			return false;
		return getAS() != null;
	}
	
	private ArmorStand getAS() {
		for(ArmorStand a : entity.getLocation().getWorld().getEntitiesByClass(ArmorStand.class)) {
			if(a.getLocation().distance(entity.getLocation()) > 12)
				continue;
			if(a.isMarker() || !a.isInvisible())
				continue;
			BaseStructure struct = BaseStructure.getStructure(a);
			if(struct == null || !struct.minionCanAttack(a))
				continue;
			if(minion.getTeam().equals(struct.getOwner().getTeam()))
				continue;
			return a;
		}
		return null;
	}

	@Override
	public PathfinderFlag[] getFlags() {
		// TODO Auto-generated method stub
		return new PathfinderFlag[] {
				PathfinderFlag.MOVEMENT
		};
	}
	ArmorStand target = null;

	@Override
	public void start() {
		target = getAS();
		entity.setTarget(target);
	}

	@Override
	public void tick() {
		if(target != null && target.isDead()) {
			entity.setTarget(null);
			target = null;
			this.stop();
		}
	}

}
