/**
 *
 */
package fut.common;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 29.11.2010
 */
public class GeodataObject implements Serializable {
    private static final long serialVersionUID = 1244802605351429073L;
    private long id;
    private double longitude;
    private double latitude;
    private double altitude;
    private java.sql.Timestamp time;
    private float azimuth;
    private float speed;
    private float accuracy;
    private long geo_source_id;
    private String geo_source;
    /**
     * @param longitude
     * @param latitude
     * @param altitude
     * @param time
     * @param azimuth
     * @param speed
     * @param accuracy
     * @param geo_source_id
     * @param geo_source
     */
    public GeodataObject(long id, double longitude, double latitude, double altitude,
	    Timestamp time, float azimuth, float speed, float accuracy,
	    long geo_source_id, String geo_source) {
	this.id = id;
	this.longitude = longitude;
	this.latitude = latitude;
	this.altitude = altitude;
	this.time = time;
	this.azimuth = azimuth;
	this.speed = speed;
	this.accuracy = accuracy;
	this.geo_source_id = geo_source_id;
	this.geo_source = geo_source;
    }
    
    public long getId() {
	return id;
    }
    public double getLongitude() {
        return longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getAltitude() {
        return altitude;
    }
    public java.sql.Timestamp getTime() {
        return time;
    }
    public float getAzimuth() {
        return azimuth;
    }
    public float getSpeed() {
        return speed;
    }
    public float getAccuracy() {
        return accuracy;
    }
    public long getGeo_source_id() {
        return geo_source_id;
    }
    public String getGeo_source() {
        return geo_source;
    }


}
