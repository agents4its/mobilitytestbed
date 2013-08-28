package cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.message;

import static com.google.common.base.Preconditions.checkNotNull;
import cz.agents.agentpolis.darptestbed.siminfrastructure.communication.passenger.receiver.PassengerReceiverVisitor;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.ondemandtransport.siminfrastructure.communication.protocol2.MessageVisitor;

/**
 * A proposal made by a driver, how he can transfer the passenger from point A
 * to B. (The response to a request.)
 * 
 * @author Lukas Canda
 */
public final class Proposal implements MessageVisitor<PassengerReceiverVisitor> {

	/**
	 * This proposal is a reply for this request
	 */
	private final Request request;
	private final String driverId;
	private final String vehicleId;
	/**
	 * Suggested price of the ride (however, depending on the algorithm, the
	 * final price can change in both directions)
	 */
	private final int price;
	/**
	 * Suggested time, when the passenger will be delivered into his target node
	 * (however, the accuracy of this time strongly depends on the algorithm)
	 */
	private final long arrivalTime;

	public Proposal(Request request, String driverId, String vehicleId) {
		this(request, driverId, vehicleId, 0, 0);
	}

	public Proposal(Request request, String driverId, String vehicleId, int price, long arrivalTime) {

		this.request = checkNotNull(request);
		this.driverId = checkNotNull(driverId);
		this.vehicleId = checkNotNull(vehicleId);
		this.price = price;
		this.arrivalTime = arrivalTime;
	}

	public Request getRequest() {
		return request;
	}

	public String getPassengerId() {
		return request.getPassengerId();
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public String getDriverId() {
		return driverId;
	}

	public int getPrice() {
		return price;
	}

	public long getArrivalTime() {
		return arrivalTime;
	}

	@Override
	public void accept(PassengerReceiverVisitor receiverVisitor) {
		receiverVisitor.visit(this);

	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + driverId + " " + vehicleId + " " + request.toString();
	}
}
