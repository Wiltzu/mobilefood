package fi.nottingham.mobilefood.presenter;

import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.view.IMainView;

public interface IMainViewPresenter {
	
	void onViewCreation(IMainView mainview);

	IFoodService getFoodService();
}
