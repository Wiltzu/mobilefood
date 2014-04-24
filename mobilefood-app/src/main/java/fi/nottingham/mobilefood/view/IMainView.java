package fi.nottingham.mobilefood.view;

import fi.nottingham.mobilefood.presenter.IMainViewPresenter;

public interface IMainView {
	
	IMainViewPresenter getPresenter();

	void showLoadingIcon();

	void notifyThatDeviceHasNoInternetConnection();

	void notifyThatFoodsAreCurrentlyUnavailable();
	
	void showRefreshButton();

	void setAvailableWeekDays(int[] expectedWeekDays);

	void setSelectedDate(int dayOfTheWeek);

	void hideLoadingIcon();

}
