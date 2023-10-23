package net.dezilla.dectf2.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.minion.PathfinderAttackStructure;
import net.dezilla.dectf2.structures.BaseStructure;
import net.md_5.bungee.api.ChatColor;

public class Minion {
	private static List<Minion> MINIONS = new ArrayList<Minion>();
	private static Map<LivingEntity, Minion> MINIONZ = new HashMap<LivingEntity, Minion>();
	
	@Deprecated
	public static Minion oldget(LivingEntity entity) {
		for(Minion m : MINIONS) {
			if(m.getEntity().equals(entity))
				return m;
		}
		return null;
	}
	
	public static Minion get(LivingEntity entity) {
		if(MINIONZ.containsKey(entity))
			return MINIONZ.get(entity);
		return null;
	}
	
	@Deprecated
	public static void oldclearMinions() {
		List<Minion> toRemove = new ArrayList<Minion>();
		for(Minion m : MINIONS) {
			if(m.isDead())
				toRemove.add(m);
		}
		for(Minion m : toRemove)
			MINIONS.remove(m);
	}
	
	public static void clearMinions() {
		List<LivingEntity> toRemove = new ArrayList<LivingEntity>();
		for(Entry<LivingEntity, Minion> e : MINIONZ.entrySet()) {
			if(e.getKey().isDead())
				toRemove.add(e.getKey());
		}
		for(LivingEntity e : toRemove)
			MINIONZ.remove(e);
	}
	
	GameTeam team;
	Mob entity;
	GamePlayer owner = null;
	int taskID = 0;
	
	public Minion(EntityType type, GameTeam team, Location loc, GamePlayer owner) {
		this.team = team;
		this.owner = owner;
		entity = (Mob) loc.getWorld().spawnEntity(loc, type);
		entity.getEquipment().setHelmet(ItemBuilder.of(Material.LEATHER_HELMET).leatherColor(team.getColor().getBukkitColor()).unbreakable().get());
		entity.setCustomName(team.getColoredTeamName()+ChatColor.WHITE+" Minion");
		entity.setCustomNameVisible(true);
		MINIONS.add(this);
		MINIONZ.put(entity, this);
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI target = brain.getTargetAI();
		PathfinderNearestAttackableTarget<LivingEntity> path = new PathfinderNearestAttackableTarget<LivingEntity>(entity, LivingEntity.class);
		PathfinderAttackStructure struct = new PathfinderAttackStructure(this);
		path.setCondition(new MinionPredicate());
		target.clear();
		target.put(path, 0);
		target.put(struct, 1);
		clearMinions();
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> onTick(), 1, 1);
	}
	
	private void onTick() {
		if(isDead()) {
			Bukkit.getScheduler().cancelTask(taskID);
			return;
		}
		if(entity.getTarget() != null && entity.getTarget().getType() == EntityType.PLAYER) {
			GamePlayer gp = GamePlayer.get((Player) entity.getTarget());
			if(gp.isInvisible() || gp.isSpawnProtected())
				entity.setTarget(null);
		}
	}
	
	public GameTeam getTeam() {
		return team;
	}
	
	public void setName(String name) {
		entity.setCustomName(name);
	}
	
	public void heal(double amount) {
		double max = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		if(entity.getHealth()+amount > max)
			entity.setHealth(max);
		else
			entity.setHealth(entity.getHealth()+amount);
	}
	
	public void addEffect(PotionEffectType type, int duration, int lvl) {
		addEffect(new PotionEffect(type, duration, lvl));
	}
	
	public void addEffect(PotionEffect effect) {
		entity.addPotionEffect(effect);
	}
	
	public LivingEntity getEntity() {
		return (LivingEntity) entity;
	}
	
	public Location getLocation() {
		return entity.getLocation();
	}
	
	public boolean isDead() {
		return entity == null || entity.isDead();
	}
	
	public GamePlayer getOwner() {
		return owner;
	}
	
	public void remove() {
		entity.remove();
		MINIONS.remove(this);
	}
	
	private class MinionPredicate implements Predicate<LivingEntity>{

		@Override
		public boolean test(LivingEntity e) {
			if(e.getType() == EntityType.PLAYER) {
				GamePlayer p = GamePlayer.get((Player) e);
				if(p.getTeam() == null || p.isSpawnProtected() || p.isInvisible())
					return false;
				return !team.equals(p.getTeam());
			}
			if(e.getType() == EntityType.ARMOR_STAND) {
				ArmorStand as = (ArmorStand) e;
				if(as.isMarker())
					return false;
				BaseStructure struct = BaseStructure.getStructure(as);
				if(struct == null)
					return false;
				if(struct.getOwner().getTeam().equals(team))
					return false;
				else
					return true;
			}
			Minion m = Minion.get(e);
			if(m == null || m.getTeam() == null)
				return false;
			return !team.equals(m.getTeam());
		}
		
	}

}
