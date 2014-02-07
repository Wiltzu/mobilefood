package fi.nottingham.mobilefood.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.text.format.DateFormat;
import android.widget.TextView;
import fi.nottingham.mobilefood.MainActivity;
import fi.nottingham.mobilefood.R;

public class MainViewSteps {
	MainActivity mainActivity;

	@BeforeStory
	public void beforeScenario() {
		MockitoAnnotations.initMocks(this);
	}

	@Given("the main view is open")
	public void the_main_view_is_open() throws Throwable {
		System.out.println("Creating main view...");
		mainActivity = Robolectric.buildActivity(MainActivity.class).create()
				.start().resume().get();
	}
	
	@Given("the following foods are provided")
	@Pending
	public void givenTheFollowingFoodsAreProvided() {
	  // PENDING
	}
	
	@Then("in the main view the week day should be current")
	public void in_the_main_view_the_week_day_should_be_current() {
		TextView tvWeekDay = (TextView) mainActivity
				.findViewById(R.id.textview_week_day);
		
		assertEquals("Week day in the main view should be current.", getWeekDay(getCurrentDateAtMidnight()), (String) tvWeekDay.getText());
	}

	private String getWeekDay(Date date) {
		return DateFormat.format("EEEE", date).toString();
	}

	@Then("in the main view the date should be current")
	@Config(qualifiers = "fi_FI")
	public void in_the_main_view_the_date_should_be_current()
			throws Throwable {
		TextView dateView = (TextView) mainActivity
				.findViewById(R.id.textview_date);
		assertEquals("Date in the main view should have been current",
				getCurrentDateAtMidnight(), DateFormat.getDateFormat(mainActivity).parse((String) dateView.getText()));
	}

	private Date getCurrentDateAtMidnight() {
		// TODO: change to JODA time    
		Calendar date = new GregorianCalendar();
		// reset hour, minutes, seconds and millis
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}

	@Then("in the main view we should have foods")
	public void in_the_main_view_we_should_have_foods() throws Throwable {
		fail("Implement me!");
	}

	@Given("in the main view foods are visible")
	public void in_the_main_view_foods_are_visible() throws Throwable {
		fail("Implement me!");
	}

	@Then("in the main view restaurants should be ordered by name")
	public void in_the_main_view_restaurants_should_be_ordered_by_name()
			throws Throwable {
		fail("Implement me!");
	}

	@When("in the main view user changes date to tomorrow")
	public void in_the_main_view_user_changes_date_to_tomorrow()
			throws Throwable {
		fail("Implement me!");
	}

	@Then("in the main view date should be changed")
	public void in_the_main_view_date_should_be_changed() throws Throwable {
		fail("Implement me!");
	}

	@Then("the application menu should be visible")
	public void the_application_menu_should_be_visible() throws Throwable {
		fail("Implement me!");
	}
}
