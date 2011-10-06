package fut.server.net;

import java.io.IOException;

import fut.server.db.DBLogic;
import fut.server.exception.ParserException;

public class DBSaver_device {
	
	private String id;
	private Long IMEI;
	private Long IMSI;
	private Integer MSSMNC;
	private String SSE;
	
	private void reset() {
		IMEI = null;
		IMSI = null;
		MSSMNC = null;
		SSE = null;
	}
	
	/**
	 * Constructor
	 * @param id User's ID
	 */
	public DBSaver_device(String id) {
		this.id = new String(id);
		reset();
	}
	
	public void setIMEI(String imei) throws ParserException {
		try {
			this.id = id;
			this.IMEI = new Long(imei);
		} catch (NumberFormatException e) {
			System.err.println("Error in parsing number setIMEI " + imei);
		}
		check();
	}
	
	public void setIMSI(String imsi) throws ParserException {
		try {
			this.IMSI = new Long(imsi);
		} catch (NumberFormatException e) {
			System.err.println("Error in parsing number setIMSI " + imsi);
		}
		check();
	}
	
	public void setMSSMNC(String ms) throws ParserException {
		try {
			this.MSSMNC = new Integer(ms);
		} catch (NumberFormatException e) {
			System.err.println("Error in parsing number setMSSMNC " + ms + " : " + e.getMessage());
		}
		check();
	}
	
	public void setSSE(String sse) throws ParserException {
		this.SSE = sse;
		check();
	}
	
	/**
	 * Checks if all data are set
	 */
	private void check() throws ParserException {
		if (IMSI == null || IMEI == null || SSE == null || MSSMNC == null) {
			System.out.println("Still cannot save");
			return;
		}
		
		System.out.println("Saving device info into the DB");
		System.out.println("IMEI: " + IMEI);
		System.out.println("IMSI: " + IMSI);
		System.out.println("SSE: " + SSE);
		System.out.println("MSSMNC: " + MSSMNC);
		if (DBLogic.getInstance().updateDevice(IMEI, IMSI, SSE, MSSMNC, id)) {
			System.out.println("Save completed");
		}
		else {
			System.err.println("Cannot save data into the database in DBSaver");
			throw new ParserException("Unsuccessful DB saving");
		}
		reset();
	}
}
