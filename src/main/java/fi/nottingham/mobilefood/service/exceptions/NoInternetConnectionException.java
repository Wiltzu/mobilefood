package fi.nottingham.mobilefood.service.exceptions;

public class NoInternetConnectionException extends Exception {
	private static final long serialVersionUID = 3280931628177537027L;

	public NoInternetConnectionException() {
		super("Device doesn't have an Internet connection.");	
	}

	
}
