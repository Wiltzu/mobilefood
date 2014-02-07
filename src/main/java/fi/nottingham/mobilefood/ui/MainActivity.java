package fi.nottingham.mobilefood.ui;

import java.util.Date;

import javax.inject.Inject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import fi.nottingham.mobilefood.DaggerBaseActivity;
import fi.nottingham.mobilefood.R;

public class MainActivity extends DaggerBaseActivity {
	private static final String TAG = "MainActivity";
	private TextView mDateTV;
	private TextView mWeekDay;

	@Inject
	protected LocationManager mLocationManager;

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

		Date now = new Date();
		mWeekDay = (TextView) findViewById(R.id.textview_week_day);
		mWeekDay.setText(DateFormat.format("EEEE", now));

		mDateTV = (TextView) findViewById(R.id.textview_date);
		mDateTV.setText(DateFormat.getDateFormat(this).format(now));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(fi.nottingham.mobilefood.R.menu.main, menu);
		return true;
	}
}
