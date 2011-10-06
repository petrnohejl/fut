/**
 *
 */
package fut.client.common;

import java.io.IOException;
import java.net.UnknownHostException;

import fut.client.Main;
import fut.client.net.Net;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 4.12.2010
 */
public class Session {
    private static volatile Session INSTANCE = null;
    private String username;
    private Net network;

    private Session() {
    }

    public static Session getInstance() {
	if (INSTANCE == null) {
	    synchronized (Session.class) {
		if (INSTANCE == null) {
		    INSTANCE = new Session();
		}
	    }
	}

	return INSTANCE;
    }

    public void login(String usrername, String psswd) throws IOException, InvalidUserException, UnknownHostException {
	if (this.network != null && this.network.isConnected()) {
	    this.network.disconnect();
	}

	Props prop = Props.getInstance();
	int port = Integer.parseInt(prop.getProperty("futclient.port", Main.propID));
	String host = prop.getProperty("futclient.host", Main.propID);

	this.network = new Net(host, port, psswd);
	this.network.connect(usrername);
	this.username = usrername;
    }

    public void register(String username, String psswd) throws IOException, InvalidUserException, UnknownHostException {
	//TODO create a new user in DB !!
	Net net = null;
	try {
		Props prop = Props.getInstance();
		int port = Integer.parseInt(prop.getProperty("futclient.port", Main.propID));
		String host = prop.getProperty("futclient.host", Main.propID);

		net = new Net(host, port, "FUT::Fut_Under_Table");
	    net.register(username, psswd);
	} finally{
	    if (net != null && net.isConnected()) {
		net.disconnect();
	    }
	}
    }

    public void logout() throws IOException {
	this.network.disconnect();
    }

    public Net getNetwork() {
	return this.network;
    }

    public String getUsername() {
	return this.username;
    }
}
