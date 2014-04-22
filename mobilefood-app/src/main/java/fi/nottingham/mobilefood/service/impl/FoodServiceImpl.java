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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.ConfigFactory;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.Restaurant;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;
import fi.nottingham.mobilefood.service.impl.FoodParser.FoodParserException;

public class FoodServiceImpl implements IFoodService {
	private static final String CHAIN_NAME = "unica";
	private static final int YEAR = 2014;
	private final Logger logger = Logger.getLogger(this.getClass());

	private final String serviceLocation;
	private int connectTimeout;
	private Map<String, Restaurant> restaurants;

	private final IFileSystemService fileSystemService;
	private final INetworkStatusService networkStatusService;
	private FoodParser parser;

	private final ExecutorService pool = Executors.newFixedThreadPool(
			2,
			new ThreadFactoryBuilder().setNameFormat(
					"FoodServicePool-thread-%d").build());

	@Inject
	public FoodServiceImpl(String serviceLocation,
			IFileSystemService fileSystemService,
			INetworkStatusService networkStatusService) {
		this.serviceLocation = checkNotNull(serviceLocation,
				"serviceLocation cannot be null");
		this.fileSystemService = checkNotNull(fileSystemService,
				"fileSystemService cannot be null");
		this.networkStatusService = checkNotNull(networkStatusService,
				"networkStatusService cannot be null");
		connectTimeout = ConfigFactory.load().getInt(
				"mobilefood.foodservice.timeout.connect");
	}

	@Override
	public synchronized List<RestaurantDay> getFoodsFromInternalStorageBy(
			final int weekNumber, final int dayOfTheWeek) {
		checkArgument(weekNumber >= 1, "week number must be at least one");

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
			try {
				parser = new FoodParser("0.9");
				List<RestaurantDay> parsedFoods = parser.parseFoods(responseFromFile,
						dayOfTheWeek);
				//TODO: fix this!!!!!!
				Map<String, Restaurant> restaurantMap = getRestaurants(responseFromFile);
				for (RestaurantDay restaurantDay : parsedFoods) {
					Restaurant restaurant = restaurantMap.get(restaurantDay.getRestaurantName());
					restaurantDay.setRestaurant(restaurant);
				}
				return parsedFoods;
			} catch (FoodParserException e) {
				logger.fatal("parsing failed", e);
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public synchronized Future<List<RestaurantDay>> getFoodsBy(
			final int weekNumber, final int dayOfTheWeek) {
		checkArgument(weekNumber >= 1, "week number must be at least one");
		return pool.submit(new Callable<List<RestaurantDay>>() {

			@Override
			public List<RestaurantDay> call() throws Exception {
				final List<RestaurantDay> foodsOfTheDay = Lists.newArrayList();

				String dataJSON = downloadDataFromService(weekNumber);

				if (dataJSON != null) {
					parser = new FoodParser("0.9");
					try {
						foodsOfTheDay.addAll(parser.parseFoods(dataJSON,
								dayOfTheWeek));
						//TODO: fix this!!!!!!
						Map<String, Restaurant> restaurantMap = getRestaurants(dataJSON);
						for (RestaurantDay restaurantDay : foodsOfTheDay) {
							Restaurant restaurant = restaurantMap.get(restaurantDay.getRestaurantName());
							restaurantDay.setRestaurant(restaurant);
						}
						
						writeJsonToFile(weekNumber, dataJSON);

					} catch (FoodParserException e) {
						logger.error(String
								.format("Unable to get foods from service. Response was: '%s'",
										dataJSON));
						throw new FoodServiceException(
								FoodServiceException.NO_FOOD_FOR_WEEK);
					} catch (IOException e) {
						// TODO: improve this exception handling!
						logger.fatal("Writing to file failed", e);
					}
				}

				return foodsOfTheDay;
			}
		});
	}

	protected Map<String, Restaurant> covertRestaurantsListToMap(
			List<Restaurant> parsedRestaurants) {
		HashMap<String, Restaurant> convertedRestaurants = Maps.newHashMap();
		for (Restaurant restaurant : parsedRestaurants) {
			convertedRestaurants.put(restaurant.getName(), restaurant);
		}
		return convertedRestaurants;
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
		if (!networkStatusService.isConnectedToInternet()) {
			throw new NoInternetConnectionException();
		}

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(
					getRequestURL(weekNumber)).openConnection();
			connection.setConnectTimeout(connectTimeout);
			// TODO: set timeouts for connecting and receiving data from service

			String response = IOUtils.toString(connection.getInputStream(),
					"UTF-8");

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

	private void writeJsonToFile(int weekNumber, String json)
			throws IOException {
		OutputStream weekOutputFile = fileSystemService
				.openOutputFile(getFileNameFor(YEAR, weekNumber, CHAIN_NAME));
		weekOutputFile.write(json.getBytes());
		weekOutputFile.flush();
		weekOutputFile.close();
	}

	private String getFileNameFor(int year, int weekNumber, String chainName) {
		return String.format("%s_w%s_%s.json", year, weekNumber, chainName);
	}

	private String getRequestURL(int weekNumber) {
		return String.format("%s?restaurant=%s&year=%s&week=%s",
				serviceLocation, "unica", 2014, weekNumber);
	}

	private Map<String, Restaurant> getRestaurants(String dataJSON) throws FoodParserException {
		if (restaurants == null) {
			restaurants = Maps.newHashMap();
			restaurants
					.putAll(covertRestaurantsListToMap(parser
							.parseRestaurants(dataJSON)));
		}
		
		return restaurants;
	}
}
