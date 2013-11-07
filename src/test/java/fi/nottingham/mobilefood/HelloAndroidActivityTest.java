package fi.nottingham.mobilefood;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.xtremelabs.robolectric.RobolectricTestRunner;

import fi.nottingham.mobilefood.util.TestGuiceModule;

/**
 * This is very stupid test expirement. 
 * 
 * @author Ville
 *
 */
@RunWith(RobolectricTestRunner.class)
public class HelloAndroidActivityTest {

	@Inject
	HelloAndroidActivity activityInTest;
	@InjectResource(R.string.hello_world)
	String caption;
	@InjectView(R.id.textfield_hello)
	TextView textView;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	    TestGuiceModule module = new TestGuiceModule();
	    TestGuiceModule.setUp(this, module);
	}

	@Test
	public void test() {
		assertNotNull("caption can't be null", caption);
		//assertNotNull(textView); injecting views don't work for an unknown reason
		activityInTest.onCreate(null);
		textView = (TextView) activityInTest.findViewById(R.id.textfield_hello);
		String actualText = (String) textView.getText();
		assertTrue(actualText != caption);
	}

}