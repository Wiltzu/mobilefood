package fi.nottingham.mobilefood.presenter.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.exceptions.FoodServiceException;
import fi.nottingham.mobilefood.service.exceptions.NoInternetConnectionException;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;

public class MainViewPresenterImpl implements IMainViewPresenter {
	private final IFoodService foodService;
	private Date selectedDate;
	private List<RestaurantDay> currentFoods;
	
	private boolean hasInternetConnection = true;

	@Inject
	public MainViewPresenterImpl(IFoodService foodService, Date timeNow) {
		this.foodService = foodService;
		selectedDate = DateUtils.getDateAtMidnight(new Date());
		currentFoods = Lists.newArrayList();
	}

	@Override
	public void onViewCreation(final IMainView mainView) {
		checkNotNull("mainView cannot be null", mainView);

		mainView.setDate(selectedDate);

		if (currentFoods.isEmpty() || !hasInternetConnection) {
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
		//expect to have a internet connection
		setHasInternetConnection(true);
		
		int dayOfTheWeek = DateUtils.getDayOfTheWeek(selectedDate);
		int weekNumber = DateUtils.getWeekOfYear(selectedDate);
		currentFoods.clear();
		try {
			currentFoods.addAll(foodService.getFoodsBy(weekNumber, dayOfTheWeek));			
		} catch (NoInternetConnectionException e) {
			//TODO: log exception
			setHasInternetConnection(false);
		} catch(FoodServiceException e) {
			//TODO: implement
		}
	}
	
	protected void updateUI(final IMainView mainView) {
		mainView.setFoods(currentFoods);
		
		if(!hasInternetConnection()) {
			mainView.notifyThatDeviceHasNoInternetConnection();
			mainView.showRefreshButton();
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

}
