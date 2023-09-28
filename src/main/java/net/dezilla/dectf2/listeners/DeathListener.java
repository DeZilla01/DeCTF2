package net.dezilla.dectf2.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.dezilla.dectf2.GamePlayer;
import net.dezilla.dectf2.util.CustomDamageCause;
import net.md_5.bungee.api.ChatColor;

//This was mostly created to keep EventListener clean from the large amount of death messages
public class DeathListener implements Listener{
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		GamePlayer p = GamePlayer.get(event.getEntity());
		GamePlayer killer = p.getLastAttacker();
		String msg = p.getColoredName()+ChatColor.RESET+" ";
		DamageCause cause = p.getPlayer().getLastDamageCause().getCause();
		List<String> list = new ArrayList<String>();
		boolean notByKiller = false;
		switch(cause) {
			case BLOCK_EXPLOSION:
				list.add("exploded into pieces");
				break;
			case CONTACT:
				list.add("kissed the wrong block");
				notByKiller=true;
				break;
			case CRAMMING:
				list.add("was squished too much");
				break;
			case CUSTOM:
				CustomDamageCause customCause = p.getCustomDamageCause();
				if(customCause == null)
					break;
				switch(customCause) {
					case ENEMY_SPAWN:
						list.add("walked in the wrong spawn");
						notByKiller = true;
						break;
					case FLAG_POISON:
						list.add("died from flag poisoning");
						notByKiller = true;
						break;
					case SPAWN_WITH_FLAG:
						list.add("decided that he would bring the flag to his spawn");
						notByKiller = true;
						break;
					case KIT_SWITCH:
						list.add("switched kit");
						notByKiller = true;
						break;
					case SHIELDED_DAMAGE:
						list.add("was pierced");
						list.add("was killed");
						list.add("was slain");
						list.add("relied too much on his shield and got killed");
						break;
					case ARCHER_HEADSHOT:
						list.add("was headshoted");
						list.add("got snipped");
						break;
					case NINJA_TELEPORT:
						list.add("teleported to his doom");
						list.add("teleported to death");
						list.add("died from teleportation");
						list.add("pearled to his death");
						notByKiller=true;
					default:
						break;
				}
				break;
			case DRAGON_BREATH:
				list.add("was roasted in dragon's breath");
				break;
			case DROWNING:
				list.add("drowned");
				notByKiller = true;
				break;
			case DRYOUT:
				list.add("died from dehydration");
				notByKiller = true;
				break;
			case ENTITY_ATTACK:
				list.add("was slain");
				list.add("was killed");
				if(killer != null && (killer.getPlayer().getInventory().getItemInMainHand() == null || killer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR)) {
					list.clear();
					list.add("was punched");
					list.add("was fisted");
				}
				break;
			case ENTITY_EXPLOSION:
				list.add("exploded into pieces");
				list.add("was blown up");
				break;
			case ENTITY_SWEEP_ATTACK:
				list.add("was sweeped");
				break;
			case FALL:
				list.add("fell from too high");
				list.add("forgot he can't fly");
				list.add("broke his legs");
				notByKiller = true;
				break;
			case FALLING_BLOCK:
				list.add("didn't look above");
				list.add("was killed by a block from above");
				notByKiller = true;
				break;
			case FIRE:
				list.add("burned to death");
				notByKiller = true;
				break;
			case FIRE_TICK:
				break;
			case FLY_INTO_WALL:
				break;
			case FREEZE:
				break;
			case HOT_FLOOR:
				break;
			case KILL:
				list.add("was killed by console");
				list.add("was killed by the magic of administrator power");
				notByKiller = true;
				break;
			case LAVA:
				list.add("swimmed in lava");
				list.add("mistook lava for water");
				notByKiller = true;
				break;
			case LIGHTNING:
				break;
			case MAGIC:
				break;
			case MELTING:
				break;
			case POISON:
				break;
			case PROJECTILE:
				list.add("was shot");
				break;
			case SONIC_BOOM:
				break;
			case STARVATION:
				break;
			case SUFFOCATION:
				break;
			case SUICIDE:
				list.add("killed himself");
				list.add("commited sudoku");
				notByKiller = true;
				break;
			case THORNS:
				break;
			case VOID:
				list.add("felled off the map");
				list.add("felled in the void");
				notByKiller=true;
				break;
			case WITHER:
				break;
			case WORLD_BORDER:
				break;
			default:
				break;
		}
		if(list.isEmpty())
			list.add("died");
		msg+=list.get((int) (Math.random()*list.size()));
		if(killer != null) {
			if(notByKiller)
				msg+=" while running from ";
			else
				msg+=" by ";
			msg+=killer.getColoredName()+ChatColor.RESET+".";
			killer.getPlayer().sendMessage(msg);
		} else
			msg+=".";
		p.getPlayer().sendMessage(msg);
	}

}
