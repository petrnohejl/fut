package fut.android;

import android.accounts.AccountManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import fut.android.contact.ContactAPI;

public class DataManager {
	
	private TelephonyManager telephonyManager;
	private LocationManager locationManager;
	private AccountManager accountManager;
	private Cursor historyManager;
	private ContactAPI contactManager;
		
	public DataManager()
	{
		this.telephonyManager = null;
		this.locationManager = null;
		this.accountManager = null;
		this.historyManager = null;
		this.contactManager = null;
	}

	public void set(TelephonyManager telephonyManager) {
		this.telephonyManager = telephonyManager;
	}
	
	public void set(LocationManager locationManager) {
		this.locationManager = locationManager;
	}
	
	public void set(AccountManager accountManager) {
		this.accountManager = accountManager;
	}
	
	public void set(Cursor historyManager) {

		this.historyManager = historyManager;
	}
	
	public void set(ContactAPI contactManager) {
		this.contactManager = contactManager;
	}
	
	public TelephonyManager getTelephonyManager () {
		return this.telephonyManager;
	}
	
	public LocationManager getLocationManager () {
		return this.locationManager;
	}
	
	public AccountManager getAccountManager () {
		return this.accountManager;
	}
	
	public Cursor getHistoryManager () {
		return this.historyManager;
	}
	
	public ContactAPI getContactManager () {
		return this.contactManager;
	}
	
}
