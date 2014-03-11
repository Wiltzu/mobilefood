package fi.nottingham.mobilefood.service.exceptions;

import static com.google.common.base.Preconditions.checkArgument;

public class FoodServiceException extends Exception {
	private static final long serialVersionUID = -6046074613818695235L;
	
	public static final int SERVICE_DOWN = 0;
	public static final int NO_FOOD_FOR_WEEK = 1;
	private static final String[] errorMessages =  {"Service is down.","No foods available for requested week."};

	private final int errorCode;
	
	/**
	 * @param errorCode use this classes predefined values {@link FoodServiceException#SERVICE_DOWN} or {@link FoodServiceException#NO_FOOD_FOR_WEEK}
	 */
	public FoodServiceException(int errorCode) {
		super(errorMessages[isLegalErrorCode(errorCode) ? errorCode : 0]);
		this.errorCode = errorCode;		
	}

	private static boolean isLegalErrorCode(int errorCode) {
		checkArgument(errorCode == SERVICE_DOWN || errorCode == NO_FOOD_FOR_WEEK, "Use predifined error codes!");
		return true;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
