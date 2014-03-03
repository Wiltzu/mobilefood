package fi.nottingham.mobilefood.util;

import static org.junit.Assert.assertEquals;

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
}
