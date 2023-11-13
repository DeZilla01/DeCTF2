package net.dezilla.dectf2.structures;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.util.Minion;
import net.md_5.bungee.api.ChatColor;

public class Turret extends BaseStructure{
	static double MAX_HEALTH = 40;
	static double FIRE_RADIUS = 15;
	static int HEALTH_BAR_SIZE = 20;
	
	double health = MAX_HEALTH;
	ArmorStand dispenser = null;
	ArmorStand display = null;
	ArmorStand hitbox = null;

	public Turret(GamePlayer owner, Location location) throws CannotBuildException {
		super(owner, location);
		removedOnDeath = true;
		removeOnSpawnProtection = true;
		minionCanAttack = true;
		placeBlocks();
	}
	
	private void placeBlocks() throws CannotBuildException {
		Block b = location.getBlock();
		addBlock(b);
		b.setType(Material.OAK_FENCE);
		Block under = b.getRelative(BlockFace.DOWN);
		addBlock(under);
		under.setType(owner.getTeam().getColor().wool());
		display = (ArmorStand) location.getWorld().spawnEntity(b.getLocation().add(.5,1.3,.5), EntityType.ARMOR_STAND);
		display.setMarker(true);
		display.setGravity(false);
		display.setInvisible(true);
		display.setInvulnerable(true);
		display.setCustomNameVisible(true);
		display.setCustomName(getDisplay());
		dispenser = (ArmorStand) location.getWorld().spawnEntity(b.getLocation().add(.5,-.4,.5), EntityType.ARMOR_STAND);
		dispenser.setMarker(true);
		dispenser.setInvulnerable(true);
		dispenser.setGravity(false);
		dispenser.setInvisible(true);
		hitbox = (ArmorStand) location.getWorld().spawnEntity(b.getLocation().add(.5,0,.5), EntityType.ARMOR_STAND);
		hitbox.setGravity(false);
		hitbox.setVisible(false);
		//stand.setInvulnerable(true);
		dispenser.getEquipment().setHelmet(new ItemStack(Material.DISPENSER));
		for(EquipmentSlot slot : EquipmentSlot.values()){
			for(LockType type : LockType.values())
				hitbox.addEquipmentLock(slot, type);
		}
		entities.add(dispenser);
		entities.add(display);
		entities.add(hitbox);
	}
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent event) {
		if(!event.getEntity().equals(hitbox))
			return;
		event.setCancelled(true);
		GamePlayer gp = Util.getOwner(event.getDamager());
		if(gp != null && owner != null) {
			if(owner.getTeam().equals(gp.getTeam()) && !owner.equals(gp))
				return;
		}
		if(owner.equals(gp))
			event.setDamage(999);
		if(health - event.getDamage() <= 0) {
			remove();
			return;
		} else
			health -= event.getDamage();
	}
	
	int ticks = 0;
	@Override
	public void onTick() {
		LivingEntity target = null;
		for(LivingEntity e : location.getWorld().getLivingEntities()) {
			if(e.getLocation().distance(location) > FIRE_RADIUS)
				continue;
			if(owner != null && owner.getTeam() != null) {
				GameTeam t = owner.getTeam();
				if(e.getType() == EntityType.PLAYER) {
					GamePlayer gp = GamePlayer.get((Player) e);
					if(t.equals(gp.getTeam()))
						continue;
					if(gp.isSpawnProtected() || gp.isInvisible())
						continue;
					if(gp.getPlayer().getGameMode() != GameMode.SURVIVAL)
						continue;
				}
				else {
					Minion m = Minion.get(e);
					if(m == null)
						continue;
					if(t.equals(m.getTeam()))
						continue;
				}
			}
			if(target == null)
				target = e;
			else if(target.getLocation().distance(location) > e.getLocation().distance(location))
				target = e;
		}
		if(ticks % 20 == 0) {
			if(target != null) {
				Location l = hitbox.getEyeLocation().add(Util.get2DVectorToLoc(dispenser.getEyeLocation(), target.getEyeLocation(), .5));
				Arrow a = location.getWorld().spawnArrow(l, Util.getVectorToLoc(l, target.getEyeLocation(), 1), 5, 0);
				if(owner != null)
					a.setShooter(owner.getPlayer());
			}
		}
		if(target!=null) {
			aimArmorStand(dispenser, target.getEyeLocation());
		}
		String name = getDisplay();
		if(!display.getCustomName().equals(name))
			display.setCustomName(name);
		ticks++;
	}
	
	private String getDisplay() {
		double a = health / MAX_HEALTH;
		String d = ChatColor.RESET+"[";
		for(int i = 1; i <= HEALTH_BAR_SIZE; i++) {
			double b = ((double)i) / HEALTH_BAR_SIZE;
			if(a>=b)
				d+=ChatColor.GREEN+"|";
			else
				d+=ChatColor.RED+"|";
		}
		d+=ChatColor.RESET+"]";
		return d;
	}

	@Override
	public boolean canPlace(Location location) {
		Block b = location.getBlock();
		if(!Util.air(b) || !Util.air(b.getRelative(BlockFace.UP)) || !Util.air(b.getRelative(BlockFace.UP).getRelative(BlockFace.UP)))
			return false;
		return !areaRestricted(b);
	}
	
	public static void aimArmorStand(ArmorStand armorstand, Location target) {
        Location l = armorstand.getLocation().add(0, 1, 0);
        double x = l.getX() - target.getX();
        double y = l.getY() - (target.getY());
        double z = l.getZ() - target.getZ();
        l.setDirection(new Vector(-x, -y, -z));

        double yaw = l.getYaw();
        double pitch = l.getPitch();

        armorstand.setHeadPose(armorstand.getHeadPose().setX(Math.toRadians(pitch)));
        armorstand.setHeadPose(armorstand.getHeadPose().setY(Math.toRadians(yaw)));
    }

}
