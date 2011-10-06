package fut.server.net;

import java.sql.Timestamp;
import java.util.Map;

import fut.server.db.DBLogic;
import fut.server.exception.ParserException;

public class DBSaver_geodata {
	private Double longitude;
	private Double latitude;
	private Double altitude;
	private Timestamp timestamp;
	private Float azimuth;
	private Float speed;
	private Float accuracy;
	private Long geosrc;
	private String id;
	private Map<String, Long> geoSources;
	
	public DBSaver_geodata (String id) {
		this.id = new String(id);
		reset();
		geoSources = DBLogic.getInstance().listGeoSources();
	}
	
	/**
	 * Initializes values to zero
	 */
	private void reset() {
		longitude = null;
		latitude = null;
		altitude = null;
		timestamp = null;
		azimuth = null;
		speed = null;
		accuracy = null;
		geosrc = null;
	}
	
	public void setLNG(String lng) throws ParserException {
		try {
			longitude = new Double(lng);
		} catch (NumberFormatException e) {
			System.err.println("Error setLNG conversion to double " + lng);
			throw new ParserException("");
		}
		check();
	}
	
	public void setLAT(String lat) throws ParserException {
		try {
			latitude = new Double(lat);
		} catch (NumberFormatException e) {
			System.err.println("Error setLAT conversion to double " + lat);
			throw new ParserException("");
		}
		check();
	}
	
	public void setALT(String alt) throws ParserException {
		try {
			altitude = new Double(alt);
		} catch (NumberFormatException e) {
			System.err.println("Error setALT conversion to double " + alt);
			throw new ParserException("");
		}
		check();
	}
	
	public void setTIME(String time) throws ParserException {
		try {
			timestamp = new Timestamp(Long.parseLong(time));
		} catch(NumberFormatException e) {
			throw new ParserException("");
		}
		check();
	}
	
	public void setAZIM(String azim) throws ParserException {
		try {
			azimuth = new Float(azim);
		} catch (NumberFormatException e) {
			System.err.println("Error setAZIM conversion to double " + azim);
			throw new ParserException("");
		}
		check();
	}
	
	public void setSPD(String spd) throws ParserException {
		try {
			speed = new Float(spd);
		} catch (NumberFormatException e) {
			System.err.println("Error setSPD conversion to double " + spd);
			throw new ParserException("");
		}
		check();
	}
	
	public void setACC(String acc) throws ParserException {
		try {
			accuracy = Float.parseFloat(acc);
		} catch (NumberFormatException e) {
			System.err.println("Error setSPD conversion to double " + acc);
			throw new ParserException("");
		}
		check();
	}
	
	public void setSRC(String src) throws ParserException {
		try {
			this.id = id;
			geosrc = geoSources.get(src);
		} catch (NumberFormatException e) {
			System.err.println("Error setSRC conversion to double " + src);
			throw new ParserException("");
		}
		check();
	}

	/**
	 * Checks if all data are set
	 */
	private void check() {
		if (geosrc != null && geosrc == geoSources.get("network") && longitude != null && latitude != null && timestamp != null) {
			System.out.println("Saving to the database data from network");
			if (DBLogic.getInstance().createGeodata(longitude, latitude, null, timestamp, null, null, null, geosrc, id) == -1) {
				System.err.println("Cannot save data into the database in DBSaver");
			}	
			reset();
		} else if (geosrc != null && geosrc == geoSources.get("gps") && longitude != null && latitude != null && altitude != null && timestamp != null && azimuth != null && 
				speed != null && accuracy != null) {
			System.out.println("Saving to the database data from network");
			if (DBLogic.getInstance().createGeodata(longitude, latitude, altitude, timestamp, azimuth, speed, accuracy, geosrc, id) == -1) {
				System.err.println("Cannot save data into the database in DBSaver");
			}	
			reset();
		} else {
			System.out.println("Still cannot save to the database");
		}
	}
}
