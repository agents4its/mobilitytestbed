package cz.agents.agentpolis.tools.geovisio.spy.darptestbed;

import java.awt.Color;

import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.DriverAgent;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerAgent;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeMethodSource;
import cz.agents.agentpolis.tools.geovisio.layer.visparameter.VisParameterMapper;
import cz.agents.agentpolis.tools.geovisio.layer.visparameter.VisParameters;
import cz.agents.agentpolis.tools.geovisio.spy.darptestbed.RequestStorage.State;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class DarpTestbedVisParameterMapper implements VisParameterMapper {

	private final RequestStorage requestStorage;
	private final AttributeMethodSource currentTimeSource;

	public DarpTestbedVisParameterMapper(RequestStorage requestStorage, AttributeMethodSource currentTimeSource) {
		super();
		this.requestStorage = requestStorage;
		this.currentTimeSource = currentTimeSource;
	}

	@Override
	public VisParameters getVisParameter(Object object) {
		if (object instanceof PassengerAgent) {
			Color color = getPassengerColor(((PassengerAgent) object).getId());
			return new VisParameters(color, 5);
		}
		if (object instanceof DriverAgent) {
			return new VisParameters(Color.GREEN, 5);
		}
		return new VisParameters(Color.BLACK, 5);
	}

	private Color getPassengerColor(String passengerId) {
		try {
			State state = requestStorage.getCurrentState(passengerId, (long) currentTimeSource.getValue(passengerId));
			switch (state) {
			case NONE:
			case SENT:
			case OUT_OF_VEHICLE:
				return Color.BLACK;
			case DELAYED_SENT:
			case DELAYED_CONFIRMED:
			case IN_VEHICLE_WITH_DELAYED_ARRIVAL:
			case IN_VEHICLE_WITH_DELAYED_DEPARTURE:
				return Color.RED;
			case IN_VEHICLE:
				return Color.ORANGE;
			case OUT_OF_VEHICLE_WITH_DELAYED_ARRIVAL:
				return Color.MAGENTA;
			case CONFIRMED:
				return Color.BLUE;

			default:
				return Color.BLACK;
			}
		} catch (IllegalArgumentException | ReflectiveOperationException e) {
			return Color.BLACK;
		}
	}

}
