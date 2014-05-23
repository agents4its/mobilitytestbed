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
    // TODO: Remove it. It was replaced by vehicleCapacity in json

    // more detailed settings follow...
    /**
     * The velocity of all vehicles in kmph (default: 5 kmph)
     */
    private static double velocityInKmph;
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
