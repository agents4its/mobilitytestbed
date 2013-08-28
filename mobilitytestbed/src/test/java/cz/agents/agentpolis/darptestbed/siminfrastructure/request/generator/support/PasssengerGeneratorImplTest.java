package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.joda.time.Duration;
import org.junit.Test;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PasssengerGenerator.RequestTimeInfo;

public class PasssengerGeneratorImplTest {

	@Test
	public void test() {
		AbstractRealDistribution createDayMockDistribution = PasssengerGeneratorImpl.createDayMockDistribution(0);

		for (int i = 0; i < 1000; i++) {
			long time = (long) createDayMockDistribution.sample();
			assertTrue(time >= 0 && time <= Duration.standardHours(23).getMillis());
		}

	}

	@Test
	public void test2() {
		PasssengerGeneratorImpl passsengerGeneratorImpl = new PasssengerGeneratorImpl(
				PasssengerGeneratorImpl.createDayMockDistribution(0), new Random(0));

		for (int i = 0; i < 100; i++) {
			RequestTimeInfo generateRequestTimeInfo = passsengerGeneratorImpl.generateRequestTimeInfo();
			assertTrue(generateRequestTimeInfo.callTimeInDayRange >= 0
					&& generateRequestTimeInfo.callTimeInDayRange < 86400000);
			assertTrue(generateRequestTimeInfo.fromTimeWindow.fromTimeInDayRange >= 0
					&& generateRequestTimeInfo.fromTimeWindow.fromTimeInDayRange < 86400000);
			assertTrue(generateRequestTimeInfo.fromTimeWindow.toTimeInDayRange >= 0
					&& generateRequestTimeInfo.fromTimeWindow.toTimeInDayRange < 86400000);
			assertTrue(generateRequestTimeInfo.toTimeWindow.fromTimeInDayRange >= 0
					&& generateRequestTimeInfo.toTimeWindow.fromTimeInDayRange < 86400000);
			assertTrue(generateRequestTimeInfo.toTimeWindow.toTimeInDayRange >= 0
					&& generateRequestTimeInfo.toTimeWindow.toTimeInDayRange < 86400000);

		}

	}
}
