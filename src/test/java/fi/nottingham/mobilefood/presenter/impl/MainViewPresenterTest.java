package fi.nottingham.mobilefood.presenter.impl;

import static org.junit.Assert.assertEquals;
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
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;
import fi.nottingham.mobilefood.view.IMainView;

public class MainViewPresenterTest {
	@InjectMocks
	IMainViewPresenter mainViewPresenter;
	@Mock
	List<RestaurantDay> currentFoods;

	IMainView mainView;
	IFoodService foodService;
	INetworkStatusService networkStatusService;

	Date date = new Date();

	@Before
	public void setUp() throws Exception {
		mainView = mock(IMainView.class);
		foodService = mock(IFoodService.class);
		networkStatusService = mock(INetworkStatusService.class);
		mainViewPresenter = new MainViewPresenterImpl(foodService, date,
				networkStatusService);
	}

	@Test
	public void getFoodService_ReturnsFoodService() {
		assertEquals(foodService, mainViewPresenter.getFoodService());
	}

	// @Test
	public void OnViewCreation_setsViewsFoodsFromFoodService()
			throws NoInternetConnectionException, FoodServiceException {
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

	@SuppressWarnings("unchecked")
	@Test
	public void fetchFoodsFromService_clearsFoodListAndAddsNewValuesFromService()
			throws FoodServiceException {
		MockitoAnnotations.initMocks(this); // inits currentFoods

		List<RestaurantDay> mockFoodsFromService = mock(List.class);
		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(mockFoodsFromService);

		((MainViewPresenterImpl) mainViewPresenter).fetchFoodsFromService();
	}

	@Test
	public void fetchFoodsFromService_withNoInternetConnection_triesTogetFoodsFromInternalStorage()
			throws FoodServiceException {

		when(networkStatusService.isConnectedToInternet()).thenReturn(false);

		((MainViewPresenterImpl) mainViewPresenter).fetchFoodsFromService();

		verify(foodService).getFoodsFromInternalStorageBy(Mockito.anyInt(),
				Mockito.anyInt());
		verify(foodService, Mockito.never()).getFoodsBy(Mockito.anyInt(),Mockito.anyInt());
	}

	@Test
	public void fetchFoodsFromService_withInternetButServiceHasNoFoodsForWeek_exceptionIsCacthed()
			throws FoodServiceException {
		MockitoAnnotations.initMocks(this); // inits currentFoods

		when(networkStatusService.isConnectedToInternet()).thenReturn(true);
		when(foodService.getFoodsFromInternalStorageBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt()))
				.thenThrow(
						new FoodServiceException(
								FoodServiceException.SERVICE_DOWN));

		((MainViewPresenterImpl) mainViewPresenter).fetchFoodsFromService();

		verify(foodService).getFoodsBy(Mockito.anyInt(), Mockito.anyInt());
		//TODO: improve
	}

	@Test
	public void updateUI_setsFoodsCurrentFoodsIfHasThem() {
		currentFoods = Lists.newArrayList(new RestaurantDay("restName",
				new ArrayList<Food>()));
		MockitoAnnotations.initMocks(this); // inits currentFoods
		
		((MainViewPresenterImpl) mainViewPresenter).updateUI(mainView);

		verify(mainView).setFoods(currentFoods);
	}

	@Test
	public void updateUI_ifHasNoInternetConnection_notifiesUserAndCreatesRefreshButton() {
		((MainViewPresenterImpl) mainViewPresenter)
				.setHasInternetConnection(false);

		((MainViewPresenterImpl) mainViewPresenter).updateUI(mainView);

		verify(mainView).notifyThatDeviceHasNoInternetConnection();
		verify(mainView).showRefreshButton();
	}

}
