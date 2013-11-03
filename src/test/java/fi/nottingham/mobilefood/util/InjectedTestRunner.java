package fi.nottingham.mobilefood.util;

import org.junit.runners.model.InitializationError;

import roboguice.RoboGuice;

import com.google.inject.Injector;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

public class InjectedTestRunner extends RobolectricTestRunner {
   
	public InjectedTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override public void prepareTest(Object test) {
        Injector injector = RoboGuice.getInjector(Robolectric.application);
        injector.injectMembers(test);
    }
}