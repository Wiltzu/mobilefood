package fi.nottingham.mobilefood.presenter.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;

public class MainViewPresenterImpl implements IMainViewPresenter {
	private final IFoodService foodService;
	private final INetworkStatusService networkStatusService;
	private Date selectedDate;
	private List<RestaurantDay> currentFoods;
	
	private boolean hasInternetConnection = true;
	private FoodServiceException foodServiceException = null;
	
	private final Logger logger = Logger.getLogger(this.getClass());

	@Inject
	public MainViewPresenterImpl(IFoodService foodService, Date timeNow, INetworkStatusService networkStatusService) {
		this.foodService = foodService;
		this.networkStatusService = networkStatusService;
		selectedDate = DateUtils.getDateAtMidnight(new Date());
	}

	@Override
	public void onViewCreation(final IMainView mainView) {
		checkNotNull("mainView cannot be null", mainView);

		mainView.setDate(selectedDate);

		if (currentFoods == null) {
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
		} else {
			mainView.setFoods(currentFoods);
		}
	}

	/**
	 * gets foods from service in synchronized way
	 */
	protected synchronized void fetchFoodsFromService() {		
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

}
