package cz.agents.agentpolis.darptestbed.global;

import java.util.Random;

import cz.agents.agentpolis.darptestbed.simmodel.agent.exception.WrongSettingsException;

public class GlobalParams {

	/**
	 * Setting up randomSeed > 0 will ensure, that there'll always be generated
	 * the same attempt
	 */
	private static long randomSeed = 0;
	private static Random random = null;
	/**
	 * True = results will be printed into a file, false = into the stand.
	 * output
	 */
	private static boolean useResultsFile;
	/**
	 * True if the request generator should include time windows in its requests
	 */
	private static boolean timeWindowsUsed;
	/**
	 * Centralized/Decentralized type of communication
	 */
	private static boolean centralized;
	/**
	 * 1 = (random) dummy parallel (pick up a bunch of passengers and then drive
	 * where they need), 2 = (random) dummy serial (take care of all passengers
	 * one by one), 3 = uniform groups (no diversion), 4 = uniform groups (with
	 * diversion)
	 */
	private static int centralAlgType = 1;
	/**
	 * 1 = send request to one closest driver (no diversion), 2 = send request
	 * to one closest driver (with diversion), 3 = propose to 5 closest, choose
	 * by price (with diversion)
	 */
	private static int decentrAlgType = 1;
	/**
	 * 1 = random request generator
	 */
	private static int requestGeneratorType = 1;
	/**
	 * The number of passengers to be generated in the city center
	 */
	private static int numberOfPassengers;

	// TODO: Remove it. It was replaced by vehicleCapacity in json
	/**
	 * The number of (5/6/7)-seat vehicles to be generated anywhere in the city
	 * (including the driver)
	 */
	private static int numberOfFiveSeatVehicles;
	private static int numberOfSixSeatVehicles;
	private static int numberOfSevenSeatVehicles;
	/**
	 * The radius of the city center, where passengers are born (recommended:
	 * 500 m)
	 */
	private static double cityCenterRadius;

	// more detailed settings follow...

	/**
	 * The velocity of all vehicles in kmph (default: 5 kmph)
	 */
	private static double velocityInKmph;
	/**
	 * The latest time when a passenger starts sending his request(s) (min. 0 =
	 * all passengers send requests at the beginning) (in simulation minutes)
	 */
	private static int maxPassengerStartLifeTime;
	/**
	 * The time after starting life, when the time window opens up (the time
	 * between start life time and possible departure) (min. 0 = all passengers
	 * can be picked up any time) (in simulation minutes)
	 */
	private static int earliestDepartureShift;
	/**
	 * min 1 = the driver has to transport the passenger onto his target
	 * directly with no diversion, just when his time window opens up, 2.5 = the
	 * time window is always 2.5 times bigger than it has to be
	 */
	private static double timeWinRelSize;
	/**
	 * Timers intervals settings (how often will timers call their callbacks)
	 */
	private static int timerDispatchingInterval;
	private static int timerDriverInterval;
	private static int timerPassengerInterval;
	/**
	 * true = the driver returns to his initial position after driving the plan,
	 * false = the driver stops at the last position of the plan
	 */
	private static boolean driverReturnsBack;
	/**
	 * Price for one kilometer with at least 1 passenger on board (if there's
	 * more passengers, they'll split this price)
	 */
	private static int pricePerKilometer;

	public static Random getRandom() {
		if (randomSeed <= 0) {
			return new Random();
		}
		if (random == null) {
			random = new Random(randomSeed);
		}
		return random;
	}

	public static long getRandomSeed() {
		return randomSeed;
	}

	/**
	 * Setting up randomSeed > 0 will ensure, that there'll always be generated
	 * the same attempt
	 * 
	 * @param randomSeed
	 *            random seed
	 */
	public static void setRandomSeed(long randomSeed) {
		GlobalParams.randomSeed = randomSeed;
	}

	public static boolean isCentralized() {
		return centralized;
	}

	/**
	 * @param centralized
	 *            Centralized/Decentralized type of communication
	 */
	public static void setCentralized(boolean centralized) {
		GlobalParams.centralized = centralized;
	}

	public static int getCentralAlgType() {
		return centralAlgType;
	}

	/**
	 * @param dispatchingAlg
	 *            1 = (random) dummy parallel (pick up a bunch of passengers and
	 *            then drive where they need), 2 = (random) dummy serial (take
	 *            care of all passengers one by one), 3 = equal groups without
	 *            diversion, 4 = equal groups with diversion
	 */
	public static void setCentralAlgType(int dispatchingAlg) {
		GlobalParams.validatePositiveNotZero(dispatchingAlg);
		GlobalParams.centralAlgType = dispatchingAlg;
	}

	public static int getRequestGeneratorType() {
		return requestGeneratorType;
	}

