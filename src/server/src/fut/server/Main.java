/**
 *
 */
package fut.server;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import fut.server.common.Log;
import fut.server.common.Props;
import fut.server.common.Server;
import fut.server.db.DBLogic;
import fut.server.exception.ServerException;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 3.11.2010
 */
//this class is for initialization code only
public class Main {
    public static final String propID = "fut";
    private static Log log = null;

    /**
     * Starting point of this application
     * @param args this application recorgnizes :<br>
     * <li> -serverArgs  - all arguments after this one will be passed to
     * appropriate server created according jdbc string.
     */
    public static void main(final String[] args) {
	String serverArgs[] = null; //custom args for server

	for (int i = 0; i < args.length; i++) {
	    if ("-serverArgs".equalsIgnoreCase(args[i])) {
		serverArgs = new String[args.length - i - 1];
		//from this arg+1 to the end
		System.arraycopy(args, i + 1, serverArgs, 0, args.length - i - 1);
		break;
	    }
	}


	init(serverArgs);
    }

    public static void shutdownServer() {
	DBLogic.getInstance().disconnect();

	if (Server.getInstance().isRunning()) {
	    try {
		Server.getInstance().destroy(true);
	    } catch (ServerException e) {
		log.severe("Problem while destroying server.", e);
	    }
	}

    }

    private static void init(String serverArgs[]) {
    	initProperties();
    	initLog();
    	initSqlServer(serverArgs);
    	connectDB();
    	startServer();

    }


    private static void initProperties() {
	Props prop = Props.getInstance();
	File root = new File(System.getProperty("user.dir"));
	File file = new File(new File(root, "conf"), "fut.properties");

	if (file.exists()) {
	    try {
		prop.loadProperty(file, propID);
	    } catch (IOException e) {
		loadDefaults(false);
	    }
	} else {
	    loadDefaults(true);
	}

	resolveProperties();
    }


