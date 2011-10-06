package fut.android;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import fut.android.storage.DataObject;
import fut.android.storage.Storage;

public class FUTActivity extends Activity {
	
	private AlertDialog alertDialog;
	
	@Override
	public void onResume()
	{
		// Vymazani textovych poli
		super.onResume();
		final EditText name = (EditText) findViewById(R.id.name);
		name.setText("");
		final EditText password = (EditText) findViewById(R.id.password);
		password.setText("");
		// Nastaveni focusu na prvni pole
		name.requestFocus();
		// Zmena nadpisu (registrace/prihlaseni)
		final TextView title = (TextView) findViewById(R.id.title);
		if (new Storage(FUTActivity.this).fileExist()) title.setText("Sign In");
		else title.setText("Registration");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Dialog pro vypis chyby
		alertDialog = new AlertDialog.Builder(FUTActivity.this).create();
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
        
		// Textova pole
		final EditText name = (EditText) findViewById(R.id.name);
		final EditText password = (EditText) findViewById(R.id.password);
        
		// Tlacitko pro registraci/prihlaseni
		final Button okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Storage storage = new Storage(FUTActivity.this);
				
				try {
					if (storage.fileExist()) { // Existuje soubor pro ulozeni nastaveni -> prihlaseni
						
						DataObject data = storage.loadPreferences();
						// Kontrola jmena a hesla
						if (name.getText().toString().equals(data.getValue(DataObject.KEY_USERNAME)) &&  password.getText().toString().equals(data.getValue(DataObject.KEY_PASSWORD))) {
							Intent myIntent = new Intent(FUTActivity.this, SettingsActivity.class);
							startActivityForResult(myIntent, 0);
						} else {
							alertDialog.setTitle("Access denied");
							alertDialog.setMessage("Incorrect user name or password");
							alertDialog.show();
						}
					} else { // Neexistuje soubor pro ulozeni nastaveni -> registrace
						String strname = name.getText().toString();
						String strpsswd = password.getText().toString();
						if (strname != null && strpsswd != null && !strname.equals("") && !strpsswd.equals("")) {
							DataObject data = DataObject.getInstance();
							data.setValue(DataObject.KEY_USERNAME, strname);
							data.setValue(DataObject.KEY_PASSWORD, strpsswd);
							data.setValue(DataObject.KEY_CONTROL_DELAY, "1800");
							data.setValue(DataObject.KEY_STOLEN_DELAY, "600");
							data.setValue(DataObject.KEY_DATA_TYPES, "111");
							//TODO checkboxy 
							storage.savePreferences(data);
							
							
							Intent myIntent = new Intent(FUTActivity.this, SettingsActivity.class);
							startActivityForResult(myIntent, 0);
						} else {
							alertDialog.setTitle("Registration failed");
							alertDialog.setMessage("Please enter your name and password");
							alertDialog.show();
						}
					}
				} catch (IOException e) {
					//TODO daco treba spravit , zalogovat , alebo dat uzivatelovi vediet co sa stalo
					e.printStackTrace();
				}
			}
		});
        
		// Tlacitko pro minimalizaci aplikace
		final Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				moveTaskToBack(false);
			}
		});
	}
	
}