/**
 *
 */
package fut.common;

import java.io.Serializable;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 4.12.2010
 */
public class FileObject implements Serializable {
    private static final long serialVersionUID = 9137035291479826852L;
    private long id;
    private String filename;
    private String meta;
    private long data_type_id;
    private String data_type;
    private long device_id;
    private byte[] data;
    private java.sql.Timestamp time;

    /**
     * @param id
     * @param filename
     * @param meta
     * @param data_type_id
     * @param data_type
     * @param device_id
     * @param data
     */
    public FileObject(long id, String filename, String meta, java.sql.Timestamp time, long data_type_id,
	    String data_type, long device_id, byte[] data) {
	this.id = id;
	this.filename = filename;
	this.meta = meta;
	this.data_type_id = data_type_id;
	this.data_type = data_type;
	this.device_id = device_id;
	this.data = data;
	this.time = time;
    }
    public long getId() {
        return id;
    }
    public String getFilename() {
        return filename;
    }
    public String getMeta() {
        return meta;
    }
    public long getData_type_id() {
        return data_type_id;
    }
    public String getData_type() {
        return data_type;
    }
    public long getDevice_id() {
        return device_id;
    }
    public byte[] getData() {
        return data;
    }
    public java.sql.Timestamp getTime() {
	return time;
    }


}
