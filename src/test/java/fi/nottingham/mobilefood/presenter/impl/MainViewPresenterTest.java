package fi.nottingham.mobilefood.presenter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;
import fi.nottingham.mobilefood.view.IMainView;

public class MainViewPresenterTest {
	@InjectMocks
	IMainViewPresenter mainViewPresenter;
	@Mock
	List<RestaurantDay> currentFoods;

	IMainView mainView;
	IFoodService foodService;

	Date date = new Date();

	@Before
	public void setUp() throws Exception {
		mainView = mock(IMainView.class);
		foodService = mock(IFoodService.class);
		mainViewPresenter = new MainViewPresenterImpl(foodService, date);
	}

	@Test
	public void getFoodService_ReturnsFoodService() {
		assertEquals(foodService, mainViewPresenter.getFoodService());
	}

	// @Test
	public void OnViewCreation_setsViewsFoodsFromFoodService()
			throws NoInternetConnectionException {
		List<RestaurantDay> foods = Lists.newArrayList();
		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(foods);

		mainViewPresenter.onViewCreation(mainView);

		verify(mainView).setFoods(foods);
	}

	@Test
	public void OnViewCreation_foodsAreFetchedFromFoodServiceInBackground() {
		mainViewPresenter.onViewCreation(mainView);
		verify(mainView).runInBackgroud(Mockito.any(Runnable.class),
				Mockito.any(Runnable.class));
	}

	@Test
	public void OnViewCreation_loadingNotificationIsShowed() {
		mainViewPresenter.onViewCreation(mainView);
		verify(mainView).showLoadingIcon();
	}

	@Test
	public void onViewCreation_ifFoodsHaveBeenLoadedEarlier_thoseAreSetRightAway() {
		currentFoods = Lists.newArrayList(new RestaurantDay("restName",
				new ArrayList<Food>()));
		MockitoAnnotations.initMocks(this); // inits currentFoods
		
		mainViewPresenter.onViewCreation(mainView);

		verify(mainView).setFoods(currentFoods);
	}

	@Test
	public void OnViewCreation_setsDateForView() {
		mainViewPresenter.onViewCreation(mainView);
		verify(mainView).setDate(Mockito.any(Date.class));
	}

	@Test
	public void fetchFoodsFromService_clearsFoodListAndAddsNewValuesFromService()
			throws NoInternetConnectionException {
		MockitoAnnotations.initMocks(this); // inits currentFoods

		@SuppressWarnings("unchecked")
		List<RestaurantDay> mockFoodsFromService = mock(List.class);
		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(mockFoodsFromService);

		((MainViewPresenterImpl) mainViewPresenter).fetchFoodsFromService();

		verify(currentFoods).clear();
		verify(currentFoods).addAll(mockFoodsFromService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fetchFoodsFromService_withNoInternetConnection_()
			throws NoInternetConnectionException {
		MockitoAnnotations.initMocks(this); // inits currentFoods

		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt()))
				.thenThrow(NoInternetConnectionException.class);

		((MainViewPresenterImpl) mainViewPresenter).fetchFoodsFromService();

		verify(currentFoods).clear();
		verify(currentFoods, Mockito.times(0)).addAll(Mockito.anyCollection());
		assertFalse("Device should have no internet connection",
				((MainViewPresenterImpl) mainViewPresenter)
						.hasInternetConnection());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void updateUI_setsFoods() {		
		((MainViewPresenterImpl) mainViewPresenter).updateUI(mainView);
		
		verify(mainView).setFoods(Mockito.anyList());
	}
	
	@Test
	public void updateUI_ifHasNoInternetConnection_notifiesUserAndCreatesRefreshButton() {
		((MainViewPresenterImpl) mainViewPresenter).setHasInternetConnection(false);
		
		((MainViewPresenterImpl) mainViewPresenter).updateUI(mainView);
		
		verify(mainView).notifyThatDeviceHasNoInternetConnection();
		verify(mainView).showRefreshButton();
	}

}
