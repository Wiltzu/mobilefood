package fi.nottingham.mobilefood.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.text.format.DateFormat;

public class DateUtils {

	public static Date getDateAtMidnight(Date date) {
		checkNotNull(date, "date cannot be null");
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		// reset hour, minutes, seconds and millis
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static String getWeekDay(Date date) {
		//TODO: use other than android solution?
		checkNotNull(date, "date cannot be null");
		return DateFormat.format("EEEE", date).toString();
	}
	
	/**
	 * 
	 * @param context
	 * @param date
	 * @return like 6.7.2014 or 12/2/2014 depending on the devices localization settings.
	 */
	public static String getDateInShortFormat(Context context, Date date) {
		//TODO: use other than android solution?
		checkNotNull(date, "date cannot be null");
		return DateFormat.getDateFormat(context).format(date);
	}

	private DateUtils() {
	}

	/**
	 * @param date
	 * @return day of the week where 0 is Monday and 6 is Sunday
	 * <pre>
	 * Monday Tuesday Wednesday Thursday Friday Saturday Sunday
	 * 0      1       2         3        4      5        6 
	 * </pre>
	 * 
	 */
	public static int getDayOfTheWeek(Date date) {
		checkNotNull(date, "date cannot be null");
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		int dayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if(dayOfTheWeek == Calendar.SUNDAY) {
			return 6;
		}
		//calendar's Monday is 2
		return dayOfTheWeek - 2;
	}

	/**
	 * @param date
	 * @return week number starting from 1
	 */
	public static int getWeekOfYear(Date date) {
		checkNotNull(date, "date cannot be null");
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.WEEK_OF_YEAR);
	}
}
