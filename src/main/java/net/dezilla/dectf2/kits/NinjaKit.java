package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.Util;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.listeners.events.SpongeLaunchEvent;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.ItemBuilder;
import net.dezilla.dectf2.util.ShieldUtil;

public class NinjaKit extends BaseKit{
	private static double NINJA_SPEED = .15;
	private static double NINJA_INVIS_REGEN = .0015;
	private static double NINJA_INVIS_CONSUME = .009;
	private static int NINJA_HP_REGEN = 20;
	private static int NINJA_CLASSIC_INVIS_INTERVAL = 10;
	
	
	private float invisMana = 1;
	private int ticksHpRegen = NINJA_HP_REGEN;
	private boolean classic = false;
	private int invisInterval = NINJA_CLASSIC_INVIS_INTERVAL;
	private boolean canPearl = true;

	public NinjaKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory(boolean resetStats) {
		super.setInventory(resetStats);
		PlayerInventory inv = player.getPlayer().getInventory();
		if(classic) {
			inv.setItemInOffHand(ShieldUtil.getShield(player));
			inv.setItem(0, ItemBuilder.of(Material.GOLDEN_SWORD).unbreakable().name("Ninja Sword").enchant(Enchantment.DAMAGE_ALL, 5).get());
			inv.setItem(1, ItemBuilder.of(Material.ENDER_PEARL).name("Ninja Pearl").amount(10).data("ninja_pearl").get());
			inv.setItem(2, ItemBuilder.of(Material.EGG).name("Flash Bomb").amount(10).data("flash_bomb").get());
			inv.setItem(3, ItemBuilder.of(Material.REDSTONE).name("Red Cocaine").amount(64).get());
		} else {
			inv.setItemInOffHand(ItemBuilder.of(Material.ENDER_PEARL).name("Ninja Pearl").data("ninja_pearl").get());
			inv.setItem(0, ItemBuilder.of(Material.GOLDEN_SWORD).unbreakable().name("Ninja Sword").enchant(Enchantment.DAMAGE_ALL, 5).get());
			inv.setItem(1, ItemBuilder.of(Material.EGG).name("Flash Bomb").data("flash_bomb").get());
			inv.setItem(2, ItemBuilder.of(Material.REDSTONE).name("Red Cocaine").get());
		}
		addToolItems();
		player.applyInvSave();
		if(resetStats) {
			invisMana = 1;
			player.getPlayer().setCooldown(Material.ENDER_PEARL, 0);
			player.getPlayer().setCooldown(Material.EGG, 0);
		}
	}
	
	@Override
	public void onTick() {
		ItemStack held = player.getPlayer().getInventory().getItemInMainHand();
		//invis
		if(classic) {
			if(held != null && held.getType() == Material.REDSTONE) {
				if(!player.isInvisible()) 
					player.setInvisible(true);
				if(invisInterval <= 0 && player.isInvisible()) {
					int amount = 2;
					if(player.getPlayer().isSneaking())
						amount = 1;
					else if(player.getPlayer().isSprinting())
						amount = 6;
					if(held.getAmount()-amount <= 0) {
						player.getPlayer().getInventory().setItemInMainHand(null);
					} else {
						held.setAmount(held.getAmount()-amount);
						player.getPlayer().getInventory().setItemInMainHand(held);
					}
					invisInterval = NINJA_CLASSIC_INVIS_INTERVAL;
				} else if(player.isInvisible()){
					invisInterval--;
				}
				
			} else {
				if(player.isInvisible())
					player.setInvisible(false);
			}
		} else {
			if(held != null && held.getType() == Material.REDSTONE) {
				if(invisMana>=NINJA_INVIS_CONSUME) {
					if(!player.isInvisible())
						player.setInvisible(true);
				} else {
					if(player.isInvisible())
						player.setInvisible(false);
				}
				if(player.isInvisible()) {
					if(invisMana-NINJA_INVIS_CONSUME<=0)
						invisMana=0;
					else
						invisMana-=NINJA_INVIS_CONSUME;
				}
			} else {
				if(player.isInvisible())
					player.setInvisible(false);
				if(invisMana+NINJA_INVIS_REGEN>=1)
					invisMana=1;
				else
					invisMana+=NINJA_INVIS_REGEN;
			}
			player.getPlayer().setExp(invisMana);
		}
		//regen
		if(held != null && held.getType() == Material.GOLDEN_SWORD && player.getPlayer().isSneaking()) {
			double max = player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			if(player.getPlayer().getHealth() >= max)
				ticksHpRegen = NINJA_HP_REGEN;
			else if (ticksHpRegen <= 0){
				if(player.getPlayer().getHealth()+1 >= max)
					player.getPlayer().setHealth(max);
				else
					player.getPlayer().setHealth(player.getPlayer().getHealth()+1);
				ticksHpRegen = NINJA_HP_REGEN;
			} else
				ticksHpRegen--;
		} else {
			ticksHpRegen = NINJA_HP_REGEN;
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getCause() == TeleportCause.ENDER_PEARL) {
			event.setCancelled(true);
			player.getPlayer().teleport(event.getTo());
			if(event.getFrom().distance(event.getTo())>5 || classic) {
				player.setCustomDamageCause(CustomDamageCause.NINJA_TELEPORT);
				player.getPlayer().damage(4);
			}
		}
	}
	
