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
public class DeviceObject implements Serializable {
    private static final long serialVersionUID = -2759478299351497805L;
    private long id;
    private long imei;
    private long imsi;
    private String sim;
    private int operator;
    private long user_id;
    private boolean stolen;
    /**
     * @param id
     * @param imei
     * @param imsi
     * @param sim
     * @param operator
     * @param user_id
     * @param stolen
     */
    public DeviceObject(long id, long imei, long imsi, String sim, int operator,
	     boolean stolen, long user_id) {
	this.id = id;
	this.imei = imei;
	this.imsi = imsi;
	this.sim = sim;
	this.operator = operator;
	this.user_id = user_id;
	this.stolen = stolen;
    }
    public long getId() {
        return id;
    }
    public long getImei() {
        return imei;
    }
    public long getImsi() {
        return imsi;
    }
    public String getSim() {
        return sim;
    }
    public int getOperator() {
        return operator;
    }
    public long getUser_id() {
        return user_id;
    }
    public boolean isStolen() {
        return stolen;
    }


}
