package fi.nottingham.mobilefood.service.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;

public class FoodServiceImpl implements IFoodService {
	private static final String CHAIN_NAME = "unica";
	private static final int YEAR = 2014;

	private final String serviceLocation;
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IFileSystemService fileSystemService;

	@Inject
	public FoodServiceImpl(String serviceLocation,
			IFileSystemService fileSystemService) {
		this.serviceLocation = checkNotNull(serviceLocation,
				"serviceLocation cannot be null");
		this.fileSystemService = checkNotNull(fileSystemService,
				"fileSystemService cannot be null");
	}

	@Override
	public synchronized List<RestaurantDay> getFoodsFromInternalStorageBy(int weekNumber,
			int dayOfTheWeek) {
		String fileName = getFileNameFor(YEAR, weekNumber, CHAIN_NAME);
		String responseFromFile = null;

		try {
			InputStream weekInputFile = fileSystemService
					.openInputFile(fileName);

			responseFromFile = IOUtils.toString(weekInputFile);
			weekInputFile.close();
		} catch (IOException e) {
			logger.debug("No food file in internal storage", e);
			return null;
		}

		if (!isNullOrEmpty(responseFromFile)) {
			return parseFoods(dayOfTheWeek, responseFromFile);
		} else {
			return null;
		}
	}

	@Override
	public synchronized List<RestaurantDay> getFoodsBy(int weekNumber, int dayOfTheWeek)
			throws FoodServiceException {
		final List<RestaurantDay> foodsOfTheDay = Lists.newArrayList();
		checkArgument(weekNumber >= 1, "week number must be at least one");

		String foodJSON = downloadDataFromService(weekNumber);

		if (foodJSON != null) {
			foodsOfTheDay.addAll(parseFoods(dayOfTheWeek, foodJSON));
		}

		return foodsOfTheDay;
	}

	private List<RestaurantDay> parseFoods(int dayOfTheWeek, String foodJSON) {
		final List<RestaurantDay> foodsOfTheDay = Lists.newArrayList();
		try {
			// TODO: JSON versioning so that version compatibility is easily
			// detected
			// TODO: move parsing to its own class
			JSONArray foodsByDay = (JSONArray) new JSONTokener(foodJSON)
					.nextValue();

			JSONObject requestedWeekDay = foodsByDay
					.optJSONObject(dayOfTheWeek);

			if (requestedWeekDay != null) {

				JSONArray foodsByRestaurant = requestedWeekDay
						.getJSONArray("foods_by_restaurant");

				for (int i = 0; i < foodsByRestaurant.length(); i++) {
					JSONObject restaurant = foodsByRestaurant.getJSONObject(i);
					JSONArray itsFoods = restaurant.getJSONArray("foods");

					List<Food> foodsOfTheRestaurant = Lists.newArrayList();
					for (int foodIndex = 0; foodIndex < itsFoods.length(); foodIndex++) {
						JSONObject food = itsFoods.getJSONObject(foodIndex);

						JSONArray foodPrices = food.getJSONArray("prices");
						List<String> prices = Lists.newArrayList();

						for (int j = 0; j < foodPrices.length(); j++) {
							prices.add(foodPrices.getString(j));
						}

						foodsOfTheRestaurant.add(new Food(food
								.getString("name"), prices, food
								.optString("diets")));
					}
					String restaurantName = restaurant
							.getString("restaurant_name");
					foodsOfTheDay.add(new RestaurantDay(restaurantName,
							foodsOfTheRestaurant));

				}

			}

		} catch (JSONException e) {
			logger.fatal("Failed to parse foods from JSON", e);
		}
		return foodsOfTheDay;
	}

	/**
	 * @param weekNumber
	 * @return foods downloaded from service
	 * @throws NoInternetConnectionException
	 *             if there is no Internet connection
	 * @throws FoodServiceException
	 *             if no foods are available requested week or service is down
	 */
	private String downloadDataFromService(int weekNumber)
			throws FoodServiceException {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(
					getRequestURL(weekNumber)).openConnection();
			// TODO: set timeouts for connecting and receiving data from service

			String response = IOUtils.toString(connection.getInputStream(),
					"UTF-8");

			if (isNullOrEmpty(response) || response.contains("ERROR")) {
				logger.error(String.format(
						"Unable to get foods from service. Response was: '%s'",
						response));
				throw new FoodServiceException(
						FoodServiceException.NO_FOOD_FOR_WEEK);
			}

			OutputStream weekOutputFile = fileSystemService
					.openOutputFile(getFileNameFor(YEAR, weekNumber, CHAIN_NAME));
			weekOutputFile.write(response.getBytes());
			weekOutputFile.flush();
			weekOutputFile.close();

			return response;
		} catch (MalformedURLException e) {
			logger.fatal("Service's URL was malformed, check URL!", e);
			throw new FoodServiceException(FoodServiceException.SERVICE_DOWN);
		} catch (IOException e) {
			logger.fatal("Service seems to down for some reason", e);
			throw new FoodServiceException(FoodServiceException.SERVICE_DOWN);
		}
	}

	private String getFileNameFor(int year, int weekNumber, String chainName) {
		return String.format("%s_w%s_%s.json", year, weekNumber, chainName);
	}

	private String getRequestURL(int weekNumber) {
		return String.format("%s?restaurant=%s&year=%s&week=%s",
				serviceLocation, "unica", 2014, weekNumber);
	}
}
