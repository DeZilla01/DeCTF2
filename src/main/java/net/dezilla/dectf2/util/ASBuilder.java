package net.dezilla.dectf2.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ASBuilder {
	
	public static ASBuilder create(Location location) {
		return new ASBuilder(location);
	}
	
	public static ASBuilder of(ArmorStand stand) {
		return new ASBuilder(stand);
	}
	
	ArmorStand stand;
	protected ASBuilder(Location location) {
		stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
	}
	
	protected ASBuilder(ArmorStand armorStand) {
		stand = armorStand;
	}
	
	public ArmorStand get() {
		return stand;
	}
	
	public ASBuilder gravity(boolean value) {
		stand.setGravity(value);
		return this;
	}
	
	public ASBuilder visible(boolean value) {
		stand.setVisible(value);
		return this;
	}
	
	public ASBuilder marker() {
		stand.setMarker(true);
		return this;
	}
	
	public ASBuilder marker(boolean value) {
		stand.setMarker(value);
		return this;
	}
	
	public ASBuilder invulnerable() {
		stand.setInvulnerable(true);
		return this;
	}
	
	public ASBuilder invulnerable(boolean value) {
		stand.setInvulnerable(value);
		return this;
	}
	
	public ASBuilder display(String value) {
		stand.setCustomNameVisible(true);
		stand.setCustomName(value);
		return this;
	}
	
	public ASBuilder displayVisible(boolean value) {
		stand.setCustomNameVisible(value);
		return this;
	}
	
	public ASBuilder helmet(ItemStack item) {
		stand.getEquipment().setHelmet(item);
		return this;
	}
	
	public ASBuilder helmet(Material material) {
		stand.getEquipment().setHelmet(new ItemStack(material));
		return this;
	}
	
	public ASBuilder chestplate(ItemStack item) {
		stand.getEquipment().setChestplate(item);
		return this;
	}
	
	public ASBuilder chestplate(Material material) {
		stand.getEquipment().setChestplate(new ItemStack(material));
		return this;
	}
	
	public ASBuilder leggings(ItemStack item) {
		stand.getEquipment().setLeggings(item);
		return this;
	}
	
	public ASBuilder leggings(Material material) {
		stand.getEquipment().setLeggings(new ItemStack(material));
		return this;
	}
	
	public ASBuilder boots(ItemStack item) {
		stand.getEquipment().setBoots(item);
		return this;
	}
	
	public ASBuilder boots(Material material) {
		stand.getEquipment().setBoots(new ItemStack(material));
		return this;
	}
	
	public ASBuilder mainHand(ItemStack item) {
		stand.getEquipment().setItemInMainHand(item);
		return this;
	}
	
	public ASBuilder mainHand(Material material) {
		stand.getEquipment().setItemInMainHand(new ItemStack(material));
		return this;
	}
	
	public ASBuilder offHand(ItemStack item) {
		stand.getEquipment().setItemInOffHand(item);
		return this;
	}
	
	public ASBuilder offHand(Material material) {
		stand.getEquipment().setItemInOffHand(new ItemStack(material));
		return this;
	}
	
	public ASBuilder small() {
		stand.setSmall(true);
		return this;
	}
	
	public ASBuilder small(boolean value) {
		stand.setSmall(value);
		return this;
	}
	
	public ASBuilder lockEquip() {
		for(EquipmentSlot slot : EquipmentSlot.values()){
			for(LockType type : LockType.values())
				stand.addEquipmentLock(slot, type);
		}
		return this;
	}

}
