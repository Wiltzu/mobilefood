package fi.nottingham.mobilefood.presenter;

import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.view.IMainView;

public interface IMainViewPresenter {
	
	void onViewCreation(IMainView mainview, Integer savedTabSelection);

	IFoodService getFoodService();

	void onDateChanged(IMainView mainView, int selectedWeekDay);

	void refreshFoods(IMainView mainView);
}
