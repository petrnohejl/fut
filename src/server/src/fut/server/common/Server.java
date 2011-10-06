/**
 *
 */
package fut.server.common;


import fut.server.exception.ServerException;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 8.11.2010
 */
public class Server {
    private Thread thread;
    private String[] params;
    private org.h2.tools.Server server;
    private static volatile Server INSTANCE;


    private  Server() {
    }

    public static Server getInstance() {
	if (INSTANCE == null ) {
	    synchronized (Server.class) {
		if (INSTANCE == null ) {
		    INSTANCE = new Server();
		}
	    }
	}

	return INSTANCE;
    }

    /**
     * Creates a new instance of server.
     *
     * @param param
     *            the parametres for server
     * @throws ServerException
     *             throwen when previous instance is running
     */
    public void create(final String[] param) throws ServerException {
	if (thread!=null && thread.isAlive()) {
	    throw new ServerException("Shutdown server first !");
	}


	thread = new Thread(new Runnable() {
	    /* (non-Javadoc)
	     * @see java.lang.Runnable#run()
	     */
	    @Override
	    public void run() {

		try {

		    server = org.h2.tools.Server.createTcpServer(param);
		    server.start();
		    
		    /* an alternative, how to start server directly using service
		    server = new TcpServer();
		    server.init(params);
		    server.start();
		    server.listen();
		    */

		} catch (Exception e) {
		    throw new RuntimeException(new ServerException("Internal server exception",e));
		}
	    }
	});


	params = new String[param.length];
	System.arraycopy(param, 0, params, 0, param.length);

	thread.setDaemon(true);
	thread.setName("H2-server-daemon");
	thread.start();

    }

    /**
     * Destroys running server<br>
     * A connection is created & shutdown command is sent.
     *
     * @param joinForServer
     *            the join for server
     * @throws ServerException
     *             the server exception
     */
    public void destroy(boolean joinForServer) throws ServerException {

	try {
	    server.stop();
	    if (joinForServer) thread.join();

	} catch (InterruptedException ignored) {
	} catch (Exception e) {
	    // if something goes wrong..
	    throw new ServerException("Problem shutting down server has occured.",e);
	}

    }

    /**
     * Checks if is running.
     *
     * @return true, if is running
     */
    public boolean isRunning() {
	try {
	    return server.isRunning(false);

	} catch (Throwable e) {
	    return false;
	}
    }


}
