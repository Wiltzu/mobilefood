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
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;
import fi.nottingham.mobilefood.view.ViewIsReadyListener;

public class MainViewPresenterImpl implements IMainViewPresenter, ViewIsReadyListener {
	private final Logger logger = Logger.getLogger(this.getClass());

	private final IFoodService foodService;
	private final INetworkStatusService networkStatusService;
	private Provider<Date> timeNow;
	private Date selectedDate;
	private List<RestaurantDay> currentFoods;
	private Future<List<RestaurantDay>> currentFoodsFuture;
	
	private boolean hasInternetConnection = true;
	private FoodServiceException foodServiceException = null;
	
	private final ExecutorService pool = Executors.newFixedThreadPool(2);
	private IMainView mainView;

	@Inject
	public MainViewPresenterImpl(IFoodService foodService, Provider<Date> timeNow, INetworkStatusService networkStatusService) {
		this.foodService = foodService;
		this.networkStatusService = networkStatusService;
		this.timeNow = timeNow;
		this.selectedDate = timeNow.get();
	}

	@Override
	public void onViewCreation(final IMainView mainView, @Nullable Integer savedSelectedWeekDay) {
		checkNotNull("mainView cannot be null", mainView);
		this.mainView = mainView;
		mainView.setAvailableWeekDays(DateUtils.getRestOfTheWeeksDayNumbersFrom(timeNow.get()));
		
		if(savedSelectedWeekDay != null) {
			selectedDate = DateUtils.getDateInThisWeekBy(selectedDate, savedSelectedWeekDay);
			mainView.setSelectedDate(savedSelectedWeekDay);					
		}

		if (currentFoods == null) {			
			//updateFoodsInBackground(mainView);
			currentFoodsFuture = foodsFromService();
		} else {
			mainView.setFoods(currentFoods);
		}
	}

	protected synchronized void updateFoodsInBackground(final IMainView mainView) {
		mainView.showLoadingIcon();
		
		mainView.runInBackgroud(new Runnable() {
			@Override
			public void run() {
				fetchFoodsFromService();
			}
		}, new Runnable() {
			@Override
			public void run() {
				updateUI(mainView);
			}
		});
	}
	
	protected Future<List<RestaurantDay>> foodsFromService() {
		
		return pool.submit(new Callable<List<RestaurantDay>>() {
			@Override
			public List<RestaurantDay> call() throws Exception {
				foodServiceException = null;
				int dayOfTheWeek = DateUtils.getDayOfTheWeek(selectedDate);
				int weekNumber = DateUtils.getWeekOfYear(selectedDate);
				
				logger.debug("Fething foods from internal storage.");
				List<RestaurantDay> foods = foodService.getFoodsFromInternalStorageBy(weekNumber, dayOfTheWeek);
				
				if(foods == null && (hasInternetConnection = networkStatusService.isConnectedToInternet())) {
					try {
						logger.debug("Fething foods from service.");
						foods = foodService.getFoodsBy(weekNumber, dayOfTheWeek);
					} catch(FoodServiceException e) {
						foodServiceException = e;
					}	
				}
				return foods;
			}
			
		});
	}

	protected void fetchFoodsFromService() {		
		foodServiceException = null;
		int dayOfTheWeek = DateUtils.getDayOfTheWeek(selectedDate);
		int weekNumber = DateUtils.getWeekOfYear(selectedDate);
		
		logger.debug("Fething foods from internal storage.");
		currentFoods = foodService.getFoodsFromInternalStorageBy(weekNumber, dayOfTheWeek);
		
		if(currentFoods == null && (hasInternetConnection = networkStatusService.isConnectedToInternet())) {
			try {
				logger.debug("Fething foods from service.");
				currentFoods = foodService.getFoodsBy(weekNumber, dayOfTheWeek);
			} catch(FoodServiceException e) {
				foodServiceException = e;
			}	
		}
		
	}
	
	protected void updateUI(final IMainView mainView) {
		mainView.hideLoadingIcon();
		if (currentFoods != null) {			
			mainView.setFoods(currentFoods);
		} else {
			mainView.setFoods(new ArrayList<RestaurantDay>());
			
			if(!hasInternetConnection()) {
				logger.debug("Device doesn't got Internet connection.");
				mainView.notifyThatDeviceHasNoInternetConnection();
				mainView.showRefreshButton();
			
			} else if (foodServiceCallResultedInException()) {
				logger.debug("Food service is down or foods are unavailable.");
				mainView.notifyThatFoodsAreCurrentlyUnavailable();
				mainView.showRefreshButton();
			}
		}
		
	}
	
	protected void updateUI2(final IMainView mainView) {
		mainView.hideLoadingIcon();
		List<RestaurantDay> foods = null;
		try {
			foods = currentFoodsFuture.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		if(foods != null) {
			mainView.setFoods(foods);
		} else {
			mainView.setFoods(new ArrayList<RestaurantDay>());
			
			if(!hasInternetConnection()) {
				logger.debug("Device doesn't got Internet connection.");
				mainView.notifyThatDeviceHasNoInternetConnection();
				mainView.showRefreshButton();
			
			} else if (foodServiceCallResultedInException()) {
				logger.debug("Food service is down or foods are unavailable.");
				mainView.notifyThatFoodsAreCurrentlyUnavailable();
				mainView.showRefreshButton();
			}
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

	public boolean foodServiceCallResultedInException() {
		return foodServiceException != null;
	}

	@Override
	public void onDateChanged(IMainView mainView, int selectedWeekDay) {
		selectedDate = DateUtils.getDateInThisWeekBy(selectedDate, selectedWeekDay);
		currentFoodsFuture = foodsFromService();
		updateUI2(mainView);
		//updateFoodsInBackground(mainView);
	}

	@Override
	public void viewIsReady() {
		updateUI2(mainView);
	}

}
