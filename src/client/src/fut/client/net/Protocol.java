package fut.client.net;

/**
 * 
 * @author jlibosva
 *
 */
public class Protocol {
	/**
	 * The separator in communication protocol
	 */
	public static final char SEPARATOR = '$';
	
	/**
	 * 
	 * @author jlibosva
	 *
	 */
	public static enum METHOD {GET, SAVE, ACK, ERROR, PING, REG};
	
	/**
	 * String for requesting data
	 */
	public static final String GET = "get";

	/**
	 * String for saving into the database
	 */
	public static final String SAVE = "save";
	
	/**
	 * String for acknowledge
	 */
	public static final String ACK = "ack";
	
	/**
	 * String for error
	 */
	public static final String ERROR = "error";
	
	public static final String PING = "ping";
	
	public static final String REG = "reg";
	
	
	/**
	 * 
	 * @author jlibosva
	 *
	 */
	public static enum SUBJECT {IMEI, IMSI, MCCMNC, SSE, LONG, LAT, TIME, SRC, ALT, AZIM, SPD, ACC, STOLEN, ACCOUNTS, CONTACTS, HISTORY, DEVICE, ALARM, GEODATA, IPWD };
	
	/**
	 * String for IMEI
	 */
	public static final String IMEI = "imei";
	
	/**
	 * String for IMSI
	 */
	public static final String IMSI = "imsi";
	
	/**
	 * String for MCC+MNC
	 */
	public static final String MCCMNC = "mccmnc";
	
	/**
	 * String for SIM SERIAL
	 */
	public static final String SSE = "sse";
	
	/**
	 * String for longitude
	 */
	public static final String LONG = "long";
	
	/**
	 * String for altitude
	 */
	public static final String LAT = "lat";
	
	/**
	 * String for time
	 */
	public static final String TIME = "time";
	
	/**
	 * String for source
	 */
	public static final String SRC = "source";
	
	/**
	 * String for height
	 */
	public static final String ALT = "altitude";
	
	/**
	 * String for azimuth
	 */
	public static final String AZIM = "azimuth"; 
	
	/**
	 * String for speed
	 */
	public static final String SPD = "speed";
	
	/**
	 * 
	 */
	public static final String ACC = "accuracy"; 
	
	/**
	 * 
	 */
	public static final String STOLEN = "stolen";
	
	public static final String ACCOUNTS = "accounts";
	
	public static final String CONTACTS = "contacts";
	
	public static final String HISTORY = "history";
	
	public static final String DEVICE = "device";
	
	public static final String ALARM = "alarm";
	
	public static final String GEODATA = "geodata";
	
	/**
	 * Invalid password
	 */
	public static final String IPWD = "ipwd";
}