package fut.server.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class provides logging functionality.
 * Based upon standart java logging api.
 *
 * @author Peter Bielik
 * @version 1.1
 * @since 1.0 - 29.3.2009
 */
public final class Log {
    //java.util.logging.config.file=a/b/c/log.properties

    /**
     * The Class NiceFormatter.
     */
    public static final class NiceFormatter extends Formatter{
	// qualified name : pdb.fototraveller.common.Log$NiceFormatter
	private Date dat = new Date();
	private MessageFormat formatter = new MessageFormat("{0,date} {0,time}");
	private Object args[] = new Object[1];
	private StringBuffer sb = new StringBuffer(256);

	/**
	 * Instantiates a new nice formatter.
	 */
	public NiceFormatter() {}

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	@Override
	public String format(LogRecord record) {
	    sb.delete(0, sb.capacity());
	    dat.setTime(record.getMillis());
	    args[0] = dat;
	    StringBuffer text = new StringBuffer();
	    formatter.format(args, text, null);
	    sb.append(text);
	    sb.append(" ");

	    sb.append(record.getLoggerName());
	    sb.append("\n");
	    sb.append(record.getLevel());
	    sb.append(": ");
	    sb.append(record.getMessage());
	    sb.append("\n");

	    return sb.toString();
	}

    }


    /**
     * The Class PrefixFilter. Filters records according prefix in the
     * name of logger. It is recommanded to use hierarchical naming convention
     * <i>(e.g.: com.aaa.bbb.something)</i>
     */
    public static final class PrefixFilter implements Filter{
	// qualified name : pdb.fototraveller.common.Log$PrefixFilter
	private String prefix="";
	private boolean noprefix = false;

	/**
	 * Instantiates a new prefix filter.
	 */
	public PrefixFilter() {
	    prefix = System.getProperty(this.getClass().getName() + ".prefix",null);
//	    System.out.println(prefix);

	    if (prefix==null) {
		//locate config file for standart logging api
		String config = System.getProperty("java.util.logging.config.file",null);
		if (config!=null) {
		    Properties prop=null;
		    prop = new Properties();

		    try {
			prop.load(new BufferedInputStream(new FileInputStream(new File(config))));
			prefix = prop.getProperty(this.getClass().getName() + ".prefix",null);
//			System.out.println(prefix);

		    } catch (FileNotFoundException ignored) {
		    } catch (IOException ignored) {
		    }

		}
	    }

	    if (prefix==null || prefix.equals("")) {
		//if no prefix set
		String tmp = this.getClass().getName();
		int i = tmp.indexOf(".");
		if (i!= -1) {
		    prefix = tmp.substring(0, i); //set prefix to be the first
		    //substring of classname, delimited by dot
		} else {
		    prefix = tmp; //there is no dot in the name
		}
	    }else if (prefix.equals("NONE")) {
		//indicates : 'use no prefix' > no filtering
		noprefix = true;
	    }

//	    System.out.println(prefix);
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Filter#isLoggable(java.util.logging.LogRecord)
	 */
	@Override
	public boolean isLoggable(LogRecord record) {
	    //do not filter !
	    if (noprefix) return true;
	    if (record.getLoggerName().startsWith(prefix))
		return true;

	    return false;
	}


	/**
	 * Gets the prefix.
	 *
	 * @return the prefix
	 */
	public String getPrefix() {
	    return prefix;
	}
    }

    private final Logger LOGGER;

    /**
     * Instantiates a new log.
     *
     * @param cls
     *            the cls
     */
    public Log(Class<?> cls) {
	this.LOGGER = Logger.getLogger(cls.getName());
    }

    //java.util.logging.config.file=a/b/c/log.properties

    /**
     * Inits the config.
     *
     * @throws SecurityException
     *             the security exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void initConfig() throws SecurityException, IOException {
	LogManager.getLogManager().readConfiguration();
    }

    /**
     * Gets the level.
     *
     * @return the level
     */
    public Level getLevel() {
	return LOGGER.getLevel();
    }

    /**
     * Checks if is loggable.
     *
     * @param level
     *            the level
     *
     * @return true, if is loggable
     */
    public boolean isLoggable(Level level) {
	return LOGGER.isLoggable(level);
    }



    /**
     * Fine.
     *
     * @param msg
     *            the msg
     */
    public void fine(String msg) {
	LOGGER.fine(msg);
    }

    /**
     * Finer.
     *
     * @param msg
     *            the msg
     */
    public void finer(String msg) {
	LOGGER.finer(msg);
    }

    /**
     * Finest.
     *
     * @param msg
     *            the msg
     */
    public void finest(String msg) {
	LOGGER.finest(msg);
    }


    /**
     * Info.
     *
     * @param msg
     *            the msg
     */
    public void info(String msg) {
	LOGGER.info(msg);
    }


    /**
     * Severe.
     *
     * @param msg
     *            the msg
     */
    public void severe(String msg) {
	LOGGER.severe(msg);
    }

    /**
     * Warning.
     *
     * @param msg
     *            the msg
     */
    public void warning(String msg) {
	LOGGER.warning(msg);
    }

    /**
     * Config.
     *
     * @param msg
     *            the msg
     */
    public void config(String msg) {
	LOGGER.config(msg);
    }


    /**
     * Fine.
     *
     * @param msg
     *            the msg
     * @param t
     *            the t
     */
    public void fine(String msg,Throwable t) {
	LOGGER.fine(msg + "\n" + t.toString());
    }

    /**
     * Finer.
     *
     * @param msg
     *            the msg
     * @param t
     *            the t
     */
    public void finer(String msg,Throwable t) {
	LOGGER.finer(msg + "\n" + t.toString());
    }

    /**
     * Finest.
     *
     * @param msg
     *            the msg
     * @param t
     *            the t
     */
    public void finest(String msg,Throwable t) {
	LOGGER.finest(msg + "\n" + t.toString());
    }


    /**
     * Info.
     *
     * @param msg
     *            the msg
     * @param t
     *            the t
     */
    public void info(String msg,Throwable t) {
	LOGGER.info(msg + "\n" + t.toString());
    }

    /**
     * Severe.
     *
     * @param msg
     *            the msg
     * @param t
     *            the t
     */
    public void severe(String msg,Throwable t) {
	LOGGER.severe(msg + "\n" + t.toString());
    }

    /**
     * Warning.
     *
     * @param msg
     *            the msg
     * @param t
     *            the t
     */
    public void warning(String msg,Throwable t) {
	LOGGER.warning(msg + "\n" + t.toString());
    }

    /**
     * Config.
     *
     * @param msg
     *            the msg
     * @param t
     *            the t
     */
    public void config(String msg,Throwable t) {
	LOGGER.config(msg + "\n" + t.toString());
    }

}
