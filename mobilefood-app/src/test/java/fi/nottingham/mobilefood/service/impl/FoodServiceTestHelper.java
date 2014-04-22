package fi.nottingham.mobilefood.service.impl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;

class FoodServiceTestHelper {

	public static InputStream getFoodTestJSONFileAsInputStream(int weekNumber)
			throws FileNotFoundException, URISyntaxException {
		return FoodServiceTestHelper.class.getResourceAsStream(String.format(
				"2014_w%s_unica.json", weekNumber));
	}
}
