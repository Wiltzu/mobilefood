package fi.nottingham.mobilefood.service;

import java.util.List;

import fi.nottingham.mobilefood.model.Food;

public interface IFoodService {

	List<Food> getFoodsBy(int dayOfTheWeek);
}
