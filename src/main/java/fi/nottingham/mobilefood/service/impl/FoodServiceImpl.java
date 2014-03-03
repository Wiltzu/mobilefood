package fi.nottingham.mobilefood.service.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.service.IFoodService;

public class FoodServiceImpl implements IFoodService {
	
	private static final int DAYS_IN_WEEK = 7;
	private final String serviceLocation;

	public FoodServiceImpl(String serviceLocation) {
		this.serviceLocation = serviceLocation;
	}
	
	public List<Food> getFoodsBy(int weekNumber, int dayOfTheWeek) {
		final List<Food> foodsOfTheDay = Lists.newArrayList();
		checkArgument(weekNumber >= 1, "week number must be at least one");
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(getRequestURL(weekNumber)).openConnection();
			
			String response = IOUtils.toString(connection.getInputStream(), "UTF-8");
			if(response.contains("ERROR")) {
				Log.e("FoodService",response);
				return foodsOfTheDay;
			}
			
			JSONArray foodsByDay = (JSONArray) new JSONTokener(response).nextValue();
			for(int i = 0; i < DAYS_IN_WEEK; i++){
				JSONObject day = foodsByDay.getJSONObject(i);
				if(day.getInt("day") == dayOfTheWeek) {
					JSONArray foodsByRestaurant = day.getJSONArray("foods_by_restaurant");
					for(int j=0;  j < foodsByRestaurant.length(); j++) {
						JSONObject restaurant = foodsByRestaurant.getJSONObject(j);
						//TODO: type mismatch
						foodsOfTheDay.add(new Food(restaurant.getJSONObject("foods").toString(),"as", "bs", restaurant.getString("restaurant_name")));
					}
					break;
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return foodsOfTheDay;
	}

	private String getRequestURL(int weekNumber) {
		return String.format("%s?restaurant=%s&year=%s&week=%s", serviceLocation, "unica", 2014, weekNumber);
	}
}
