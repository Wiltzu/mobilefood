package fi.nottingham.mobilefood;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.robolectric.RobolectricTestRunner;

import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
//@CucumberOptions(format = "pretty")
public class CucumberTest {
	
	public static Runner runner;

	@BeforeClass
	public static void testCucumberStories() throws Exception {
		runner = new RobolectricTestRunner(CucumberTest.class);
		runner.run(new RunNotifier());
	}
	
	@Test
	public void dummyTest() {
	}
}
