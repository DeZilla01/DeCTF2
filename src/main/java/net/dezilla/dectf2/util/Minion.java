package net.dezilla.dectf2.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameTeam;
import net.md_5.bungee.api.ChatColor;

public class Minion {
	private static List<Minion> MINIONS = new ArrayList<Minion>();
	
	public static Minion get(LivingEntity entity) {
		for(Minion m : MINIONS) {
			if(m.getEntity().equals(entity))
				return m;
		}
		return null;
	}
	
	public static void clearMinions() {
		List<Minion> toRemove = new ArrayList<Minion>();
		for(Minion m : MINIONS) {
			if(m.isDead())
				toRemove.add(m);
		}
		for(Minion m : toRemove)
			MINIONS.remove(m);
	}
	
	GameTeam team;
	Mob entity;
	
	public Minion(EntityType type, GameTeam team, Location loc) {
		this.team = team;
		entity = (Mob) loc.getWorld().spawnEntity(loc, type);
		entity.getEquipment().setHelmet(ItemBuilder.of(Material.LEATHER_HELMET).leatherColor(team.getColor().getBukkitColor()).unbreakable().get());
		entity.setCustomName(team.getColoredTeamName()+ChatColor.WHITE+" Minion");
		entity.setCustomNameVisible(true);
		MINIONS.add(this);
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI target = brain.getTargetAI();
		PathfinderNearestAttackableTarget<LivingEntity> path = new PathfinderNearestAttackableTarget<LivingEntity>(entity, LivingEntity.class);
		path.setCondition(new MinionPredicate());
		target.clear();
		target.put(path, 0);
	}
	
	public GameTeam getTeam() {
		return team;
	}
	
	public LivingEntity getEntity() {
		return (LivingEntity) entity;
	}
	
	public boolean isDead() {
		return entity == null || entity.isDead();
	}
	
	public void remove() {
		entity.remove();
		MINIONS.remove(this);
	}
	
	private class MinionPredicate implements Predicate<LivingEntity>{

		@Override
		public boolean test(LivingEntity e) {
			if(e instanceof Player) {
				GamePlayer p = GamePlayer.get((Player) e);
				if(p.getTeam() == null)
					return false;
				return !team.equals(p.getTeam());
			}
			clearMinions();
			Minion m = Minion.get(e);
			if(m == null || m.getTeam() == null)
				return false;
			return !team.equals(m.getTeam());
		}
		
	}

}
