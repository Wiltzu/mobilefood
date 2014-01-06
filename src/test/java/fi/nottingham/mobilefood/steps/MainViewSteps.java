package fi.nottingham.mobilefood.steps;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;

import android.widget.TextView;
import cucumber.api.PendingException;
import fi.nottingham.mobilefood.MainActivity;
import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.util.TestGuiceModule;




public class MainViewSteps {
	MainActivity mainActivity;

	@BeforeStory
	public void beforeScenario() {
		MockitoAnnotations.initMocks(this);
	    TestGuiceModule module = new TestGuiceModule();
	    TestGuiceModule.setUp(this, module);
	}
	
	@Given("^the main view is open$")
	public void the_main_view_is_open() throws Throwable {
	   mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
	}

	@Then("^in the main view the date should be current$")
	public void in_the_main_view_the_date_should_be_current() throws Throwable {
	    TextView dateView = (TextView) mainActivity.findViewById(R.id.textview_date);
	    assertEquals("Date in the top of the page should have been current", new SimpleDateFormat("d.M.yyyy").format(new Date()), dateView.getText().toString());
	}

	@Then("^in the main view we should have foods$")
	public void in_the_main_view_we_should_have_foods() throws Throwable {
	    // Express the Regexp above with the code you wish you had
	    throw new PendingException();
	}

	@Given("^in the main view foods are visible$")
	public void in_the_main_view_foods_are_visible() throws Throwable {
	    // Express the Regexp above with the code you wish you had
	    throw new PendingException();
	}

	@Then("^in the main view restaurants should be ordered by name$")
	public void in_the_main_view_restaurants_should_be_ordered_by_name() throws Throwable {
	    // Express the Regexp above with the code you wish you had
	    throw new PendingException();
	}

	@When("^in the main view user changes date to tomorrow$")
	public void in_the_main_view_user_changes_date_to_tomorrow() throws Throwable {
	    // Express the Regexp above with the code you wish you had
	    throw new PendingException();
	}

	@Then("^in the main view date should be changed$")
	public void in_the_main_view_date_should_be_changed() throws Throwable {
	    // Express the Regexp above with the code you wish you had
	    throw new PendingException();
	}

	@Then("^the application menu should be visible$")
	public void the_application_menu_should_be_visible() throws Throwable {
	    // Express the Regexp above with the code you wish you had
	    throw new PendingException();
	}
}
