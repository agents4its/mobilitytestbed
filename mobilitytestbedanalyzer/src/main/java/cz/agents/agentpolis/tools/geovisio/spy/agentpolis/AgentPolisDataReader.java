package cz.agents.agentpolis.tools.geovisio.spy.agentpolis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opengis.referencing.operation.TransformException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Injector;

import cz.agents.agentpolis.siminfrastructure.time.TimeProvider;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simmodel.entity.AgentPolisEntity;
import cz.agents.agentpolis.simmodel.entity.EntityType;
import cz.agents.agentpolis.simmodel.entity.vehicle.Vehicle;
import cz.agents.agentpolis.simmodel.environment.model.AgentPositionModel;
import cz.agents.agentpolis.simmodel.environment.model.AgentStorage;
import cz.agents.agentpolis.simmodel.environment.model.EntityPositionModel;
import cz.agents.agentpolis.simmodel.environment.model.EntityStorage;
import cz.agents.agentpolis.simmodel.environment.model.VehiclePositionModel;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.tools.geovisio.annotation.spatial.SpatialFieldTypeEnum;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeFieldSource;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeMethodSource;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeSource;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeSourceContainer;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeSourceFactory;
import cz.agents.agentpolis.tools.geovisio.attributesource.AttributeStaticSource;
import cz.agents.agentpolis.tools.geovisio.attributesource.reflectiondescriptor.FieldDescriptor;
import cz.agents.agentpolis.tools.geovisio.attributesource.reflectiondescriptor.MethodDescriptor;
import cz.agents.alite.common.event.EventProcessor;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public abstract class AgentPolisDataReader {

	protected Injector injector;
	protected String visName;
	protected final int interval;

	protected AttributeSource agentPositionEntrySetSource;
	protected AttributeSource vehiclePositionEntrySetSource;
	protected AttributeSource allNodesSource;
	protected AttributeSource allAgentsSource;
	protected AttributeSource timeProviderSource;

	protected AttributeMethodSource currentTimeSource;

	protected AttributeSourceFactory reflectionFactory = new AttributeSourceFactory();

	public AgentPolisDataReader(Injector injector, String visName, int interval) {
		super();
		this.injector = injector;
		this.visName = visName;
		this.interval = interval;
	}

	public abstract void onSimulationFinished() throws SQLException, TransformException;

	public abstract void initReadingAndRead() throws Exception;

	public abstract void readAndSaveData() throws IllegalArgumentException, SQLException, TransformException,
	        ReflectiveOperationException;

	protected void initHandeling() {
		EventProcessor eventProcessor = injector.getInstance(EventProcessor.class);
		SpyEventHandler handler = new SpyEventHandler(eventProcessor, interval, this);
		handler.startHandeling();
	}

	protected List<AttributeSource> createAttributeSourcesForVehicle(AttributeSource vehicleSource)
	        throws ReflectiveOperationException {
		List<AttributeSource> sources = new ArrayList<>(5);

		sources.add(reflectionFactory.createAttributeFieldSource(AgentPolisEntity.class, "id", "vehicle_id",
		        vehicleSource));
		sources.add(reflectionFactory.createAttributeFieldSource(Vehicle.class, "vehicleType", "vehicle_type",
		        vehicleSource, String.class));
		sources.add(reflectionFactory.createAttributeFieldSource(Vehicle.class, "usingGraphTypeForMoving",
		        "graph_type_for_moving", vehicleSource, String.class));
		sources.add(reflectionFactory.createAttributeFieldSource(Vehicle.class, "vehiclePassengerCapacity",
		        "vehicle_capacity", vehicleSource));
		sources.add(reflectionFactory.createAttributeFieldSource(Vehicle.class, "lengthInMeters", "vehicle_length",
		        vehicleSource));

		return sources;
	}

	/**
	 * Create {@code AttributeSourceContainer} for subclasses of {@code Agent}
	 * class with basic agent's attributes - id,type and current simulation
	 * time.
	 * 
	 * @return
	 * @throws ReflectiveOperationException
	 */
	protected AttributeSourceContainer createSourceContainerForBasicAgent() throws ReflectiveOperationException {
		AttributeSourceContainer container = new AttributeSourceContainer();

		AttributeFieldSource idFieldSource = createAgentIdSource();
		container.add(idFieldSource);
		container.addToSpatialFields(SpatialFieldTypeEnum.SINGLE_NODE, idFieldSource);

		FieldDescriptor typeField = new FieldDescriptor(Agent.class, "type");
		AttributeFieldSource typeFieldSource = new AttributeFieldSource(null, "type",  String.class,
		        typeField);
		container.add(typeFieldSource);

		MethodDescriptor timeMethod = new MethodDescriptor(TimeProvider.class, "getCurrentSimTime");
		currentTimeSource = new AttributeMethodSource(timeProviderSource, null, null, timeMethod);
		container.setFromTime(currentTimeSource);

		return container;
	}

	protected AttributeFieldSource createAgentIdSource() throws ReflectiveOperationException {
		FieldDescriptor idField = new FieldDescriptor(AgentPolisEntity.class, "id");
		return new AttributeFieldSource(null, "agentid", String.class, idField);
	}

	protected void initSources() throws ReflectiveOperationException, InterruptedException {

		MethodDescriptor getInstanceMethod = new MethodDescriptor(Injector.class, "getInstance", Class.class);
		FieldDescriptor entityPositionMapField = new FieldDescriptor(EntityPositionModel.class, "entityPositionMap");
		FieldDescriptor allNetworkNodesField = new FieldDescriptor(AllNetworkNodes.class, "allNetworkNodes");
		FieldDescriptor allAgentsField = new FieldDescriptor(EntityStorage.class, "entities");

		// agent-node map
		AttributeMethodSource agentPositionDataStorage = new AttributeMethodSource(null, null, null, getInstanceMethod,
		        AgentPositionModel.class);
		agentPositionEntrySetSource = new AttributeFieldSource(agentPositionDataStorage, null, null,
		        entityPositionMapField);

		// vehicle-node map
		AttributeMethodSource vehiclePositionDataStorage = new AttributeMethodSource(null, null, null,
		        getInstanceMethod, VehiclePositionModel.class);
		vehiclePositionEntrySetSource = new AttributeFieldSource(vehiclePositionDataStorage, null, null,
		        entityPositionMapField);

		// nodes storage
		AttributeMethodSource allNetworkNodesWrapper = new AttributeMethodSource(null, null, null, getInstanceMethod,
		        AllNetworkNodes.class);
		allNodesSource = new AttributeFieldSource(allNetworkNodesWrapper, null, null, allNetworkNodesField);

		// agentstorage
		AttributeMethodSource agentStorage = new AttributeMethodSource(null, null, null, getInstanceMethod,
		        AgentStorage.class);
		allAgentsSource = new AttributeFieldSource(agentStorage, null, null, allAgentsField);

		timeProviderSource = new AttributeMethodSource(null, null, null, getInstanceMethod, TimeProvider.class);
		timeProviderSource.setParent(new AttributeStaticSource(null, null, null, injector));
	}

	protected Multimap<EntityType, Agent> classifyAgents(Collection<Agent> agents) {

		Multimap<EntityType, Agent> filtered = ArrayListMultimap.create();
		for (Agent agent : agents) {
			filtered.put(agent.getType(), agent);
		}
		return filtered;
	}

	protected Map<String, Agent> getAllAgents() throws IllegalArgumentException, ReflectiveOperationException {
		return (Map<String, Agent>) allAgentsSource.getValue(injector);
	}

	protected Map<String, Long> getAgentsPositions() throws IllegalArgumentException, ReflectiveOperationException {
		return (Map<String, Long>) agentPositionEntrySetSource.getValue(injector);
	}

	protected Map<String, Long> getVehiclesPositions() throws IllegalArgumentException, ReflectiveOperationException {
		return (Map<String, Long>) vehiclePositionEntrySetSource.getValue(injector);
	}

	protected Map<Long, Node> getAllNodes() throws IllegalArgumentException, ReflectiveOperationException {
		return (Map<Long, Node>) allNodesSource.getValue(injector);
	}

}