	/**
	 * @param requestGeneratorType
	 *            1 = random request generator
	 */
	public static void setRequestGeneratorType(int requestGeneratorType) {
		GlobalParams.validatePositiveNotZero(requestGeneratorType);
		GlobalParams.requestGeneratorType = requestGeneratorType;
	}

	public static boolean isTimeWindowsUsed() {
		return timeWindowsUsed;
	}

	/**
	 * @param timeWindowsUsed
	 *            true = time windows are generated and respected, false = no
	 *            time windows are used in the whole simulation
	 */
	public static void setTimeWindowsUsed(boolean timeWindowsUsed) {
		GlobalParams.timeWindowsUsed = timeWindowsUsed;
	}

	public static double getVelocityInKmph() {
		return velocityInKmph;
	}

	/**
	 * @param velocityInKmph
	 *            velocity of all vehicles in kmph (default: 5 kmph)
	 */
	public static void setVelocityInKmph(double velocityInKmph) {
		GlobalParams.validatePositiveNotZero(velocityInKmph);
		GlobalParams.velocityInKmph = velocityInKmph;
	}

	public static int getMaxPassengerStartLifeTime() {
		return maxPassengerStartLifeTime;
	}

	/**
	 * @param maxPassengerStartLifeTime
	 *            The latest time when a passenger starts sending his request(s)
	 *            (min. 0 = all passengers send requests at the beginning) (in
	 *            simulation minutes)
	 */
	public static void setMaxPassengerStartLifeTime(int maxPassengerStartLifeTime) {
		GlobalParams.validatePositiveOrZero(maxPassengerStartLifeTime);
		GlobalParams.maxPassengerStartLifeTime = maxPassengerStartLifeTime;
	}

	public static int getEarliestDepartureShift() {
		return earliestDepartureShift;
	}

	/**
	 * The time after starting life, when the time window opens up (the time
	 * between start life time and possible departure) (min. 0 = all passengers
	 * can be picked up any time) (in simulation minutes)
	 * 
	 * @param earliestDepartureShift
	 *            earliest departure shift (in minutes)
	 */
	public static void setEarliestDepartureShift(int earliestDepartureShift) {
		GlobalParams.validatePositiveOrZero(earliestDepartureShift);
		GlobalParams.earliestDepartureShift = earliestDepartureShift;
	}

	public static double getTimeWinRelSize() {
		return timeWinRelSize;
	}

	/**
	 * @param timeWinRelSize
	 *            min 1 = the driver has to transport the passenger to his
	 *            target directly with no diversion, just when his time window
	 *            opens up, 2.5 = the time window is always 2.5 times bigger
	 *            than it has to be
	 */
	public static void setTimeWinRelSize(double timeWinRelSize) {
		if (timeWinRelSize < 1) {
			throw new WrongSettingsException("Time win rel size has to be set >= 1");
		}
		GlobalParams.timeWinRelSize = timeWinRelSize;
	}

	public static int getNumberOfPassengers() {
		return numberOfPassengers;
	}

	/**
	 * @param numberOfPassengers
	 *            the number of passengers to be generated in the city center
	 */
	public static void setNumberOfPassengers(int numberOfPassengers) {
		GlobalParams.validatePositiveNotZero(numberOfPassengers);
		GlobalParams.numberOfPassengers = numberOfPassengers;
	}

	public static int getNumberOfFiveSeatVehicles() {
		return numberOfFiveSeatVehicles;
	}

	/**
	 * @param numberOfFiveSeatVehicles
	 *            the number of five-seat vehicles to be generated anywhere in
	 *            the city (including the driver)
	 */
	public static void setNumberOfFiveSeatVehicles(int numberOfFiveSeatVehicles) {
		GlobalParams.validatePositiveOrZero(numberOfFiveSeatVehicles);
		GlobalParams.numberOfFiveSeatVehicles = numberOfFiveSeatVehicles;
	}

	public static int getNumberOfSixSeatVehicles() {
		return numberOfSixSeatVehicles;
	}

	/**
	 * @param numberOfSixSeatVehicles
	 *            the number of six-seat vehicles to be generated anywhere in
	 *            the city (including the driver)
	 */
	public static void setNumberOfSixSeatVehicles(int numberOfSixSeatVehicles) {
		GlobalParams.validatePositiveOrZero(numberOfSixSeatVehicles);
		GlobalParams.numberOfSixSeatVehicles = numberOfSixSeatVehicles;
	}

	public static int getNumberOfSevenSeatVehicles() {
		return numberOfSevenSeatVehicles;
	}

