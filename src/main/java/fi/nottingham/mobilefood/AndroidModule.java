package fi.nottingham.mobilefood;

import android.content.Context;
import android.location.LocationManager;
import dagger.Module;
import dagger.Provides;
import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.IFoodService;

import javax.inject.Singleton;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module(library = true)
public class AndroidModule {
	private final DaggerApplication application;

	public AndroidModule(DaggerApplication application) {
		this.application = application;
	}

	/**
	 * Allow the application context to be injected but require that it be
	 * annotated with {@link ForApplication @Annotation} to explicitly
	 * differentiate it from an activity context.
	 */
	@Provides
	@Singleton
	@ForApplication
	Context provideApplicationContext() {
		return application;
	}

	@Provides
	@Singleton
	LocationManager provideLocationManager() {
		return (LocationManager) application.getSystemService(LOCATION_SERVICE);
	}

	@Provides
	@Singleton
	IFileSystemService provideFileSystemService() {
		return application;
	}
}
