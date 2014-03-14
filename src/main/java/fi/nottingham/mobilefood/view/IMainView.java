package fi.nottingham.mobilefood.view;

import java.util.List;

import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;

public interface IMainView {
	
	IMainViewPresenter getPresenter();

	void setFoods(List<RestaurantDay> foodsByRestaurant);

	void runInBackgroud(Runnable backgroundTask, Runnable uiUpdateTask);

	void showLoadingIcon();

	void notifyThatDeviceHasNoInternetConnection();

	void notifyThatFoodsAreCurrentlyUnavailable();
	
	void showRefreshButton();

	void setAvailableWeekDays(int[] expectedWeekDays);

	void setSelectedDate(int dayOfTheWeek);

}
