package cz.agents.agentpolis.tools.geovisio.spy;

import com.google.inject.Injector;

import cz.agents.agentpolis.simulator.creator.initializator.InitFactory;

/**
 *
 *@author Marek Cuchy
 *
 */
public class SpyThreadInitFactory implements InitFactory{
	
	private int interval;
	private String visName;

	public SpyThreadInitFactory(int interval, String visName) {
		super();
		this.interval = interval;
		this.visName = visName;
	}

	public void initRestEnvironment(Injector injector) {
		Spy spy= new Spy(injector, interval, visName);
		spy.start();		
	}

}
