package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.message;

import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.driver.receiver.DriverReceiverVisitor;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol.MessageVisitor;

public class PassengerSaysTaxiIsTooLateMessage implements MessageVisitor<DriverReceiverVisitor> {

	public final String passengerId;
	/**
	 * Departure/arrival was delayed
	 */
	public final boolean departure;
	/**
	 * The delay in milliseconds (counted according to time window)
	 */
	public final long delay;

	public PassengerSaysTaxiIsTooLateMessage(String passengerId, boolean departure, long delay) {
		super();
		this.passengerId = passengerId;
		this.departure = departure;
		this.delay = delay;
	}

	@Override
	public void accept(DriverReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);

	}

}
