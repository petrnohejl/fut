package fut.android.net;


public class InvalidUserException  extends Throwable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private String message;
	
	/**
	 * 
	 * @param message
	 */
	public InvalidUserException(String message) {
		this.message = new String("InvalidUserException: " + message);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}
}