	/**
	 * @param numberOfSevenSeatVehicles
	 *            the number of seven-seat vehicles to be generated anywhere in
	 *            the city (including the driver)
	 */
	public static void setNumberOfSevenSeatVehicles(int numberOfSevenSeatVehicles) {
		GlobalParams.validatePositiveOrZero(numberOfSevenSeatVehicles);
		GlobalParams.numberOfSevenSeatVehicles = numberOfSevenSeatVehicles;
	}

	public static int getTimerDispatchingInterval() {
		return timerDispatchingInterval;
	}

	/**
	 * Timers intervals settings (how often will timers call their callbacks)
	 * 
	 * @param timerDispatchingInterval
	 *            timer interval in simulation minutes
	 */
	public static void setTimerDispatchingInterval(int timerDispatchingInterval) {
		GlobalParams.validatePositiveNotZero(timerDispatchingInterval);
		GlobalParams.timerDispatchingInterval = timerDispatchingInterval;
	}

	public static int getTimerDriverInterval() {
		return timerDriverInterval;
	}

	/**
	 * Timers intervals settings (how often will timers call their callbacks)
	 * 
	 * @param timerDriverInterval
	 *            timer interval in simulation minutes
	 */
	public static void setTimerDriverInterval(int timerDriverInterval) {
		GlobalParams.validatePositiveNotZero(timerDriverInterval);
		GlobalParams.timerDriverInterval = timerDriverInterval;
	}

	public static int getTimerPassengerInterval() {
		return timerPassengerInterval;
	}

	/**
	 * Timers intervals settings (how often will timers call their callbacks)
	 * 
	 * @param timerPassengerInterval
	 *            timer interval in simulation minutes
	 */
	public static void setTimerPassengerInterval(int timerPassengerInterval) {
		GlobalParams.validatePositiveNotZero(timerPassengerInterval);
		GlobalParams.timerPassengerInterval = timerPassengerInterval;
	}

	public static boolean isDriverReturnsBack() {
		return driverReturnsBack;
	}

	/**
	 * @param driverReturnsBack
	 *            true = the driver returns to his initial position after
	 *            driving the plan, false = the driver stops at the last
	 *            position of the plan
	 */
	public static void setDriverReturnsBack(boolean driverReturnsBack) {
		GlobalParams.driverReturnsBack = driverReturnsBack;
	}

	public static int getDecentrAlgType() {
		return decentrAlgType;
	}

	/**
	 * 1 = send request to one closest driver (no diversion), 2 = send request
	 * to one closest driver (with diversion), 3 = propose to 5 closest, choose
	 * by price (with diversion)
	 * 
	 * @param decentrAlgType
	 */
	public static void setDecentrAlgType(int decentrAlgType) {
		GlobalParams.validatePositiveNotZero(decentrAlgType);
		GlobalParams.decentrAlgType = decentrAlgType;
	}

	public static int getPricePerKilometer() {
		return pricePerKilometer;
	}

	/**
	 * Price paid for one kilometer with at least 1 passenger on board (if
	 * there's more passengers, they'll split this price)
	 * 
	 * @param pricePerKilometer
	 */
	public static void setPricePerKilometer(int pricePerKilometer) {
		GlobalParams.validatePositiveOrZero(pricePerKilometer);
		GlobalParams.pricePerKilometer = pricePerKilometer;
	}

	public static double getCityCenterRadius() {
		return cityCenterRadius;
	}

	/**
	 * The radius of the city center, where passengers are born (recommended:
	 * 500 m)
	 * 
	 * @param cityCenterRadius
	 *            the radius in meters
	 */
	public static void setCityCenterRadius(double cityCenterRadius) {
		GlobalParams.validatePositiveNotZero(cityCenterRadius);
		GlobalParams.cityCenterRadius = cityCenterRadius;
	}

	private static void validatePositiveNotZero(int value) {
		GlobalParams.validatePositiveOrZero(value);
		if (value == 0) {
			throw new WrongSettingsException("The user set a zero value");
		}
	}

	private static void validatePositiveOrZero(int value) {
		if (value < 0) {
			throw new WrongSettingsException("The user set a negative value (" + value + ")");
		}
	}

	private static void validatePositiveNotZero(double value) {
		GlobalParams.validatePositiveOrZero(value);
		if (value == 0) {
			throw new WrongSettingsException("The user set a zero value");
		}
	}

	private static void validatePositiveOrZero(double value) {
		if (value < 0) {
			throw new WrongSettingsException("The user set a negative value (" + value + ")");
		}
	}

	public static boolean isUseResultsFile() {
		return useResultsFile;
	}

	/**
	 * True = results will be printed into a file, false = into the stand.
	 * output
	 * 
	 * @param useResultsFile
	 *            true, if you want to use the results file
	 */
	public static void setUseResultsFile(boolean useResultsFile) {
		GlobalParams.useResultsFile = useResultsFile;
	}

}
