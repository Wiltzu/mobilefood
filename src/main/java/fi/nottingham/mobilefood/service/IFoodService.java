package fi.nottingham.mobilefood.service;

import java.util.List;
import java.util.concurrent.Future;

import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;

public interface IFoodService {

	/**
	 * @param weekNumber
	 * @param dayOfTheWeek
	 * @return foods for week's day
	 * @throws {@link FoodServiceException} if no foods are available requested week or service is down
	 * @throws {@link NoInternetConnectionException} if device has no Internet connection
	 */
	Future<List<RestaurantDay>> getFoodsBy(int weekNumber, int dayOfTheWeek);

	/**
	 * @param weekNumber
	 * @param dayOfTheWeek
	 * @return foods for specific day or null if there are no foods in internal storage
	 */
	Future<List<RestaurantDay>> getFoodsFromInternalStorageBy(int weekNumber, int dayOfTheWeek);
}
