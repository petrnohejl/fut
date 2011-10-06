/**
 *
 */
package fut.server.exception;

/**
 * The Class ServerException.This exception signals serious prblems with SQL server.
 *
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 29.3.2009
 */
public final class ServerException extends Exception {
    private static final long serialVersionUID = 6864599356454616157L;

    /**
     * Instantiates a new creating server exception.
     *
     * @param ex
     *            the description of an exception
     */
    public ServerException(String ex) {
	super(ex);
    }

    /**
     * Instantiates a new creating server exception.
     *
     * @param ex
     *            the description of an exception
     * @param t
     *            the cause of this exception
     */
    public ServerException(String ex,Throwable t) {
	super(ex,t);
    }

}
