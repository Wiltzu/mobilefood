package fi.nottingham.mobilefood.view.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Joiner;

import fi.nottingham.mobilefood.DaggerBaseFragment;
import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.RestaurantDay;
import fi.nottingham.mobilefood.view.ViewIsReadyListener;

public class OneDayLunchesFragment extends DaggerBaseFragment {

	private static final String TAG = "OneDayLunchesFragment";
	private ListView mFoodsListView;
	private ViewIsReadyListener listener;

	public void setListener(ViewIsReadyListener listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_main, container,
				false);
		mFoodsListView = checkNotNull(
				(ListView) rootView.findViewById(R.id.listview_foods),
				"Somehow ListView is happened to be null...");
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "Activity has been created");
		if (listener != null) {
			listener.viewIsReady();
		}
	}

	public void setFoods(List<RestaurantDay> foodsByRestaurant) {
		checkNotNull(foodsByRestaurant, "foodsByRestaurant cannot be null");

		if (isAdded()) {
			Log.d(TAG, "Fragment is added to activity");
			if (!foodsByRestaurant.isEmpty()) {
				Log.d(TAG, "Foods set to listView of the fragment");
				if (mFoodsListView == null) {
					// ListView might just null but don't know why
					mFoodsListView = (ListView) getActivity().findViewById(
							R.id.listview_foods);
				}
				if (mFoodsListView.getAdapter() == null
						|| mFoodsListView.getAdapter().getCount() != foodsByRestaurant
								.size()) {
					mFoodsListView.setAdapter(new RestaurantDayViewAdapter(
							getActivity(), foodsByRestaurant));
				}
			} else {
				Log.d(TAG, "Empty Foods set to listView of the fragment");
				mFoodsListView
						.setAdapter(new ArrayAdapter<String>(getActivity(),
								android.R.layout.simple_list_item_1,
								getResources().getStringArray(
										R.array.no_food_for_day)));
			}
		}

	}

	class RestaurantDayViewAdapter extends ArrayAdapter<RestaurantDay> {
		private static final String TAG = "RestaurantDayViewAdapter";

		private boolean[] infoWasOpened = new boolean[getCount()];
		private boolean[] hasAlert = new boolean[getCount()];

		public RestaurantDayViewAdapter(Context context,
				List<RestaurantDay> items) {
			super(context, R.layout.restaurant_item, items);
			Log.d(TAG, "Added Following RestaurantDays" + items);
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			final LayoutInflater inflater = getActivity().getLayoutInflater();
			final View restaurantDayView = inflater.inflate(
					R.layout.restaurant_item, parent, false);

			if (position == 0) {
				// top margin for first item to make it look good
				addTopMarginForItem(restaurantDayView);
			}

			ImageView chainLogo = (ImageView) restaurantDayView
					.findViewById(R.id.restaurant_item_chain_logo);
			TextView restaurantNameTV = (TextView) restaurantDayView
					.findViewById(R.id.restaurant_item_restaurant_name);
			TextView restaurantStreetAddressTV = (TextView) restaurantDayView
					.findViewById(R.id.restaurant_item_restaurant_street_address);
			final LinearLayout lunchLayout = (LinearLayout) restaurantDayView
					.findViewById(R.id.restaurant_item_food_layout);
			final TextView alertTV = (TextView) restaurantDayView
					.findViewById(R.id.restaurant_item_alert_textview);
			final LinearLayout alertLayout = (LinearLayout) restaurantDayView
					.findViewById(R.id.restaurant_item_alert_layout);
			final View restaurantItemInfo = (View) restaurantDayView
					.findViewById(R.id.restaurant_item_info_layout);

			final ImageButton restaurantInfoBtn = (ImageButton) restaurantDayView
					.findViewById(R.id.restaurant_item_restaurant_info_button);
			final ImageButton foodListBtn = (ImageButton) restaurantDayView
					.findViewById(R.id.restaurant_item_food_list_button);

			restaurantInfoBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					infoWasOpened[position] = true;
					showRestaurantInfo(lunchLayout, alertLayout,
							restaurantItemInfo, restaurantInfoBtn, foodListBtn);
				}
			});

			foodListBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					infoWasOpened[position] = false;
					showFoods(lunchLayout, alertTV, alertLayout,
							restaurantItemInfo, restaurantInfoBtn, foodListBtn,
							position);
				}
			});

			if (infoWasOpened[position]) {
				showRestaurantInfo(lunchLayout, alertLayout,
						restaurantItemInfo, restaurantInfoBtn, foodListBtn);
			}

			final RestaurantDay restaurantDay = getItem(position);

			String alert = restaurantDay.getAlert();
			if (!isNullOrEmpty(alert)) {
				hasAlert[position] = true;
				alertTV.setText(alert);
			} else {
				alertLayout.setVisibility(View.GONE);
			}

			chainLogo.setImageResource(R.drawable.unica_logo);
			restaurantNameTV.setText(restaurantDay.getRestaurantName());
			if (restaurantDay.getRestaurant() != null) {
				restaurantStreetAddressTV.setText(restaurantDay.getRestaurant()
						.getAddress());
			}

			for (Food lunch : restaurantDay.getLunches()) {
				lunchLayout.addView(createLunchLayout(parent, inflater, lunch));
			}

			Log.d(TAG, "Restaurant added to ui:" + restaurantDay);
			return restaurantDayView;
		}

		private void addTopMarginForItem(View restaurantDayView) {
			LinearLayout restaurantItemLayout = (LinearLayout) restaurantDayView
					.findViewById(R.id.restaurant_item_layout);
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) restaurantItemLayout
					.getLayoutParams();
			layoutParams.topMargin = layoutParams.bottomMargin;
		}

		private View createLunchLayout(ViewGroup parent,
				LayoutInflater inflater, Food lunch) {
			View lunchlayoutItem = inflater.inflate(R.layout.food_item, parent,
					false);
			((TextView) lunchlayoutItem.findViewById(R.id.food_item_name))
					.setText(lunch.getFoodName());
			((TextView) lunchlayoutItem.findViewById(R.id.food_item_diets))
					.setText(lunch.getDiets());

			TextView pricesTV = (TextView) lunchlayoutItem
					.findViewById(R.id.food_item_prices);
			setPrices(lunch.getPrices(), pricesTV);
			return lunchlayoutItem;
		}

		private void setPrices(List<String> prices, TextView pricesTV) {
			pricesTV.setText(Joiner.on(" / ").join(prices));
			if (!prices.isEmpty()) {
				// add Euro sign if there are prices
				pricesTV.setText(pricesTV.getText() + " €");
			}
		}

		private void showFoods(final LinearLayout lunchLayout,
				final TextView alertTV, final LinearLayout alertLayout,
				final View restaurantItemInfo,
				final ImageButton restaurantInfoBtn,
				final ImageButton foodListBtn, int position) {
			lunchLayout.setVisibility(View.VISIBLE);
			if (hasAlert[position]) {
				alertLayout.setVisibility(View.VISIBLE);
			}
			restaurantItemInfo.setVisibility(View.GONE);
			foodListBtn.setVisibility(View.INVISIBLE);
			restaurantInfoBtn.setVisibility(View.VISIBLE);
		}

		private void showRestaurantInfo(final LinearLayout lunchLayout,
				final LinearLayout alertLayout, final View restaurantItemInfo,
				final ImageButton restaurantInfoBtn,
				final ImageButton foodListBtn) {
			lunchLayout.setVisibility(View.GONE);
			if (alertLayout.getVisibility() == View.VISIBLE) {
				alertLayout.setVisibility(View.GONE);
			}
			restaurantItemInfo.setVisibility(View.VISIBLE);
			foodListBtn.setVisibility(View.VISIBLE);
			restaurantInfoBtn.setVisibility(View.INVISIBLE);
		}

		@Override
		public boolean isEnabled(int position) {
			// no item can be selected
			return false;
		}

	}

}
