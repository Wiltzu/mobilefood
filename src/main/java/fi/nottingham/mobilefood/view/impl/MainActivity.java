package fi.nottingham.mobilefood.view.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import fi.nottingham.mobilefood.DaggerBaseActivity;
import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.presenter.IMainViewPresenter;
import fi.nottingham.mobilefood.util.DateUtils;
import fi.nottingham.mobilefood.view.IMainView;

public class MainActivity extends DaggerBaseActivity implements IMainView {
	private static final String TAG = "MainActivity";

	private TextView mDateTV;
	private TextView mWeekDay;
	private ListView mFoodsTV;
	private ProgressBar mProgressBar;

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
	}

	@Override
	protected void onResume() {
		presenter.onViewCreation(this);
		super.onResume();
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

	public void setFoods(List<Food> foods) {
		checkNotNull(foods, "foods cannot be null");
		mFoodsTV.setAdapter(new ArrayAdapter<Food>(this, R.layout.food_item,
				foods));
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
				
				if(mProgressBar.isShown()) {					
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
}
