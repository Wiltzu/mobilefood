package fi.nottingham.mobilefood;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.presenter.impl.MainViewPresenterImpl;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.impl.FoodServiceImpl;
import fi.nottingham.mobilefood.view.impl.MainActivity;

@Module(
    injects = {MainActivity.class, MainViewPresenterImpl.class},
    complete = false
)
public class MobilefoodModule {
	@Provides
	@Singleton
	IFoodService provideFoodService() {
		return new FoodServiceImpl();
	}
	
	@Provides
	@Singleton
	IMainViewPresenter provideMainViewPresenter(IFoodService foodService) {
		return new MainViewPresenterImpl(foodService);
	}
}
