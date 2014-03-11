package fi.nottingham.mobilefood.service;

import java.util.List;

import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;

public interface IFoodService {

	/**
	 * @param weekNumber
	 * @param dayOfTheWeek
	 * @return foods for week's day
	 * @throws NoInternetConnectionException when foods are not already downloaded and device don't have an Internet connection
	 * @throws FoodServiceException if no foods are available requested week or service is down
	 */
	List<RestaurantDay> getFoodsBy(int weekNumber, int dayOfTheWeek) throws NoInternetConnectionException, FoodServiceException;
}
