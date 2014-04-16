package fi.nottingham.mobilefood.service.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.ConfigFactory;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;

public class FoodServiceImpl implements IFoodService {
	private static final String CHAIN_NAME = "unica";
	private static final int YEAR = 2014;
	private final Logger logger = Logger.getLogger(this.getClass());

	private final String serviceLocation;
	private int connectTimeout;
	
	private final IFileSystemService fileSystemService;
	private final INetworkStatusService networkStatusService;

	private final ExecutorService pool = Executors.newFixedThreadPool(
			2,
			new ThreadFactoryBuilder().setNameFormat(
					"FoodServicePool-thread-%d").build());

	@Inject
	public FoodServiceImpl(String serviceLocation,
			IFileSystemService fileSystemService, INetworkStatusService networkStatusService) {
		this.serviceLocation = checkNotNull(serviceLocation,
				"serviceLocation cannot be null");
		this.fileSystemService = checkNotNull(fileSystemService,
				"fileSystemService cannot be null");
		this.networkStatusService = checkNotNull(networkStatusService, "networkStatusService cannot be null");
		connectTimeout = ConfigFactory.load().getInt(
				"mobilefood.foodservice.timeout.connect");
	}

	@Override
	public synchronized Future<List<RestaurantDay>> getFoodsFromInternalStorageBy(
			final int weekNumber, final int dayOfTheWeek) {
		checkArgument(weekNumber >= 1, "week number must be at least one");
		return pool.submit(new Callable<List<RestaurantDay>>() {

			@Override
			public List<RestaurantDay> call() throws Exception {
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
			
		});
		
	}

	@Override
	public synchronized Future<List<RestaurantDay>> getFoodsBy(final int weekNumber,
			final int dayOfTheWeek) {
		checkArgument(weekNumber >= 1, "week number must be at least one");
		return pool.submit(new Callable<List<RestaurantDay>>() {

			@Override
			public List<RestaurantDay> call() throws Exception {
				final List<RestaurantDay> foodsOfTheDay = Lists.newArrayList();

				String foodJSON = downloadDataFromService(weekNumber);

				if (foodJSON != null) {
					foodsOfTheDay.addAll(parseFoods(dayOfTheWeek, foodJSON));
				}

				return foodsOfTheDay;
			}
		});
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
			throws FoodServiceException, NoInternetConnectionException {
		if(!networkStatusService.isConnectedToInternet()) {
			throw new NoInternetConnectionException();
		}
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(
					getRequestURL(weekNumber)).openConnection();
			connection.setConnectTimeout(connectTimeout);
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
		} catch (SocketTimeoutException e) {
			logger.fatal(
					"Connecting to service resulted in a timeout. Service is likely down!",
					e);
			throw new FoodServiceException(FoodServiceException.SERVICE_DOWN);
		} catch (MalformedURLException e) {
			logger.fatal("Service's URL was malformed, check URL!", e);
			throw new FoodServiceException(FoodServiceException.SERVICE_DOWN);
		} catch (IOException e) {
			logger.fatal("Can't read data from service!", e);
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
