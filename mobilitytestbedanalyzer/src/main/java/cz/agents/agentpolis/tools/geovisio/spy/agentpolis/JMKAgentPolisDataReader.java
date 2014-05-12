package cz.agents.agentpolis.tools.geovisio.spy.agentpolis;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opengis.referencing.operation.TransformException;

import com.google.common.collect.Multimap;
import com.google.inject.Injector;
import com.vividsolutions.jts.geom.Point;

import cz.agents.agentpolis.publictransport.simmodel.agent.PTEntity;
import cz.agents.agentpolis.publictransport.simmodel.agent.driver.PublicTransportDriverAgent;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trip;
import cz.agents.agentpolis.siminfrastructure.time.TimeProvider;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simmodel.entity.AgentPolisEntity;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.tools.geovisio.annotation.spatial.SpatialFieldTypeEnum;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeFieldSource;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeMethodSource;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeSource;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeSourceContainer;
import cz.agents.agentpolis.tools.geovisio.attributesource.reflectiondescriptor.FieldDescriptor;
import cz.agents.agentpolis.tools.geovisio.attributesource.reflectiondescriptor.MethodDescriptor;
import cz.agents.agentpolis.tools.geovisio.geometryadapter.PointAdapter;
import cz.agents.agentpolis.tools.geovisio.geometryadapter.reader.CoordinateReader;
import cz.agents.agentpolis.tools.geovisio.layer.BoundingBox;
import cz.agents.agentpolis.tools.geovisio.layer.Layer;
import cz.agents.agentpolis.tools.geovisio.layer.LayerSettings;
import cz.agents.agentpolis.tools.geovisio.settings.NameSettings;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.agentstate.AgentState;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.agentstate.AgentStateStorage;
import cz.agents.agentpolis.tools.geovisio.visualisation.Visualisation;
import cz.agents.agentpolis.tools.geovisio.visualisation.VisualisationSettings;
import eu.superhub.wp4.model.simmodel.agent.citizen.attribute.ECitzen;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class JMKAgentPolisDataReader extends AgentPolisDataReader {

	private static final Logger logger = Logger.getLogger(JMKAgentPolisDataReader.class);

	private final static VisualisationSettings AGENT4ITS = new VisualisationSettings("mf.felk.cvut.cz", 5432, "visio",
	        "geovisio", "visio", "http://mf.felk.cvut.cz:8080/geoserver", "admin", "geovisio");
	private final static BoundingBox BRNO_BOUNDING_BOX = new BoundingBox(15.5932660040707, 48.7203890081813,
	        17.6299490085198, 49.6677640100243, 4326);

	// private final static BoundingBox GRID_TEST_BOUNDING_BOX = new
	// BoundingBox(14.3813896179199, 50.0901718139648,
	// 14.4517412185669, 50.1352806091309, 4326);

	private Visualisation vis;
	private AttributeSourceContainer citizenContainer;
	private AttributeSourceContainer driverContainer;

	private PointAdapter agentPointAdapter;
	private PointAdapter vehiclePointAdapter;

	private Collection<Agent> citizenAgents;
	private Layer<Point> citizenlayer;

	private Collection<Agent> busDriverAgents;
	private Layer<Point> busDriverlayer;

	private Collection<Agent> tramDriverAgents;
	private Layer<Point> tramDriverlayer;

	private final AgentStateStorage agentStateStorage;

	private final int sourceSrid = 4326;
	private final Class<? extends Agent> citizenClass;

	public JMKAgentPolisDataReader(Injector injector, String visName, int interval, Class<? extends Agent> citizenClass)
	        throws ReflectiveOperationException, InterruptedException {
		super(injector, visName, interval);
		this.citizenClass = citizenClass;
		agentStateStorage = injector.getInstance(AgentStateStorage.class);
		initSources();
	}

	public void onSimulationFinished() throws SQLException {
		citizenlayer.createIndexes();
		busDriverlayer.createIndexes();
		tramDriverlayer.createIndexes();
		vis.closeConnectionToDb();
	}

	@Override
	public void initReadingAndRead() throws Exception {

		logger.info("start initing");

		// init position reader
		Map<String, Long> agentsPositions = getAgentsPositions();
		Map<String, Long> vehiclesPositions = getVehiclesPositions();
		Map<Long, Node> allNodes = getAllNodes();

		CoordinateReader coordinateReader = new AgentPolisNodeMapCoordinateReader(agentsPositions, allNodes);
		agentPointAdapter = new PointAdapter(sourceSrid, coordinateReader);
		CoordinateReader vehicleCoordinateReader = new AgentPolisNodeMapCoordinateReader(vehiclesPositions, allNodes);
		vehiclePointAdapter = new PointAdapter(sourceSrid, vehicleCoordinateReader);

		vis = new Visualisation(visName, sourceSrid, 900913, AGENT4ITS, BRNO_BOUNDING_BOX,
		        new AgentPolisVisParameterMapper(agentStateStorage, citizenClass), true);
		citizenContainer = initAttributeContainerForCitizen();
		driverContainer = initAttributeContainerForBusDriver();

		Multimap<EntityType, Agent> agentMap = classifyAgents(getAllAgents().values());
		citizenAgents = agentMap.get(ECitzen.CITIZEN);
		busDriverAgents = agentMap.get(PTEntity.PUBLIC_TRANPORT_DRIVER_BUS);
		tramDriverAgents = agentMap.get(PTEntity.PUBLIC_TRANPORT_DRIVER_TRAM);

		// layer settings
		Class<Point> geometryType = Point.class;
		String styleName = NameSettings.POINT_PARAMETER_STYLE_NAME;
		boolean enableTimeDimension = true;
		boolean createIndexes = false;

		// init citizenlayer and save current values
		citizenlayer = vis.visualize(citizenAgents, citizenContainer, new LayerSettings(geometryType, "agents",
		        styleName, enableTimeDimension, createIndexes), agentPointAdapter);
		busDriverlayer = vis.visualize(busDriverAgents, driverContainer, new LayerSettings(geometryType, "bus_drivers",
		        styleName, enableTimeDimension, createIndexes), vehiclePointAdapter);
		tramDriverlayer = vis.visualize(tramDriverAgents, driverContainer, new LayerSettings(geometryType,
		        "tram_drivers", styleName, enableTimeDimension, createIndexes), vehiclePointAdapter);

		// init event handler
		initHandeling();
		logger.info("initing finished");
	}

	@Override
	public void readAndSaveData() throws IllegalArgumentException, SQLException, TransformException,
	        ReflectiveOperationException {
		long start = System.currentTimeMillis();

		vis.addToLayer(citizenAgents, citizenContainer, citizenlayer, agentPointAdapter);
		vis.addToLayer(busDriverAgents, driverContainer, busDriverlayer, vehiclePointAdapter);
		vis.addToLayer(tramDriverAgents, driverContainer, tramDriverlayer, vehiclePointAdapter);

		long end = System.currentTimeMillis();
		logger.info("Dump time: " + (end - start));

	}

	private AttributeSourceContainer initAttributeContainerForCitizen() throws ReflectiveOperationException {
		AttributeSourceContainer container = createSourceContainerForBasicAgent();

		AttributeSource agentStateSource = new AttributeSource(null) {
			@Override
			protected Object getValueFromSource(Object source) throws IllegalArgumentException,
			        ReflectiveOperationException {
				String id = ((Agent) source).getId();
				return agentStateStorage.getState(id);
			}
		};

		FieldDescriptor stateField = new FieldDescriptor(AgentState.class, "state");
		AttributeFieldSource stateFieldSource = new AttributeFieldSource(agentStateSource, "state", String.class,
		        stateField);
		container.add(stateFieldSource);
		FieldDescriptor durationField = new FieldDescriptor(AgentState.class, "duration");
		AttributeFieldSource durationFieldSource = new AttributeFieldSource(agentStateSource, "duration",
		        Timestamp.class, durationField);
		container.add(durationFieldSource);
		FieldDescriptor startTimeField = new FieldDescriptor(AgentState.class, "startTime");
		AttributeFieldSource startTimeFieldSource = new AttributeFieldSource(agentStateSource, "start_time",
		        Timestamp.class, startTimeField);
		container.add(startTimeFieldSource);
		FieldDescriptor endTimeField = new FieldDescriptor(AgentState.class, "endTime");
		AttributeFieldSource endTimeFieldSource = new AttributeFieldSource(agentStateSource, "end_time",
		        Timestamp.class, endTimeField);
		container.add(endTimeFieldSource);
		FieldDescriptor descriptionField = new FieldDescriptor(AgentState.class, "description");
		AttributeFieldSource descriptionFieldSource = new AttributeFieldSource(agentStateSource, "description",
		        String.class, descriptionField);
		container.add(descriptionFieldSource);

		FieldDescriptor dayPlanField = new FieldDescriptor(citizenClass, "dayPlanActivities");
		AttributeFieldSource dayPlanFieldSource = new AttributeFieldSource(null, null, null, dayPlanField);

		MethodDescriptor sizeMethod = new MethodDescriptor(List.class, "size");
		AttributeMethodSource remainingActivitiesMethodSource = new AttributeMethodSource(dayPlanFieldSource,
		        "remaining_activities", int.class, sizeMethod);
		container.add(remainingActivitiesMethodSource);

		return container;
	}

	private AttributeSourceContainer initAttributeContainerForBusDriver() throws ReflectiveOperationException {
		AttributeSourceContainer container = createSourceContainerForBasicAgent();

		FieldDescriptor vehicleField = new FieldDescriptor(PublicTransportDriverAgent.class, "vehicleDrivenByDriver");
		AttributeFieldSource vehicleFieldSource = new AttributeFieldSource(null, null, null, vehicleField);

		container.addAll(createAttributeSourcesForVehicle(vehicleFieldSource));

		FieldDescriptor tripField = new FieldDescriptor(PublicTransportDriverAgent.class, "vehicleTrip");
		AttributeFieldSource tripFieldSource = new AttributeFieldSource(null, null, null, tripField);

		MethodDescriptor tripLengthMethod = new MethodDescriptor(Trip.class, "numOfCurrentTripItems");
		AttributeMethodSource tripLengthMethodSource = new AttributeMethodSource(tripFieldSource, "trip_length",
		        int.class, tripLengthMethod);
		container.add(tripLengthMethodSource);

		return container;
	}

	private AttributeSourceContainer initAttributeContainerForEdges() throws ReflectiveOperationException {
		AttributeSourceContainer container = new AttributeSourceContainer();

		FieldDescriptor idField = new FieldDescriptor(AgentPolisEntity.class, "id");
		AttributeFieldSource idFieldSource = new AttributeFieldSource(null, "agentid", String.class, idField);
		container.add(idFieldSource);
		container.addToSpatialFields(SpatialFieldTypeEnum.SINGLE_NODE, idFieldSource);

		FieldDescriptor typeField = new FieldDescriptor(Agent.class, "type");
		AttributeFieldSource typeFieldSource = new AttributeFieldSource(null, "type", String.class, typeField);
		container.add(typeFieldSource);

		MethodDescriptor timeMethod = new MethodDescriptor(TimeProvider.class, "getCurrentSimTime");
		AttributeMethodSource timeMethodSource = new AttributeMethodSource(timeProviderSource, null, null, timeMethod);
		container.setFromTime(timeMethodSource);

		return container;
	}

	private AttributeSourceContainer initAttributeContainerForNodes() throws ReflectiveOperationException {
		AttributeSourceContainer container = new AttributeSourceContainer();

		FieldDescriptor idField = new FieldDescriptor(AgentPolisEntity.class, "id");
		AttributeFieldSource idFieldSource = new AttributeFieldSource(null, "agentid", String.class, idField);
		container.add(idFieldSource);
		container.addToSpatialFields(SpatialFieldTypeEnum.SINGLE_NODE, idFieldSource);

		FieldDescriptor typeField = new FieldDescriptor(Agent.class, "type");
		AttributeFieldSource typeFieldSource = new AttributeFieldSource(null, "type", String.class, typeField);
		container.add(typeFieldSource);

		MethodDescriptor timeMethod = new MethodDescriptor(TimeProvider.class, "getCurrentSimTime");
		AttributeMethodSource timeMethodSource = new AttributeMethodSource(timeProviderSource, null, null, timeMethod);
		container.setFromTime(timeMethodSource);

		return container;
	}

}
