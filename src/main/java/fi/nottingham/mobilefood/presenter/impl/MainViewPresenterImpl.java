package fi.nottingham.mobilefood.presenter.impl;

import java.util.Date;

import javax.inject.Inject;

import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;

public class MainViewPresenterImpl implements IMainViewPresenter {
	private final IFoodService foodService;
	private Date selectedDate;

	@Inject
	public MainViewPresenterImpl(IFoodService foodService, Date timeNow) {
		this.foodService = foodService;
		selectedDate = DateUtils.getDateAtMidnight(new Date());
	}

	public void onViewCreation(IMainView mainView) {
		mainView.setFoods(selectedDate,
				foodService.getFoodsBy(DateUtils.getDayOfTheWeek(selectedDate)));
	}

	public IFoodService getFoodService() {
		return foodService;
	}

}
