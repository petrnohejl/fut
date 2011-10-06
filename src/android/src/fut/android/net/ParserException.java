package fut.android.net;

/**
 * 
 * @author jlibosva
 *
 */
public class ParserException extends Throwable {
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
	public ParserException(String message) {
		this.message = new String("ParserException: " + message);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getMessage() {
		return message;
	}
}
