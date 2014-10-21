package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support;
//
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import java.util.Random;
//
//import org.joda.time.Duration;
//import org.junit.Test;
//
public class RandomRequestCallTimeGeneratorTest {
//
//	@Test
//	public void test() {
//		testGeneratedValue(0.0, Duration.standardMinutes(20).getMillis(), Duration.standardHours(1).getMillis());
//	}
//
//	@Test
//	public void test2() {
//		testGeneratedValue(1.0, Duration.standardMinutes(20).getMillis(), Duration.standardHours(1).getMillis());
//	}
//
//	@Test
//	public void test3() {
//		testGeneratedValue(0.0, Duration.standardMinutes(20).getMillis(), Duration.standardMinutes(15).getMillis());
//	}
//
//	@Test
//	public void test4() {
//		testGeneratedValue(1.0, Duration.standardMinutes(20).getMillis(), Duration.standardMinutes(15).getMillis());
//	}
//
//	@Test
//	public void test5() {
//		testGeneratedValue(0.0, Duration.standardMinutes(20).getMillis(), Duration.standardMinutes(20).getMillis());
//	}
//
//	@Test
//	public void test6() {
//		testGeneratedValue(1.0, Duration.standardMinutes(20).getMillis(), Duration.standardMinutes(20).getMillis());
//	}
//
//	@Test
//	public void test7() {
//		testGeneratedValue(0.0, Duration.standardMinutes(20).getMillis(), Duration.standardMinutes(0).getMillis());
//	}
//
//	@Test
//	public void test8() {
//		testGeneratedValue(1.0, Duration.standardMinutes(20).getMillis(), Duration.standardMinutes(0).getMillis());
//	}
//
//	private void testGeneratedValue(double returnRandomValue, long duration, long inputTime) {
//		Random random = mock(Random.class);
//		when(random.nextDouble()).thenReturn(returnRandomValue);
//		PassengerGeneratorImpl randomRequestCallTimeGenerator = new PassengerGeneratorImpl(duration,
//				random);
//
//		long requestCallTimeInDayRange = randomRequestCallTimeGenerator.generateRequestCallTimeInDayRange(inputTime);
//
//		assertTrue(requestCallTimeInDayRange <= inputTime);
//
//	}
//
}
