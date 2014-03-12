package fi.nottingham.mobilefood.view;

import java.util.Date;
import java.util.List;

import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;

public interface IMainView {
	
	IMainViewPresenter getPresenter();

	void setFoods(List<RestaurantDay> foodsByRestaurant);

	void runInBackgroud(Runnable backgroundTask, Runnable uiUpdateTask);

	void showLoadingIcon();

	void setDate(Date date);

	void notifyThatDeviceHasNoInternetConnection();

	void notifyThatFoodsAreCurrentlyUnavailable();
	
	void showRefreshButton();

}
