package cz.agents.agentpolis.darptestbed.simmodel.environment;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import cz.agents.agentpolis.darptestbed.simmodel.entity.vehicle.TestbedVehicle;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageProtocol;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageReceiverCallback;
import cz.agents.agentpolis.ondemandtransport.simmodel.enviroment.OndemandTransportEnvironmentModul;
import cz.agents.alite.common.event.EventProcessor;
import cz.agents.alite.communication.DefaultCommunicator;
import cz.agents.alite.communication.channel.CommunicationChannelException;
import cz.agents.alite.communication.eventbased.EventBasedCommunicationChannel;

/**
 * The modul of basic simulation environment
 * 
 * @author Lukas Canda
 */
public class TestbedEnvironmentModul extends AbstractModule {

	private final EventProcessor eventProcessor;

	public TestbedEnvironmentModul(EventProcessor eventProcessor) {

		super();
		this.eventProcessor = eventProcessor;
	}

	@Override
	protected void configure() {
		// you can add some binding here
		// bind(DispatchingLogic.class).to(DispatchingLogicDummyParallel.class);
		bind(new TypeLiteral<Map<String, TestbedVehicle>>() {
		}).toInstance(new HashMap<String, TestbedVehicle>());

	}

	@Provides
	@Singleton
	MessageProtocol provideMessageProtocol() {

		DefaultCommunicator communicator = new DefaultCommunicator(OndemandTransportEnvironmentModul.class.getClass()
				.getSimpleName() + "@" + MessageProtocol.class.hashCode());

		EventBasedCommunicationChannel eventBasedCommunicationChannel;
		try {
			eventBasedCommunicationChannel = new EventBasedCommunicationChannel(communicator, eventProcessor);
			communicator.addChannel(eventBasedCommunicationChannel);
		} catch (CommunicationChannelException e) {
			e.printStackTrace();
		}

		return new MessageProtocol(communicator, "Demo message protocol",
				new HashMap<String, MessageReceiverCallback>());
	}

}
