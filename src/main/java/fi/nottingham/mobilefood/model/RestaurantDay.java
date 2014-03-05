package fi.nottingham.mobilefood.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * One day in the restaurant.
 * 
 * @author Ville
 * 
 */
@Immutable
public class RestaurantDay {

	private final String restaurantName;
	private final List<Food> lunches;

	public RestaurantDay(String restaurantName, List<Food> lunches) {
		this.restaurantName = checkNotNull(restaurantName,
				"restaurantName cannot be null");
		this.lunches = ImmutableList.copyOf(lunches);
	}

	/**
	 * @return {@link ImmutableList} of lunches
	 */
	public List<Food> getLunches() {
		return lunches;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("restaurantName", restaurantName).add("lunches", lunches)
				.toString();
	}
}
