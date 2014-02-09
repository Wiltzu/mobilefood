package fi.nottingham.mobilefood.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * Represents foods in the application.
 * 
 * @author Ville Ahti
 *
 */
public class Food {

	private final String foodName;
	private final String prices;
	private final String diet;
	private final String restaurantName;

	public Food(String foodName, String prices, @Nullable String diet,
			String restaurantName) {
		this.foodName = checkNotNull(foodName, "foodName cannot be null");
		this.prices = checkNotNull(prices, "price cannot be null");
		this.diet = diet;
		this.restaurantName = restaurantName;
	}

	public String getFoodName() {
		return foodName;
	}

	public String getPrices() {
		return prices;
	}

	/**
	 * @return <code>null</code> if there are no diets
	 */
	public String getDiets() {
		return diet;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper((Object) this).add("foodName", foodName)
				.add("price", prices).add("diet", diet)
				.add("restaurantName", restaurantName).toString();
	}

}
