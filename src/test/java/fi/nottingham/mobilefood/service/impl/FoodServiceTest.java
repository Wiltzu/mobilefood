package fi.nottingham.mobilefood.service.impl;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import fi.nottingham.mobilefood.service.IFoodService;

public class FoodServiceTest {
	
	private IFoodService foodService;

	@Before
	public void setUp() {
		foodService = new FoodServiceImpl("http://localhost:4730/mobilerest/");
	}

	@Test
	public void getFoodsBy_dontReturnNull() {
		int dayOfTheWeek = 0, weekNumber = 1;
		assertNotNull(foodService.getFoodsBy(weekNumber, dayOfTheWeek));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void getFoodsBy_weekNumberIsAtLeastOne() {
		foodService.getFoodsBy(0, 1);
	}
}
