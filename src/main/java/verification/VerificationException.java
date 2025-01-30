package verification;


public class VerificationException extends Exception {

	private static final long serialVersionUID = -4910213921113744412L;
	
	/**
	 * A constructor that simply calls the super constructor of Exception with the message as a parameter.
	 * @param message Message to set in Exception indicating reason for failure.
	 */
	public VerificationException(String message) {
		super(message);
	}
	
	

}
