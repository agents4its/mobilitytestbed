package cz.agents.agentpolis.tools.geovisio.spy.darptestbed;

import java.awt.Color;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opengis.referencing.operation.TransformException;
import org.openstreetmap.osm.data.coordinates.LatLon;

import com.google.common.collect.Multimap;
import com.google.inject.Injector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import cz.agents.agentpolis.darptestbed.simmodel.agent.TestbedEntityType;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TimeWindow;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.DriverAgent;
import cz.agents.agentpolis.darptestbed.simmodel.agent.driver.logic.DriverLogic;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.PassengerAgent;
import cz.agents.agentpolis.darptestbed.simmodel.agent.passenger.logic.PassengerLogic;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.tools.geovisio.annotation.spatial.SpatialFieldTypeEnum;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeAllocator;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeFieldSource;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeSource;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeSourceContainer;
import cz.agents.agentpolis.tools.geovisio.database.DatabaseComponentFactory;
import cz.agents.agentpolis.tools.geovisio.database.DatabaseTable;
import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnection;
import cz.agents.agentpolis.tools.geovisio.database.connection.DatabaseConnectionSettings;
import cz.agents.agentpolis.tools.geovisio.database.h2.H2GeometryTableInserter;
import cz.agents.agentpolis.tools.geovisio.database.h2.H2InMemoryComponentFactory;
import cz.agents.agentpolis.tools.geovisio.database.h2.H2InMemoryConnection;
import cz.agents.agentpolis.tools.geovisio.database.table.GeometryTableFactory;
import cz.agents.agentpolis.tools.geovisio.geometryadapter.GeometryAdapter;
import cz.agents.agentpolis.tools.geovisio.geometryadapter.GeometryNotCreatedException;
import cz.agents.agentpolis.tools.geovisio.geometryadapter.PointAdapter;
import cz.agents.agentpolis.tools.geovisio.geometryadapter.reader.CoordinateNotCreatedException;
import cz.agents.agentpolis.tools.geovisio.geometryadapter.reader.CoordinateReader;
import cz.agents.agentpolis.tools.geovisio.layer.GeometryRecord;
import cz.agents.agentpolis.tools.geovisio.projection.ProjectionTransformer;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.AgentPolisDataReader;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.AgentPolisNodeMapCoordinateReader;
import cz.agents.agentpolis.tools.geovisio.spy.darptestbed.RequestStorage.State;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class DarpTestbedDataReader extends AgentPolisDataReader {

	private static final Logger logger = Logger.getLogger(DarpTestbedDataReader.class);

	private final int sourceSrid;

	private PointAdapter agentPointAdapter;
	private PointAdapter vehiclePointAdapter;

	private H2InMemoryConnection connection;

	private Collection<Agent> passengerAgents;
	private AttributeSourceContainer passengerContainer;
	private H2GeometryTableInserter passengerTableInserter;

	private Collection<Agent> taxiDriverAgents;
	private AttributeSourceContainer taxiDriverContainer;
	private H2GeometryTableInserter taxiDriverTableInserter;

	private final RequestStorage requestStorage;

	private final DatabaseConnectionSettings settings;

	private DarpTestbedVisParameterMapper visParameterMapper;

	private ProjectionTransformer transformer;

	private DatabaseComponentFactory dbFactory = new H2InMemoryComponentFactory();

	public DarpTestbedDataReader(DatabaseConnectionSettings settings, Injector injector, String visName, int interval,
			int sourceSrid, RequestStorage requestStorage) throws ReflectiveOperationException, InterruptedException {
		super(injector, visName, interval);
		this.sourceSrid = sourceSrid;
		this.requestStorage = requestStorage;
		this.settings = settings;
		initSources();
	}

	@Override
	public void readAndSaveData() throws IllegalArgumentException, SQLException, TransformException,
			ReflectiveOperationException {
		long start = System.currentTimeMillis();

		logger.debug("Number of passengers saved: " + passengerAgents.size());

		addToTable(passengerAgents, passengerContainer, passengerTableInserter, agentPointAdapter);
		addToTable(taxiDriverAgents, taxiDriverContainer, taxiDriverTableInserter, vehiclePointAdapter);

		long end = System.currentTimeMillis();
		logger.debug("Dump time: " + (end - start));
	}

	private <X extends Geometry> void addToTable(Collection<?> list, AttributeSourceContainer container,
			H2GeometryTableInserter inserter, GeometryAdapter<X> adapter) throws SQLException,
			IllegalArgumentException, ReflectiveOperationException, TransformException {
		AttributeAllocator<X> allocator = new AttributeAllocator<X>(container, visParameterMapper, adapter, transformer);

		allocator.openConnections();

		for (Object t : list) {
			GeometryRecord<X> record;
			try {
				record = allocator.createRecord(t);
				inserter.add(record);
			} catch (GeometryNotCreatedException e) {
			}
		}
		inserter.saveToDatabase();
		allocator.closeConnections();
	}

	@Override
	public void initReadingAndRead() throws Exception {
		logger.info("start initing");

		transformer = new ProjectionTransformer(sourceSrid, 4326);
		connection = new H2InMemoryConnection(settings);
		connection.connect();

		// init position reader
		Map<String, Long> agentsPositions = getAgentsPositions();
		Map<String, Long> vehiclesPositions = getVehiclesPositions();
		Map<Long, Node> allNodes = getAllNodes();

		CoordinateReader coordinateReader = new AgentPolisNodeMapCoordinateReader(agentsPositions, allNodes);
		agentPointAdapter = new PointAdapter(sourceSrid, coordinateReader);
		CoordinateReader vehicleCoordinateReader = new AgentPolisNodeMapCoordinateReader(vehiclesPositions, allNodes);
		vehiclePointAdapter = new PointAdapter(sourceSrid, vehicleCoordinateReader);

		passengerContainer = initAttributeContainerForPassenger();
		taxiDriverContainer = initAttributeContainerForTaxiDriver();
		visParameterMapper = new DarpTestbedVisParameterMapper(requestStorage, currentTimeSource);
		// vis = new Visualisation(visName, sourceSrid, 900913,
		// visualisationSettings, boundingBox, visParameterMapper,
		// true);

		Multimap<EntityType, Agent> agentMap = classifyAgents(getAllAgents().values());
		passengerAgents = agentMap.get(TestbedEntityType.PASSENGER);
		taxiDriverAgents = agentMap.get(TestbedEntityType.TAXI_DRIVER);

		// layer settings
		// ColumnType geometryType = ColumnType.POINT;
		// String styleName = NameSettings.POINT_PARAMETER_STYLE_NAME;
		// boolean enableTimeDimension = true;
		// boolean createIndexes = false;

		passengerTableInserter = createTableAndInserter(passengerContainer, "passengers");
		taxiDriverTableInserter = createTableAndInserter(taxiDriverContainer, "taxi_drivers");

		// passengerLayer = vis.visualize(passengerAgents, passengerContainer,
		// new LayerSettings(geometryType,
		// "passengers", styleName, enableTimeDimension, createIndexes),
		// agentPointAdapter);
		// taxiDriverlayer = vis.visualize(taxiDriverAgents,
		// taxiDriverContainer, new LayerSettings(geometryType,
		// "taxi_drivers", styleName, enableTimeDimension, createIndexes),
		// vehiclePointAdapter);

		visualizeNodes(allNodes);

		// init event handler
		initHandeling();
		logger.info("initing finished");

	}

	private H2GeometryTableInserter createTableAndInserter(AttributeSourceContainer container, String tableName)
			throws SQLException {
		GeometryTableFactory tableFactory = new GeometryTableFactory(dbFactory);
		DatabaseTable table = tableFactory.createGeomTable(tableName, Point.class, container);
		connection.executeUpdate(table.getCreateSQL());
		return new H2GeometryTableInserter(table, connection);
	}

	private void visualizeNodes(Map<Long, Node> allNodes) throws IllegalArgumentException, SQLException,
			TransformException, ReflectiveOperationException {
		AttributeSourceContainer nodesContainer = createAttributeContainerForNodes();
		PointAdapter adapter = new PointAdapter(sourceSrid, new CoordinateReader() {

			@Override
			public Coordinate getCoordinate(Object source) throws SQLException, CoordinateNotCreatedException {
				LatLon ll = (LatLon) source;
				return new Coordinate(ll.lon(), ll.lat());
			}

			@Override
			public void connect() throws SQLException, ClassNotFoundException {
			}

			@Override
			public void close() throws SQLException {
			}
		});

		H2GeometryTableInserter nodesInserter = createTableAndInserter(nodesContainer, "nodes");
		addToTable(allNodes.values(), nodesContainer, nodesInserter, adapter);
	}

	private AttributeSourceContainer createAttributeContainerForNodes() throws ReflectiveOperationException {
		AttributeSourceContainer container = new AttributeSourceContainer();
		container.addToSpatialFields(SpatialFieldTypeEnum.SINGLE_NODE,
				reflectionFactory.createAttributeFieldSource(Node.class, "latLon"));
		container.add(reflectionFactory.createAttributeFieldSource(Node.class, "id", "node_id"));

		return container;
	}

	private AttributeSourceContainer initAttributeContainerForTaxiDriver() throws ReflectiveOperationException {
		AttributeSourceContainer container = createSourceContainerForBasicAgent();

		AttributeFieldSource logicSource = reflectionFactory.createAttributeFieldSource(DriverAgent.class, "logic");

		AttributeFieldSource vehicleSource = reflectionFactory.createAttributeFieldSource(DriverLogic.class, "vehicle",
				logicSource);

		container.addAll(createAttributeSourcesForVehicle(vehicleSource));

		container.add(reflectionFactory.createAttributeFieldSource(DriverLogic.class, "numOfPassenToGetIn",
				"number_of_passengers_to_get_in", logicSource));

		container.add(reflectionFactory.createAttributeMethodSource(DriverLogic.class, "isOnTheWay", "is_on_the_way",
				logicSource));
		container.add(reflectionFactory.createAttributeMethodSource(DriverLogic.class, "isEverybodyOnBoard",
				"is_everybody_on_board", logicSource));

		return container;
	}

	private AttributeSourceContainer initAttributeContainerForPassenger() throws ReflectiveOperationException {
		AttributeSourceContainer container = createSourceContainerForBasicAgent();

		AttributeFieldSource logicSource = reflectionFactory.createAttributeFieldSource(PassengerAgent.class, "logic");
		final AttributeFieldSource confirmedRequestSource = reflectionFactory.createAttributeFieldSource(
				PassengerLogic.class, "currentRequestConfirmed");
		final AttributeFieldSource lastSentRequestSource = reflectionFactory.createAttributeFieldSource(
				PassengerLogic.class, "requestLastSent");

		AttributeSource requestSource = new AttributeSource(logicSource) {
			@Override
			protected Object getValueFromSource(Object source) throws IllegalArgumentException,
					ReflectiveOperationException {
				Object confirmedRequest = confirmedRequestSource.getValue(source);
				if (confirmedRequest != null) {
					return confirmedRequest;
				} else {
					return lastSentRequestSource.getValue(source);
				}
			}

		};

        AttributeFieldSource agentIdSource = createAgentIdSource();
        AttributeSource requestStatusSource = new AttributeSource(agentIdSource, "request_status", String.class) {
			@Override
			protected Object getValueFromSource(Object source) throws IllegalArgumentException,
					ReflectiveOperationException {
				long time = currentTimeSource.getValue(source);
				State state = requestStorage.getCurrentState((String) source, time);
				return state;
			}
		};
		container.add(requestStatusSource);
		container.add(reflectionFactory.createAttributeFieldSource(PassengerLogic.class, "currentDriverId",
				"current_driver_id", logicSource));
		container.add(reflectionFactory.createAttributeFieldSource(PassengerLogic.class, "currentVehicleId",
				"current_vehicle_id", logicSource));

        AttributeSource successfulArrivalTimeSource = new AttributeSource(logicSource, "successful_arrival_time",
                Timestamp.class) {
            @Override
            protected Object getValueFromSource(Object source) throws IllegalArgumentException, ReflectiveOperationException {
                PassengerLogic logic = (PassengerLogic) source;
                return logic.getSuccessfulArrivalTime();
            }
        };
        container.add(successfulArrivalTimeSource);

        container.add(reflectionFactory.createAttributeFieldSource(Request.class, "fromNode", "request_from_node",
				requestSource));
        container.add(reflectionFactory.createAttributeFieldSource(Request.class, "toNode", "request_to_node",
				requestSource));

        AttributeFieldSource timeWindowSource = reflectionFactory.createAttributeFieldSource(Request.class,
				"timeWindow", requestSource);

        container.add(reflectionFactory.createAttributeFieldSource(TimeWindow.class, "departMin",
				"request_departure_min", timeWindowSource, Timestamp.class));
        container.add(reflectionFactory.createAttributeFieldSource(TimeWindow.class, "departMax",
				"request_departure_max", timeWindowSource, Timestamp.class));
        container.add(reflectionFactory.createAttributeFieldSource(TimeWindow.class, "arriveMin",
				"request_arrival_min", timeWindowSource, Timestamp.class));
        container.add(reflectionFactory.createAttributeFieldSource(TimeWindow.class, "arriveMax",
				"request_arrival_max", timeWindowSource, Timestamp.class));

        container.add(reflectionFactory.createAttributeFieldSource(Request.class, "callTimeInDayRange",
                "request_call_time", requestSource, Timestamp.class));


		return container;
	}

	@Override
	public void onSimulationFinished() throws SQLException, TransformException {
		// passengerLayer.createIndexes();
		// taxiDriverlayer.createIndexes();

		passengerTableInserter.createIndexes();
		taxiDriverTableInserter.createIndexes();

		// layers created by selects from tables created during simulation
		// createTaxiToPassengerLayer();
		// createRequestLayer();

		// vis.closeConnectionToDb();
	}

	/**
	 * Create layer with requests from already existing tables
	 * 
	 * @throws SQLException
	 * @throws TransformException
	 */
	private void createRequestLayer() throws SQLException, TransformException {
		String layerName = "requests";

		DatabaseTable table = dbFactory.createTable(layerName);
		table.addColumn(dbFactory.createColumn("geom", LineString.class, true));
		table.addColumn(dbFactory.createColumn("from_time", Timestamp.class, true));
		table.addColumn(dbFactory.createColumn("passenger", String.class, true));
		table.addColumn(dbFactory.createColumn("taxi_driver", String.class, true));
		table.addColumn(dbFactory.createColumn("request_status", String.class, true));
		table.addColumn(dbFactory.createColumn("request_departure_min", Timestamp.class, true));
		table.addColumn(dbFactory.createColumn("request_departure_max", Timestamp.class, true));
		table.addColumn(dbFactory.createColumn("request_arrival_min", Timestamp.class, true));
		table.addColumn(dbFactory.createColumn("request_arrival_max", Timestamp.class, true));
        table.addColumn(dbFactory.createColumn("successful_arrival_time", Timestamp.class, true));
		table.addColumn(dbFactory.createColumn("color", double.class, true));
		table.addColumn(dbFactory.createColumn("style_parameter", double.class, true));
		connection.createComponent(table);

		// vis.getGeoserverConnection().publishLayer(
		// new Layer<LineString>(layerName, table, visName, visName,
		// NameSettings.LINE_PARAMETER_STYLE_NAME, vis
		// .getProjectionTransformer().transform(boundingBox), true), 900913);

		Color color = Color.MAGENTA;
		double styleParam = 2;

		DatabaseConnection conn = connection;
		conn.executeUpdate("INSERT INTO "
				+ table.getFullName()
				+ " select st_setsrid(st_makeline(n1.geom,n2.geom),900913) as geom,pas.from_time,pas.agentid as passenger, pas.current_driver_id as taxi_driver,pas.request_status,pas.request_departure_min, pas.request_departure_max,pas.request_arrival_min, pas.request_arrival_max, "
				+ color.getRGB() + " as color," + styleParam + "as style_parameter from " + "passengers as pas, "
				+ "nodes as n1, "
				+ "nodes as n2 where pas.request_from_node = n1.node_id and pas.request_to_node = n2.node_id ");
		conn.executeUpdate(table.getCreateIndexesSQL());
	}

	/**
	 * Create layer with taxis assigned to passenger from already existing
	 * tables
	 * 
	 * @throws SQLException
	 * @throws TransformException
	 */
	private void createTaxiToPassengerLayer() throws SQLException, TransformException {
		String layerName = "taxi_to_passenger_pair";

		DatabaseTable table = dbFactory.createTable(layerName);
		table.addColumn(dbFactory.createColumn("geom", LineString.class, true));
		table.addColumn(dbFactory.createColumn("from_time", Timestamp.class, true));
		table.addColumn(dbFactory.createColumn("passenger", String.class, true));
		table.addColumn(dbFactory.createColumn("taxi_driver", String.class, true));
		table.addColumn(dbFactory.createColumn("color", double.class, true));
		table.addColumn(dbFactory.createColumn("style_parameter", double.class, true));
		connection.createComponent(table);

		// vis.getGeoserverConnection().publishLayer(
		// new Layer<LineString>(layerName, table, visName, visName,
		// NameSettings.LINE_PARAMETER_STYLE_NAME, vis
		// .getProjectionTransformer().transform(boundingBox), true), 900913);

		Color color = Color.CYAN;
		double styleParam = 2;

		DatabaseConnection conn = connection;
		conn.executeUpdate("INSERT INTO "
				+ table.getFullName()
				+ " select st_setsrid(st_makeline(pas.geom , taxi.geom),900913) as geom,pas.from_time, pas.agentid as passenger, taxi.agentid as taxi_driver, "
				+ color.getRGB() + " as color, " + styleParam + " as style_parameter from "
				+ "passengers as pas inner join "
				+ "taxi_drivers as taxi on pas.current_driver_id = taxi.agentid and pas.from_time = taxi.from_time ");
		conn.executeUpdate(table.getCreateIndexesSQL());
	}

}
