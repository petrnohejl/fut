package fut.client.common;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

/**
 * The Class Props.<br>
 * <p>
 * This class manages user & system properties.
 * <p>
 * All data stored in this '<i>property manager</i>' are <b>KEY-VALUE</b>
 * pairs.<br> Pairs, which logically belong together, create a group.
 * Each group has unique name - <i>identifier</i> to
 * access values stored under certain <b>KEY</b> in particular group.
 * <p>
 * Class is constructed as singleton object to ensure that there is only one
 * instance at a time.
 *
 * @author Peter Bielik
 * @since 25.2.2008
 * @version 1.0
 */
public final class Props {
    private static volatile Props INSTANCE=null ;
    private Properties prop = null; //helper
    private Map<String, Properties> cache = null;
    //The cache, where all the properties are stored in.

    private Props() {
	cache = new HashMap<String, Properties>();//default init capacity of 16
    }

    /**
     * Gets the single instance of Props.
     *
     * @return the instance of '<i>property manager</i>'
     */
    public static Props getInstance() {
	if (INSTANCE == null) {//double check idiom
	    synchronized (Props.class) {
		if (INSTANCE == null) {
		    INSTANCE = new Props();
		}
	    }
	}

	return INSTANCE;
    }

    /**
     * Disposes all used resources
     */
    public void dispose() {
	cache.clear();

	cache = null;
	prop = null;

    }

    /**
     * Loads properties from given file and store them under identifier
     * <code>ID</code>.
     *
     * @param file
     *            The property file to load from.
     * @param ID
     *            The identifier of a group.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void loadProperty(File file, String ID) throws IOException {
	BufferedInputStream bis = null;
	prop = new Properties();

	try {
	    bis = new BufferedInputStream(new FileInputStream(file));
	    prop.load(bis);
	    cache.put(ID, prop);
	} finally {
	    try {
		if (bis != null)
		    bis.close();
	    } catch (IOException e) {
		throw new RuntimeException(
			"Unable to load requested property file : "
				+ file.getName(), e);
	    }

	    prop = null;
	}

    }

    /**
     * Gets a value stored under <code>key</code>. If value is not found
     * (value == <code>null</code>), then
     * default value is returned.
     *
     * @param key
     *            An identifier of a value.
     * @param defaultValue
     *            Some default value, <code>null</code> allowed.
     * @param ID
     *            An identifier of a group.
     *
     * @return Value (or default value) of requested Key in a 'group' marked ID.
     */
    public String getProperty(String key, String defaultValue, String ID) {
	prop = cache.get(ID);

	if (prop != null)
	    return prop.getProperty(key, defaultValue);

	return null;
    }

    /**
     * Gets a value stored under <code>key</code>.
     *
     * @param key
     *            An identifier of a value
     * @param ID
     *            An identifier of a group.
     *
     * @return Value of requested Key in a 'group' marked ID. If there is
     * no such key, <code>null</code> is returned.
     */
    public String getProperty(String key, String ID) {
	prop = cache.get(ID);

	if (prop != null)
	    return prop.getProperty(key);

	return null;
    }


    /**
     * Checks if is property is empty.
     *
     * @param key
     *            An identifier of a value
     * @param ID
     *            An identifier of a group.
     * @return true, if property is empty. It means, property is NULL or
     * property has length == 0 or a group referenced by ID does not exist.
     */
    public boolean isPropertyEmpty(String key, String ID) {
	prop = cache.get(ID);
	String tmp = null;

	if (prop != null)
	    tmp = prop.getProperty(key);

	return (tmp == null || tmp.isEmpty());
    }


    /**
     * Gets all properties as object {@link Properties}.
     *
     * @param ID
     *            The identifier of a group.
     * @return an <b>copy</b> of properties stored in the cache.
     * @see java.util.Properties
     *
     */
    public Properties getAllProperties(String ID) {
	Properties dst = new Properties();
	Enumeration<Object> en = null;
	prop = cache.get(ID);

	if (prop!= null) {
	    en = prop.elements();
	    for (String key; en.hasMoreElements();) {
		key = (String)en.nextElement();
		dst.setProperty(key, prop.getProperty(key));
	    }
	    return  dst;
	}

	return null;
    }


