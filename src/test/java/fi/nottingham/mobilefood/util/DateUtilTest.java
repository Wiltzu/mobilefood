package fi.nottingham.mobilefood.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class DateUtilTest {
	
	@Test
	public void getWeekOfYear_returnsCorrectWeekForFirstDayOfTheYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.JANUARY, 1);
		Date testDate = calendar.getTime();
		
		assertEquals("First day of the year 2014 should be in week 1.", 1, DateUtils.getWeekOfYear(testDate));		
	}
	
	@Test
	public void getDayOfTheWeek_returnsCorrectWeekDayForFirstDayOfTheYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.JANUARY, 1);
		Date testDate = calendar.getTime();
		
		final int WEDNESDAY = 2;
		assertEquals("First day of the year should be Wednesday.", WEDNESDAY, DateUtils.getDayOfTheWeek(testDate));		
	}
	
	@Test
	public void getWeekOfYear_returnsCorrectWeekMarch3rd() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.MARCH, 3);
		Date testDate = calendar.getTime();
		
		assertEquals("March 3rd 2014 should be in week 10.", 10, DateUtils.getWeekOfYear(testDate));		
	}
	
	@Test
	public void getDayOfTheWeek_returnsCorrectWeekDayForMarch3rd() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.MARCH, 3);
		Date testDate = calendar.getTime();
		
		final int MONDAY = 0;
		assertEquals("March 3rd 2014 should be Monday.", MONDAY, DateUtils.getDayOfTheWeek(testDate));		
	}
	
	@Test
	public void getDateAtMidnight_returnDateThatTimeIsMidnight() {
		int year = 2014, month = 11, day = 3;
		Date dateFromUtil = DateUtils.getDateAtMidnight(year, month, day);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFromUtil);
		
		assertEquals(cal.get(Calendar.YEAR), 2014);
		assertEquals(cal.get(Calendar.MONTH), Calendar.DECEMBER);
		assertEquals(cal.get(Calendar.DAY_OF_MONTH), 3);
		assertEquals(cal.get(Calendar.HOUR_OF_DAY), 0);
		assertEquals(cal.get(Calendar.MINUTE), 0);
		assertEquals(cal.get(Calendar.MILLISECOND), 0);
	}
}
