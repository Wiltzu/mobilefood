package fi.nottingham.mobilefood.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.AbsListView;

import com.robotium.solo.Solo;

import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.view.impl.MainActivity;

public class ITMainActivityRobotiumTest extends ActivityInstrumentationTestCase2<MainActivity> {

	public ITMainActivityRobotiumTest() {
		super(MainActivity.class);
	}
	
	public void testSimple() throws InterruptedException {
		Solo solo = new Solo(getInstrumentation(), getActivity());
		solo.scrollDownList((AbsListView) getActivity().findViewById(R.id.listview_foods));
		assertNotNull(solo.getText("Mikro", false));
		Thread.sleep(2000);
		solo.finishOpenedActivities();
	}

}
