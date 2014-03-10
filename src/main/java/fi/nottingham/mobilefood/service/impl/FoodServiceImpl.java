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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;

public class FoodServiceImpl implements IFoodService {
	private static final String CHAIN_NAME = "unica";
	private static final int YEAR = 2014;

	private final String serviceLocation;
	private final Logger logger = Logger.getLogger("FoodService");
	private final IFileSystemService fileSystemService;
	private final INetworkStatusService networkStatusService;

	@Inject
	public FoodServiceImpl(String serviceLocation,
			IFileSystemService fileSystemService, INetworkStatusService networkStatusService) {
		this.serviceLocation = checkNotNull(serviceLocation,
				"serviceLocation cannot be null");
		this.fileSystemService = checkNotNull(fileSystemService,
				"fileSystemService cannot be null");
		this.networkStatusService = checkNotNull(networkStatusService, "networkStatusService cannot be null");
	}

	public List<RestaurantDay> getFoodsBy(int weekNumber, int dayOfTheWeek) throws NoInternetConnectionException {
		// TODO: much better error handling
		final List<RestaurantDay> foodsOfTheDay = Lists.newArrayList();
		checkArgument(weekNumber >= 1, "week number must be at least one");

		String foodData = null;

		String responseFromFile = getDataFromInternalStorage(weekNumber);

		if (isNullOrEmpty(responseFromFile)) {
			foodData = downloadDataFromService(weekNumber);
		} else {
			foodData = responseFromFile;
		}
		
		if (foodData != null) {
			try {
				//TODO: JSON versioning so that version compatibility is easily detected
				//TODO: move parsing to own class
				JSONArray foodsByDay = (JSONArray) new JSONTokener(foodData)
						.nextValue();

				JSONObject requestedWeekDay = foodsByDay
						.getJSONObject(dayOfTheWeek);

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

			} catch (JSONException e) {
				logger.throwing("FoodService",
						"getFoodsBy: JSONParsing failed!", e);
			}
		}

		return foodsOfTheDay;
	}

	/**
	 * @param weekNumber
	 * @return foods from file or null if not found
	 */
	private String getDataFromInternalStorage(int weekNumber) {
		String fileName = getFileNameFor(YEAR, weekNumber, CHAIN_NAME);
		String responseFromFile = null;

		try {
			InputStream weekInputFile = fileSystemService
					.openInputFile(fileName);

			responseFromFile = IOUtils.toString(weekInputFile);
			weekInputFile.close();

		} catch (IOException e) {
			return null;
		}

		return responseFromFile;
	}

	/**
	 * @param weekNumber
	 * @return foods downloaded from service
	 * @throws NoInternetConnectionException if there is no Internet connection
	 */
	private String downloadDataFromService(int weekNumber) throws NoInternetConnectionException {
		if(!networkStatusService.isConnectedToInternet()) {
			throw new NoInternetConnectionException();
		}
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(
					getRequestURL(weekNumber)).openConnection();

			String response = IOUtils.toString(connection.getInputStream(),
					"UTF-8");

			if (response == null || response.contains("ERROR")) {
				logger.log(Level.SEVERE, response);
				return null;
			}

			OutputStream weekOutputFile = fileSystemService
					.openOutputFile(getFileNameFor(YEAR, weekNumber, CHAIN_NAME));
			weekOutputFile.write(response.getBytes());
			weekOutputFile.flush();
			weekOutputFile.close();

			return response;
		} catch (MalformedURLException e) {
			logger.throwing("FoodService", "getFoodsBy", e);
		} catch (IOException e) {
			logger.throwing("FoodService", "getFoodsBy", e);
		}

		return null;
	}

	private String getFileNameFor(int year, int weekNumber, String chainName) {
		return String.format("%s_w%s_%s.json", year, weekNumber, chainName);
	}

	private String getRequestURL(int weekNumber) {
		return String.format("%s?restaurant=%s&year=%s&week=%s",
				serviceLocation, "unica", 2014, weekNumber);
	}
}
