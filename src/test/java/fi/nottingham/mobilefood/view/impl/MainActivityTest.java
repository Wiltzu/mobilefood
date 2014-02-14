package fi.nottingham.mobilefood.view.impl;

import static org.mockito.Mockito.verify;

import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

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
	}

	@Test
	public void testViewCreationNotifiesPresenter() {
		mainView = Robolectric.buildActivity(MainActivity.class).create().start().resume().get();
		verify(mainViewPresenter).onViewCreation(mainView);
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