    private static void initLog() {
	//java.util.logging.config.file=a/b/c/log.properties
	System.setProperty("java.util.logging.config.file",
		Props.getInstance().getProperty("futserver.log.config", propID));
	try {
	    Log.initConfig();
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	log = new Log(Main.class);
	log.info(" ** Application started ** ");

    }


    private static void connectDB() {
	// in order to keep SQL server running, we have to connect
	DBLogic.getInstance().connect();
    }

    private static void initSqlServer(String serverArgs[]) {
	//	if db.startserver=false, connect to given url address
	if (Props.getInstance().getProperty("db.startserver", propID).equalsIgnoreCase("false")) {
	    log.info("Connecting to remote SQL server...");
	    return;
	}

	String[] param = null;
	if (serverArgs != null) {
	    param = new String[serverArgs.length];
	    System.arraycopy(serverArgs, 0, param, 0, serverArgs.length);
	} else {
	    //hardcoded server params
	    param = new String[]{"-tcp", //use tcp connection, acceptable from localhost only
		    "-tcpPort",//use specified port instead of default
		    Props.getInstance().getProperty("db.port", propID),
		    "-baseDir", //DB is located here
		    Props.getInstance().getProperty("db.dir", propID)
	    };
	}

	log.info("Starting local SQL server (jdbc = '" +
		Props.getInstance().getProperty("db.jdbc", propID) + "')...");

	try {
	    Server.getInstance().create(param);
	} catch (ServerException e) {
	    JOptionPane.showMessageDialog(null,
		    "Problem has occured during starting SQL server.\n" + e.getMessage() + "\nApplication will exit...");
	    log.severe(e.getMessage());
	    System.exit(1);
	    //	    throw new RuntimeException(e);
	}

    }

    private static void startServer() {
    	int port = Integer.parseInt(Props.getInstance().getProperty("futserver.port", propID));

    	try {
    		fut.server.net.Net androidServer = new fut.server.net.Net(port);
    		androidServer.run();
    	} catch (IOException ioe) {
    		System.err.println("IOException when creating server: " + ioe.getMessage());
    		log.severe("Cannot start server due to IOException ", ioe);
    		shutdownServer();
    		System.exit(1);
    	}

    }


    private static void loadDefaults(boolean save) {
	Props prop = Props.getInstance();

	prop.setProperty("futserver.log.config", "log_file.properties", propID);
	prop.setProperty("futserver.port","12345", propID );
	prop.setProperty("db.url", "jdbc:h2:tcp://localhost:9090/${db.file};DB_CLOSE_ON_EXIT=FALSE", propID);
	prop.setProperty("db.jdbc", "org.h2.Driver", propID);
	prop.setProperty("db.port", "9090", propID);
	prop.setProperty("db.dir", "dat", propID);
	prop.setProperty("db.file", "fut_database", propID);

	//	#true - starts local SQL server on db.port port-number
	//	#false - connects to db.url
	prop.setProperty("db.startserver", "true", propID);

	if (save) {

	    File root = new File(System.getProperty("user.dir"), "conf");
	    File tmp = new File(root, "fut.properties");
	    try {
		if (!root.exists()) {
		    if (root.mkdir()) {
			prop.store(tmp, propID);
		    }
		} else {
		    prop.store(tmp, propID);
		}
	    } catch (IOException emptyCatch) {
		//not interesting, still able to continue
	    }
	}

    }

    private static void resolveProperties() {

	Props prop = Props.getInstance();
	File root = new File(System.getProperty("user.dir"));
	File file = null;
	String tmp = "";
	String tmp2 = "";
	Pattern link = Pattern.compile("\\$\\{(.*?)\\}"); //matches ${*}, eg.  ${db.file}
	Matcher match = null;

	// ############ datatype check ################
	//test if port is integer
	tmp = prop.getProperty("db.port", "9090", propID);
	try {
	    Integer.parseInt(tmp);
	} catch (NumberFormatException e) {
	    prop.setProperty("db.port", "9090", propID);
	}

	tmp = prop.getProperty("futserver.port", "12345", propID);
	try {
	    Integer.parseInt(tmp);
	} catch (NumberFormatException e) {
	    prop.setProperty("futserver.port", "12345", propID);
	}

	//check for logical value
	tmp = prop.getProperty("db.startlocal", "true", propID);
	if ("true".equalsIgnoreCase(tmp) || "yes".equalsIgnoreCase(tmp) || "on".equalsIgnoreCase(tmp)) {
	    prop.setProperty("db.startserver", "true", propID);
	} else {
	    prop.setProperty("db.startserver", "false", propID);
	}

	//###########  links resolving #########################

	Enumeration<?> en = prop.getPropertyNames(propID);
	for (String key = ""; en.hasMoreElements(); key = (String) en.nextElement()) {

	    if ((tmp2 = prop.getProperty(key, propID)) != null) {
		match = link.matcher(tmp2);

		if (match.find()) {
		    for (int i = 1; i <= match.groupCount(); i++) {
			tmp2 = tmp2.replaceAll("\\$\\{" + match.group(i) + "\\}", prop.getProperty(match.group(i), propID));
		    }
		    prop.setProperty(key, tmp2, propID);
		}
	    }
	}



	// ########### relativity ##################
	//resolve all relative dirs/files to absolute paths
	tmp = prop.getProperty("futserver.log.config", "log_file.properties", propID);
	file = new File(new File(root, "conf"), tmp);
	prop.setProperty("futserver.log.config", file.getAbsolutePath(), propID);

	//DB path
	tmp = prop.getProperty("db.dir", "dat", propID);
	file = new File(root, tmp);
	prop.setProperty("db.dir", file.getAbsolutePath(), propID);
	//DB file
	tmp = prop.getProperty("db.file", "fut_datatabse", propID);
	file = new File(file, tmp);
	prop.setProperty("db.file", file.getAbsolutePath(), propID);


	//where is 'conf' dir ?
	prop.setProperty("futserver.confdir", new File(root, "conf").getAbsolutePath(), propID);


	//################# existence #####################
	//check if folder 'db.dir' exists
	tmp = prop.getProperty("db.dir", "dat", propID);
	file = new File(tmp);
	if (!file.exists()) {
	    file.mkdir();
	}
    }


}
