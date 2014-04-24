package fi.nottingham.mobilefood.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;

/**
 * Represents foods in the application.
 * 
 * @author Ville Ahti
 * 
 */
@Immutable
public class Food {

	private final String name;
	private final List<String> prices;
	private final String diets;

	public Food(String foodName, List<String> prices, @Nullable String diet) {
		this.name = checkNotNull(foodName, "foodName cannot be null");
		this.prices = checkNotNull(prices, "price cannot be null");
		this.diets = diet;
	}

	public String getName() {
		return name;
	}

	public List<String> getPrices() {
		return prices;
	}

	/**
	 * @return <code>null</code> if there are no diets
	 */
	public String getDiets() {
		return diets;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper((Object) this).add("foodName", name)
				.add("price", prices).add("diet", diets).toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName(), getPrices(), getDiets());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Food) {
			Food other = (Food) o;
			return Objects.equal(name, other.name)
					&& Objects.equal(prices, other.prices)
					&& Objects.equal(diets, other.diets);
		}
		return false;
	}

}
