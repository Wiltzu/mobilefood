package fi.nottingham.mobilefood.presenter.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import fi.nottingham.mobilefood.model.Food;
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

	public void onViewCreation(final IMainView mainView) {
		checkNotNull("mainView cannot be null",mainView);
		
		mainView.setDate(selectedDate);
		mainView.showLoadingIcon();
		
		//TODO: figure out a better solution
		final List<Food> foods = Lists.newArrayList();
		mainView.runInBackgroud(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				foods.addAll(foodService.getFoodsBy(DateUtils
						.getDayOfTheWeek(selectedDate)));
			}
		}, new Runnable() {	
			@Override
			public void run() {
				mainView.setFoods(foods);
			}
		});
	}

	public IFoodService getFoodService() {
		return foodService;
	}

}
