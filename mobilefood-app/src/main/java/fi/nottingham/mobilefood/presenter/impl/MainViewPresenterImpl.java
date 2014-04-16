package fi.nottingham.mobilefood.presenter.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;

import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;
import fi.nottingham.mobilefood.view.ViewIsReadyListener;

//TODO: refactor this class and make tests for it
public class MainViewPresenterImpl implements IMainViewPresenter,
		ViewIsReadyListener {
	private final Logger logger = Logger.getLogger(this.getClass());

	private final IFoodService foodService;
	private Provider<Date> timeNow;
	private Date selectedDate;
	private Future<List<RestaurantDay>> currentFoodsFuture;
	private List<RestaurantDay> foodsFromInternalStorage;

	private boolean hasInternetConnection = true;

	private IMainView mainView;

	@Inject
	public MainViewPresenterImpl(IFoodService foodService,
			Provider<Date> timeNow) {
		this.foodService = foodService;
		this.timeNow = timeNow;
		this.selectedDate = timeNow.get();
	}

	@Override
	public void onViewCreation(final IMainView mainView,
			@Nullable Integer savedSelectedWeekDay) {
		checkNotNull("mainView cannot be null", mainView);
		this.mainView = mainView;
		// TODO: reconsider can we set this here
		mainView.setAvailableWeekDays(DateUtils
				.getRestOfTheWeeksDayNumbersFrom(timeNow.get()));

		if (savedSelectedWeekDay != null) {
			selectedDate = DateUtils.getDateInThisWeekBy(selectedDate,
					savedSelectedWeekDay);
			mainView.setSelectedDate(savedSelectedWeekDay);
		}

		mainView.showLoadingIcon();
		getInitialFoodsFromService();
	}

	protected void getInitialFoodsFromService() {
		int dayOfTheWeek = DateUtils.getDayOfTheWeek(selectedDate);
		int weekNumber = DateUtils.getWeekOfYear(selectedDate);

		foodsFromInternalStorage = foodService.getFoodsFromInternalStorageBy(weekNumber,
				dayOfTheWeek);

		if (foodsFromInternalStorage == null) {
			currentFoodsFuture = foodService.getFoodsBy(weekNumber,
					dayOfTheWeek);
		}
	}

	protected void updateUI(final IMainView mainView, List<RestaurantDay> foods) {
			mainView.hideLoadingIcon();
			if (foods != null) {
				mainView.setFoods(foods);
			} else {
				mainView.setFoods(new ArrayList<RestaurantDay>());
			}
	}

	private void updateUIFromWebService(final IMainView mainView) {
		List<RestaurantDay> foods = null;
		try {
			logger.debug("Setting foods from WebService...");
			foods = currentFoodsFuture.get();
		} catch (InterruptedException e) {
			logger.error("Unexpected exception", e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof FoodServiceException) {
				logger.debug("Food service is down or foods are unavailable.");
				mainView.notifyThatFoodsAreCurrentlyUnavailable();
				mainView.showRefreshButton();
			} else if (e.getCause() instanceof NoInternetConnectionException) {
				logger.debug("Device doesn't got Internet connection.");
				mainView.notifyThatDeviceHasNoInternetConnection();
				mainView.showRefreshButton();
			} else {
				logger.error("Unexpected error", e);
			}
		} finally {
			updateUI(mainView, foods);
		}
	}

	protected boolean hasInternetConnection() {
		return hasInternetConnection;
	}

	protected void setHasInternetConnection(boolean hasConnection) {
		hasInternetConnection = hasConnection;
	}

	@Override
	public IFoodService getFoodService() {
		return foodService;
	}

	@Override
	public void onDateChanged(IMainView mainView, int selectedWeekDay) {
		currentFoodsFuture = null;
		selectedDate = DateUtils.getDateInThisWeekBy(selectedDate,
				selectedWeekDay);		
		foodsFromInternalStorage = foodService.getFoodsFromInternalStorageBy(
				DateUtils.getWeekOfYear(selectedDate),
				DateUtils.getDayOfTheWeek(selectedDate));
		
		updateUI(mainView, foodsFromInternalStorage);
	}

	@Override
	public void viewIsReady() {
		if(foodsFromInternalStorage == null) {
			updateUIFromWebService(mainView);
		} else {			
			updateUI(mainView, foodsFromInternalStorage);
		}
	}

	@Override
	public void refreshFoods(IMainView mainView) {
		mainView.showLoadingIcon();
		getInitialFoodsFromService();
		updateUIFromWebService(mainView);
	}

}
