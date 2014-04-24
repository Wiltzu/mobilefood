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
import fi.nottingham.mobilefood.view.DailyFoodView;
import fi.nottingham.mobilefood.view.ViewIsReadyListener;

public class OneDayLunchesFragment extends DaggerBaseFragment implements
		DailyFoodView {
	public static ViewIsReadyListener listener;

	private static final String TAG = "OneDayLunchesFragment";
	private ListView mFoodsListView;
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

	@Override
	public Integer getWeekDay() {
		return weekDay;
	}

	static class ViewHolder {
		public ImageView chainLogo;
		public TextView restaurantNameTV;
		public TextView restaurantStreetAddressTV;
		public LinearLayout lunchLayout;
		public LinearLayout alertLayout;
		public TextView alertTV;
		public ImageButton restaurantInfoBtn;
		public ImageButton foodListBtn;
		public View restaurantItemInfo;
	}
	
	static class LunchViewHolder {
		public TextView nameTV;
		public TextView dietsTV;
		public TextView pricesTV;
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
			View rowView = convertView;
			LayoutInflater inflater = null;

			if (rowView == null) {
				inflater = getActivity().getLayoutInflater();
				rowView = inflater.inflate(R.layout.restaurant_item, parent,
						false);

				ViewHolder holder = new ViewHolder();

				holder.chainLogo = (ImageView) rowView
						.findViewById(R.id.restaurant_item_chain_logo);
				holder.restaurantNameTV = (TextView) rowView
						.findViewById(R.id.restaurant_item_restaurant_name);
				holder.restaurantStreetAddressTV = (TextView) rowView
						.findViewById(R.id.restaurant_item_restaurant_street_address);
				holder.lunchLayout = (LinearLayout) rowView
						.findViewById(R.id.restaurant_item_food_layout);
				holder.alertTV = (TextView) rowView
						.findViewById(R.id.restaurant_item_alert_textview);
				holder.alertLayout = (LinearLayout) rowView
						.findViewById(R.id.restaurant_item_alert_layout);
				holder.restaurantItemInfo = (View) rowView
						.findViewById(R.id.restaurant_item_info_layout);

				holder.restaurantInfoBtn = (ImageButton) rowView
						.findViewById(R.id.restaurant_item_restaurant_info_button);
				holder.foodListBtn = (ImageButton) rowView
						.findViewById(R.id.restaurant_item_food_list_button);
				rowView.setTag(holder);
			}
			
			if (position == 0) {
				// top margin for first item to make it look good
				addTopMarginForItem(rowView);
			} else {
				removeTopMarginForItem(rowView);
			}

			final ViewHolder holder = (ViewHolder) rowView.getTag();

			holder.restaurantInfoBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					infoWasOpened[position] = true;
					showRestaurantInfo(holder);
				}
			});

			holder.foodListBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					infoWasOpened[position] = false;
					showFoods(holder, position);
				}
			});

			if (infoWasOpened[position]) {
				showRestaurantInfo(holder);
			}

			final RestaurantDay restaurantDay = getItem(position);

			String alert = restaurantDay.getAlert();
			if (!isNullOrEmpty(alert)) {
				hasAlert[position] = true;
				holder.alertTV.setText(alert);
			} else {
				holder.alertLayout.setVisibility(View.GONE);
			}

			holder.chainLogo.setImageResource(R.drawable.unica_logo);
			holder.restaurantNameTV.setText(restaurantDay.getRestaurantName());
			if (restaurantDay.getRestaurant() != null) {
				holder.restaurantStreetAddressTV.setText(restaurantDay
						.getRestaurant().getAddress());
			}

			List<Food> lunches = restaurantDay.getLunches();
			
			if (inflater == null) {
				inflater = getActivity().getLayoutInflater();
			}
			
			int lunchCount = 0;
			for (Food lunch : lunches) {
				View lunchView = holder.lunchLayout.getChildAt(lunchCount);
				if (lunchView == null) {
					lunchView = createLunchLayout(parent, inflater, lunch);
					holder.lunchLayout.addView(lunchView);
				}
				LunchViewHolder lunchHolder = (LunchViewHolder) lunchView.getTag();
				lunchHolder.nameTV.setText(lunch.getName());
				lunchHolder.dietsTV.setText(lunch.getDiets());
				setLunchPrices(lunch.getPrices(), lunchHolder.pricesTV);
				
				lunchCount++;
			}
			
			for(int i = lunchCount; i < holder.lunchLayout.getChildCount(); i++) {
				//remove views that are not needed
				holder.lunchLayout.removeViewAt(i);
			}

			Log.d(TAG, "Restaurant added to ui:" + restaurantDay);
			return rowView;
		}

		private void removeTopMarginForItem(View rowView) {
			LinearLayout restaurantItemLayout = (LinearLayout) rowView
					.findViewById(R.id.restaurant_item_layout);
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) restaurantItemLayout
					.getLayoutParams();
			layoutParams.topMargin = 0;
		}

		private void addTopMarginForItem(View rowView) {
			LinearLayout restaurantItemLayout = (LinearLayout) rowView
					.findViewById(R.id.restaurant_item_layout);
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) restaurantItemLayout
					.getLayoutParams();
			layoutParams.topMargin = layoutParams.bottomMargin;
		}

		private View createLunchLayout(ViewGroup parent,
				LayoutInflater inflater, Food lunch) {
			View lunchlayoutItem = inflater.inflate(R.layout.food_item, parent,
					false);
			
			LunchViewHolder holder = new LunchViewHolder();
			holder.nameTV = (TextView) lunchlayoutItem.findViewById(R.id.food_item_name);
			holder.dietsTV = (TextView) lunchlayoutItem.findViewById(R.id.food_item_diets);
			holder.pricesTV = (TextView) lunchlayoutItem
					.findViewById(R.id.food_item_prices);
			lunchlayoutItem.setTag(holder);
			
			return lunchlayoutItem;
		}

		private void setLunchPrices(List<String> prices, TextView pricesTV) {
			pricesTV.setText(Joiner.on(" / ").join(prices));
			if (!prices.isEmpty()) {
				// add Euro sign if there are prices
				pricesTV.setText(pricesTV.getText() + " €");
			}
		}

		private void showFoods(ViewHolder holder, int position) {
			holder.lunchLayout.setVisibility(View.VISIBLE);
			if (hasAlert[position]) {
				holder.alertLayout.setVisibility(View.VISIBLE);
			}
			holder.restaurantItemInfo.setVisibility(View.GONE);
			holder.foodListBtn.setVisibility(View.INVISIBLE);
			holder.restaurantInfoBtn.setVisibility(View.VISIBLE);
		}

		private void showRestaurantInfo(ViewHolder holder) {
			holder.lunchLayout.setVisibility(View.GONE);
			if (holder.alertLayout.getVisibility() == View.VISIBLE) {
				holder.alertLayout.setVisibility(View.GONE);
			}
			holder.restaurantItemInfo.setVisibility(View.VISIBLE);
			holder.foodListBtn.setVisibility(View.VISIBLE);
			holder.restaurantInfoBtn.setVisibility(View.INVISIBLE);
		}

		@Override
		public boolean isEnabled(int position) {
			// no item can be selected
			return false;
		}

	}

}
