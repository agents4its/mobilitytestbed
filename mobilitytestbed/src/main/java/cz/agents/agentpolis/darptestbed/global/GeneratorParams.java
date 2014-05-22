package cz.agents.agentpolis.darptestbed.global;

import java.util.Random;

import cz.agents.agentpolis.darptestbed.simmodel.agent.exception.WrongSettingsException;

public class GeneratorParams {

    /**
     * Setting up randomSeed > 0 will ensure, that there'll always be generated
     * the same attempt
     */
    private static long randomSeed = 0;
    private static Random random = null;
    /**
     * The number of passengers to be generated in the city center
     */
    private static int numberOfPassengers;

    // TODO: Remove it. It was replaced by vehicleCapacity in json

    // more detailed settings follow...

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

    public static Random getRandom() {
        if (randomSeed <= 0) {
            return new Random();
        }
        if (random == null) {
            random = new Random(randomSeed);
        }
        return random;
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
        GeneratorParams.validatePositiveOrZero(maxPassengerStartLifeTime);
        GeneratorParams.maxPassengerStartLifeTime = maxPassengerStartLifeTime;
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
        GeneratorParams.validatePositiveOrZero(earliestDepartureShift);
        GeneratorParams.earliestDepartureShift = earliestDepartureShift;
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
        GeneratorParams.timeWinRelSize = timeWinRelSize;
    }

    public static int getNumberOfPassengers() {
        return numberOfPassengers;
    }

    /**
     * @param numberOfPassengers
     *            the number of passengers to be generated in the city center
     */
    public static void setNumberOfPassengers(int numberOfPassengers) {
        GeneratorParams.validatePositiveNotZero(numberOfPassengers);
        GeneratorParams.numberOfPassengers = numberOfPassengers;
    }

    private static void validatePositiveNotZero(int value) {
        GeneratorParams.validatePositiveOrZero(value);
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
        GeneratorParams.validatePositiveOrZero(value);
        if (value == 0) {
            throw new WrongSettingsException("The user set a zero value");
        }
    }

    private static void validatePositiveOrZero(double value) {
        if (value < 0) {
            throw new WrongSettingsException("The user set a negative value (" + value + ")");
        }
    }

}
