package fi.nottingham.mobilefood.service;

import java.util.List;

import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;

public interface IFoodService {

	/**
	 * @param weekNumber
	 * @param dayOfTheWeek
	 * @return foods for week's day
	 * @throws FoodServiceException if no foods are available requested week or service is down
	 */
	List<RestaurantDay> getFoodsBy(int weekNumber, int dayOfTheWeek) throws FoodServiceException;

	/**
	 * @param weekNumber
	 * @param dayOfTheWeek
	 * @return foods for specific day or null if there are no foods in internal storage
	 */
	List<RestaurantDay> getFoodsFromInternalStorageBy(int weekNumber, int dayOfTheWeek);
}
