package fi.nottingham.mobilefood.view.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Maps;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
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

public class MainActivity extends DaggerBaseActivity implements IMainView,
		TabListener {

	public class LunchDayPagerAdapter extends FragmentStatePagerAdapter {
		private final ActionBar actionBar;

		public LunchDayPagerAdapter(FragmentManager fm, ActionBar actionBar) {
			super(fm);
			this.actionBar = checkNotNull(actionBar, "ActionBar cannot be null");
		}

		@Override
		public int getCount() {
			return actionBar.getTabCount();
		}

		@Override
		public Fragment getItem(int position) {
			Log.d("LunchDayPagerAdapter", "fragment created");
			OneDayLunchesFragment lunchFragment = new OneDayLunchesFragment();
			lunchFragment.setListener((ViewIsReadyListener) presenter);
			mLunchFragmentsMap.put(position, lunchFragment);
			return lunchFragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			mLunchFragmentsMap.remove(position);
			super.destroyItem(container, position, object);
		}

	}

	private static final String LAST_WEEK_DAY_SELECTION = "lastWeekDaySelection";
	private static final String TAG = "MainActivity";

	private ProgressBar mProgressBar;
	private Button mRefreshButton;
	private ActionBar mActionbar;
	private ViewPager mViewPager;

	private Map<Integer, OneDayLunchesFragment> mLunchFragmentsMap = Maps.newHashMap();

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
		setContentView(R.layout.main_activity_parent);

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

		Integer savedSelectedWeekDay = null;
		if (savedInstanceState != null) {
			savedSelectedWeekDay = savedInstanceState
					.getInt(LAST_WEEK_DAY_SELECTION);
		}
		presenter.onViewCreation(this, savedSelectedWeekDay);
		

		mViewPager
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						mActionbar.setSelectedNavigationItem(position);
						presenter.onDateChanged(MainActivity.this,
								(Integer) mActionbar.getTabAt(position)
										.getTag());
					}
					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
					}
					@Override
					public void onPageScrollStateChanged(int arg0) {
					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(fi.nottingham.mobilefood.R.menu.main, menu);
		//return true;
		return false;
	}

	public IMainViewPresenter getPresenter() {
		return presenter;
	}

	public void setFoods(List<RestaurantDay> foodsByRestaurant) {
		OneDayLunchesFragment currentLunchFragment = mLunchFragmentsMap
				.get(mActionbar.getSelectedTab().getPosition());
		if (currentLunchFragment != null) {
			currentLunchFragment.setFoods(foodsByRestaurant);
		}
	}

	@Override
	public void showLoadingIcon() {
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.bringToFront();
	}

	@Override
	public void setAvailableWeekDays(int[] availableWeekDays) {
		if (mActionbar.getTabCount() == 0) {
			String[] weekDayNames = getResources().getStringArray(
					R.array.week_days);
			for (int weekDayNumber : availableWeekDays) {
				mActionbar.addTab(mActionbar.newTab()
						.setText(weekDayNames[weekDayNumber])
						.setTag(weekDayNumber).setTabListener(this));
			}
			mViewPager.setAdapter(new LunchDayPagerAdapter(
					getSupportFragmentManager(), mActionbar));
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
	public void runInBackgroud(final Runnable backgroundTask,
			final Runnable uiUpdateTask) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				Log.d(TAG, "Running task in background thread...");
				backgroundTask.run();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Log.d(TAG, "Running ui update task in main thread...");
				uiUpdateTask.run();
			}

		}.execute();
	}

	@Override
	public void hideLoadingIcon() {
		if (mProgressBar.isShown()) {
			mProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		mViewPager.setCurrentItem(tab.getPosition());
		presenter.onDateChanged(this, (Integer) tab.getTag());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(LAST_WEEK_DAY_SELECTION, (Integer) mActionbar
				.getSelectedTab().getTag());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void setSelectedDate(int dayOfTheWeek) {
		for (int tabIndex = 0; tabIndex < mActionbar.getTabCount(); tabIndex++) {
			Tab currentTab = mActionbar.getTabAt(tabIndex);
			if (dayOfTheWeek == (Integer) currentTab.getTag()) {
				mActionbar.selectTab(currentTab);
			}
		}
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// NOT NEEDED
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// NOT NEEDED EITHER
	}
}
