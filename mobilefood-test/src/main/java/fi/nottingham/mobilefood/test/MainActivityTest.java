package fi.nottingham.mobilefood.test;

import org.junit.Test;

import android.test.ActivityInstrumentationTestCase2;
import fi.nottingham.mobilefood.view.impl.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity>{

	public MainActivityTest(Class<MainActivity> activityClass) {
		super(activityClass);
	}
	
	@Test
	public void test() {
		assertNotNull(getActivity());
	}

}
