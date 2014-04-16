package fi.nottingham.mobilefood;

import static android.content.Context.LOCATION_SERVICE;

import javax.inject.Singleton;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import dagger.Module;
import dagger.Provides;
import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.INetworkStatusService;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module(library = true, injects = DaggerApplication.class)
public class AndroidModule {
	private DaggerApplication application;
	
	void setDaggerApplication(DaggerApplication application) {
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
	@ForApplication
	ConnectivityManager provideConnectivityManager() {
		return (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	@Provides
	@Singleton
	IFileSystemService provideFileSystemService() {
		return application;
	}
	
	@Provides
	@Singleton
	INetworkStatusService provideNetworkStatusService() {
		return application;
	}
}
