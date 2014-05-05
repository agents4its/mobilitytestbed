package cz.agents.agentpolis.darptestbed.simulator.initializator.osm;

import com.vividsolutions.jts.geom.Coordinate;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.utils.spatialrefsys.WGS84Convertor;
import net.sf.javaml.core.kdtree.KDTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class KNodesExtendedFunction extends NodeExtendedFunction {

	public KNodesExtendedFunction(Map<Long, Coordinate> projectedNodeCoordinates, KDTree kdTreeForAllNodes,
                                  WGS84Convertor wgs84Convertor) {
		super(projectedNodeCoordinates, kdTreeForAllNodes, wgs84Convertor);
	}

    @Override
	public Long getNearestNodeByNodeId(double longitude, double latitude) {
		return getKNearestNodesByNodeId(longitude, latitude, 1).get(0);

	}

    public List<Long> getKNearestNodesByNodeId(double longitude, double latitude, int count) {
        Coordinate coordinate = getWgs84Convertor().convert(longitude, latitude);
        return (List<Long>) (List) Arrays.asList(getKdTreeForAllNodes().nearest(new double[]{coordinate.x,
                coordinate.y}, count));

    }

    public Long getNearestNodeByNode(Node node) {
        return getKNearestNodesByNode(node, 1).get(0);

    }

    public List<Long> getKNearestNodesByNode(Node node, int count) {
        Coordinate coordinate = getWgs84Convertor().convert(node.getLatLon().lon(), node.getLatLon().lat());
        return (List<Long>) (List) Arrays.asList(getKdTreeForAllNodes().nearest(new double[]{coordinate.x, coordinate.y},
                count));

    }

    public List<Long> getSquareWithNodeInCenter(Node node, int edge) {
        Coordinate coordinate = getWgs84Convertor().convert(node.getLatLon().lon(), node.getLatLon().lat());
        return (List<Long>) (List) Arrays.asList(getKdTreeForAllNodes().range(
                new double[] {coordinate.x - edge / 2, coordinate.y - edge / 2},
                new double[] {coordinate.x + edge / 2, coordinate.y + edge / 2}));

    }



    @Override
	public double computeDistanceBetweenNodes(long fromNodeId, long toNodeId) {
		Coordinate from = getProjectedNodeCoordinats().get(fromNodeId);
		Coordinate to = getProjectedNodeCoordinats().get(toNodeId);
		return from.distance(to);
	}

}
