package fi.nottingham.mobilefood.view.adapter;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Joiner;

import fi.nottingham.mobilefood.R;
import fi.nottingham.mobilefood.model.Food;
import fi.nottingham.mobilefood.model.Restaurant;
import fi.nottingham.mobilefood.model.RestaurantDay;


public class RestaurantDayArrayAdapter extends ArrayAdapter<RestaurantDay> {
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
	
	private static final String TAG = "RestaurantDayViewAdapter";

	private boolean[] infoWasOpened = new boolean[getCount()];
	private boolean[] hasAlert = new boolean[getCount()];
	
	public RestaurantDayArrayAdapter(Context context,
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
			inflater = getLayoutInflater();
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

		// top margin for first item to make it look good
		if (position == 0) {
			addTopMarginForItem(rowView);
		} else {
			removeTopMarginForItem(rowView);
		}

		final ViewHolder holder = (ViewHolder) rowView.getTag();
		
		holder.chainLogo.setImageResource(R.drawable.unica_logo);

		holder.restaurantInfoBtn.setOnClickListener(new RestaurantInfoListener(position, holder));
		holder.foodListBtn.setOnClickListener(new FoodListener(position, holder));

		if (infoWasOpened[position]) {
			showRestaurantInfo(holder);
		}

		final RestaurantDay restaurantDay = getItem(position);
		
		initAlertLayout(restaurantDay.getAlert(), holder, position);
		holder.restaurantNameTV.setText(restaurantDay.getRestaurantName());
		initLunchLayout(restaurantDay.getLunches(), holder, inflater,
				parent);
		
		Restaurant restaurant = restaurantDay.getRestaurant();
		if (restaurant != null) {
			holder.restaurantStreetAddressTV.setText(restaurant.getAddress());
		}

		Log.d(TAG, "Restaurant added to ui:" + restaurantDay);
		return rowView;
	}

	private void initAlertLayout(String alert, final ViewHolder holder,
			final int position) {
		if (!isNullOrEmpty(alert)) {
			hasAlert[position] = true;
			holder.alertTV.setText(alert);
		} else {
			holder.alertLayout.setVisibility(View.GONE);
		}
	}

	private void initLunchLayout(List<Food> lunches,
			final ViewHolder holder, LayoutInflater inflater,
			final ViewGroup parent) {
		
		if (inflater == null) {
			inflater = getLayoutInflater();
		}

		int lunchIndex = 0;
		for (Food lunch : lunches) {
			View lunchView = holder.lunchLayout.getChildAt(lunchIndex);
			if (lunchView == null) {
				lunchView = createLunchLayout(parent, inflater, lunch);
				holder.lunchLayout.addView(lunchView);
			}
			LunchViewHolder lunchHolder = (LunchViewHolder) lunchView
					.getTag();
			lunchHolder.nameTV.setText(lunch.getName());
			lunchHolder.dietsTV.setText(lunch.getDiets());
			setLunchPrices(lunch.getPrices(), lunchHolder.pricesTV);

			lunchIndex++;
		}
		
		for (int i = lunchIndex; i < holder.lunchLayout.getChildCount();) {
			// remove lunch items that are not needed
			holder.lunchLayout.removeViewAt(i);
		}
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
		holder.nameTV = (TextView) lunchlayoutItem
				.findViewById(R.id.food_item_name);
		holder.dietsTV = (TextView) lunchlayoutItem
				.findViewById(R.id.food_item_diets);
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
	
	private void showRestaurantInfo(ViewHolder holder) {
		holder.lunchLayout.setVisibility(View.GONE);
		if (holder.alertLayout.getVisibility() == View.VISIBLE) {
			holder.alertLayout.setVisibility(View.GONE);
		}
		holder.restaurantItemInfo.setVisibility(View.VISIBLE);
		holder.foodListBtn.setVisibility(View.VISIBLE);
		holder.restaurantInfoBtn.setVisibility(View.INVISIBLE);
	}

	private LayoutInflater getLayoutInflater() {
		return (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public boolean isEnabled(int position) {
		// no item can be selected
		return false;
	}

	class RestaurantInfoListener implements OnClickListener {
		private final int position;
		private final ViewHolder holder;
		
		public RestaurantInfoListener(int position, ViewHolder holder) {
			this.position = position;
			this.holder = holder;
		}
		
		@Override
		public void onClick(View v) {
			infoWasOpened[position] = true;
			showRestaurantInfo(holder);	
		}	
	}

	class FoodListener implements OnClickListener {
		private final int position;
		private final ViewHolder holder;
		
		public FoodListener(int position, ViewHolder holder) {
			this.position = position;
			this.holder = holder;
		}
		
		@Override
		public void onClick(View v) {
			infoWasOpened[position] = false;
			showFoods();	
		}
		
		private void showFoods() {
			holder.lunchLayout.setVisibility(View.VISIBLE);
			if (hasAlert[position]) {
				holder.alertLayout.setVisibility(View.VISIBLE);
			}
			holder.restaurantItemInfo.setVisibility(View.GONE);
			holder.foodListBtn.setVisibility(View.INVISIBLE);
			holder.restaurantInfoBtn.setVisibility(View.VISIBLE);
		}
	}

}

