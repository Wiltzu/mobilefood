package fi.nottingham.mobilefood.service;

import java.util.List;

import fi.nottingham.mobilefood.model.RestaurantDay;

public interface IFoodService {

	List<RestaurantDay> getFoodsBy(int weekNumber, int dayOfTheWeek);
}
