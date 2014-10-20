package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.TimeWindow;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.random.Well19937c;
import org.apache.log4j.Logger;
import org.joda.time.Duration;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public class PassengerGeneratorWithContinousRequestCallTimeDistribution implements PassengerGenerator {
    private static final Logger LOGGER = Logger.getLogger(PassengerGeneratorWithContinousRequestCallTimeDistribution.class);

    private final static long HOUR23 = Duration.standardHours(23).getMillis();
    private final static long HOUR23_30MIN = Duration.standardMinutes(23 * 60 + 30).getMillis();
    private final static long HOUR24 = Duration.standardDays(1).getMillis();
    private final static long REQUEST_DURATION_TIME = Duration.standardMinutes(30).getMillis();
    private final static long MIN_TIME_WINDOW_RANGE = Duration.standardMinutes(15).getMillis();
    private final static long MIN_CALL_TIME_BEFORE_REQUEST_FROM_TIME = Duration.standardMinutes(15).getMillis();
    private final static long MIN_REQUEST_DURATION_TIME = Duration.standardMinutes(15).getMillis();

    private static long TIME_WINDOW_RANGE = Duration.standardMinutes(30).getMillis();
    private static long CALL_TIME_BEFORE_REQUEST_FROM_TIME = Duration.standardMinutes(30).getMillis();

    private final AbstractRealDistribution dayDistribution;
    private final Random random;
    private final Utils utils;

    public static AbstractRealDistribution createDayMockDistribution(final int seed) {
        int intermediateStepsCount = 60;
        final EmpiricalDistribution dayDistributionMock = new EmpiricalDistribution(24 * intermediateStepsCount, new Well19937c(seed));

        int[] demandsOverDay = new int[]{/* 0 */9,/* 1 */6,/* 2 */6,
        /* 3 */6,/* 4 */7,/* 5 */12,/* 6 */25,/* 7 */37,/* 8 */49,
        /* 9 */38,/* 10 */30,/* 11 */30,/* 12 */30,/* 13 */30,/* 14 */31,
        /* 15 */36,/* 16 */45,/* 17 */41,/* 18 */31,/* 19 */27,/* 20 */25,/* 21 */22,/* 22 */18,/* 23 */11};


        List<Double> dayHourDistribution = Lists.newArrayList();

        int hour = 0;
        for (int i = 0; i < demandsOverDay.length - 1; ++i) {

            double startHourInMillis = Duration.standardHours(hour).getMillis();
            double endHourInMillis = Duration.standardHours(hour + 1).getMillis();

            double demandsStep = ((double) (demandsOverDay[hour] -
                    demandsOverDay[(hour + 1) % 24])) / intermediateStepsCount;
            double timeStep = (endHourInMillis - startHourInMillis) / intermediateStepsCount;

            for (int j = 0; j < intermediateStepsCount; ++j) {
                int demands = (int) (demandsOverDay[i] + demandsStep * j);
                for (int k = 0; k < demands; ++k)
                    dayHourDistribution.add(startHourInMillis + j * timeStep);
            }

            hour++;
        }

        dayDistributionMock.load(Doubles.toArray(dayHourDistribution));

        return dayDistributionMock;

    }
    
    public PassengerGeneratorWithContinousRequestCallTimeDistribution(AbstractRealDistribution dayDistribution, Random random, Utils utils) {
        super();
        this.dayDistribution = dayDistribution;
        this.random = random;
        this.utils = utils;
    }

    public PassengerGeneratorWithContinousRequestCallTimeDistribution(AbstractRealDistribution dayDistribution, 
    		Random random, Utils utils, int timeWindowRangeInMinutes, int callTimeBeforeRequestFromTimeInMinutes) {
    	this(dayDistribution, random, utils);
    	
    	// in addition to calling the constructor, adjust the time-related vars
    	TIME_WINDOW_RANGE = Duration.standardMinutes(timeWindowRangeInMinutes).getMillis();
    	CALL_TIME_BEFORE_REQUEST_FROM_TIME = Duration.standardMinutes(callTimeBeforeRequestFromTimeInMinutes).getMillis();
    }
    
    @Override
    public Set<String> generateAdditionalRequirements() {
        return Sets.newHashSet(AdditionalRequirementsVehicleEquipment.WHEELCHAIR_SUPPORT.additionalRequirements);
    }

    @Override
    public RequestTimeInfo generateRequestTimeInfo() {
        long fromTimeMin = (long) dayDistribution.sample();
        long fromTimeMax = fromTimeMin + generateTime(TIME_WINDOW_RANGE, MIN_TIME_WINDOW_RANGE);
        long toTimeMin = fromTimeMax + generateTime(REQUEST_DURATION_TIME, MIN_REQUEST_DURATION_TIME);

        long requestCallTimeInDayRange = fromTimeMin - generateTime(CALL_TIME_BEFORE_REQUEST_FROM_TIME,
                MIN_CALL_TIME_BEFORE_REQUEST_FROM_TIME);

        checkArgument(fromTimeMin >= 0 && fromTimeMin <= HOUR23,
                "The distribution generated value, which is out of range 0 - " + HOUR23);

        if (requestCallTimeInDayRange < 0) {
            requestCallTimeInDayRange = 0;
        }

        if (toTimeMin > HOUR23_30MIN) {
            toTimeMin = fromTimeMax;
        }

        long toTimeMax = toTimeMin + generateTime(TIME_WINDOW_RANGE, MIN_TIME_WINDOW_RANGE);

        System.exit(1);

        return new RequestTimeInfo(requestCallTimeInDayRange, new TimeWindow(fromTimeMin, fromTimeMax), new TimeWindow(
                toTimeMin, toTimeMax));
    }

    @Override
    public RequestTimeInfo generateRequestTimeInfo(GPS fromGPS, GPS toGPS) {
        long fromTimeMin = (long) dayDistribution.sample();
        long fromTimeMax = fromTimeMin + generateTime(TIME_WINDOW_RANGE, MIN_TIME_WINDOW_RANGE);
        long toTimeMin = fromTimeMax + generateTime(REQUEST_DURATION_TIME, MIN_REQUEST_DURATION_TIME);

        long requestCallTimeInDayRange = fromTimeMin - generateTime(CALL_TIME_BEFORE_REQUEST_FROM_TIME,
                MIN_CALL_TIME_BEFORE_REQUEST_FROM_TIME);

        checkArgument(fromTimeMin >= 0 && fromTimeMin <= HOUR23,
                "The distribution generated value, which is out of range 0 - " + HOUR23);

        if (requestCallTimeInDayRange < 0) {
            requestCallTimeInDayRange = 0;
        }

        if (toTimeMin > HOUR23_30MIN) {
            toTimeMin = fromTimeMax;
        }

        long toTimeMax = toTimeMin + generateTime(TIME_WINDOW_RANGE, MIN_TIME_WINDOW_RANGE);

        return new RequestTimeInfo(requestCallTimeInDayRange, new TimeWindow(fromTimeMin, fromTimeMax), new TimeWindow(
                toTimeMin, toTimeMax));
    }

    @Override
    public RequestTimeInfo generateRequestTimeInfo(long fromNode, long toNode) {
        long fromTimeMin = (long) dayDistribution.sample();
        LOGGER.debug("Sample: " + fromTimeMin);
        long fromTimeMinToMax = generateTime(TIME_WINDOW_RANGE, MIN_TIME_WINDOW_RANGE);
        long fromTimeMax = fromTimeMin + fromTimeMinToMax;
        long requestDurationTime = generateTime(REQUEST_DURATION_TIME, MIN_REQUEST_DURATION_TIME);
        Long drivingTime = utils.computeDrivingTime(fromNode, toNode, 30);
        long toTimeMin = fromTimeMax + requestDurationTime + drivingTime;

        long callTimeBeforeRequestTime = generateTime(CALL_TIME_BEFORE_REQUEST_FROM_TIME,
                MIN_CALL_TIME_BEFORE_REQUEST_FROM_TIME);
        long requestCallTimeInDayRange = fromTimeMin - callTimeBeforeRequestTime;

        if (fromTimeMin == 0)
            ++fromTimeMin;

        checkArgument(fromTimeMin >= 0 && fromTimeMin <= HOUR23,
                "The distribution generated value, which is out of range 0 - " + HOUR23);

        if (requestCallTimeInDayRange < 0) {
            requestCallTimeInDayRange = 0;
        }

        if (toTimeMin > HOUR23_30MIN) {
            toTimeMin = fromTimeMax;
        }
        long toTimeMinToMax = generateTime(TIME_WINDOW_RANGE, MIN_TIME_WINDOW_RANGE);
        long toTimeMax = toTimeMin + toTimeMinToMax;

        long interval = toTimeMax - fromTimeMin;

        long drivingTimeTest = utils.computeDrivingTime(fromNode, toNode, 30);
        long slack = interval - drivingTimeTest;
        if (slack < 0) {
            LOGGER.debug(slack + " - " + drivingTimeTest + " - impossible request (slack="+slack+"; toTimeMax="+toTimeMax+")");
            return generateRequestTimeInfo(fromNode, toNode);
        }

        RequestTimeInfo requestTimeInfo = new RequestTimeInfo(requestCallTimeInDayRange,
                new TimeWindow(fromTimeMin, fromTimeMax), new TimeWindow(toTimeMin, toTimeMax));

        LOGGER.debug(String.format("Request - Slack %d %d %d %f: %s", interval, drivingTimeTest, drivingTime,
                utils.computeDistance(fromNode, toNode), requestTimeInfo));
        return requestTimeInfo;
    }

    private long generateTime(long time, long minTime) {
        return minTime + (long) (random.nextDouble() * (time - minTime));
    }
}
