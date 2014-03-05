package fi.nottingham.mobilefood.presenter.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;

public class MainViewPresenterImpl implements IMainViewPresenter {
	private final IFoodService foodService;
	private Date selectedDate;
	private List<RestaurantDay> currentFoods;

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

		if (currentFoods.isEmpty()) {
			mainView.showLoadingIcon();

			mainView.runInBackgroud(new Runnable() {
				@Override
				public void run() {
					updateFoodsFromService();
				}
			}, new Runnable() {
				@Override
				public void run() {
					mainView.setFoods(currentFoods);
				}
			});
		} else {
			mainView.setFoods(currentFoods);
		}
	}

	/**
	 * Synchronized manner gets new foods from service
	 */
	protected synchronized void updateFoodsFromService() {
		int dayOfTheWeek = DateUtils.getDayOfTheWeek(selectedDate);
		int weekNumber = DateUtils.getWeekOfYear(selectedDate);
		currentFoods.clear();
		currentFoods.addAll(foodService.getFoodsBy(weekNumber, dayOfTheWeek));
	}

	@Override
	public IFoodService getFoodService() {
		return foodService;
	}

}
