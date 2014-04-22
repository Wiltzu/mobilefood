package fi.nottingham.mobilefood.service.impl;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import fi.nottingham.mobilefood.model.Restaurant;
import fi.nottingham.mobilefood.model.RestaurantDay;

public class FoodParser {
	private final Logger logger = Logger.getLogger(this.getClass());

	public class FoodParserException extends Exception {
		private static final long serialVersionUID = 8418414551683257191L;

	}

	private final String parserVersion;

	public FoodParser(String parserVersion) {
		this.parserVersion = parserVersion;
	}

	public String getParserVersion() {
		return parserVersion;
	}

	public List<RestaurantDay> parse(String foodJSON, int dayOfTheWeek)
			throws FoodParserException {
		try {
			JSONObject rootElement = (JSONObject) new JSONTokener(foodJSON)
					.nextValue();

			if (!isResponseOK(rootElement)) {
				throw new FoodParserException();
			}

			List<RestaurantDay> foodsOfTheDay = Lists.newArrayList();
			Type foodListType = new TypeToken<List<RestaurantDay>>() {
			}.getType();
			JSONObject requestedWeekDay = rootElement
					.getJSONArray("foodsByDay").optJSONObject(dayOfTheWeek);
			if (requestedWeekDay != null) {
				foodsOfTheDay = new Gson().fromJson(requestedWeekDay
						.getJSONArray("foodsByRestaurant").toString(),
						foodListType);
			}

			return foodsOfTheDay;

		} catch (JSONException e) {
			logger.fatal("Unexpected Exception while parsing", e);
			throw new FoodParserException();
		}
	}

	public List<Restaurant> parseRestaurants(String json) throws FoodParserException {
		try {
			JSONObject rootElement = (JSONObject) new JSONTokener(json)
					.nextValue();
			
			if (!isResponseOK(rootElement)) {
				throw new FoodParserException();
			}
			
			List<Restaurant> restaurants = Lists.newArrayList();
			Type restaurantListType = new TypeToken<List<Restaurant>>() {
			}.getType();
			JSONArray restaurantsJSON = rootElement.optJSONArray("restaurants");
			if(restaurantsJSON != null) {
				restaurants = new Gson().fromJson(restaurantsJSON.toString(),restaurantListType);
			}

			return restaurants;
			
		} catch (JSONException e) {
			logger.fatal("Unexpected Exception while parsing", e);
			throw new FoodParserException();
		}
	}

	private boolean isResponseOK(JSONObject rootElement) throws JSONException {
		String status = rootElement.getString("status");
		if (!status.equals("OK")) {
			return false;
		}

		String version = rootElement.optString("version");
		if (version != null && !version.equals(parserVersion)) {
			return false;
		}

		return true;
	}

}
