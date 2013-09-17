package ca.ubc.clicker.driver.exception;

public class ClickerException extends Exception {
	private static final long serialVersionUID = -3862950600304325520L;

	public ClickerException() {
		super();
	}

	public ClickerException(String message) {
		super(message);
	}

	public ClickerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClickerException(Throwable cause) {
		super(cause);
	}
}
