package net.dezilla.dectf2.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.dezilla.dectf2.GameMain;
import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.game.GameMatch;
import net.dezilla.dectf2.game.GameTeam;
import net.dezilla.dectf2.game.GameTimer;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.dezilla.dectf2.util.GameConfig;
import net.dezilla.dectf2.util.ItemBuilder;

public class MedicKit extends BaseKit{
	private static int MEDIC_WEB_REGEN = 120;
	private static int MEDIC_HP_REGEN = 120;
	private static int MEDIC_WEB_AMOUNT = 8;
	private static ItemStack webItem = ItemBuilder.of(Material.SNOWBALL).name("Totally functional web thing").data("medicweb").get();
	
	private int ticksWebRegen = MEDIC_WEB_REGEN;
	private int ticksHpRegen = MEDIC_HP_REGEN;

	public MedicKit(GamePlayer player) {
		super(player);
	}
	
	@Override
	public void setInventory() {
		super.setInventory();
		PlayerInventory inv = player.getPlayer().getInventory();
		inv.setHelmet(ItemBuilder.of(Material.GOLDEN_HELMET).name("Medic Helmet").unbreakable().armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
		inv.setChestplate(ItemBuilder.of(Material.GOLDEN_CHESTPLATE).name("Medic Chestplate").unbreakable().armorTrim(TrimPattern.SENTRY, color().getTrimMaterial()).get());
		inv.setLeggings(ItemBuilder.of(Material.GOLDEN_LEGGINGS).name("Medic Leggings").unbreakable().armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
		inv.setBoots(ItemBuilder.of(Material.GOLDEN_BOOTS).name("Medic Boots").unbreakable().armorTrim(TrimPattern.SILENCE, color().getTrimMaterial()).get());
		
		inv.setItem(0, ItemBuilder.of(Material.GOLDEN_SWORD).unbreakable().name("Medic Sword").get());
		inv.setItem(1, ItemBuilder.of(GameConfig.foodMaterial).name("Steak").amount(6).get());
		inv.setItem(2, ItemBuilder.of(webItem.clone()).amount(MEDIC_WEB_AMOUNT).get());
		
		inv.setItemInOffHand(ItemBuilder.of(Material.SHIELD).unbreakable().shieldColor(color().dyeColor()).get());
	}
	
	@Override
	public void onTick() {
		int amount = 0;
		for(ItemStack i : player.getPlayer().getInventory().getContents()) {
			if(i == null || i.getType() == Material.AIR)
				continue;
			if(ItemBuilder.getData(i) != null && ItemBuilder.getData(i).equals("medicweb")) {
				amount += i.getAmount();
			}
		}
		if(amount<MEDIC_WEB_AMOUNT) {
			if(ticksWebRegen<=0) {
				player.getPlayer().getInventory().addItem(webItem.clone());
				ticksWebRegen = MEDIC_WEB_REGEN;
			}
			else
				ticksWebRegen--;
		} else
			ticksWebRegen = MEDIC_WEB_REGEN;
		//passive regen
		double max = player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		if(player.getPlayer().getHealth() >= max)
			ticksHpRegen = MEDIC_HP_REGEN;
		else if (ticksHpRegen <= 0){
			if(player.getPlayer().getHealth()+1 >= max)
				player.getPlayer().setHealth(max);
			else
				player.getPlayer().setHealth(player.getPlayer().getHealth()+1);
			ticksHpRegen = MEDIC_HP_REGEN;
		} else
			ticksHpRegen--;
	}
	
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(!event.getPlayer().equals(player.getPlayer()))
			return;
		if(event.getItem() != null && event.getItem().getType() == Material.SNOWBALL && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if(player.getPlayer().getCooldown(Material.SNOWBALL) == 0) {
				Bukkit.getScheduler().runTask(GameMain.getInstance(), () -> player.getPlayer().setCooldown(Material.SNOWBALL, 5));
			}
		}
	}
	
	@EventHandler
	public void onHitOtherPlayer(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player) || !((Player)event.getDamager()).equals(player.getPlayer()))
			return;
		if(!(event.getEntity() instanceof Player))
			return;
		if(player.getPlayer().getInventory().getItemInMainHand() == null || player.getPlayer().getInventory().getItemInMainHand().getType() != Material.GOLDEN_SWORD)
			return;
		GamePlayer target = GamePlayer.get((Player) event.getEntity());
		if(target.getTeam() != null && player.getTeam() != null && target.getTeam().equals(player.getTeam())) {
			target.setCustomDamageCause(CustomDamageCause.MEDIC_HEAL);
			target.getPlayer().damage(0);
			target.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 25, 4));
		}
	}
	
	@EventHandler
	public void onWebHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Snowball) {
			Snowball s = (Snowball) event.getEntity();
			if(s.getShooter()!= null && s.getShooter().equals(player.getPlayer())) {
				Block b = event.getEntity().getLocation().getBlock();
				if(event.getHitBlock() != null)
					b = event.getHitBlock().getRelative(event.getHitBlockFace());
				Material m = b.getType();
				//prevent placing web on spawn block
				if(GameMatch.currentMatch != null) {
					for(GameTeam t : GameMatch.currentMatch.getTeams()) {
						if(b.getRelative(BlockFace.DOWN).getType() == t.getSpawnMaterial())
							return;
					}
				}
				//prevent web being placed where web already exists
				if(b.getType() == Material.COBWEB)
					return;
				
				b.setType(Material.COBWEB);
				GameTimer t = new GameTimer(2);
				t.unpause();
				final Block B = b;
				t.onEnd((timer) -> {
					B.setType(m);
					timer.unregister();
				});
			}
		}
	}
	
	@Override
	public void setEffects() {
		super.setEffects();
		player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
	}

	@Override
	public String getName() {
		return "Medic";
	}
	@Override
	public String getVariation() {
		return "Default";
	}

	@Override
	public ItemStack getIcon() {
		return new ItemStack(Material.GOLDEN_SWORD);
	}

	@Override
	public String[] getVariations() {
		String[] variations = {"default"};
		return variations;
	}

}