package fi.nottingham.mobilefood.presenter.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;

public class MainViewPresenterTest {
	
	IMainViewPresenter mainViewPresenter;
	@Mock
	IMainView mainView;
	@Mock
	IFoodService foodService;
	
	Date date = new Date();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mainViewPresenter = new MainViewPresenterImpl(foodService, date);
	}

	@Test
	public void getFoodService_ReturnsFoodService() {
		assertEquals(foodService, mainViewPresenter.getFoodService());
	}
	
	@Test
	public void OnViewCreation_setsViewsFoodsFromFoodService() {		
		List<Food> foods = Lists.newArrayList();
		when(foodService.getFoodsBy(Mockito.anyInt())).thenReturn(foods);
		
		mainViewPresenter.onViewCreation(mainView);
		
		verify(mainView).setFoods(DateUtils.getDateAtMidnight(date), foods);
	}

}
