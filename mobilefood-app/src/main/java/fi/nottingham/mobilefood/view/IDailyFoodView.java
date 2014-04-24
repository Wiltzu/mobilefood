package fi.nottingham.mobilefood.view;

import java.util.List;

import fi.nottingham.mobilefood.model.RestaurantDay;

public interface IDailyFoodView {

	void setFoods(List<RestaurantDay> dailyFoodsByrestaurant);
	
	Integer getWeekDay();
}
