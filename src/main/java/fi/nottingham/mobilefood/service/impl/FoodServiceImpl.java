package fi.nottingham.mobilefood.service.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
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
import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;

public class FoodServiceImpl implements IFoodService {
	private final String serviceLocation;
	private final Logger logger = Logger.getLogger("FoodService");
	private final IFileSystemService fileSystemService;

	@Inject
	public FoodServiceImpl(String serviceLocation, IFileSystemService fileSystemService) {
		this.serviceLocation = checkNotNull(serviceLocation, "serviceLocation cannot be null");
		this.fileSystemService = checkNotNull(fileSystemService, "fileSystemService cannot be null");
	}

	public List<Food> getFoodsBy(int weekNumber, int dayOfTheWeek) {
		final List<Food> foodsOfTheDay = Lists.newArrayList();
		checkArgument(weekNumber >= 1, "week number must be at least one");

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(
					getRequestURL(weekNumber)).openConnection();

			String response = IOUtils.toString(connection.getInputStream(),
					"UTF-8");
			if (response.contains("ERROR")) {
				logger.log(Level.SEVERE, response);
				return foodsOfTheDay;
			}

			JSONArray foodsByDay = (JSONArray) new JSONTokener(response)
					.nextValue();

			JSONObject requestedWeekDay = foodsByDay.getJSONObject(dayOfTheWeek);

			JSONArray foodsByRestaurant = requestedWeekDay
					.getJSONArray("foods_by_restaurant");

			for (int j = 0; j < foodsByRestaurant.length(); j++) {
				JSONObject restaurant = foodsByRestaurant.getJSONObject(j);
				JSONArray itsFoods = restaurant.getJSONArray("foods");
				for (int foodIndex = 0; foodIndex < itsFoods.length(); foodIndex++) {
					JSONObject food = itsFoods.getJSONObject(foodIndex);

					foodsOfTheDay.add(new Food(food.getString("name"), food
							.getJSONArray("prices").toString(), null,
							restaurant.getString("restaurant_name")));
				}
			}
		} catch (MalformedURLException e) {
			logger.throwing("FoodService", "getFoodsBy", e);
		} catch (IOException e) {
			logger.throwing("FoodService", "getFoodsBy", e);
		} catch (JSONException e) {
			logger.throwing("FoodService", "getFoodsBy", e);
		}
		return foodsOfTheDay;
	}

	private String getRequestURL(int weekNumber) {
		return String.format("%s?restaurant=%s&year=%s&week=%s",
				serviceLocation, "unica", 2014, weekNumber);
	}
}
