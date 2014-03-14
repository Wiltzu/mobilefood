package fi.nottingham.mobilefood.view.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Joiner;

import fi.nottingham.mobilefood.DaggerBaseActivity;
import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.view.IMainView;

public class MainActivity extends DaggerBaseActivity implements IMainView,
		TabListener {
	private static final String LAST_WEEK_DAY_SELECTION = "lastWeekDaySelection";
	private static final String TAG = "MainActivity";

	private ListView mFoodsTV;
	private ProgressBar mProgressBar;
	private Button mRefreshButton;
	private ActionBar mActionbar;

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
		setContentView(R.layout.activity_main);

		mFoodsTV = (ListView) findViewById(R.id.listview_foods);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar_indeterminate);
		mRefreshButton = (Button) findViewById(R.id.main_refresh_button);

		mActionbar = getSupportActionBar();
		mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "Refresh button clicked.");
				mRefreshButton.setVisibility(View.INVISIBLE);
				presenter.onViewCreation(MainActivity.this, null);
			}
		});
		
		Integer savedSelectedWeekDay = null;
		if(savedInstanceState != null) {
			savedSelectedWeekDay = savedInstanceState.getInt(LAST_WEEK_DAY_SELECTION);
		}

		presenter.onViewCreation(this, savedSelectedWeekDay);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(fi.nottingham.mobilefood.R.menu.main, menu);
		return true;
	}

	public IMainViewPresenter getPresenter() {
		return presenter;
	}

	public void setFoods(List<RestaurantDay> foodsByRestaurant) {
		checkNotNull(foodsByRestaurant, "foodsByRestaurant cannot be null");
		mFoodsTV.setAdapter(new RestaurantDayViewAdapter(this,
				foodsByRestaurant));
	}

	@Override
	public void showLoadingIcon() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void setAvailableWeekDays(int[] availableWeekDays) {
		if(mActionbar.getTabCount() == 0) {
			String[] weekDayNames = getResources()
					.getStringArray(R.array.week_days);
			for (int weekDayNumber : availableWeekDays) {
				mActionbar.addTab(mActionbar.newTab()
						.setText(weekDayNames[weekDayNumber]).setTag(weekDayNumber)
						.setTabListener(this));
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

				if (mProgressBar.isShown()) {
					mProgressBar.setVisibility(View.INVISIBLE);
				}
			}
		}.execute();
	}

	class RestaurantDayViewAdapter extends ArrayAdapter<RestaurantDay> {
		private static final String TAG = "RestaurantDayViewAdapter";

		public RestaurantDayViewAdapter(Context context,
				List<RestaurantDay> items) {
			super(context, R.layout.restaurant_item, items);
			Log.d(TAG, "Added Following RestaurantDays" + items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View restaurantDayView = inflater.inflate(R.layout.restaurant_item,
					parent, false);

			ImageView chainLogo = (ImageView) restaurantDayView
					.findViewById(R.id.restaurant_item_chain_logo);
			TextView textView = (TextView) restaurantDayView
					.findViewById(R.id.restaurant_item_restaurant_name);
			LinearLayout lunchLayout = (LinearLayout) restaurantDayView
					.findViewById(R.id.restaurant_item_food_layout);

			RestaurantDay restaurantDay = getItem(position);

			chainLogo.setImageResource(R.drawable.unica_logo);
			textView.setText(restaurantDay.getRestaurantName());

			for (Food lunch : restaurantDay.getLunches()) {
				View lunchlayoutItem = inflater.inflate(R.layout.food_item,
						null, false);
				((TextView) lunchlayoutItem.findViewById(R.id.food_item_name))
						.setText(lunch.getFoodName());
				((TextView) lunchlayoutItem.findViewById(R.id.food_item_diets))
						.setText(lunch.getDiets());
				((TextView) lunchlayoutItem.findViewById(R.id.food_item_prices))
						.setText(Joiner.on(" / ").join(lunch.getPrices()));
				lunchLayout.addView(lunchlayoutItem);
			}

			Log.d(TAG, "Restaurant added to ui:" + restaurantDay);
			return restaurantDayView;

		}

	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		//NOT NEEDED
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		if(tab.getTag() != null) {			
			presenter.onDateChanged(this, (Integer) tab.getTag());
		}
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		//NOT NEEDED EITHER
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(LAST_WEEK_DAY_SELECTION, (Integer) mActionbar.getSelectedTab().getTag());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void setSelectedDate(int dayOfTheWeek) {
		for(int tabIndex = 0; tabIndex < mActionbar.getTabCount(); tabIndex++) {
			Tab currentTab = mActionbar.getTabAt(tabIndex);
			if(dayOfTheWeek == (Integer) currentTab.getTag()) {
				mActionbar.selectTab(currentTab);
			}
		}
	}
}
