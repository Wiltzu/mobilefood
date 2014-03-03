package fi.nottingham.mobilefood.service.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.service.IFoodService;

public class FoodServiceImpl implements IFoodService {

	public List<Food> getFoodsBy(int weekNumber, int dayOfTheWeek) {
		checkArgument(weekNumber >= 1, "week number must be at least one");
		return Lists.newArrayList(new Food("dasd", "asd", "adsad", "asdasd"));
	}
}
