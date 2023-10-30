package net.dezilla.dectf2.listeners.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

public class SpongeLaunchEvent extends PlayerEvent implements Cancellable{
	private static final HandlerList hlist = new HandlerList();
	
	private boolean cancelled = false;
	private boolean complete = false;
	private Vector vector;
	private int taskId;

	public SpongeLaunchEvent(Player player, Vector vector) {
		super(player);
		this.vector = vector;
	}
	
	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void setTaskId(int id) {
		taskId = id;
	}
	
	public int getTaskId() {
		return taskId;
	}
	
	public Vector getVector() {
		return vector;
	}
	
	public void incrementVector(double x, double y, double z) {
		Vector v = new Vector(x, y, z);
		vector.add(v);
	}
	
	public double getX() {
		return vector.getX();
	}
	
	public double getY() {
		return vector.getY();
	}
	
	public double getZ() {
		return vector.getZ();
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public void setComplete(boolean value) {
		complete = value;
	}

	@Override
	public HandlerList getHandlers() {
		return hlist;
	}
	
	public static HandlerList getHandlerList() {
		return hlist;
	}

}
