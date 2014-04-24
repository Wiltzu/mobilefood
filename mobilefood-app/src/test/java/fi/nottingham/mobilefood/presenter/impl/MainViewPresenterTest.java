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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.DailyFoodView;
import fi.nottingham.mobilefood.view.IMainView;
import fi.nottingham.mobilefood.view.ViewIsReadyListener;

public class MainViewPresenterTest {
	//TODO: fix these tests
	IMainViewPresenter mainViewPresenter;

	IMainView mainView;
	IFoodService foodService;
	INetworkStatusService networkStatusService;
	DailyFoodView foodView;

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
		foodView = mock(DailyFoodView.class);
		mainViewPresenter = new MainViewPresenterImpl(foodService, dateProvider);
	}

	@Test
	public void getFoodService_ReturnsFoodService() {
		assertEquals(foodService, mainViewPresenter.getFoodService());
	}

	@Test
	public void OnViewCreation_ifFoodsAreNotAlreadyInInternalStorage_thenFoodsAreFetchedFromWebService() throws Exception{
		when(foodService.getFoodsFromInternalStorageBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		mainViewPresenter.onViewCreation(mainView, null);
		
		verify(foodService).getFoodsBy(Mockito.anyInt(), Mockito.anyInt());
	}
	
	@Test
	public void onViewCreation_fetchesFoodsFromInternalStorage() {
		mainViewPresenter.onViewCreation(mainView, null);		
		verify(foodService).getFoodsFromInternalStorageBy(Mockito.anyInt(), Mockito.anyInt());
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
	public void onViewCreation_setsAvailableWeekDaysForView() {
		int[] expectedWeekDays = {2, 3, 4, 5, 6};
		//March 12th 2014 is Wednesday
		Provider<Date> dProvider = new Provider<Date>() {
			@Override
			public Date get() {
				return DateUtils.getDateAtMidnight(2014, Calendar.MARCH, 12);
			}
		};
		mainViewPresenter = new MainViewPresenterImpl(foodService, dProvider);
		mainViewPresenter.onViewCreation(mainView, null);
		verify(mainView).setAvailableWeekDays(expectedWeekDays);
	}
	
	@Test
	public void onDateChanged_setsFoodsForSelectedDate() {
		int selectedWeekDay = 3;
		mainViewPresenter.onDateChanged(mainView, selectedWeekDay);
		
		verify(foodService).getFoodsFromInternalStorageBy(Mockito.anyInt(), Mockito.eq(selectedWeekDay));
	}

	@Test
	public void getInitialFoodsFromService_withNoInternetConnection_triesToGetFoodsFromInternalStorage()
			throws FoodServiceException, InterruptedException, ExecutionException {

		when(networkStatusService.isConnectedToInternet()).thenReturn(false);

		((MainViewPresenterImpl) mainViewPresenter).getInitialFoodsFromService();

		verify(foodService).getFoodsFromInternalStorageBy(Mockito.anyInt(),
				Mockito.anyInt());
	}

	@Test
	public void viewIsReady_withInternetButServiceHasNoFoodsForWeek_notificationInUIIsShown()
			throws FoodServiceException, InterruptedException, ExecutionException {
		when(networkStatusService.isConnectedToInternet()).thenReturn(true);
		when(foodService.getFoodsFromInternalStorageBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		
		@SuppressWarnings("unchecked")
		Future<List<RestaurantDay>> foodsFuture = mock(Future.class);
		when(foodsFuture.get()).thenThrow(new ExecutionException(new FoodServiceException(FoodServiceException.SERVICE_DOWN)));
		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(foodsFuture);

		mainViewPresenter.onViewCreation(mainView, null);
		//TODO: fix!
		((ViewIsReadyListener) mainViewPresenter).viewIsReady(Mockito.any(DailyFoodView.class));

		verify(foodService).getFoodsBy(Mockito.anyInt(), Mockito.anyInt());
		verify(mainView).notifyThatFoodsAreCurrentlyUnavailable();
	}
	
	@Test
	public void viewIsReady_withInternetButServiceHasNoFoodsForWeek_userIsNotifyThatThereAreNoFoodsAndRefreshButtonIsShown()
			throws Exception {
		when(networkStatusService.isConnectedToInternet()).thenReturn(true);
		when(foodService.getFoodsFromInternalStorageBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		@SuppressWarnings("unchecked")
		Future<List<RestaurantDay>> foodsFuture = mock(Future.class);
		when(foodsFuture.get()).thenThrow(new ExecutionException(new FoodServiceException(FoodServiceException.NO_FOOD_FOR_WEEK)));
		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(foodsFuture);


		mainViewPresenter.onViewCreation(mainView, null);
		//TODO: fix!
		((ViewIsReadyListener) mainViewPresenter).viewIsReady(Mockito.any(DailyFoodView.class));

		verify(foodService).getFoodsBy(Mockito.anyInt(), Mockito.anyInt());
		
		verify(mainView).notifyThatFoodsAreCurrentlyUnavailable();
		verify(mainView).showRefreshButton();
	}
	
	@Test
	public void viewIsReady_withNoInternet_userIsShownANotificationThatHeShouldEnableInternetAndrefreshButton()
			throws Exception {
		when(networkStatusService.isConnectedToInternet()).thenReturn(true);
		when(foodService.getFoodsFromInternalStorageBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		@SuppressWarnings("unchecked")
		Future<List<RestaurantDay>> foodsFuture = mock(Future.class);
		when(foodsFuture.get()).thenThrow(new ExecutionException(new NoInternetConnectionException()));
		when(foodService.getFoodsBy(Mockito.anyInt(), Mockito.anyInt())).thenReturn(foodsFuture);


		mainViewPresenter.onViewCreation(mainView, null);
		//TODO: fix!
		((ViewIsReadyListener) mainViewPresenter).viewIsReady(Mockito.any(DailyFoodView.class));

		verify(foodService).getFoodsBy(Mockito.anyInt(), Mockito.anyInt());
		
		verify(mainView).notifyThatDeviceHasNoInternetConnection();
		verify(mainView).showRefreshButton();
	}

	@Test
	public void updateUI_ifHasFoods_setsFoods() {
		@SuppressWarnings("unchecked")
		List<RestaurantDay> foods = Collections.emptyList();
		((MainViewPresenterImpl) mainViewPresenter).updateUI(mainView, foodView, foods);

		verify(foodView).setFoods(foods);
	}
	
	@Test
	public void updateUI_ifFoodsAreNull_setsFoods() {
		((MainViewPresenterImpl) mainViewPresenter).updateUI(mainView, foodView, null);

		verify(foodView).setFoods(Mockito.anyList());
	}
	
	@Test
	public void updateUI_hidesLoadingIcon() {
		((MainViewPresenterImpl) mainViewPresenter).updateUI(mainView, foodView, null);

		verify(mainView).hideLoadingIcon();
	}
	
}
