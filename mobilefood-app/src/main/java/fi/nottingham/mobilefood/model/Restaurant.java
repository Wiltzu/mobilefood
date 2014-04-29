package fi.nottingham.mobilefood.model;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;

@Immutable
public class Restaurant {

	private final String name;
	private final String address;
	private final String zip;
	private final String postOffice;
	private final float longitude;
	private final float latitude;
	private final List<LunchTime> lunchTimes;

	public Restaurant(String name, String address, String zip,
			String postOffice, float longitude, float latitude, List<LunchTime> lunchTimes) {
		this.name = name;
		this.address = address;
		this.zip = zip;
		this.postOffice = postOffice;
		this.longitude = longitude;
		this.latitude = latitude;
		this.lunchTimes = lunchTimes;
	}

	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public String getPostOffice() {
		return postOffice;
	}

	public String getZip() {
		return zip;
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public List<LunchTime> getLunchTimes() {
		return lunchTimes;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this.getClass()).add("name", getName())
				.add("address", getAddress()).add("zip", getZip())
				.add("postOffice", getPostOffice())
				.add("longitude", getLongitude())
				.add("latitude", getLatitude())
				.add("lunchTimes", getLunchTimes()).toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName(), getAddress(), getZip(),
				getPostOffice(), getLongitude(), getLatitude());
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Restaurant) {
			Restaurant other = (Restaurant) o;
			return Objects.equal(name, other.name) &&
					Objects.equal(address, other.address) &&
					Objects.equal(zip, other.zip) &&
					Objects.equal(postOffice, other.postOffice) &&
					Objects.equal(longitude, other.longitude) &&
					Objects.equal(latitude, other.latitude);
		}
		return false;
	}

}
