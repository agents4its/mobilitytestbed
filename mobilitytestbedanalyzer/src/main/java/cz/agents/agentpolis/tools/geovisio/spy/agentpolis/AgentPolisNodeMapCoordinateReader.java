package cz.agents.agentpolis.tools.geovisio.spy.agentpolis;

import java.sql.SQLException;
import java.util.Map;

import org.openstreetmap.osm.data.coordinates.LatLon;

import com.vividsolutions.jts.geom.Coordinate;

import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.tools.geovisio.geometryadapter.reader.CoordinateNotCreatedException;
import cz.agents.agentpolis.tools.geovisio.geometryadapter.reader.CoordinateReader;

/**
 *
 *@author Marek Cuchy
 *
 */
public class AgentPolisNodeMapCoordinateReader implements CoordinateReader {

	private Map<?,?> agentToNodeIdMap;
	private Map<?,Node> nodeIdMap;
	
	public AgentPolisNodeMapCoordinateReader(Map<?, ?> agentToNodeIdMap, Map<?, Node> nodeIdMap) {
		super();
		this.agentToNodeIdMap = agentToNodeIdMap;
		this.nodeIdMap = nodeIdMap;
	}

	public Coordinate getCoordinate(Object agentId) throws SQLException, CoordinateNotCreatedException {
		Object nodeId = agentToNodeIdMap.get(agentId);
		Node node = nodeIdMap.get(nodeId);
		if(node==null){
			throw new CoordinateNotCreatedException(agentId);
		}
		LatLon latLon = node.getLatLon();
		return new Coordinate(latLon.lon(), latLon.lat());
	}

	public void close() throws SQLException {
	}

	public void connect() throws SQLException, ClassNotFoundException {
	}

}
