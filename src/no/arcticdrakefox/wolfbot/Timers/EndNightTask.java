package no.arcticdrakefox.wolfbot.Timers;

import java.util.TimerTask;

import no.arcticdrakefox.wolfbot.management.GameCore;
import no.arcticdrakefox.wolfbot.model.WolfBotModel;

// This force-ends night.
public class EndNightTask extends TimerTask {

	WolfBotModel model;
	
	public EndNightTask (WolfBotModel model)
	{
		this.model = model;
	}
	
	@Override
	public void run() {
		GameCore.endNight(model);
	}

}
