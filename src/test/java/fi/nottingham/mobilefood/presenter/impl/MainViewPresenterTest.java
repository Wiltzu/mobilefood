package fi.nottingham.mobilefood.presenter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;

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
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;

public class MainViewPresenterTest {
	@InjectMocks
	IMainViewPresenter mainViewPresenter;
	@Mock
	List<RestaurantDay> currentFoods;

	IMainView mainView;
	IFoodService foodService;
	INetworkStatusService networkStatusService;

	Provider<Date> dateProvider = new Provider<Date>() {
		@Override
		public Date get() {
			return new Date();
		}	
	};

	@Before
	public void setUp() throws Exception {
		mainView = mock(IMainView.class);
		foodService = mock(IFoodService.class);
		networkStatusService = mock(INetworkStatusService.class);
		mainViewPresenter = new MainViewPresenterImpl(foodService, dateProvider,
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

		mainViewPresenter.onViewCreation(mainView, null);

		verify(mainView).setFoods(foods);
	}
	
	public void updateFoodsInBackground_showLoadingIconAndFetchesFoodsFromService() {
		((MainViewPresenterImpl) mainViewPresenter).updateFoodsInBackground(mainView);
		verify(mainView).showLoadingIcon();
		verify(mainView).runInBackgroud(Mockito.any(Runnable.class), Mockito.any(Runnable.class));
	}

	@Test
	public void OnViewCreation_foodsAreFetchedFromFoodServiceInBackground() {
		mainViewPresenter.onViewCreation(mainView, null);
		verify(mainView).runInBackgroud(Mockito.any(Runnable.class),
				Mockito.any(Runnable.class));
	}
	
	@Test
	public void OnViewCreation_ifSavedWeekDayIsGiven_itsSelectedInMainView() {
		int savedWeekDay = 0;
		mainViewPresenter.onViewCreation(mainView, savedWeekDay);
		verify(mainView).setSelectedDate(savedWeekDay);
	}

	@Test
	public void OnViewCreation_loadingNotificationIsShowed() {
		mainViewPresenter.onViewCreation(mainView, null);
		verify(mainView).showLoadingIcon();
	}

	@Test
	public void onViewCreation_ifFoodsHaveBeenLoadedEarlier_thoseAreSetRightAway() {
		currentFoods = Lists.newArrayList(new RestaurantDay("restName",
				new ArrayList<Food>()));
		MockitoAnnotations.initMocks(this); // inits currentFoods

		mainViewPresenter.onViewCreation(mainView, null);

		verify(mainView).setFoods(currentFoods);
	}
	
	@Test
	public void onViewCreation_setsAvailableWeekDaysForView() {
		int[] expectedWeekDays = {2, 3, 4, 5, 6};
		//March 12th 2014 is Wednesday
		Provider<Date> dProvider = new Provider<Date>() {
			@Override
			public Date get() {
				return DateUtils.getDateAtMidnight(2014, Calendar.MARCH, 12);
			}
		};
		mainViewPresenter = new MainViewPresenterImpl(foodService, dProvider, networkStatusService);
		mainViewPresenter.onViewCreation(mainView, null);
		verify(mainView).setAvailableWeekDays(expectedWeekDays);
	}
	
	@Test
	public void onDateChanged_setsFoodsCorrespondingTheDay() {
		int selectedWeekDay = 3;
		mainViewPresenter.onDateChanged(mainView, selectedWeekDay);
		
		verify(mainView).showLoadingIcon();
		//verify(foodService).getFoodsFromInternalStorageBy(Mockito.anyInt(), selectedWeekDay);
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
		when(networkStatusService.isConnectedToInternet()).thenReturn(true);
		when(foodService.getFoodsFromInternalStorageBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt()))
				.thenThrow(
						new FoodServiceException(
								FoodServiceException.SERVICE_DOWN));

		((MainViewPresenterImpl) mainViewPresenter).fetchFoodsFromService();

		verify(foodService).getFoodsBy(Mockito.anyInt(), Mockito.anyInt());
		
		assertTrue(((MainViewPresenterImpl) mainViewPresenter).foodServiceCallResultedInException());
	}
	
	@Test
	public void updateUI_withInternetButServiceHasNoFoodsForWeek_userIsNotifyThatThereAreNoFoodsAndRefreshButtonIsShown()
			throws FoodServiceException {
		when(networkStatusService.isConnectedToInternet()).thenReturn(true);
		when(foodService.getFoodsFromInternalStorageBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt()))
				.thenThrow(
						new FoodServiceException(
								FoodServiceException.SERVICE_DOWN));

		((MainViewPresenterImpl) mainViewPresenter).fetchFoodsFromService();
		((MainViewPresenterImpl) mainViewPresenter).updateUI(mainView);

		verify(foodService).getFoodsBy(Mockito.anyInt(), Mockito.anyInt());
		
		verify(mainView).notifyThatFoodsAreCurrentlyUnavailable();
		verify(mainView).showRefreshButton();
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
	
	@Test
	public void updateUI_hidesLoadingIcon() {
		((MainViewPresenterImpl) mainViewPresenter).updateUI(mainView);

		verify(mainView).hideLoadingIcon();
	}
	

}
