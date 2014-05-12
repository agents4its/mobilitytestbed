package cz.agents.agentpolis.tools.geovisio.spy.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.apache.log4j.Logger;

import com.google.inject.Injector;

import cz.agents.agentpolis.siminfrastructure.planner.trip.Trip;
import cz.agents.agentpolis.siminfrastructure.planner.trip.Trips;
import cz.agents.agentpolis.simmodel.agent.Agent;
import cz.agents.agentpolis.simulator.creator.initializator.AgentInitFactory;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.agentstate.AgentState;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.agentstate.AgentState.AgentStateType;
import cz.agents.agentpolis.tools.geovisio.spy.agentpolis.agentstate.AgentStateStorage;
import eu.superhub.wp4.model.siminfrastructure.citizen.activityscheduler.dayplansheduler.Activity;
import eu.superhub.wp4.model.siminfrastructure.citizen.activityscheduler.dayplansheduler.LocationActivity;
import eu.superhub.wp4.model.siminfrastructure.citizen.activityscheduler.dayplansheduler.TravelActivity;
import eu.superhub.wp4.model.siminfrastructure.citizen.activityscheduler.dayplansheduler.WaitingActivity;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class AgentProxyFactory implements AgentInitFactory {

	private static final Logger logger = Logger.getLogger(AgentProxyFactory.class);

	private final  AgentInitFactory agentInit;

	private ProxyFactory factory = new ProxyFactory();
	
	private AgentStateStorage agentStateStorage;

	public AgentProxyFactory(AgentInitFactory agentInit) {
		this.agentInit = agentInit;
	}

	public List<Agent> initAllAgentLifeCycles(Injector injector) {
		agentStateStorage = injector.getInstance(AgentStateStorage.class);
		
		List<Agent> agents = agentInit.initAllAgentLifeCycles(injector);
		List<Agent> proxyAgents;
		try {
			proxyAgents = createProxyAgents(agents);
		} catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
				| InvocationTargetException e) {

			logger.error("Proxy agents not created. Returned empty list.", e);
			
			proxyAgents = Collections.emptyList();
		}

		return proxyAgents;
	}

	private List<Agent> createProxyAgents(List<Agent> agents) throws NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
		List<Agent> proxyAgents = new ArrayList<Agent>(agents.size());

		for (Agent agent : agents) {
			Agent proxyAgent = createProxyAgent(agent);
			proxyAgents.add(proxyAgent);
		}

		return proxyAgents;
	}

	private Agent createProxyAgent(final Agent agent) throws NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<? extends Agent> clazz = agent.getClass();

		Constructor<?> constructor = clazz.getConstructors()[0];
		Class<?>[] parameterTypes = constructor.getParameterTypes();
//		reflectionFactory = new ProxyFactory();
		factory.setSuperclass(clazz);
		factory.setFilter(new MethodFilter() {

			public boolean isHandled(Method m) {
				if(m.getName().equals("visitActivity")){
					return true;
				}
				return false;
			}
		});

		Object[] params = createParameters(agent, constructor);

		Agent proxyAgent = (Agent) factory.create(parameterTypes, params, createMethodHandler(agent));
		
		clone(agent,proxyAgent,clazz);
		
		return proxyAgent;
	}

	private <T> void clone(T source, T target, Class<? extends T> clazz) throws IllegalArgumentException, IllegalAccessException {
		for (Field field : clazz.getDeclaredFields()) {
//			field.
			if(Modifier.isStatic(field.getModifiers())) continue;
			field.setAccessible(true);
			Object fieldValue= field.get(source);
			field.set(target, fieldValue);
		}
	}

	private MethodHandler createMethodHandler(final Agent agent) {

		return new MethodHandler() {
			Agent sourceAgent = agent;

			@Override
			public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
//				logger.debug(thisMethod.getName());
//				logger.debug(Arrays.toString(args));
				
				Object object = args[0];
				Activity activity = (Activity) object;
				
				AgentStateType state = null;
				Timestamp duration = new Timestamp(activity.getDurationInMillisDayRange());
				Timestamp startTime =  new Timestamp(activity.getStarTimeInMillisDayRange());
				Timestamp endTime =  new Timestamp(activity.getEndTimeInMillisDayRange());
				String description = "";
				
				if(activity instanceof TravelActivity){
					Trips clone = ((TravelActivity) activity).getTrips().clone();
					while(clone.hasTrip()){
						Trip<?> trip = clone.getAndRemoveFirstTrip();
						description+=trip.getClass().getSimpleName()+", ";	
						
					}
					state = AgentStateType.TRAVEL;
				}else if(object instanceof WaitingActivity){
					state = AgentStateType.WAITING;
				}else if(object instanceof LocationActivity){
					state = AgentStateType.LOCATION;
				}
				
				
				Agent a = (Agent) self;
				agentStateStorage.setState(a.getId(), new AgentState(state, startTime, endTime, duration, description));
//				System.out.println(self);
//				System.out.println(Arrays.toString(args));
//				if (!thisMethod.getName().equals("getType") && !thisMethod.getName().equals("getId")) {
//					logger.debug(thisMethod.getName());
//				}

				return proceed.invoke(self, args);
			}
		};
	}
	

	private Object[] createParameters(Agent agent, Constructor<?> constructor) {
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		Object[] parameters = new Object[parameterTypes.length];
		 int i = 0;
		 parameters[i++] = agent.getId();
		 parameters[i++] = agent.getType();
		//
		return parameters;
	}

}
