package fi.nottingham.mobilefood;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView text;

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
		text = (TextView) findViewById(R.id.textview_date);
		Time now = new Time();
		now.setToNow();
		text.setText(now.format("d.M.yyyy"));
		text.setBackgroundColor(Color.BLUE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(fi.nottingham.mobilefood.R.menu.main, menu);
		return true;
	}

}
