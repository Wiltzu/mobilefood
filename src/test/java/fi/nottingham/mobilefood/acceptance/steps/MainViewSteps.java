package fi.nottingham.mobilefood.acceptance.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;

import android.text.format.DateFormat;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;

import dagger.Module;
import dagger.Provides;
import fi.nottingham.mobilefood.MobilefoodModule;
import fi.nottingham.mobilefood.MobilefoodModules;
import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;
import fi.nottingham.mobilefood.view.impl.MainActivity;

public class MainViewSteps {
	MainActivity mainActivity;
	IMainView mainView;
	@Mock
	IFoodService foodService;
	IMainViewPresenter mainViewPresenter;

	@BeforeStory
	public void beforeScenario() {
		MockitoAnnotations.initMocks(this);
	}

	@Given("the main view is open")
	public void the_main_view_is_open() throws Throwable {
		mainActivity = Robolectric.buildActivity(MainActivity.class).create()
				.start().resume().get();
		mainView = mainActivity;
	}

	@Given("the main view is opened and the following foods are provided: $providedFoods")
	public void givenTheFollowingFoodsAreProvided(ExamplesTable foodsTable) throws NoInternetConnectionException, FoodServiceException {
		MockitoAnnotations.initMocks(this);
		MobilefoodModules.getModules().add(new TestModule());
		//get provided foods
		List<RestaurantDay> foodList = getExampleFoodsAsList(foodsTable);
		//add provided foods to service's mock
		Mockito.when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				foodList);
		//start activity
		mainActivity = Robolectric.buildActivity(MainActivity.class).create().start().resume().get();
	}

	private List<RestaurantDay> getExampleFoodsAsList(ExamplesTable foods) {
		List<RestaurantDay> foodList = Lists.newArrayList();
		
		Map<String, List<Food>> restaurantNameToLunches = Maps.newHashMap();
		for (Map<String, String> food : foods.getRows()) {
			String restaurantName = food.get("restaurant");
			String foodName = food.get("food");
			String diets = food.get("diets");
			List<String> prices = Lists.newArrayList(food.get("price"));
			
			if(!restaurantNameToLunches.containsKey(restaurantName)) {
				restaurantNameToLunches.put(restaurantName, new ArrayList<Food>());
			}
			restaurantNameToLunches.get(restaurantName).add(new Food(foodName, prices, diets));
		}
		for(String restaurantName :restaurantNameToLunches.keySet()) {			
			foodList.add(new RestaurantDay(restaurantName, restaurantNameToLunches.get(restaurantName)));
		}
		return foodList;
	}

	@Module(includes = { MobilefoodModule.class }, injects = MainViewSteps.class, overrides = true)
	class TestModule {
		@Provides
		@Singleton
		IFoodService provideFoodService(Config conf) {
			return foodService;
		}
		
		@Provides
		@Singleton
		INetworkStatusService provideNetworkStatus() {
			return mock(INetworkStatusService.class);
		}
	}

	@Then("in the main view the week day should be current")
	public void in_the_main_view_the_week_day_should_be_current() {
		TextView tvWeekDay = (TextView) mainActivity
				.findViewById(R.id.textview_week_day);

		assertEquals("Week day in the main view should be current.",
				getWeekDay(DateUtils.getDateAtMidnight(new Date())),
				(String) tvWeekDay.getText());
	}

	private String getWeekDay(Date date) {
		return DateFormat.format("EEEE", date).toString();
	}

	@Then("in the main view the date should be current")
	@org.robolectric.annotation.Config(qualifiers = "fi_FI")
	public void in_the_main_view_the_date_should_be_current() throws Throwable {
		TextView dateView = (TextView) mainActivity
				.findViewById(R.id.textview_date);
		assertEquals(
				"Date in the main view should have been current",
				DateUtils.getDateAtMidnight(new Date()),
				DateFormat.getDateFormat(mainActivity).parse(
						(String) dateView.getText()));
	}

	@Then("in the main view we should have the following foods: $foods")
	public void in_the_main_view_we_should_have_foods(ExamplesTable foodsTable)
			throws Throwable {
		
		List<RestaurantDay> expectedFoods = getExampleFoodsAsList(foodsTable);
		ListView mFoodsTV = (ListView) mainActivity
				.findViewById(R.id.listview_foods);
		
		//be sure that foods have been fetched and added to UI
		Robolectric.runBackgroundTasks();
		Robolectric.runUiThreadTasks();
		
		assertEquals("Foods didn't match.", expectedFoods.get(0).toString(),
				mFoodsTV.getAdapter().getItem(0).toString());
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
