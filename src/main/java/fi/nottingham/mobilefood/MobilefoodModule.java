package fi.nottingham.mobilefood;

import java.util.Date;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import dagger.Module;
import dagger.Provides;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.presenter.impl.MainViewPresenterImpl;
import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.service.impl.FoodServiceImpl;
import fi.nottingham.mobilefood.view.impl.MainActivity;

@Module(
    injects = {MainActivity.class, MainViewPresenterImpl.class},
    complete = false
)
public class MobilefoodModule {
	@Provides
	@Singleton
	IFoodService provideFoodService(Config config, IFileSystemService fileSystemService) {
		return new FoodServiceImpl(config.getString("mobilefood.foodservice.url"), fileSystemService);
	}
	
	@Provides
	@Singleton
	IMainViewPresenter provideMainViewPresenter(IFoodService foodService, Provider<Date> timeNow, INetworkStatusService networkStatusService) {
		return new MainViewPresenterImpl(foodService, timeNow, networkStatusService);
	}
	
	@Provides
	Date provideDate() {
		return new Date();
	}
	
	@Provides
	@Singleton
	Config provideConfigurations() {
		return ConfigFactory.load();
	}
}
