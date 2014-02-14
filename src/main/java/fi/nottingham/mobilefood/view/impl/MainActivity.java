package fi.nottingham.mobilefood.view.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

	public void setFoods(Date selectedDate, List<Food> foods) {
		checkNotNull(selectedDate, "selectedDate cannot be null.");
		checkNotNull(foods, "foods cannot be null");
		
		mWeekDay.setText(DateUtils.getWeekDay(selectedDate));
		mDateTV.setText(DateUtils.getDateInShortFormat(this, selectedDate));
		mFoodsTV.setAdapter(new ArrayAdapter<Food>(this, R.layout.food_item, foods));
	}
}
