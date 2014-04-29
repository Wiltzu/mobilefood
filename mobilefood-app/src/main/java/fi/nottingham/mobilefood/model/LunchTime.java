package fi.nottingham.mobilefood.model;

import com.google.common.base.Objects;

public class LunchTime {

	private final String weekDays;
	private final String hours;

	public LunchTime(String weekDays, String hours) {
		this.weekDays = weekDays;
		this.hours = hours;
	}

	public String getHours() {
		return hours;
	}

	public String getWeekDays() {
		return weekDays;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("weekDays", getWeekDays())
				.add("hours", getHours()).toString();
	}
}
