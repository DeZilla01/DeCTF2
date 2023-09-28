package net.dezilla.dectf2.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.Minion;

public class TestyKit extends BaseKit{
	private static double movementSpeed = .3;
	
	boolean minion = false;
	boolean inversion = false;
	List<InvertPosition> moveHistory = new ArrayList<InvertPosition>();

	public TestyKit(GamePlayer player) {
		super(player);
	}

	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.clear();
		inv.setItem(0, ItemBuilder.of(Material.DEBUG_STICK).name("test").unbreakable().get());
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(64).get());
		if(minion) {
			inv.setItem(2, ItemBuilder.of(Material.IRON_NUGGET).data("zombie").name("Zombie").get());
			inv.setItem(3, ItemBuilder.of(Material.IRON_NUGGET).data("spider").name("Spider").get());
			inv.setItem(4, ItemBuilder.of(Material.IRON_NUGGET).data("skeleton").name("Skeleton").get());
			inv.setItem(5, ItemBuilder.of(Material.IRON_NUGGET).data("creeper").name("Creeper").get());
			inv.setItem(6, ItemBuilder.of(Material.IRON_NUGGET).data("blaze").name("Blaze").get());
			inv.setItem(7, ItemBuilder.of(Material.IRON_NUGGET).data("golem").name("Iron Golem").get());
			inv.setItem(8, ItemBuilder.of(Material.IRON_NUGGET).data("chicken").name("Warden").get());
		}
		else if(inversion) {
			inv.setItem(2, ItemBuilder.of(Material.GOLD_INGOT).data("invert").name("Inversion").get());
			moveHistory.clear();
		}
		else {
			inv.setItem(2, ItemBuilder.of(Material.IRON_NUGGET).data("4x4").name("4x4 block test").get());
		}
	}
	
	@Override
	public void onTick() {
		if(inversion) {
			ItemStack i = player.getPlayer().getInventory().getItemInMainHand();
			if(ItemBuilder.dataMatch(i, "invert")) {
				if(moveHistory.isEmpty())
					return;
				InvertPosition ip = moveHistory.get(moveHistory.size()-1);
				moveHistory.remove(ip);
				player.getPlayer().teleport(ip.getLocation());
				player.getPlayer().setVelocity(ip.getVelocity().multiply(-1));
			} else {
				InvertPosition ip = new InvertPosition(player.getLocation(), player.getPlayer().getVelocity());
				moveHistory.add(ip);
				if(moveHistory.size()>300)
					moveHistory.remove(0);
			}
		}
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(ItemBuilder.dataMatch(event.getItem(), "zombie")) {
			new Minion(EntityType.ZOMBIE, player.getTeam(), player.getLocation(), player);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "spider")) {
			new Minion(EntityType.SPIDER, player.getTeam(), player.getLocation(), player);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "skeleton")) {
			new Minion(EntityType.SKELETON, player.getTeam(), player.getLocation(), player);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "creeper")) {
			new Minion(EntityType.CREEPER, player.getTeam(), player.getLocation(), player);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "blaze")) {
			new Minion(EntityType.BLAZE, player.getTeam(), player.getLocation(), player);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "golem")) {
			new Minion(EntityType.IRON_GOLEM, player.getTeam(), player.getLocation(), player);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "chicken")) {
			new Minion(EntityType.WARDEN, player.getTeam(), player.getLocation(), player);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "4x4")) {
			for(Block b : Util.get4x4Blocks(player.getLocation()))
				b.setType(Material.STONE);
		}
	}

	@Override
	public String getName() {
		return "Testy";
	}
	
	@Override
	public String getVariation() {
		if(minion)
			return "Minion";
		if(inversion)
			return "Inversion";
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.COMMAND_BLOCK);
	}
	
	@Override
	public double getMovementSpeed() {
		return movementSpeed;
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("minion")) 
			minion = true;
		if(variation.equalsIgnoreCase("inversion")) 
			inversion = true;
	}

	@Override
	public String[] getVariations() {
		return new String[] {"default", "minion", "inversion"};
	}
	
	public class InvertPosition {
		Location loc;
		Vector vel;
		
		public InvertPosition(Location location, Vector velocity) {
			loc = location;
			vel = velocity;
		}
		
		public Location getLocation() {
			return loc.clone();
		}
		
		public Vector getVelocity() {
			return vel.clone();
		}
	}

}
