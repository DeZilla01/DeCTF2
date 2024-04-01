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
import net.dezilla.dectf2.structures.MobSpawner;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.ShieldUtil;

public class NecroKit extends BaseKit{
	
	MobSpawner spawner = null;

	public NecroKit(GamePlayer player) {
		super(player);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setInventory(boolean resetStats) {
		super.setInventory(resetStats);
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setHelmet(ItemBuilder.of(Material.IRON_HELMET).unbreakable().name("Necro Helmet").armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		inv.setChestplate(ItemBuilder.of(Material.GOLDEN_CHESTPLATE).unbreakable().name("Necro Chestplate").armorTrim(TrimPattern.SILENCE, TrimMaterial.GOLD).get());
		inv.setLeggings(ItemBuilder.of(Material.IRON_LEGGINGS).unbreakable().name("Necro Leggings").get());
		inv.setBoots(ItemBuilder.of(Material.IRON_BOOTS).unbreakable().name("Necro Boots").get());
		inv.setItem(0, ItemBuilder.of(Material.DIAMOND_PICKAXE).unbreakable().name("Necro Pickaxe").get());
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(6).get());
		inv.setItem(2, ItemBuilder.of(Material.GOLDEN_SWORD).unbreakable().enchant(Enchantment.DAMAGE_ALL, 1).name("Necro Sword").get());
		inv.setItem(3, ItemBuilder.of(Material.SPAWNER).data("spawner").get());
		inv.setItem(4, ItemBuilder.of(Material.ZOMBIE_SPAWN_EGG).get());
		inv.setItem(5, ItemBuilder.of(Material.SKELETON_SPAWN_EGG).get());
		
		inv.setItemInOffHand(ShieldUtil.getShield(player));
		addToolItems();
		player.applyInvSave();
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
		if(event.getItem().getType().toString().endsWith("_SPAWN_EGG")) {
			if(event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.SPAWNER) {
				event.setCancelled(true);
				return;
			}
		}
		String key = ItemBuilder.getData(event.getItem());
		long now = GameMain.getServerTick();
		if(!lastUse.containsKey(key) || lastUse.get(key) != now)
			lastUse.put(key, now);
		else
			return;
		Location l = event.getClickedBlock().getLocation().add(.5,1,.5);
		event.setCancelled(true);
		if(key.equals("spawner")) {
			if(spawner != null && !spawner.isDead()) {
				if(player.getPlayer().isSneaking()) {
					spawner.remove();
					player.notify("Spawner Recalled");
				}
				return;
			}
			try {
				spawner = new MobSpawner(player, l);
			}catch(CannotBuildException e) {
				player.notify(e.getMessage());
			}
		}
	}

	@Override
	public String getName() {
		return "Necro";
	}

	@Override
	public String getVariation() {
		// TODO Auto-generated method stub
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.ZOMBIE_SPAWN_EGG);
	}

	@Override
	public ItemStack getIcon(String variation) {
		return getIcon();
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
				new ItemStack(Material.DIAMOND_CHESTPLATE),
				new ItemStack(Material.DIAMOND_LEGGINGS),
				new ItemStack(Material.DIAMOND_HELMET),
				new ItemStack(Material.DIAMOND_SWORD),
				new ItemStack(Material.DIAMOND_BOOTS),
				new ItemStack(Material.DIAMOND_LEGGINGS),
				new ItemStack(Material.DIAMOND_CHESTPLATE),
				new ItemStack(Material.NETHER_STAR)
		};
	}

}
