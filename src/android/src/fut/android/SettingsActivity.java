package fut.android;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import fut.android.loop.Loop;
import fut.android.net.InvalidUserException;
import fut.android.net.Net;
import fut.android.storage.DataObject;
import fut.android.storage.Storage;


public class SettingsActivity extends PreferenceActivity {
		
	private boolean minimized = false;
	private Intent intent;
	private Storage storage;
	private AlertDialog alertDialog;
	private DataObject data;
	private String defaultPasswd;
	private String defaultControl;
	private String defaultStolen;
	
	final Pattern IP_PATTERN = Pattern.compile(
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	@Override
	public void onResume()
	{
		Log.d("FUT", "SettingsActivity - onDestroy");
		
		super.onResume();
		if (minimized)
		{
			minimized = false;
			Intent intent = new Intent();
			setResult(RESULT_OK, intent);
			finish();
		}
	}
	
	@Override
	public void onPause()
	{
		Log.d("FUT", "SettingsActivity - onPause");
		
		super.onPause();
		minimized = true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Log.d("FUT", "SettingsActivity - onCreate");
			
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	    
		storage = new Storage(SettingsActivity.this);	    
		intent = new Intent(this, Loop.class);
	    
		/*DataManager dm = new DataManager();
		dm.set((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE));
		dm.set((LocationManager)getSystemService(Context.LOCATION_SERVICE));
		dm.set(AccountManager.get(this));
		Cursor hist = getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");
		startManagingCursor(hist);
		dm.set(hist);
		ContactAPI api = ContactAPI.getAPI();
		api.setCr(getContentResolver());
		dm.set(api);*/
	    
		// Dialog pro vypis chyby
		alertDialog = new AlertDialog.Builder(SettingsActivity.this).create();
		alertDialog.setTitle("Error");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		
		// Nacteni defaultnich hodnot
		try {
			data = storage.loadPreferences();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		defaultPasswd = data.getValue(DataObject.KEY_PASSWORD);
		defaultControl = data.getValue(DataObject.KEY_CONTROL_DELAY);
		defaultStolen = data.getValue(DataObject.KEY_STOLEN_DELAY);

		// Zmena hesla uzivatele
		Preference passwdPref = (Preference) findPreference("password");
		passwdPref.setDefaultValue((Object) defaultPasswd);
		passwdPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				stopServ();
				
				try {
					if (newValue.toString().equals("")) // Nesmime zadat prazdny retezec
					{
						alertDialog.setMessage("Missing password");
						alertDialog.show();
						return false;
					}
					else {
						DataObject data = DataObject.getInstance();
						data.setValue(DataObject.KEY_PASSWORD, newValue.toString());
						storage.savePreferences(data);
						startServ();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
						
				return true;
			}
		});
	    
		// Zmena portu
		Preference portPref = (Preference) findPreference("port");
		portPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				boolean err = false;
				
				stopServ();
								
				try {
					int value = Integer.parseInt(newValue.toString());
					if(value < 0 || value > 65535) err = true;
				} catch (NumberFormatException e){
					err = true;
				}
				
				if (err) {
					alertDialog.setMessage("Invalid value. Set number between 0 and 65535.");
					alertDialog.show();
					return false;
				}
				else {
					try {
						DataObject data = DataObject.getInstance();
						data.setValue(DataObject.KEY_SERVER_PORT, newValue.toString());
						storage.savePreferences(data);
						startServ();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				return true;
			}
		});
	    
		// Zmena IP adresy
		Preference ipAddPref = (Preference) findPreference("ipAddress");
		ipAddPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	    	
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				stopServ();
				if (IP_PATTERN.matcher(newValue.toString()).matches())
				{
					try {
						DataObject data = DataObject.getInstance();
						data.setValue(DataObject.KEY_SERVER_IP, newValue.toString());
						storage.savePreferences(data);
						startServ();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					alertDialog.setMessage("Invalid IP value");
					alertDialog.show();
					return false;
				}
				
				return true;
			}
		});
	    
		// Zmena casu pro zjistovani, zda byl mobil ukraden
		Preference controlPref = (Preference) findPreference("controlDelay");
		controlPref.setDefaultValue((Object) defaultControl);
		controlPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				stopServ();
				try {
					DataObject data = DataObject.getInstance();
					data.setValue(DataObject.KEY_CONTROL_DELAY, newValue.toString());
					storage.savePreferences(data);
					startServ();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return true;
			}
		});
	    
		// Zmena casu pro zasila dat na server
		Preference stolenPref = (Preference) findPreference("stolenDelay");
		stolenPref.setDefaultValue((Object) defaultStolen);
		stolenPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				stopServ();
				try {
					DataObject data = DataObject.getInstance();
					data.setValue(DataObject.KEY_STOLEN_DELAY, newValue.toString());
					storage.savePreferences(data);
					startServ();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return true;
			}
		});
	    
		// Vyber dat, ktera se budou posilat na server
		Preference dataPref = (Preference) findPreference("data");	    
		dataPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				stopServ();
				// Zatim jenom vypisuje vybrane polozky
				String[] selected = ListPreferenceMultiSelect.parseStoredValue(newValue.toString());
				char [] dataTypes = {'0', '0', '0'};
				if (selected != null)
	            {                  
                    for (int i = 0; i < selected.length; i++) 
                    {
                        if (selected[i].equals("contacts")) dataTypes[0] = '1';
                        if (selected[i].equals("callHistory")) dataTypes[1] = '1';
                        if (selected[i].equals("accounts")) dataTypes[2] = '1';
                    }
	            }
				
				try {
					DataObject data = DataObject.getInstance();
					data.setValue(DataObject.KEY_DATA_TYPES, new String(dataTypes));
					Log.d("FUT", "SettingActivity: " + new String(dataTypes));
					storage.savePreferences(data);
					startServ();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return true;
			}
		});
		
		//Checks set data for connection
	    startServ();
	}
		
		
	// Uprava tlacitka ZPET (nevrati se na obrazovku prihlaseni/registrace)
		
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (Integer.valueOf(android.os.Build.VERSION.SDK) < 7
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			onBackPressed();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
		return;
	}
	
	private void startServ() {
//		ProgressDialog pd = new ProgressDialog(getApplicationContext());
//		pd.setCancelable(false);
//		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		pd.setTitle("FUT");
//		pd.setMessage("Connecting to the server");
//		pd.show();
		
		try {
			String ip = data.getValue(DataObject.KEY_SERVER_IP);
			String port = data.getValue(DataObject.KEY_SERVER_PORT);
			if (port == null || ip == null) return;
			
			Net net = new Net(ip, Integer.parseInt(port), data.getValue(DataObject.KEY_PASSWORD));
			Log.d("FUT", "startServ - trying to connect to the server");
			net.connect(data.getValue(DataObject.KEY_USERNAME));
			Log.d("FUT", "startServ - connection was successfully estabilished, disconnecting");
			net.disconnect();
			Log.d("FUT", "startServ - disconnected successfully");
		} catch(UnknownHostException e) {
			alertDialog.setMessage("Unknown host");
			alertDialog.show();
			return;
		} catch (IOException e) {
			alertDialog.setMessage("Cannot connect to the server");
			alertDialog.show();
			return;
		} catch (InvalidUserException e) {
			alertDialog.setMessage("Bad username or password");
			alertDialog.show();
			return;
		}		
		Log.d("FUT", "startServ - showing toast about connection");
		Toast toast = Toast.makeText(getApplicationContext(), "Connected to the FUT server", Toast.LENGTH_SHORT);
		toast.show();
		startService(intent);
	}
	
	private void stopServ() {
		stopService(intent);
	}

}
