package cz.agents.dbtokmlexporter.utils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 
 * @author Marek Cuchy
 * 
 */
public class TimeUtils {

	public static final String DURATION_PATTERN = "HH:mm:ss.SSS";
	public static final String DURATION_WITH_TIME_PATTERN = "HH:mm:ss.SSS";
	public static final String ACTUAL_TIME_PATTERN = "'Day'D-HH:mm:ss.SSS";

	private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat(DURATION_PATTERN);

	static {
		FORMAT.setTimeZone(UTC_TIMEZONE);
	}

	public static String formatDurationMillisToString(long millis) {
		return formatMillisToString(millis, DURATION_PATTERN);
	}

	public static String formatActualTimeMillisToString(long millis) {
		return formatMillisToString(millis, ACTUAL_TIME_PATTERN);
	}

	public static String formatMillisToString(long millis, String pattern) {
		Date date = new Date(millis);
		FORMAT.applyPattern(pattern);
		return FORMAT.format(date);
	}

	public static String formatDurationWithDaysMillisToString(long millis) {
		String format = formatMillisToString(millis, DURATION_WITH_TIME_PATTERN);
		long days = millis / (24 * 3600 * 1000);
		String daysString;
		if (days == 1) {
			daysString = "1 Day";
		} else {
			daysString = days + " Days";
		}
		return daysString + "-" + format;
	}

}
