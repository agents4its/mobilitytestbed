package cz.agents.agentpolis.tools.geovisio.spy;

import com.google.inject.Injector;

import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.JMKAgentPolisDataReader;
import eu.superhub.wp4.model.simmodel.agent.citizen.CitizenAgent;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class Spy extends Thread {

	private final Injector injector;
	private final int interval;
	private final String visName;

	public Spy(Injector injector, int interval, String visName) {
		super();
		this.injector = injector;
		this.interval = interval;
		this.visName = visName;
	}

	@Override
	public void run() {
		try {
			JMKAgentPolisDataReader reader = new JMKAgentPolisDataReader(injector, visName, interval,
					CitizenAgent.class);
			reader.initReadingAndRead();
		} catch (ReflectiveOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
