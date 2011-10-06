package fut.android.loop;

import java.io.IOException;
import java.net.UnknownHostException;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import fut.android.DataManager;
import fut.android.R;
import fut.android.account.Accounts;
import fut.android.call.CallHistory;
import fut.android.contact.ContactAPI;
import fut.android.contact.ObjectContactList;
import fut.android.geolocation.Geolocation;
import fut.android.net.InvalidUserException;
import fut.android.net.Net;
import fut.android.phone.Phone;
import fut.android.storage.DataObject;
import fut.android.storage.Storage;

/** 
 * The main polling loop
 * @author jlibosva
 */
public class Loop extends Service {
	
	private static final String TAG = "FUT";
	
	/**
	 * Defines whether main loop runs
	 */
	private boolean run;
	private Net net;
	private DataObject data;
	private DataManager dmanager;
	private int interval;
	boolean stolen;
	boolean alarm;
	private Storage storage;
	String uname;
	Thread main;
	private MediaPlayer mp;
	
	
	/**
	 * Starts the main loop
	 */
	private void start() {
		if (run) {
			return;
		}
		
		Log.d(TAG,"Loop: start");
		String address = data.getValue(DataObject.KEY_SERVER_IP);
		int port = Integer.parseInt(data.getValue(DataObject.KEY_SERVER_PORT));
		String password = data.getValue(DataObject.KEY_PASSWORD);
		interval = Integer.parseInt(data.getValue(DataObject.KEY_CONTROL_DELAY));
		uname = data.getValue(DataObject.KEY_USERNAME);
		stolen = false;
		run = true;
		
		Log.d(TAG,"Loop: Creating net");
		net = new Net(address, port, password);
		Log.d(TAG, "Loop: Net created successfully");
		
		main = new Thread() {
			public void run() {
				try {
					Log.d(TAG, "Loop: Connecting to the server");
					net.connect(uname);
				} catch (UnknownHostException e) {
					Log.e(TAG, "Loop: Connecting: UnknownHostException: " + e.getMessage());
					run = false;
					return;
				} catch (IOException e) {
					Log.e(TAG, "Loop: Connecting: IOException: " + e.getMessage());
					run = false;
					return;
				} catch (InvalidUserException e) {
					Log.e(TAG, "Loop: Connecting: InvalidUserException: " + e.getMessage());
					run = false;
					return;
				}
				
				
				/** The main loop */
				while(run) {
					Log.d(TAG, "Loop: Begin of the loop");
					service();
					try {
						net.disconnect();
						Log.d(TAG, "Loop: Sleeping " + interval + " seconds");
						Thread.sleep(interval*1000);
						net.connect(uname);
					} catch (InterruptedException e) {						
						Log.e(TAG, "Loop: InterruptedException: " + e.getMessage());
						System.out.println("Interrupt came");
					} catch (IOException e) {					
						Log.e(TAG, "Loop: IOException: " + e.getMessage());
						e.printStackTrace();
					} catch (InvalidUserException e) {					
						Log.e(TAG, "Loop: InvalidUserException: " + e.getMessage());
						e.printStackTrace();
					}
				}
				
				try {
					Log.d(TAG, "Loop: interrupted");
					net.disconnect();
				} catch (IOException e) {
					Log.e(TAG, "Loop interrupted: IO exception: " + e.getMessage());
				}
			}
		};
		
		main.setDaemon(true);
		main.start();
	}
	
	/**
	 * Stops the main loop
	 */
	public void stop() {
		run = false;
		if (main != null) {
			main.interrupt();
		}
		main = null;
	}
	
	/**
	 * 
	 * @param st
	 * @return If everything is set properly, returns true
	 */
	public boolean checkSettings() {
		if (data.getValue(DataObject.KEY_SERVER_IP) == null || 
				data.getValue(DataObject.KEY_SERVER_PORT) == null ||
				data.getValue(DataObject.KEY_CONTROL_DELAY) == null || 
				data.getValue(DataObject.KEY_STOLEN_DELAY) == null) {
			Log.d(TAG, "checkSettings: Data are still not properly set");
			return false; 
		}
		
		Log.d(TAG, "checkSettings: Seems like all needed is set");
		return true;
	}
	
