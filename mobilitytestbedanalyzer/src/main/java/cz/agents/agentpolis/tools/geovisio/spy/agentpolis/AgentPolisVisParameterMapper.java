package cz.agents.agentpolis.tools.geovisio.spy.agentpolis;

import java.awt.Color;

import cz.agents.agentpolis.publictransport.simmodel.agent.driver.PublicTransportDriverAgent;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.tools.geovisio.layer.visparameter.VisParameterMapper;
import cz.agents.agentpolis.tools.geovisio.layer.visparameter.VisParameters;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.agentstate.AgentState;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.agentstate.AgentStateStorage;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class AgentPolisVisParameterMapper implements VisParameterMapper {

	private final AgentStateStorage agentStateStorage;
	private final Class<? extends Agent> citizenClass;

	public AgentPolisVisParameterMapper(AgentStateStorage agentStateStorage, Class<? extends Agent> citizenClass) {
		super();
		this.agentStateStorage = agentStateStorage;
		this.citizenClass = citizenClass;
	}

	public VisParameters getVisParameter(Object object) {

		if (citizenClass.equals(object.getClass())) {
			AgentState state = agentStateStorage.getState(citizenClass.cast(object).getId());

			if (state == null) {
				return new VisParameters(Color.BLACK, 8);
			}

			Color color = Color.BLACK;
			switch (state.getState()) {
			case LOCATION:
				color = Color.GREEN;
				break;
			case TRAVEL:
				color = Color.CYAN;
				break;
			case WAITING:
				color = Color.MAGENTA;
				break;
			}

			return new VisParameters(color, 8);
		}
		if (object instanceof PublicTransportDriverAgent) {
			return new VisParameters(Color.RED, 5);
		}

		return new VisParameters(Color.BLACK, 5);
	}

}
