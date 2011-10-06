package fut.android.storage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataObject implements  Serializable{
	//for serialization
	private static final long serialVersionUID = -3389022579204544766L;
	private static volatile DataObject INSTANCE = null;
	private Map<String, String> cache = new HashMap<String, String>();
	
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_SERVER_IP = "server_ip";
	public static final String KEY_SERVER_PORT = "server_port";
	public static final String KEY_CONTROL_DELAY = "control_delay";
	public static final String KEY_STOLEN_DELAY = "stolen_delay";
	public static final String KEY_DATA_TYPES = "data_types";

	private DataObject() {
	}

	public void setValue (String key, String value) {
		cache.put(key,value);
	}

	public String getValue (String key) {
		return cache.get(key);
	}

	public static DataObject getInstance() {
		if (INSTANCE == null ) {
			synchronized (DataObject.class) {
				if (INSTANCE == null ) {
					INSTANCE = new DataObject();
				}
			}
		}

		return INSTANCE;
	}

	//for deserialization
	protected Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}
	
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        synchronized (DataObject.class) {
            if (INSTANCE == null) {
                // re-initialize if needed
                INSTANCE = this; // only if everything succeeds
            }
        }
    }

}
