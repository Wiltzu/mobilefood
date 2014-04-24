package fi.nottingham.mobilefood.view.impl;

import java.util.List;

import javax.inject.Inject;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import fi.nottingham.mobilefood.DaggerBaseActivity;
import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.view.IMainView;
import fi.nottingham.mobilefood.view.ViewIsReadyListener;
import fi.nottingham.mobilefood.view.adapter.TabsAdapter;
import fi.nottingham.mobilefood.view.adapter.TabsAdapter.TabInfo;

public class MainActivity extends DaggerBaseActivity implements IMainView {

	private static final String LAST_WEEK_DAY_SELECTION = "lastWeekDaySelection";
	private static final String TAG = "MainActivity";

	private ProgressBar mProgressBar;
	private Button mRefreshButton;
	private ActionBar mActionbar;
	private ViewPager mViewPager;

	private TabsAdapter tabsAdapter;

	@Inject
	IMainViewPresenter presenter;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_parent);
		Log.d(TAG, "onCreate called");

		mViewPager = (ViewPager) findViewById(R.id.slide_view_pager);

		mActionbar = getSupportActionBar();
		mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mProgressBar = (ProgressBar) findViewById(R.id.progressbar_indeterminate);
		mRefreshButton = (Button) findViewById(R.id.main_refresh_button);

		mRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "Refresh button clicked.");
				mRefreshButton.setVisibility(View.INVISIBLE);
				presenter.refreshFoods(MainActivity.this);
			}
		});

		if (OneDayLunchesFragment.listener == null) {
			OneDayLunchesFragment.listener = (ViewIsReadyListener) presenter;
		}

		Integer savedSelectedWeekDay = null;
		if (savedInstanceState != null) {
			savedSelectedWeekDay = savedInstanceState
					.getInt(LAST_WEEK_DAY_SELECTION);
		}
		presenter.onViewCreation(this, savedSelectedWeekDay);

		/*
		 * mViewPager .setOnPageChangeListener(new
		 * ViewPager.OnPageChangeListener() {
		 * 
		 * @Override public void onPageSelected(int position) {
		 * mActionbar.setSelectedNavigationItem(position); //
		 * presenter.onDateChanged(MainActivity.this, // (Integer)
		 * mActionbar.getTabAt(position) // .getTag()); }
		 * 
		 * @Override public void onPageScrolled(int arg0, float arg1, int arg2)
		 * { }
		 * 
		 * @Override public void onPageScrollStateChanged(int arg0) { } }); //
		 * just cache all days mViewPager.setOffscreenPageLimit(7);
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(fi.nottingham.mobilefood.R.menu.main,
		// menu);
		// return true;
		return false;
	}

	public IMainViewPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setFoods(List<RestaurantDay> foodsByRestaurant) {
		//TODO: is this method needed any longer??
//		Log.d(TAG, "setFoods called");
//		OneDayLunchesFragment currentLunchFragment = mLunchFragmentsMap
//				.get(mActionbar.getSelectedTab().getPosition());
//		if (currentLunchFragment != null) {
//			currentLunchFragment.setFoods(foodsByRestaurant);
//		}
	}

	@Override
	public void showLoadingIcon() {
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.bringToFront();
	}

	@Override
	public void setAvailableWeekDays(int[] availableWeekDays) {
		if (mActionbar.getTabCount() == 0) {
			Log.d(TAG, "Resetting week day tabs...");
			tabsAdapter = new TabsAdapter(this, mViewPager);

			String[] weekDayNames = getResources().getStringArray(
					R.array.week_days);
			for (int weekDayNumber : availableWeekDays) {
				Bundle args = new Bundle();
				args.putInt("weekDay", weekDayNumber);
				tabsAdapter.addTab(
						mActionbar.newTab()
								.setText(weekDayNames[weekDayNumber]),
						OneDayLunchesFragment.class, args);
			}
		}
	}

	@Override
	public void notifyThatDeviceHasNoInternetConnection() {
		Toast.makeText(this, getText(R.string.no_internet), Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public void notifyThatFoodsAreCurrentlyUnavailable() {
		Toast.makeText(this, getText(R.string.no_foods_available),
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void showRefreshButton() {
		mRefreshButton.setVisibility(View.VISIBLE);
		mRefreshButton.bringToFront();
	}

	@Override
	public void hideLoadingIcon() {
		mProgressBar.setVisibility(View.INVISIBLE);
		mProgressBar.invalidate();
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(LAST_WEEK_DAY_SELECTION, ((TabInfo) mActionbar
				.getSelectedTab().getTag()).args.getInt("weekDay"));
		super.onSaveInstanceState(outState);
	}

	@Override
	public void setSelectedDate(int dayOfTheWeek) {
		for (int tabIndex = 0; tabIndex < mActionbar.getTabCount(); tabIndex++) {
			Tab currentTab = mActionbar.getTabAt(tabIndex);
			if (dayOfTheWeek == (Integer) ((TabInfo) currentTab.getTag()).args
					.getInt("weekDay")) {
				mActionbar.selectTab(currentTab);
			}
		}
	}
}