	/**
     * 
     */
    private void service() {
        String subject;
        
        // Stolen
        Log.d(TAG, "Service: checking stolen");
        if (net.getStolen(uname)) {
            interval = Integer.parseInt(data.getValue(DataObject.KEY_STOLEN_DELAY));
            stolen = true;
            Log.d(TAG, "Service: stolen is starting");
        } else if (stolen) {
            interval = Integer.parseInt(data.getValue(DataObject.KEY_CONTROL_DELAY));
            stolen = false;
            Log.d(TAG, "Service: stolen is stopping");
        }
        
        
        // Alarm
        Log.d(TAG, "Service: checking alarm");
        if (net.getAlarm(uname)) {
        	if (!alarm) {
	        	Log.d(TAG, "Service: alarm is starting");
	        	mp.start(); 
	        	alarm = true;
        	}
        } else if (alarm) {
        	Log.d(TAG, "Service: alarm is stopping");
        	mp.pause(); 
        	alarm = false;      	
        }
        

        // Stolen mode - posila data
        if (stolen) {
        	Log.d(TAG, "Service: stolen is true");
        	     	
        	// Phone infos            
            Phone phone = new Phone(dmanager.getTelephonyManager());
            subject = phone.getIMEI();
            Log.d(TAG, "Service: sending IMEI: " + subject);
            if (net.sendIMEI(uname, subject) != 0) {
                Log.e(TAG,"Service: error sending an IMEI");
            }
                        
            subject = phone.getIMSI();
            Log.d(TAG, "Service: sending IMSI: " + subject);
            if (net.sendIMSI(uname, subject) != 0) {
                Log.e(TAG,"Service: error sending an IMSI");
            }
                                 
            subject = Integer.toString(phone.getSIMoperator());
            Log.d(TAG, "Service: sending SIM operator: " + subject);
            if (net.sendMCCMNC(uname, subject) != 0) {
                Log.e(TAG,"Service: error sending an MCCMNC");
            }
                     
            subject = phone.getSIMserial();
            Log.d(TAG, "Service: sending SIM serial: " + subject);
            if (net.sendSSE(uname, subject) != 0) {
                Log.e(TAG,"Service: error sending an SIM serial");
            }
            
          
            // Geolocation
            Geolocation geolocation = new Geolocation(dmanager.getLocationManager());
            if(geolocation.getGPSsuccess()) {
            	Log.d(TAG, "Service: sending an location GPS SRC");
                if (net.sendSRC(uname, "gps") != 0) {
                    Log.e(TAG, "Service: error sending an SRC");
                }

                subject = Double.toString(geolocation.getGPSaltitude());
                Log.d(TAG, "Service: sending an location GPS altitude: " + subject);
                if (net.sendALT(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an ALT");
                }

                subject = Float.toString(geolocation.getGPSbearing());
                Log.d(TAG, "Service: sending an location GPS azimuth: " + subject);
                if (net.sendAZIM(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an AZIM");
                }

                subject = Float.toString(geolocation.getGPSspeed());
                Log.d(TAG, "Service: sending an location GPS speed: " + subject);
                if (net.sendSPD(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an SPD");
                }

                subject = Float.toString(geolocation.getGPSaccuracy());
                Log.d(TAG, "Service: sending an location GPS accuracy: " + subject);
                if (net.sendACC(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an ACC");
                }

                subject = Long.toString(geolocation.getGPStime());
                Log.d(TAG, "Service: sending an location GPS time: " + subject);
                if (net.sendTIME(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an TIME");
                }
                
                subject = Double.toString(geolocation.getGPSlatitude());
                Log.d(TAG, "Service: sending an location GPS latitude: " + subject);
                if (net.sendLAT(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an LAT");
                }

                subject = Double.toString(geolocation.getGPSlongitude());
                Log.d(TAG, "Service: sending an location GPS longitude: " + subject);
                if (net.sendLONG(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an LONG");
                }
            } else {
            	Log.d(TAG, "Cannot retrieve position from GPS");
            }
            if (geolocation.getNETsuccess()) {
            	Log.d(TAG, "Service: sending an NET SRC");
                if (net.sendSRC(uname, "network") != 0) {
                    Log.e(TAG, "Service: error sending an SRC");
                }
                
                subject = Double.toString(geolocation.getNETlatitude());
                Log.d(TAG, "Service: sending an location NET latitude: " + subject);
                if (net.sendLAT(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an latitude");
                }
                
                subject = Double.toString(geolocation.getNETlongitude());
                Log.d(TAG, "Service: sending an location NET longitude: " + subject);
                if (net.sendLONG(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an Longitude");
                }
                
                subject = Long.toString(geolocation.getNETtime());
                Log.d(TAG, "Service: sending an location NET time: " + subject);
                if (net.sendTIME(uname, subject) != 0) {
                    Log.e(TAG, "Service: error sending an Time");
                }
            } else {
            	Log.d(TAG, "Cannot retrieve position from network");
            }
            
            // Kontaky
            subject = data.getValue(DataObject.KEY_DATA_TYPES);
            if(subject.charAt(0)=='1')
            {
	            ObjectContactList contacts = dmanager.getContactManager().newContactList();
	            subject = dmanager.getContactManager().createCsvString(contacts);
	            Log.d(TAG, "Service: sending contacts");
	            if (net.sendContacts(uname, subject) != 0) {
	                Log.e(TAG, "Service: error sending contacts");
	            }
            }

            // Historie
            subject = data.getValue(DataObject.KEY_DATA_TYPES);
            if(subject.charAt(1)=='1')
            {
	            CallHistory history = new CallHistory(dmanager.getHistoryManager());
	            subject = history.createCsvString();
	            Log.d(TAG, "Service: sending history");
	            if (net.sendHistory(uname, subject) != 0) {
	                Log.e(TAG, "Service: error sending history");
	            }
            }
            
            // Ucty
            subject = data.getValue(DataObject.KEY_DATA_TYPES);
            if(subject.charAt(2)=='1')
            {
	            Accounts accounts = new Accounts(dmanager.getAccountManager());
	            subject = accounts.createCsvString();
	            Log.d(TAG, "Service: sending accounts");
	            if (net.sendAccounts(uname, subject) != 0) {
	                Log.e(TAG, "Service: error sending accounts");
	            }
            }
        }
    }
	
	/**
	 * Checks wheter loop is running
	 * @return True, if loop is running. False otherwise
	 */
	public boolean isRunning() {
		return this.run;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Loop: onCreate");
		
		storage = new Storage(Loop.this);
		try {
			data = storage.loadPreferences();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		dmanager = new DataManager();
		dmanager.set((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE));
		dmanager.set((LocationManager)getSystemService(Context.LOCATION_SERVICE));
		dmanager.set(AccountManager.get(this));
	    Cursor hist = getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");
	    //startManagingCursor(hist);
	    dmanager.set(hist);
	    ContactAPI api = ContactAPI.getAPI();
        api.setCr(getContentResolver());
        dmanager.set(api);
        
        // alarm
        mp = MediaPlayer.create(Loop.this, R.raw.siren);
    	mp.setLooping(true);
    	alarm = false;
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "Loop: onStart");
		run = false;
		
		if (checkSettings()) {
			start();
		}
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "Loop: onDestroy");
		stop();
	}

}
