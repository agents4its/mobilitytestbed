package cz.agents.agentpolis.darptestbed.simulator.initializator.osm;

import java.util.Collection;
import java.util.Map;

import net.sf.javaml.core.kdtree.KDTree;

import org.openstreetmap.osm.data.coordinates.LatLon;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.vividsolutions.jts.geom.Coordinate;

import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNetwork;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.highway.HighwayNode;
import cz.agents.agentpolis.simulator.creator.initializator.InitModulFactory;
import cz.agents.agentpolis.utils.spatialrefsys.WGS84Convertor;

public class NearestNodeInitModulFactory implements InitModulFactory {

	private final int epsg;

	public NearestNodeInitModulFactory(int epsg) {
		super();
		this.epsg = epsg;
	}

	@Override
	public AbstractModule injectModule(Injector injector) {

		Collection<HighwayNode> allNetworkNodes = injector.getInstance(HighwayNetwork.class).getNetwork().getAllNodes();
		WGS84Convertor wgs84Convertor = WGS84Convertor.createConvertorFromWGS84ToSpatialRefSys(epsg);

		Map<Long, Coordinate> projectedNodeCoordinats = Maps.newHashMap();

		final KDTree kdTree = new KDTree(2);
		for (Node node : allNetworkNodes) {
			LatLon latLon = node.getLatLon();
			Coordinate coordinate = wgs84Convertor.convert(latLon.lon(), latLon.lat());
			projectedNodeCoordinats.put(node.getId(), coordinate);
			kdTree.insert(new double[] { coordinate.x, coordinate.y }, node.getId());

		}

		final NodeExtendedFunction nearestNodeFinder = new NodeExtendedFunction(projectedNodeCoordinats, kdTree,
				wgs84Convertor);

		return new AbstractModule() {

			@Override
			protected void configure() {
				// TODO Auto-generated method stub

			}

			@Singleton
			@Provides
			public NodeExtendedFunction provideNearestNodeFinder() {
				return nearestNodeFinder;
			}

		};
	}

}