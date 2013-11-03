package fi.nottingham.mobilefood;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.widget.TextView;
import fi.nottingham.mobilefood.util.InjectedTestRunner;

@RunWith(InjectedTestRunner.class)
public class HelloAndroidActivityTest {

	//@Inject
	//HelloAndroidActivity activityInTest;
	@InjectResource(R.string.hello_world)
	String caption;
	@InjectView(R.id.textfield_hello)
	TextView textView;
	
	@Before
	public void setUp() {
	}

	@Test
	public void test() {
		assertFalse(textView.getText() != caption);
	}

}
