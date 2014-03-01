package fi.nottingham.mobilefood.view.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.google.common.collect.Lists;
import com.ximpleware.extended.xpath.VariableExpr;

import dagger.Module;
import dagger.Provides;
import fi.nottingham.mobilefood.MobilefoodModule;
import fi.nottingham.mobilefood.MobilefoodModules;
import fi.nottingham.mobilefood.acceptance.steps.MainViewSteps;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.view.IMainView;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

	IMainView mainView;
	@Mock
	IMainViewPresenter mainViewPresenter;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		MobilefoodModules.getModules().add(new TestModule());
		mainView = Robolectric.buildActivity(MainActivity.class).create()
				.start().resume().get();
	}

	@Test
	public void testViewCreationNotifiesPresenter() {
		verify(mainViewPresenter).onViewCreation(mainView);
	}

	@Test
	public void runInBackground_runsActionsInAndroidBackgroudThread() {
		final List<String> testList = Lists.newArrayList();
		Robolectric.getBackgroundScheduler().pause();
		mainView.runInBackgroud(new Runnable() {
			@Override
			public void run() {
				testList.add("test");
			}
		}, mock(Runnable.class));

		assertTrue(
				"list should have been empty because backgroound thread is paused",
				testList.isEmpty());
		Robolectric.runBackgroundTasks();
		assertFalse("list should no longer be empty, because tasks were run.",
				testList.isEmpty());
	}
	
	@Test
	public void runInBackground_runsUiUpdateInUIThread() {
		final List<String> testList = Lists.newArrayList();
		Robolectric.getUiThreadScheduler().pause();
		mainView.runInBackgroud(mock(Runnable.class), new Runnable() {
			@Override
			public void run() {
				testList.add("test");
			}
		});
		assertTrue(
				"list should have been empty because backgroound thread is paused",
				testList.isEmpty());
		Robolectric.runUiThreadTasks();
		assertFalse("list should no longer be empty, because ui thread tasks were run.",
				testList.isEmpty());
	}

	@Module(includes = { MobilefoodModule.class }, injects = MainViewSteps.class, overrides = true)
	class TestModule {
		@Provides
		@Singleton
		public IMainViewPresenter provideMainViewPresenter() {
			return mainViewPresenter;
		}
	}
}