    /**
     * Loads properties from a XML file and store them under identifier
     * <code>ID</code>.
     *
     * @param file
     *            The property file to load from.
     * @param ID
     *            The identifier of a group.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws InvalidPropertiesFormatException
     *             Data on input stream does not constitute a valid XML document
     *             with the mandated document type.
     */
    public void loadFromXML(File file, String ID) throws IOException,
	    InvalidPropertiesFormatException {

	BufferedInputStream bis = null;
	prop = new Properties();

	try {
	    bis = new BufferedInputStream(new FileInputStream(file));
	    prop.loadFromXML(bis);
	    cache.put(ID, prop);
	} finally {
	    try {
		if (bis != null)
		    bis.close();
	    } catch (IOException e) {
		throw new RuntimeException(
			"Unable to load requested property file : "
				+ file.getName(), e);
	    }
	}

    }

    /**
     * Returns an enumeration of all the keys in property list.
     *
     * @param ID
     *            The ID of a group.
     *
     * @return An enumeration of all the keys in property list or <code>null</code>
     * if no such prperty list exists.
     */
    public Enumeration<?> getPropertyNames(String ID) {
	prop = cache.get(ID);

	if (prop != null) {
	    return prop.propertyNames();
	}

	return null;
    }

    /**
     * Sets a value to given property <code>key</code>
     * under a group <code>ID</code>.<br>
     * If such a group of properties does not exist, a new group is created and
     * new <code>KEY-VALUE</code> pair stored.
     *
     * @param key
     *            An identifier of a value.
     * @param value
     *            Value to be stored, <code>null</code> allowed.
     * @param ID
     *            An identifier of a group.
     */
    public void setProperty(String key, String value, String ID) {
	prop = cache.get(ID);

	if (prop != null) {
	    prop.setProperty(key, value);
	} else {
	    prop = new Properties();
	    prop.setProperty(key, value);
	    cache.put(ID, prop);
	}
    }


    /**
     * Removes a  property with given <code>key</code>
     * under a group <code>ID</code>.<br>
     *
     * @param key
     *            An identifier of a value.
     * @param ID
     *            An identifier of a group.
     */
    public void removeProperty(String key, String ID) {
	prop = cache.get(ID);

	if (prop != null && key != null) {
	    prop.remove(key);
	}
    }

    /**
     * Removes a logical group of KEY-VALUE pairs
     * stored under given <code>ID</code>.<br>
     *
     * @param ID
     *            An identifier of a group.
     */
    public void removePropertyGroup(String ID) {
	if (cache.containsKey(ID) ) {
	    cache.remove(ID);
	}
    }


    /**
     * Stores all properties under group <code>ID</code> to a file.
     *
     * @param file
     *            File path & name where to store properties.
     * @param ID
     *            The ID of group.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws IllegalArgumentException
     * 		   if there are no properties under ID.
     */
    public void store(File file, String ID) throws IOException {

	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
	try {

	    prop = cache.get(ID);
	    if (prop != null) {
		prop.store(bos, "");
	    } else {
		throw new IllegalArgumentException(
			"No such properties under requested ID : " + ID);
	    }

	} finally {
	    try {
		if (bos != null) {
		    bos.flush();
		    bos.close();
		}

		bos=null;
	    } catch (IOException e) {
		throw new RuntimeException(
			"Unable to store properties into the file : "
				+ file.getName(), e);
	    }
	}
    }

    /**
     * Stores all properties under given group to a XML file.
     *
     * @param xml
     *            File path & name where to store properties.
     * @param encoding
     *            The encoding of xml file.
     * @param ID
     *            The ID of group.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws IllegalArgumentException
     * 		   if there are no properties under ID.
     */
    public void storeToXML(File xml, String encoding, String ID)
	    throws IOException {
	BufferedOutputStream bos = new BufferedOutputStream(
		new FileOutputStream(xml));
	try {

	    prop = cache.get(ID);
	    if (prop != null) {
		prop.storeToXML(bos, ID, encoding);
	    } else {
		throw new IllegalArgumentException(
			"No such properties under requested ID : " + ID);
	    }

	} finally {
	    try {
		if (bos != null) {
		    bos.flush();
		    bos.close();
		}

		bos=null;
	    } catch (IOException e) {
		throw new RuntimeException(
			"Unable to store properties into the file : "
				+ xml.getName(), e);
	    }
	}
    }

}
