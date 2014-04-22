package fi.nottingham.mobilefood.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.Restaurant;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.service.impl.FoodParser.FoodParserException;

public class FoodParserTest {

	FoodParser parser;
	int weekNumber = 17;
	int dayOfTheWeek = 0;
	String parserVersion = "0.9";

	@Before
	public void setUp() {
		parser = new FoodParser(parserVersion);
	}

	@Test(expected = FoodParserException.class)
	public void parse_ifStatusIsError_throwsException() throws IOException,
			URISyntaxException, FoodParserException {
		parser.parseFoods("{\"status\": \"error\"}", dayOfTheWeek);
	}

	@Test(expected = FoodParserException.class)
	public void parse_ifStatusIsOKButVersionIsOtherThanExcepted_throwsException()
			throws IOException, URISyntaxException, FoodParserException {
		String parserVersion = "0.9";
		parser = new FoodParser(parserVersion);
		parser.parseFoods("{\"status\": \"OK\", \"version\": \"1.0\"}",
				dayOfTheWeek);
	}

	@Test
	public void parse_ifStatusIsOKAndVersionIsCorrect_thenNoExceptionsAndReturnsEmptyList()
			throws IOException, URISyntaxException, FoodParserException {
		String parserVersion = "0.9";
		parser = new FoodParser(parserVersion);
		assertTrue(parser.parseFoods(
				"{\"status\": \"OK\", \"version\": \"" + parserVersion
						+ "\", \"foodsByDay\": []}", dayOfTheWeek).isEmpty());
	}

	@Test
	public void parse_returnsCorrectFoodData() throws IOException,
			URISyntaxException, FoodParserException {
		int dayOfTheWeek = 0;
		List<Food> foods = Lists.newArrayList(new Food("kala",
				new ArrayList<String>(), null));
		RestaurantDay expectedRestaurantDay = new RestaurantDay("Tottis",
				foods, null);
		List<RestaurantDay> restaurantDays = Lists
				.newArrayList(expectedRestaurantDay);

		parser = new FoodParser(parserVersion);
		List<RestaurantDay> foodList = parser.parseFoods(
				getOKMessageWith(
						getLunchDayAsJSON(dayOfTheWeek, restaurantDays), "[]"),
				dayOfTheWeek);

		assertThat(foodList, Matchers.hasItem(expectedRestaurantDay));
	}

	@Test
	public void parseRestaurants_returnsCorrectData()
			throws FoodParserException {
		Restaurant restaurant = new Restaurant("Tottis", "osoite", "20500",
				"Turku", "22.222", "23.111");
		List<Restaurant> expectedRestaurants = Lists.newArrayList(restaurant);
		String json = getOKMessageWith("",
				new Gson().toJson(expectedRestaurants));
		List<Restaurant> actualRestaurants = parser.parseRestaurants(json);

		assertEquals(expectedRestaurants, actualRestaurants);
	}

	@Test
	public void parseFoods_withRealData() throws FoodParserException,
			FileNotFoundException, IOException, URISyntaxException {
		InputStream foodTestJSONFileAsInputStream = FoodServiceTestHelper
				.getFoodTestJSONFileAsInputStream(weekNumber);
		String dataJSON = IOUtils.toString(foodTestJSONFileAsInputStream);
		foodTestJSONFileAsInputStream.close();

		List<RestaurantDay> foods = parser.parseFoods(dataJSON, 1);
		assertFalse(foods.isEmpty());
	}

	public void parseRestaurants_withRealData() throws FileNotFoundException,
			IOException, URISyntaxException, FoodParserException {
		InputStream foodTestJSONFileAsInputStream = FoodServiceTestHelper
				.getFoodTestJSONFileAsInputStream(weekNumber);
		String dataJSON = IOUtils.toString(foodTestJSONFileAsInputStream);
		foodTestJSONFileAsInputStream.close();

		List<Restaurant> restaurants = parser.parseRestaurants(dataJSON);
		assertFalse(restaurants.isEmpty());
	}

	private String getOKMessageWith(String foodsByDay, String restaurantArray) {
		return String.format("{\"status\": \"OK\", \"version\": \""
				+ parserVersion
				+ "\", \"foodsByDay\": [%s], \"restaurants\": %s}", foodsByDay,
				restaurantArray);
	}

	private String getLunchDayAsJSON(int dayOfTheWeek,
			List<RestaurantDay> restaurantDays) {
		return "{\"day\": \"" + dayOfTheWeek + "\", \"foodsByRestaurant\": "
				+ new Gson().toJson(restaurantDays) + "}";
	}

}
