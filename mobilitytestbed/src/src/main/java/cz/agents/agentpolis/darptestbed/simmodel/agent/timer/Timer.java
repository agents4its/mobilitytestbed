package cz.agents.agentpolis.darptestbed.simmodel.agent.timer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.alite.common.event.Event;
import cz.agents.alite.common.event.EventHandler;
import cz.agents.alite.common.event.EventProcessor;
import cz.agents.alite.common.event.EventProcessorEventType;

/**
 * The timer has a list of callbacks (e.g. agents) and an interval. Every time
 * the interval elapses, the timer calls all the callbacks in its list. (It
 * doesn't work with real time, but simulation time instead).
 * 
 * @author Lukas Canda
 */
public class Timer {

	private static final Logger LOGGER = Logger.getLogger(Timer.class);

	private final String id;
	/**
	 * Every time the interval elapses, the timer starts its activity (calling
	 * callbacks).
	 */
	protected long interval;
	/**
	 * Objects, whose callback methods should be called regularly by this timer.
	 */
	protected List<TimerCallback> callbacks;
	/**
	 * The total number of timers running in the simulation.
	 */
	protected int numberOfTimers;
	/**
	 * Core processor for processing all simulation events.
	 */
	private final EventProcessor eventProcessor;
	/**
	 * An event for the event processor.
	 */
	private EventHandler eventHandler;
	/**
	 * A storage to save all data concerning taxi drivers and passengers
	 */
	protected final TestbedModel taxiModel;
	/**
	 * Utils is a class that contains useful methods to measure distances etc.
	 * Here, we call it to update its map of distances now and then.
	 */
	private final Utils utils;
	/**
	 * How many empty calls should the timer do before stopping the simulation.
	 * If this number is too low, then the simulation can stop in the middle,
	 * because nothing much is happening. If it is too big, the simulation can
	 * wait a lot of time after finishing.
	 */
	private final int NUMBER_OF_EMPTY_CALLS = 20;
	/**
	 * This helps us stop at the right time (after running out of events for x
	 * times in a row)
	 */
	private int stopIn = NUMBER_OF_EMPTY_CALLS;

	public Timer(String timerId, EventProcessor eventProcessor, TestbedModel taxiModel, Utils utils,
			int numberOfTimers, long interval) {

		if (interval <= 0) {
			throw new RuntimeException("It is not possible to set up interval less or equal zero");
		}
		callbacks = new ArrayList<TimerCallback>();

		this.id = timerId;
		this.eventProcessor = eventProcessor;
		this.taxiModel = taxiModel;
		this.utils = utils;
		this.interval = interval;
		this.numberOfTimers = numberOfTimers;
	}

	public Timer(String timerId, EventProcessor eventProcessor, TestbedModel taxiModel, Utils utils,
			int numberOfTimers, Duration interval) {

		this(timerId, eventProcessor, taxiModel, utils, numberOfTimers, interval.getMillis());
	}

	/**
	 * The main method of the timer.
	 */
	public void start() {

		eventHandler = new EventHandler() {

			/**
			 * This is the code to be repeated over and over again (using the
			 * interval).
			 */
			@Override
			public void handleEvent(Event event) {
				long startTime = System.currentTimeMillis();
				// the simulation is terminated when it doesn't contain any
				// events except timers
				if (eventProcessor.getCurrentTime() > interval
						&& eventProcessor.getCurrentQueueLength() <= numberOfTimers) {

					if (stopIn <= 0) {
						eventProcessor.addEvent(EventProcessorEventType.STOP, null, null, null);
					} else {
						stopIn--;
					}
				} else {
					stopIn = NUMBER_OF_EMPTY_CALLS;
				}
				eventProcessor.addEvent(eventHandler, interval);
				//LOGGER.debug("timer " + id + " callbacks");

				// // refresh map of distances to keep it updated
				// utils.refreshPassenAndDistMap();
				// callbacks
				for (TimerCallback call : callbacks) {
					call.timerCallback();
				}
				utils.logAlgRealTime(System.currentTimeMillis() - startTime);
			}

			@Override
			public EventProcessor getEventProcessor() {
				return null;
			}
		};

		eventProcessor.addEvent(eventHandler, interval);
	}

	public void addCallback(TimerCallback callback) {
		this.callbacks.add(callback);
	}

	public String getId() {
		return id;
	}

}
