package fi.nottingham.mobilefood.service.impl;

import java.util.List;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.service.IFoodService;

public class FoodServiceImpl implements IFoodService {

	public List<Food> getFoodsBy(int dayOfTheWeek) {
		return Lists.newArrayList(new Food("dasd", "asd", "adsad", "asdasd"));
	}
}
