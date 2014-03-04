package fi.nottingham.mobilefood.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;

public class FoodServiceTest {

	private IFoodService foodService;

	@Before
	public void setUp() {
		foodService = new FoodServiceImpl("http://localhost:4730/mobilerest/", mock(IFileSystemService.class));
	}

	@Test
	public void getFoodsBy_dontReturnNull() {
		int dayOfTheWeek = 0, weekNumber = 1;
		assertNotNull(foodService.getFoodsBy(weekNumber, dayOfTheWeek));
	}
	
	//@Test
	public void getFoodsBy_returns() {
		int dayOfTheWeek = 0, weekNumber = 10;
		assertFalse(foodService.getFoodsBy(weekNumber, dayOfTheWeek).isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void getFoodsBy_weekNumberIsAtLeastOne() {
		foodService.getFoodsBy(0, 1);
	}
}
