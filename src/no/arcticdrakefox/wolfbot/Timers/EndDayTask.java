package no.arcticdrakefox.wolfbot.Timers;

import java.util.TimerTask;

import no.arcticdrakefox.wolfbot.management.GameCore;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

public class EndDayTask extends TimerTask {

	private WolfBotModel model;
	
	public EndDayTask (WolfBotModel model)
	{
		this.model = model;
	}
	
	@Override	
	public void run() {
		GameCore.endDay(false, model);
	}

}
