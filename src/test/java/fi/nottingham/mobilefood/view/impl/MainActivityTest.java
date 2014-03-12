package fi.nottingham.mobilefood.view.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.inject.Singleton;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowToast;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.google.common.collect.Lists;

import dagger.Module;
import dagger.Provides;
import fi.nottingham.mobilefood.MobilefoodModule;
import fi.nottingham.mobilefood.MobilefoodModules;
import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.service.IFileSystemService;
import fi.nottingham.mobilefood.service.INetworkStatusService;
import fi.nottingham.mobilefood.view.IMainView;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

	IMainView mainView;
	@Mock
	IMainViewPresenter mainViewPresenter;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		MobilefoodModules.getModules().add(new TestModule());
		mainView = Robolectric.buildActivity(MainActivity.class).create()
				.start().resume().get();
	}

	@Test
	public void onViewCreation_notifiesPresenter() {
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
	
	@Test
	@Config(qualifiers = "fi")
	public void notifyThatDeviceHasNoInternetConnection_createsToastWithCorrectText() {
		mainView.notifyThatDeviceHasNoInternetConnection();
		
		CharSequence expectedToastText = ((Activity) mainView).getText(R.string.no_internet);
		ShadowHandler.idleMainLooper();
	    assertThat(ShadowToast.getTextOfLatestToast(), Matchers.equalTo(expectedToastText));
	}
	
	@Test
	public void showRefreshButton_showsRefreshButtonAndHidesFoods() {
		mainView.showRefreshButton();
		
		Button refreshButton = (Button) ((Activity) mainView).findViewById(R.id.main_refresh_button);
		
		ShadowHandler.idleMainLooper();
		assertThat(refreshButton.getVisibility(), Matchers.equalTo(View.VISIBLE));
	}
	
	@Test
	public void clickingRefreshButton_triesToReloadFoods() {
		Button refreshButton = (Button) ((Activity) mainView).findViewById(R.id.main_refresh_button);
		
		refreshButton.performClick();
		
		verify(mainViewPresenter, Mockito.times(2)).onViewCreation(mainView);
	}
	
	@Test
	public void notifyThatFoodsAreCurrentlyUnavailable_createsToastWithCorrectText() {
		mainView.notifyThatFoodsAreCurrentlyUnavailable();
		
		CharSequence expectedToastText = ((Activity) mainView).getText(R.string.no_foods_available);
		ShadowHandler.idleMainLooper();
	    assertThat(ShadowToast.getTextOfLatestToast(), Matchers.equalTo(expectedToastText));
	}

	@Module(includes = { MobilefoodModule.class}, overrides = true, library = true)
	class TestModule {
		@Provides
		@Singleton
		public IMainViewPresenter provideMainViewPresenter() {
			return mainViewPresenter;
		}
		
		@Provides
		@Singleton
		public IFileSystemService provideFileSystemService() {
			return mock(IFileSystemService.class);
		}
		
		@Provides
		@Singleton
		public INetworkStatusService provideNetworkStatusService() {
			return mock(INetworkStatusService.class);
		}
	}
}
