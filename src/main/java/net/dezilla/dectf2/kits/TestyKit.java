package net.dezilla.dectf2.kits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.Minion;

public class TestyKit extends BaseKit{
	private static double movementSpeed = .3;

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
		inv.setItem(2, ItemBuilder.of(Material.IRON_NUGGET).data("zombie").name("Zombie").get());
		inv.setItem(3, ItemBuilder.of(Material.IRON_NUGGET).data("spider").name("Spider").get());
		inv.setItem(4, ItemBuilder.of(Material.IRON_NUGGET).data("skeleton").name("Skeleton").get());
		inv.setItem(5, ItemBuilder.of(Material.IRON_NUGGET).data("creeper").name("Creeper").get());
		inv.setItem(6, ItemBuilder.of(Material.IRON_NUGGET).data("blaze").name("Blaze").get());
		inv.setItem(7, ItemBuilder.of(Material.IRON_NUGGET).data("golem").name("Iron Golem").get());
		inv.setItem(8, ItemBuilder.of(Material.IRON_NUGGET).data("chicken").name("Chicken").get());
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(ItemBuilder.dataMatch(event.getItem(), "zombie")) {
			new Minion(EntityType.ZOMBIE, player.getTeam(), player.getLocation());
		}
		if(ItemBuilder.dataMatch(event.getItem(), "spider")) {
			new Minion(EntityType.SPIDER, player.getTeam(), player.getLocation());
		}
		if(ItemBuilder.dataMatch(event.getItem(), "skeleton")) {
			new Minion(EntityType.SKELETON, player.getTeam(), player.getLocation());
		}
		if(ItemBuilder.dataMatch(event.getItem(), "creeper")) {
			new Minion(EntityType.CREEPER, player.getTeam(), player.getLocation());
		}
		if(ItemBuilder.dataMatch(event.getItem(), "blaze")) {
			new Minion(EntityType.BLAZE, player.getTeam(), player.getLocation());
		}
		if(ItemBuilder.dataMatch(event.getItem(), "golem")) {
			new Minion(EntityType.IRON_GOLEM, player.getTeam(), player.getLocation());
		}
		if(ItemBuilder.dataMatch(event.getItem(), "chicken")) {
			new Minion(EntityType.WARDEN, player.getTeam(), player.getLocation());
		}
	}

	@Override
	public String getName() {
		return "Testy";
	}
	
	@Override
	public String getVariation() {
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
	public String[] getVariations() {
		return new String[] {"Default"};
	}

}
