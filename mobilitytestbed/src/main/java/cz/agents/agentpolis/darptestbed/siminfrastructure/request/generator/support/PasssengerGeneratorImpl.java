package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.random.Well19937c;
import org.joda.time.Duration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.TimeWindow;

public class PasssengerGeneratorImpl implements PasssengerGenerator {

	private final static long HOUR23 = Duration.standardHours(23).getMillis();
	private final static long HOUR23_30MIN = Duration.standardMinutes(23 * 60 + 30).getMillis();
	private final static long HOUR24 = Duration.standardDays(1).getMillis();
	private final static long TIME_WINDOW_RANGE = Duration.standardMinutes(30).getMillis();
	private final static long CALL_TIME_BEFORE_REQUEST_FROM_TIME = Duration.standardMinutes(30).getMillis();
	private final static long REQUEST_DURATION_TIME = Duration.standardHours(2).getMillis();

	private final AbstractRealDistribution dayDistribution;
	private final Random random;

	public static AbstractRealDistribution createDayMockDistribution(final int seed) {
		final EmpiricalDistribution dayDistributionMock = new EmpiricalDistribution(new Well19937c(seed));

		int[] demandsOverDay = new int[] {/* 0 */9,/* 1 */6,/* 2 */6,
		/* 3 */6,/* 4 */7,/* 5 */12,/* 6 */25,/* 7 */37,/* 8 */49,
		/* 9 */38,/* 10 */30,/* 11 */30,/* 12 */30,/* 13 */30,/* 14 */31,
		/* 15 */36,/* 16 */45,/* 17 */41,/* 18 */31,/* 19 */27,/* 20 */25,/* 21 */22,/* 22 */18,/* 23 */11 };

		List<Double> dayHourDistribution = Lists.newArrayList();

		int day = 0;
		for (int demand : demandsOverDay) {
			double hourInMillis = Duration.standardHours(day).getMillis();
			for (int i = 0; i < demand; i++) {
				dayHourDistribution.add(hourInMillis);
			}
			day++;
		}

		dayDistributionMock.load(Doubles.toArray(dayHourDistribution));

		return dayDistributionMock;

	}

	public PasssengerGeneratorImpl(AbstractRealDistribution dayDistribution, Random random) {
		super();
		this.dayDistribution = dayDistribution;
		this.random = random;
	}

	@Override
	public Set<String> generateAdditionalRequirements() {
		return Sets.newHashSet(AdditionalRequirementsVehicleEquipment.WHEELCHAIR_SUPPORT.additionalRequirements);
	}

	@Override
	public RequestTimeInfo generateRequestTimeInfo() {

		long fromTimeMin = (long) dayDistribution.sample();
		long fromTimeMax = fromTimeMin + generateTime(TIME_WINDOW_RANGE);
		long toTimeMin = fromTimeMax + generateTime(REQUEST_DURATION_TIME) + Duration.standardHours(4).getMillis();

		long requestCallTimeInDayRange = fromTimeMin - generateTime(CALL_TIME_BEFORE_REQUEST_FROM_TIME);

		checkArgument(fromTimeMin >= 0 && fromTimeMin <= HOUR23,
				"The distribution generated value, which is outof range 0 - " + HOUR23);

		if (requestCallTimeInDayRange < 0) {
			requestCallTimeInDayRange = 0;
		}

		if (toTimeMin > HOUR23_30MIN) {
			toTimeMin = fromTimeMax;
		}

		long toTimeMax = toTimeMin + generateTime(TIME_WINDOW_RANGE);

		return new RequestTimeInfo(requestCallTimeInDayRange, new TimeWindow(fromTimeMin, fromTimeMax), new TimeWindow(
				toTimeMin, toTimeMax));
	}

	private long generateTime(long time) {
		return (long) (random.nextDouble() * time);
	}
}
