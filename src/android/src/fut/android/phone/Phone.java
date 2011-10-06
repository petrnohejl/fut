package fut.android.phone;

import android.telephony.TelephonyManager;

//http://www.itwizard.ro/shared-preferences-119.html

/**
 * @author Petr Nohejl
 * @version 1.0
 * @since 1.0 - 22.11.2010
 */
public class Phone {
	
	private String IMEI; 
	private String IMSI; 
	private int SIMoperator;
	private String SIMoperatorName;
	private String SIMserial;
	private String SIMcountry;
	
	
	/**
     * Object with informations about phone and SIM.
     * 
     * @param telephonyManager
     *            phone manager 
     */
	public Phone(TelephonyManager telephonyManager)
	{
		this.refreshData(telephonyManager);
	}
	
	
	public void refreshData(TelephonyManager telephonyManager)
	{	
		this.IMEI = telephonyManager.getDeviceId();									// IMEI
		this.IMSI = telephonyManager.getSubscriberId();								// IMSI
		this.SIMoperator = Integer.parseInt( telephonyManager.getSimOperator() );	// id cislo operatora (23003) MCC+MNC
		this.SIMoperatorName = telephonyManager.getSimOperatorName();				// nazev operatora (Vodafone CZ)
		this.SIMserial = telephonyManager.getSimSerialNumber();						// cislo SIM
		this.SIMcountry = telephonyManager.getNetworkCountryIso();					// jazyk (cz)
	}
	
	
	public String getIMEI() {
 		return IMEI;
 	}
 	
 	public void setIMEI(String IMEI) {
 		this.IMEI = IMEI;
 	}
 	
 	public String getIMSI() {
 		return IMSI;
 	}
 	
 	public void setIMSI(String IMSI) {
 		this.IMSI = IMSI;
 	}
 	
 	public int getSIMoperator() {
 		return SIMoperator;
 	}
 	
 	public void setSIMoperator(int SIMoperator) {
 		this.SIMoperator = SIMoperator;
 	}
 	
 	public String getSIMoperatorName() {
 		return SIMoperatorName;
 	}
 	
 	public void setSIMoperatorName(String SIMoperatorName) {
 		this.SIMoperatorName = SIMoperatorName;
 	}
 	
 	public String getSIMserial() {
 		return SIMserial;
 	}
 	
 	public void setSIMserial(String SIMserial) {
 		this.SIMserial = SIMserial;
 	}
 	
 	public String getSIMcountry() {
 		return SIMcountry;
 	}
 	
 	public void setSIMcountry(String SIMcountry) {
 		this.SIMcountry = SIMcountry;
 	}

}
