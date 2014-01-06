package fi.nottingham.mobilefood;

import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import fi.nottingham.mobilefood.steps.MainViewSteps;

@RunWith(RobolectricTestRunner.class)
public class MainViewTest extends JUnitStory {
	
	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new MainViewSteps());
	}
}
