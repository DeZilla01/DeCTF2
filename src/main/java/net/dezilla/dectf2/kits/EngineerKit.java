package net.dezilla.dectf2.kits;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.structures.CannotBuildException;
import net.dezilla.dectf2.structures.Dispenser;
import net.dezilla.dectf2.structures.Entrance;
import net.dezilla.dectf2.structures.Exit;
import net.dezilla.dectf2.structures.Turret;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class EngineerKit extends BaseKit{
	Turret turret = null;
	Dispenser dispenser = null;
	Entrance entry = null;
	Exit exit = null;

	public EngineerKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory(boolean resetStats) {
		super.setInventory(resetStats);
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setHelmet(ItemBuilder.of(Material.IRON_HELMET).unbreakable().name("Engineer Helmet").armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		inv.setChestplate(ItemBuilder.of(Material.LEATHER_CHESTPLATE).unbreakable().name("Engineer Chestplate").armorTrim(TrimPattern.SILENCE, TrimMaterial.COPPER).get());
		inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).unbreakable().name("Engineer Leggings").armorTrim(TrimPattern.SILENCE, TrimMaterial.COPPER).get());
		inv.setBoots(ItemBuilder.of(Material.IRON_BOOTS).unbreakable().name("Engineer Boots").armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
		
		inv.setItem(0, ItemBuilder.of(Material.DIAMOND_PICKAXE).unbreakable().name("Engineer Pickaxe").enchant(Enchantment.DAMAGE_ALL, 0).get());
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(4).get());
		inv.setItem(2, ItemBuilder.of(Material.WOODEN_SWORD).name("Manual Aiming Sword").data("aim_tool").unbreakable().get());
		inv.setItem(3, ItemBuilder.of(Material.DISPENSER).name("Turret").data("turret").get());
		inv.setItem(4, ItemBuilder.of(Material.CAKE).name("Dispenser").data("dispenser").get());
		inv.setItem(5, ItemBuilder.of(Material.HEAVY_WEIGHTED_PRESSURE_PLATE).name("Entrance").data("entrance").get());
		inv.setItem(6, ItemBuilder.of(Material.LIGHT_WEIGHTED_PRESSURE_PLATE).name("Exit").data("exit").get());
		addToolItems();
		player.applyInvSave();
		if(resetStats) {
			if(turret != null && !turret.isDead())
				turret.remove();
			if(dispenser != null && !dispenser.isDead())
				dispenser.remove();
			if(entry != null && !entry.isDead())
				entry.remove();
			if(exit != null && !exit.isDead())
				exit.remove();
		}
	}
	
	//to prevent double clicking
	Map<String, Long> lastUse = new HashMap<String, Long>();
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getItem() == null || ItemBuilder.getData(event.getItem()) == null)
			return;
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		String key = ItemBuilder.getData(event.getItem());
		long now = GameMain.getServerTick();
		if(!lastUse.containsKey(key) || lastUse.get(key) != now)
			lastUse.put(key, now);
		else
			return;
		Location l = event.getClickedBlock().getLocation().add(.5,1,.5);
		event.setCancelled(true);
		if(key.equals("turret")) {
			if(turret != null && !turret.isDead()) {
				if(player.getPlayer().isSneaking()) {
					turret.remove();
					player.notify("Turret Recalled");
				}
				return;
			}
			try {
				turret = new Turret(player, l);
			}catch(CannotBuildException e) {
				player.notify(e.getMessage());
			}
		}
		else if(key.equals("dispenser")) {
			if(dispenser != null && !dispenser.isDead()) {
				if(player.getPlayer().isSneaking()) {
					dispenser.remove();
					player.notify("Dispenser Recalled");
				}
				return;
			}
			try {
				dispenser = new Dispenser(player, l);
			}catch(CannotBuildException e) {
				player.notify(e.getMessage());
			}
		}
		else if(key.equals("entrance")) {
			if(entry != null && !entry.isDead()) {
				if(player.getPlayer().isSneaking()) {
					entry.remove();
					player.notify("Entrance Recalled");
				}
				return;
			}
			try {
				entry = new Entrance(player, l);
				if(exit != null && !exit.isDead())
					entry.setExit(exit);
			}catch(CannotBuildException e) {
				player.notify(e.getMessage());
			}
		}
		else if(key.equals("exit")) {
			if(exit != null && !exit.isDead()) {
				if(player.getPlayer().isSneaking()) {
					exit.remove();
					player.notify("Exit Recalled");
				}
				return;
			}
			try {
				exit = new Exit(player, l);
				if(entry != null && !entry.isDead())
					entry.setExit(exit);
			}catch(CannotBuildException e) {
				player.notify(e.getMessage());
			}
		}
	}

	@Override
	public String getName() {
		return "Engineer";
	}

	@Override
	public String getVariation() {
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.DISPENSER);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default"};
		return variations;
	}
	
	@Override
	public ItemStack[] getFancyDisplay() {
		return new ItemStack[] {
				new ItemStack(Material.NETHER_STAR),
				new ItemStack(Material.DISPENSER),
				new ItemStack(Material.CAKE),
				new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE),
				new ItemStack(Material.WOODEN_SWORD),
				new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE),
				new ItemStack(Material.CAKE),
				new ItemStack(Material.DISPENSER),
				new ItemStack(Material.NETHER_STAR)
		};
	}

}
