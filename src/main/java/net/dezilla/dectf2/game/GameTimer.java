package net.dezilla.dectf2.game;

import org.bukkit.Bukkit;

import net.dezilla.dectf2.GameMain;

public class GameTimer {
	int taskId;
	boolean paused = true;
	long ticks = 1; //amount of ticks that has passed since timer creation
	TimerRunnable onEnd = null;
	TimerRunnable onTick = null;
	TimerRunnable onSecond = null;
	int seconds = 0;
	
	public GameTimer(int seconds) {
		this.seconds = seconds;
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameMain.getInstance(), () -> onTick(), 0, 1);
	}
	
	private void onTick() {
		if(ticks % 20 == 0) {
			//every seconds
			if(seconds == 0 && !paused) {
				pause();
				if(onEnd != null)
					onEnd.run(this);
			} else if(!paused) {
				seconds--;
			}
			if(onSecond!=null)
				onSecond.run(this);
		}
		if(onTick!=null)
			onTick.run(this);
		ticks++;
	}
	
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	
	public int getSeconds() {
		return seconds;
	}
	
	public String getTimeLeftDisplay() {
		int m = seconds/60;
		int s = seconds-(m*60);
		return String.format("%02d", m) + ":" + String.format("%02d", s);
	}
	
	public void pause() {
		paused = true;
	}
	
	public void onTick(TimerRunnable run) {
		onTick = run;
	}
	
	public void onSecond(TimerRunnable run) {
		onSecond = run;
	}
	
	public void onEnd(TimerRunnable run) {
		onEnd = run;
	}
	
	public void unpause() {
		paused = false;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public void unregister() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
	
	static public interface TimerRunnable {
		public void run(GameTimer timer);
	}

}
