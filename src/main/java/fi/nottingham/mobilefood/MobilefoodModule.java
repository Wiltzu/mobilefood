package fi.nottingham.mobilefood;

import java.util.Date;

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
	IMainViewPresenter provideMainViewPresenter(IFoodService foodService, Date timeNow) {
		return new MainViewPresenterImpl(foodService, timeNow);
	}
	
	@Provides
	Date provideDate() {
		return new Date();
	}
}
