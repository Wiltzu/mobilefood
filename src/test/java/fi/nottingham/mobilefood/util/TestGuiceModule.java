package fi.nottingham.mobilefood.util;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.robolectric.Robolectric;

import roboguice.RoboGuice;
import roboguice.config.DefaultRoboModule;
import roboguice.inject.ContextSingleton;
import roboguice.inject.RoboInjector;
import android.app.Activity;
import android.app.Application;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;

public class TestGuiceModule extends AbstractModule {

	private HashMap<Class<?>, Object> bindings;

	public TestGuiceModule() {
		bindings = new HashMap<Class<?>, Object>();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void configure() {
		//bind dummy Android Activity provider because it can't be null
		bind(Activity.class).toProvider(DummyActivityProvider.class).in(ContextSingleton.class);
		
		Set<Entry<Class<?>, Object>> entries = bindings.entrySet();
		for (Entry<Class<?>, Object> entry : entries) {
			bind((Class<Object>) entry.getKey()).toInstance(entry.getValue());
		}
	}

	public void addBinding(Class<?> type, Object object) {
		bindings.put(type, object);
	}

	public static void setUp(Object testObject, TestGuiceModule module) {
		Module roboGuiceModule = RoboGuice
				.newDefaultRoboModule(Robolectric.application);
		Module testModule = Modules.override(roboGuiceModule).with(module);
		RoboGuice.setBaseApplicationInjector(Robolectric.application,
				RoboGuice.DEFAULT_STAGE, testModule);
		RoboInjector injector = RoboGuice.getInjector(Robolectric.application);
		injector.injectMembers(testObject);
	}

	public static void tearDown() {
		RoboGuice.util.reset();
		Application app = Robolectric.application;
		DefaultRoboModule defaultModule = RoboGuice.newDefaultRoboModule(app);
		RoboGuice.setBaseApplicationInjector(app, RoboGuice.DEFAULT_STAGE,
				defaultModule);
	}
}

class DummyActivityProvider implements Provider<Activity> {

	public Activity get() {
		return Robolectric.buildActivity(Activity.class).create().get();
	}

}
