package fi.nottingham.mobilefood.service.impl;

import java.util.List;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.service.IFoodService;

public class FoodServiceImpl implements IFoodService {

	public List<Food> getFoodsBy(int dayOfTheWeek) {
		return Lists.newArrayList(new Food("Makaroonilaatikko", "2,50 / 5,40 / 6,70","L G", "Mikro"), new Food("Jauhelihakeitto", "2,50 / 5,40 / 6,70","L G", "Mikro"));
	}
}
