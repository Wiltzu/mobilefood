package fi.nottingham.mobilefood.view;

import java.util.List;

import fi.nottingham.mobilefood.model.RestaurantDay;

public interface DailyFoodView {

	void setFoods(List<RestaurantDay> dailyFoodsByrestaurant);
	
	Integer getWeekDay();
}
