package fut.android.geolocation;

import java.sql.Timestamp;

import android.location.Location;
import android.location.LocationManager;

/**
 * @author Petr Nohejl
 * @version 1.0
 * @since 1.0 - 22.11.2010
 */
public class Geolocation {
	
	private double GPSlatitude;
	private double GPSlongitude;
	private double GPSaltitude;
	private float GPSbearing;
	private float GPSspeed;
	private float GPSaccuracy;
	private long GPStime;	
	private String GPStimeText;
	private boolean GPSsuccess;
	
	private double NETlatitude;
	private double NETlongitude;
	private long NETtime;
	private String NETtimeText;
	private boolean NETsuccess;
	
	
	/**
     * Object with informations about geolocation.
     * 
     * @param locationManager
     *            location manager 
     */
	public Geolocation(LocationManager locationManager)
	{
		refreshData(locationManager);
	}
	
	
	public void refreshData(LocationManager locationManager)
	{	
		// nacteni GPS dat
		Location GPSlocation = null;
		try
		{
			GPSlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);				
		}
		catch(IllegalArgumentException e)
		{
			GPSlocation = null;
		}
		if (GPSlocation != null)
		{	
			if(GPSlocation.hasAccuracy()) 
				this.GPSaccuracy = GPSlocation.getAccuracy();
			else
				this.GPSaccuracy = 0;
			
			if(GPSlocation.hasAltitude())
				this.GPSaltitude = GPSlocation.getAltitude();
			else
				this.GPSaltitude = 0;
			
			if(GPSlocation.hasBearing()) 
				this.GPSbearing = GPSlocation.getBearing();
			else
				this.GPSbearing = 0;
			
			if(GPSlocation.hasSpeed())
				this.GPSspeed = GPSlocation.getSpeed();
			else
				this.GPSspeed = 0;
			
			Timestamp ts = new Timestamp(GPSlocation.getTime());
			this.GPStimeText = ts.toString();

			this.GPSlatitude = GPSlocation.getLatitude();
			this.GPSlongitude = GPSlocation.getLongitude();			
			this.GPStime = GPSlocation.getTime();
			this.GPSsuccess = true;
		}
		else
		{
			this.GPSaccuracy = 0;
			this.GPSaltitude = 0;
			this.GPSbearing = 0;
			this.GPSspeed = 0;
			this.GPSlatitude = 0;
			this.GPSlongitude = 0;			
			this.GPStime = 0;
			this.GPStimeText = "";
			this.GPSsuccess = false;
		}	
		
		// nacteni GPS dat
		Location NETlocation = null;
		try
		{
			NETlocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);				
		}
		catch(IllegalArgumentException e)
		{
			NETlocation = null;
		}	
		if (NETlocation != null)
		{	
			Timestamp ts = new Timestamp(NETlocation.getTime());
			this.NETtimeText = ts.toString();
			
			this.NETlatitude = NETlocation.getLatitude();
			this.NETlongitude = NETlocation.getLongitude();			
			this.NETtime = NETlocation.getTime();
			this.NETsuccess = true;
		}
		else
		{
			this.NETlatitude = 0;
			this.NETlongitude = 0;			
			this.NETtime = 0;
			this.NETsuccess = false;
		}
	}
	
	
	public double getGPSlatitude() {
 		return GPSlatitude;
 	}
 	
 	public void setGPSlatitude(double GPSlatitude) {
 		this.GPSlatitude = GPSlatitude;
 	}
 	
 	public double getGPSlongitude() {
 		return GPSlongitude;
 	}
 	
 	public void setGPSlongitude(double GPSlongitude) {
 		this.GPSlongitude = GPSlongitude;
 	}
 	
 	public double getGPSaltitude() {
 		return GPSaltitude;
 	}
 	
 	public void setGPSaltitude(double GPSaltitude) {
 		this.GPSaltitude = GPSaltitude;
 	}
 	
 	public float getGPSbearing() {
 		return GPSbearing;
 	}
 	
 	public void setGPSbearing(float GPSbearing) {
 		this.GPSbearing = GPSbearing;
 	}
 	
 	public float getGPSspeed() {
 		return GPSspeed;
 	}
 	
 	public void setGPSspeed(float GPSspeed) {
 		this.GPSspeed = GPSspeed;
 	}
 	
 	public float getGPSaccuracy() {
 		return GPSaccuracy;
 	}
 	
 	public void setGPSaccuracy(float GPSaccuracy) {
 		this.GPSaccuracy = GPSaccuracy;
 	}
 	
 	public long getGPStime() {
 		return GPStime;
 	}
 	
 	public void setGPStime(long GPStime) {
 		this.GPStime = GPStime;
 	}
 	
 	public String getGPStimeText() {
 		return GPStimeText;
 	}
 	
 	public void setGPStimeText(String GPStimeText) {
 		this.GPStimeText = GPStimeText;
 	}
 	
 	public boolean getGPSsuccess() {
 		return GPSsuccess;
 	}
 	
 	public void setGPSsuccess(boolean GPSsuccess) {
 		this.GPSsuccess = GPSsuccess;
 	}
 	
 	public double getNETlatitude() {
 		return NETlatitude;
 	}
 	
 	public void setNETlatitude(double NETlatitude) {
 		this.NETlatitude = NETlatitude;
 	}
 	
 	public double getNETlongitude() {
 		return NETlongitude;
 	}
 	
 	public void setNETlongitude(double NETlongitude) {
 		this.NETlongitude = NETlongitude;
 	}
 	
 	public long getNETtime() {
 		return NETtime;
 	}
 	
 	public void setNETtime(long NETtime) {
 		this.NETtime = NETtime;
 	}
 	
 	public String getNETtimeText() {
 		return NETtimeText;
 	}
 	
 	public void setNETtimeText(String NETtimeText) {
 		this.NETtimeText = NETtimeText;
 	}
 	
 	public boolean getNETsuccess() {
 		return NETsuccess;
 	}
 	
 	public void setNETsuccess(boolean NETsuccess) {
 		this.NETsuccess = NETsuccess;
 	}
	
}
