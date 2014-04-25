package fi.nottingham.mobilefood.view.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import fi.nottingham.mobilefood.DaggerBaseFragment;
import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.view.IDailyFoodView;
import fi.nottingham.mobilefood.view.IViewIsReadyListener;
import fi.nottingham.mobilefood.view.adapter.RestaurantDayArrayAdapter;

public class OneDayLunchesFragment extends DaggerBaseFragment implements
		IDailyFoodView {
	public static IViewIsReadyListener listener;

	private static final String TAG = "OneDayLunchesFragment";
	private ListView mFoodsListView;
	private RelativeLayout mNoLunchesLayout;
	private Integer weekDay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(false);
		weekDay = getArguments().getInt("weekDay");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_lunch, container,
				false);
		mFoodsListView = (ListView) rootView.findViewById(R.id.listview_foods);
		mNoLunchesLayout = (RelativeLayout) rootView.findViewById(R.id.no_foods_view);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "Activity has been created");

		if (listener != null) {
			Log.d(TAG,
					"Notified listener that Fragment UI is ready to be created.");
			listener.viewIsReady(this);
		}
	}

	@Override
	public void onResume() {
		Log.d(TAG, String.format("Fragment for week day %s is initialized",
				getArguments() != null ? getArguments().getInt("weekDay") : ""));
		super.onResume();
	}

	@Override
	public void setFoods(List<RestaurantDay> foodsByRestaurant) {
		checkNotNull(foodsByRestaurant, "foodsByRestaurant cannot be null");

		if (isAdded()) {
			if (mFoodsListView == null) {
				// ListView might just null but don't know why
				mFoodsListView = (ListView) getActivity().findViewById(
						R.id.listview_foods);
			}
			
			if (!foodsByRestaurant.isEmpty()) {
				Log.d(TAG, "Setting daily foods to listView of the fragment");
				if (mFoodsListView.getAdapter() == null
						|| mFoodsListView.getAdapter().getCount() != foodsByRestaurant
								.size()) {
					mFoodsListView.setAdapter(new RestaurantDayArrayAdapter(
							getActivity(), foodsByRestaurant));
				}
			} else {
				Log.d(TAG, "Had empty foods for a day... setting no food view visible.");
				mFoodsListView.setVisibility(View.GONE);
				mNoLunchesLayout.setVisibility(View.VISIBLE);
			}
		}

	}

	@Override
	public Integer getWeekDay() {
		return weekDay;
	}
}