	@EventHandler
	public void onDed(PlayerDeathEvent event) {
		if(!event.getEntity().equals(player.getPlayer()))
			return;
		for(Entity e : player.getLocation().getWorld().getEntities()) {
			if(!(e instanceof EnderPearl))
				continue;
			EnderPearl pearl = (EnderPearl) e;
			if(pearl.getShooter() == null || !pearl.getShooter().equals(player.getPlayer()))
				continue;
			pearl.remove();
		}
	}
	
	@EventHandler
	public void onSponge(SpongeLaunchEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		canPearl = false;
		GameTimer timer = new GameTimer(-1);
		timer.onTick(t -> {
			if(event.isComplete()) {
				canPearl = true;
				t.unregister();
			}
		});
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		//pearl
		if(ItemBuilder.dataMatch(event.getItem(), "ninja_pearl")) {
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(!canPearl) {
					event.setCancelled(true);
					Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {
						player.getPlayer().setCooldown(Material.ENDER_PEARL, 0);
					});
					return;
				}
				if(ItemBuilder.dataMatch(player.getPlayer().getInventory().getItemInMainHand(), "flash_bomb")) {
					event.setCancelled(true);
					Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {
						player.getPlayer().setCooldown(Material.ENDER_PEARL, 0);
					});
					return;
				}
				if(classic) {
					Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> player.getPlayer().setCooldown(Material.ENDER_PEARL, 0));
				} else {
					int count = 0;
					int invSlot = 0;
					for(ItemStack i : player.getPlayer().getInventory().getContents()) {
						if(ItemBuilder.dataMatch(i, "ninja_pearl")) {
							invSlot = count;
							break;
						}
						count++;
					}
					final int slot = invSlot;
					Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> player.getPlayer().getInventory().setItem(slot, ItemBuilder.of(Material.ENDER_PEARL).name("Ninja Pearl").data("ninja_pearl").get()));
				}
			}
		}
		//flash bomb
		if(ItemBuilder.dataMatch(event.getItem(), "flash_bomb") && !classic) {
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(player.getPlayer().getCooldown(Material.EGG)>0)
					return;
				int count = 0;
				int invSlot = 0;
				for(ItemStack i : player.getPlayer().getInventory().getContents()) {
					if(ItemBuilder.dataMatch(i, "flash_bomb")) {
						invSlot = count;
						break;
					}
					count++;
				}
				final int slot = invSlot;
				Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> {
					player.getPlayer().getInventory().setItem(slot, ItemBuilder.of(Material.EGG).name("Flash Bomb").data("flash_bomb").get());
					Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> player.getPlayer().setCooldown(Material.EGG, 80));
				});
			}
		}
	}
	
	@EventHandler
	public void onEggHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Egg) {
			Egg e = (Egg) event.getEntity();
			GamePlayer p = Util.getOwner(e);
			if(p == null || !p.equals(player))
				return;
			if(event.getHitEntity() != null && event.getHitEntity() instanceof Player) {
				GamePlayer victim = GamePlayer.get((Player) event.getHitEntity());
				if(victim.getTeam() != null && player.getTeam() != null && !victim.getTeam().equals(player.getTeam())) {
					if(classic)
						victim.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
					else
						victim.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 90, 1));
				}
			}
			Location loc = e.getLocation();
			for(Player v : loc.getWorld().getPlayers()) {
				if(v.getLocation().distance(loc) < 3) {
					GamePlayer victim = GamePlayer.get(v);
					if(victim.getTeam() != null && player.getTeam() != null && !victim.getTeam().equals(player.getTeam())) {
						if(classic)
							victim.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 120, 1));
						else
							victim.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 180, 1));
					}
				}
			}
			loc.getWorld().createExplosion(loc, 0);
		}
	}

	@Override
	public String getName() {
		return "Ninja";
	}
	
	@Override
	public String getVariation() {
		if(classic)
			return "Classic";
		return "Default";
	}
	
	@Override
	public int getStealDelay() {
		return 15;
	}
	
	@Override
	public double getMovementSpeed() {
		return NINJA_SPEED;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.ENDER_PEARL);
	}
	
	@Override
	public ItemStack getIcon(String variation) {
		if(variation.equalsIgnoreCase("classic"))
			return ItemBuilder.of(Material.ENDER_PEARL).amount(10).get();
		return getIcon();
	}
	
	@Override
	public void setVariation(String variation) {
		if(variation.equalsIgnoreCase("classic")) 
			classic = true;
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default", "classic"};
		return variations;
	}
	
	@Override
	public ItemStack[] getFancyDisplay() {
		return new ItemStack[] {
				new ItemStack(Material.NETHER_STAR),
				new ItemStack(Material.ENDER_PEARL),
				new ItemStack(Material.EGG),
				new ItemStack(Material.REDSTONE),
				new ItemStack(ItemBuilder.of(Material.GOLDEN_SWORD).enchant(Enchantment.DAMAGE_ALL, 4).get()),
				new ItemStack(Material.REDSTONE),
				new ItemStack(Material.EGG),
				new ItemStack(Material.ENDER_PEARL),
				new ItemStack(Material.NETHER_STAR)
		};
	}

}
