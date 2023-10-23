package net.dezilla.dectf2.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.util.Vector;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.structures.BaseStructure;
import net.dezilla.dectf2.structures.CannotBuildException;
import net.dezilla.dectf2.structures.Dispenser;
import net.dezilla.dectf2.structures.MobSpawner;
import net.dezilla.dectf2.structures.TestThing;
import net.dezilla.dectf2.structures.Turret;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.Minion;

public class TestyKit extends BaseKit{
	private static double movementSpeed = .3;
	
	boolean minion = false;
	boolean inversion = false;
	boolean structure = false;
	boolean pissmaster = false;
	boolean tools = false;
	List<InvertPosition> moveHistory = new ArrayList<InvertPosition>();
	TestThing building = null;

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
			inv.setItem(9, ItemBuilder.of(Material.IRON_NUGGET).data("manyzombie").name("lots of zombies").get());
		}
		else if(inversion) {
			inv.setItem(2, ItemBuilder.of(Material.GOLD_INGOT).data("invert").name("Inversion").get());
			moveHistory.clear();
		}
		else if(structure) {
			inv.setItem(2, ItemBuilder.of(Material.IRON_NUGGET).data("building").name("Test structure").get());
			inv.setItem(3, ItemBuilder.of(Material.IRON_NUGGET).data("inspect").name("inspect").get());
			inv.setItem(4, ItemBuilder.of(Material.IRON_NUGGET).data("turret").name("turret").get());
			inv.setItem(5, ItemBuilder.of(Material.IRON_NUGGET).data("dispenser").name("dispenser").get());
			inv.setItem(6, ItemBuilder.of(Material.IRON_NUGGET).data("spawner").name("spawner").get());
		} else if(pissmaster) {
			inv.setHelmet(ItemBuilder.of(Material.LEATHER_HELMET).leatherColor(Color.fromRGB(228, 247, 82)).unbreakable().armorTrim(TrimPattern.TIDE, color().getTrimMaterial()).get());
			inv.setChestplate(ItemBuilder.of(Material.LEATHER_CHESTPLATE).leatherColor(Color.fromRGB(228, 247, 82)).unbreakable().armorTrim(TrimPattern.TIDE, color().getTrimMaterial()).get());
			inv.setLeggings(ItemBuilder.of(Material.LEATHER_LEGGINGS).leatherColor(Color.fromRGB(228, 247, 82)).unbreakable().get());
			inv.setBoots(ItemBuilder.of(Material.LEATHER_BOOTS).leatherColor(Color.fromRGB(228, 247, 82)).unbreakable().get());
			inv.setItem(0, ItemBuilder.of(Material.GOLDEN_SWORD).unbreakable().enchant(Enchantment.DAMAGE_ALL, 0).name("Pissmaster Sword").get());
			inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(4).get());
			inv.setItem(2, ItemBuilder.of(Material.YELLOW_DYE).data("piss").name("Piss").get());
		}else if(tools) {
			inv.setItem(2, ItemBuilder.of(Material.SPYGLASS).name("Spyglass").get());
			inv.setItem(3, ItemBuilder.of(Material.COMPASS).name("Poiting to "+player.getObjectiveLocationName()).data("objective_tracker").get());
			inv.setItem(4, ItemBuilder.of(Material.GOLDEN_CARROT).name("Pointer").data("pointer").get());
			inv.setItem(5, ItemBuilder.of(Material.NETHER_STAR).name("Kit Selector").data("kit_selector").get());
		}
		else {
			inv.setItem(2, ItemBuilder.of(Material.IRON_NUGGET).data("4x4").name("4x4 block test").get());
			inv.setItem(3, ItemBuilder.of(Material.IRON_NUGGET).data("trident_test").name("trident test").get());
			inv.setItem(4, ItemBuilder.of(Material.IRON_NUGGET).data("struct_check").name("structure check").get());
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
		if(ItemBuilder.dataMatch(event.getItem(), "manyzombie")) {
			for(int i = 0; i < 500; i++) {
				new Minion(EntityType.ZOMBIE, player.getTeam(), player.getLocation(), player);
			}
		}
		if(ItemBuilder.dataMatch(event.getItem(), "4x4")) {
			for(Block b : Util.get4x4Blocks(player.getLocation()))
				b.setType(Material.STONE);
		}
		if(ItemBuilder.dataMatch(event.getItem(), "building") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(event.getClickedBlock().getType() == Material.STONE)
				return;
			if(building != null && !building.isDead())
				building.remove();
			try {
				Location l = event.getClickedBlock().getLocation();
				player.notify(l.getY()+"");
				building = new TestThing(player, l.add(0,1,0));
			}catch(CannotBuildException e) {
				player.notify(e.getMessage());
			}
		}
		if(ItemBuilder.dataMatch(event.getItem(), "turret") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(event.getClickedBlock().getType() == Material.OAK_FENCE)
				return;
			try {
				Location l = event.getClickedBlock().getLocation();
				new Turret(player, l.add(0,1,0));
			}catch(CannotBuildException e) {
				player.notify(e.getMessage());
			}
		}
		if(ItemBuilder.dataMatch(event.getItem(), "dispenser") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(event.getClickedBlock().getType().toString().contains("_STAINED_GLASS"))
				return;
			try {
				Location l = event.getClickedBlock().getLocation();
				new Dispenser(player, l.add(0,1,0));
			}catch(CannotBuildException e) {
				player.notify(e.getMessage());
			}
		}
		if(ItemBuilder.dataMatch(event.getItem(), "spawner") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(event.getClickedBlock().getType().toString().contains("_STAINED_GLASS"))
				return;
			try {
				Location l = event.getClickedBlock().getLocation();
				new MobSpawner(player, l.add(0,1,0));
			}catch(CannotBuildException e) {
				player.notify(e.getMessage());
			}
		}
		if(ItemBuilder.dataMatch(event.getItem(), "inspect")) {
			if(event.getClickedBlock() != null) {
				Location l = event.getClickedBlock().getLocation();
				player.notify(event.getClickedBlock().getType()+" "+l.getX()+" "+l.getY()+" "+l.getZ());
			}
		}
		if(ItemBuilder.dataMatch(event.getItem(), "struct_check")) {
			if(event.getClickedBlock() != null) {
				player.notify(""+BaseStructure.getStructure(event.getClickedBlock()));
			}
		}
		if(ItemBuilder.dataMatch(event.getItem(), "piss")) {
			Snowball s = player.getPlayer().launchProjectile(Snowball.class, Util.inFront(player.getPlayer(), 1));
			s.setItem(new ItemStack(Material.YELLOW_DYE));
		}
		if(ItemBuilder.dataMatch(event.getItem(), "trident_test")) {
			Trident t = player.getPlayer().launchProjectile(Trident.class, Util.inFront(player.getPlayer(), 1));
			t.setItem(new ItemStack(Material.DIAMOND_SWORD));
		}
	}
	
	public void onProjectileHit(ProjectileHitEvent event) {
		GamePlayer p = Util.getOwner((Entity) event.getEntity().getShooter());
		if(p == null || !p.getPlayer().equals(player.getPlayer()) || event.getHitEntity() == null)
			return;
		if(event.getHitEntity() instanceof Player) {
			GamePlayer victim = GamePlayer.get((Player) event.getHitEntity());
			if(victim.getTeam().equals(player.getTeam()))
				return;
			victim.setLastAttacker(player);
			victim.getPlayer().damage(1, player.getPlayer());
		} else if(event.getHitEntity() instanceof LivingEntity) {
			Minion m = Minion.get((LivingEntity) event.getHitEntity());
			if(m == null || m.getTeam().equals(player.getTeam()))
				return;
			m.getEntity().damage(1, player.getPlayer());
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
		if(structure)
			return "Structure";
		if(pissmaster)
			return "Pissmaster";
		if(tools)
			return "Tools";
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.COMMAND_BLOCK);
	}
	
	@Override
	public double getMovementSpeed() {
		if(pissmaster)
			return defaultMovementSpeed;
		return movementSpeed;
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("minion")) 
			minion = true;
		if(variation.equalsIgnoreCase("inversion")) 
			inversion = true;
		if(variation.equalsIgnoreCase("structure"))
			structure = true;
		if(variation.equalsIgnoreCase("pissmaster"))
			pissmaster = true;
		if(variation.equalsIgnoreCase("tools"))
			tools = true;
	}

	@Override
	public String[] getVariations() {
		return new String[] {"default", "minion", "inversion", "structure", "tools"};
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
	
	@Override
	public ItemStack[] getFancyDisplay() {
		return new ItemStack[] {
				new ItemStack(Material.DIAMOND_SWORD),
				new ItemStack(Material.DIAMOND_CHESTPLATE),
				new ItemStack(Material.DIAMOND_LEGGINGS),
				new ItemStack(Material.DIAMOND_HELMET),
				new ItemStack(GameConfig.foodMaterial),
				new ItemStack(Material.DIAMOND_BOOTS),
				new ItemStack(Material.DIAMOND_LEGGINGS),
				new ItemStack(Material.DIAMOND_CHESTPLATE),
				new ItemStack(Material.DIAMOND_SWORD)
		};
	}

}
