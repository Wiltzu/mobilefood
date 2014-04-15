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

//TODO: refactor this class and make tests for it
public class MainViewPresenterImpl implements IMainViewPresenter, ViewIsReadyListener {
	private final Logger logger = Logger.getLogger(this.getClass());

	private final IFoodService foodService;
	private final INetworkStatusService networkStatusService;
	private Provider<Date> timeNow;
	private Date selectedDate;
	private Future<List<RestaurantDay>> currentFoodsFuture;
	
	private boolean hasInternetConnection = true;
	
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
		//TODO: reconsider can we set this here 
		mainView.setAvailableWeekDays(DateUtils.getRestOfTheWeeksDayNumbersFrom(timeNow.get()));
		
		if(savedSelectedWeekDay != null) {
			selectedDate = DateUtils.getDateInThisWeekBy(selectedDate, savedSelectedWeekDay);
			mainView.setSelectedDate(savedSelectedWeekDay);					
		}

		mainView.showLoadingIcon();
		currentFoodsFuture = getFoodsFromService();
	}
	
	protected Future<List<RestaurantDay>> getFoodsFromService() {
		
		return pool.submit(new Callable<List<RestaurantDay>>() {
			@Override
			public List<RestaurantDay> call() throws Exception {
				int dayOfTheWeek = DateUtils.getDayOfTheWeek(selectedDate);
				int weekNumber = DateUtils.getWeekOfYear(selectedDate);
				
				logger.debug("Fething foods from internal storage.");
				List<RestaurantDay> foods = foodService.getFoodsFromInternalStorageBy(weekNumber, dayOfTheWeek);
				
				if(foods == null && (hasInternetConnection = networkStatusService.isConnectedToInternet())) {
					logger.debug("Fething foods from service.");
					foods = foodService.getFoodsBy(weekNumber, dayOfTheWeek);
				}
				return foods;
			}
			
		});
	}
	
	protected void updateUI(final IMainView mainView) {
		//TODO: Refactor this!!
		List<RestaurantDay> foods = null;
		try {
			if(currentFoodsFuture != null) {
				logger.debug("Getting foods from FoodService...");
				foods = currentFoodsFuture.get();				
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			if(e.getCause() instanceof FoodServiceException) {
				logger.debug("Food service is down or foods are unavailable.");
				mainView.notifyThatFoodsAreCurrentlyUnavailable();
				mainView.showRefreshButton();
			} else {
				logger.error("Unexpected error", e);
			}
		}		
		
		mainView.hideLoadingIcon();
		if(foods != null) {
			mainView.setFoods(foods);
		} else {
			mainView.setFoods(new ArrayList<RestaurantDay>());
			
			if(!hasInternetConnection()) {
				logger.debug("Device doesn't got Internet connection.");
				mainView.notifyThatDeviceHasNoInternetConnection();
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

	@Override
	public void onDateChanged(IMainView mainView, int selectedWeekDay) {
		mainView.showLoadingIcon();
		selectedDate = DateUtils.getDateInThisWeekBy(selectedDate, selectedWeekDay);
		currentFoodsFuture = getFoodsFromService();
		updateUI(mainView);
	}

	@Override
	public void viewIsReady() {
		updateUI(mainView);
	}

	@Override
	public void refreshFoods(IMainView mainView) {
		mainView.showLoadingIcon();
		currentFoodsFuture = getFoodsFromService();
		updateUI(mainView);
	}

}
