package fi.nottingham.mobilefood.view.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Joiner;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import fi.nottingham.mobilefood.DaggerBaseActivity;
import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;

public class MainActivity extends DaggerBaseActivity implements IMainView {
	private static final String TAG = "MainActivity";

	private TextView mDateTV;
	private TextView mWeekDay;
	private ListView mFoodsTV;
	private ProgressBar mProgressBar;
	private Button mRefreshButton;

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

		mWeekDay = (TextView) findViewById(R.id.textview_week_day);
		mDateTV = (TextView) findViewById(R.id.textview_date);
		mFoodsTV = (ListView) findViewById(R.id.listview_foods);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar_indeterminate);
		mRefreshButton = (Button) findViewById(R.id.main_refresh_button);
		
		mRefreshButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				Log.i(TAG, "Refresh button clicked.");
				mRefreshButton.setVisibility(View.INVISIBLE);
				presenter.onViewCreation(MainActivity.this);
			}
		});
		
		presenter.onViewCreation(this);
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

	@Override
	public void showLoadingIcon() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void setDate(Date selectedDate) {
		checkNotNull(selectedDate, "selectedDate cannot be null.");
		mWeekDay.setText(DateUtils.getWeekDay(selectedDate));
		mDateTV.setText(DateUtils.getDateInShortFormat(this, selectedDate));
	}

	@Override
	public void notifyThatDeviceHasNoInternetConnection() {
		Toast.makeText(this, getText(R.string.no_internet), Toast.LENGTH_LONG).show();
	}

	@Override
	public void showRefreshButton() {
		mRefreshButton.setVisibility(View.VISIBLE);
		mRefreshButton.bringToFront();
	}

	class RestaurantDayViewAdapter extends ArrayAdapter<RestaurantDay> {

		private static final int FOOD_ITEM_LAYOUT = R.layout.restaurant_item;
		private static final String TAG = "RestaurantDayViewAdapter";

		public RestaurantDayViewAdapter(Context context,
				List<RestaurantDay> items) {
			super(context, FOOD_ITEM_LAYOUT, items);
			Log.d(TAG, "Added Following RestaurantDays" + items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View restaurantDayView = inflater.inflate(FOOD_ITEM_LAYOUT, parent,
					false);

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
				View lunchlayoutItem = inflater.inflate(R.layout.food_item, null, false);
				((TextView) lunchlayoutItem.findViewById(R.id.food_item_name))
						.setText(lunch.getFoodName());
				((TextView) lunchlayoutItem.findViewById(R.id.food_item_diets))
						.setText(lunch.getDiets());
				((TextView) lunchlayoutItem.findViewById(R.id.food_item_prices))
						.setText(Joiner.on(" / ").join(lunch.getPrices()));
				lunchLayout.addView(lunchlayoutItem);
			}

			Log.d(TAG, "Restaurant added to ui:"  + restaurantDay);
			return restaurantDayView;

		}

	}
}
