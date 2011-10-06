package fut.android.call;

//TODO kontrolovat carky v CSV a pripadne uvozovat do uvozovek

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import android.database.Cursor;

/**
 * @author Petr Nohejl
 * @version 1.0
 * @since 1.0 - 22.11.2010
 */
public class CallHistory {
	
	public static final String CSV_SEPARATOR = ",";
	public static final String CSV_HEADER = "Phone Number,Name,Call Type,Call Duration in seconds,Call Date";
	public static final String CSV_NA = "Unknown";
	
	private ArrayList<ObjectCall> callList = new ArrayList<ObjectCall>();

	
	/**
     * Object with informations about call history.
     * 
     * @param cursor
     *            cursor to SQLite call history 
     */
	public CallHistory(Cursor cursor)
	{
		refreshData(cursor);
	}
	
	
	public void refreshData(Cursor cursor)
	{	
		// vyprazdneni seznamu
		this.callList.clear();
		
		// sloupce tabulky
		int numberColumn = cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER);
		int dateColumn = cursor.getColumnIndex(android.provider.CallLog.Calls.DATE);
		int typeColumn = cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE);
		int durationColumn = cursor.getColumnIndex(android.provider.CallLog.Calls.DURATION);		
		int nameColumn = cursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME);
		
		// jeden hovor
		String callNumber;
		String callName;	
		int callType;
		long callDuration;
		long callDate;
		
		// Loop through all entries the cursor provides to us.
		while(cursor.moveToNext())
		{	
			callNumber = cursor.getString(numberColumn);
			callName = cursor.getString(nameColumn);		
			callType = cursor.getInt(typeColumn);
			callDuration = cursor.getLong(durationColumn);
			callDate = cursor.getLong(dateColumn);
			
			if(callName == null) callName = CSV_NA;
			
			ObjectCall call = new ObjectCall(callNumber, callName, callType, callDuration, callDate);
			this.callList.add(call);
		}
	}
	
	
	/**
     * Create CSV file with call history.<br>
     * Call log including phone number, name, type of call, duration of call in seconds, date and time.
     *
     * @param directoryPath
     *            directory path of CSV file
     * @param fileName
     *            name of CSV file
     * @return true, if CSV file was created successfully
     */
	public boolean createCsvFile(String directoryPath, String fileName)
    {
        // textovy vystup do souboru
        File fileParent = new File(directoryPath);
        if(!fileParent.exists()) fileParent.mkdirs();
    	File file = new File(fileParent, fileName);
    	BufferedWriter writer;	
		
    	try 
		{
    		// vytvoreni bufferu
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(CSV_HEADER);
			writer.newLine();
			
			// prochazeni hovoru
    		ObjectCall call;
			for(int i=0; i < this.callList.size(); i++)
			{
				// ziskani jednoho hovoru
				call = callList.get(i);
				
				// zapis cisla
	        	writer.write(call.getNumber());
	        	writer.write(CSV_SEPARATOR);
	        	
	        	// zapis jmena
	        	writer.write(call.getName());
	        	writer.write(CSV_SEPARATOR);
	        	
	        	// zapis typu
	        	int callType = call.getType();
	        	if(callType < 0 || callType >= call.types.length) callType = 0;
	        	String callTypeText = call.types[callType];	    		
	        	writer.write(callTypeText);
	        	writer.write(CSV_SEPARATOR);
	        	
	        	// zapis delky hovoru
	        	String callDurationText = Long.toString(call.getDuration());
	        	writer.write(callDurationText);
	        	writer.write(CSV_SEPARATOR);
	        	
	        	// zapis data
	        	Timestamp ts = new Timestamp(call.getDate());
	        	String callDateText = ts.toString();
	        	writer.write(callDateText);
	        	
	        	// zalomeni radku
	            writer.newLine();
			}

			// uzavreni souboru
            writer.flush();
            writer.close();
            
            return true;
		}
    	catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
    }
	
	
	
	/**
     * Create CSV string with call history.<br>
     * Call log including phone number, name, type of call, duration of call in seconds, date and time.
     *
     * @return string with content
     */
	public String createCsvString()
    {
        // textovy vystup do stringu
		StringBuffer output = new StringBuffer("");
		String newline = System.getProperty("line.separator");	

		output = output.append(CSV_HEADER);
		output = output.append(newline);
		
		// prochazeni hovoru
		ObjectCall call;
		for(int i=0; i < this.callList.size(); i++)
		{
			// ziskani jednoho hovoru
			call = callList.get(i);
			
			// zapis cisla
			output = output.append(call.getNumber());
			output = output.append(CSV_SEPARATOR);
        	
        	// zapis jmena
			output = output.append(call.getName());
			output = output.append(CSV_SEPARATOR);
        	
        	// zapis typu
        	int callType = call.getType();
        	if(callType < 0 || callType >= call.types.length) callType = 0;
        	String callTypeText = call.types[callType];	    		
        	output = output.append(callTypeText);
        	output = output.append(CSV_SEPARATOR);
        	
        	// zapis delky hovoru
        	String callDurationText = Long.toString(call.getDuration());
        	output = output.append(callDurationText);
        	output = output.append(CSV_SEPARATOR);
        	
        	// zapis data
        	Timestamp ts = new Timestamp(call.getDate());
        	String callDateText = ts.toString();
        	output = output.append(callDateText);
        	
        	// zalomeni radku
        	output = output.append(newline);
		}
        
        return output.toString();
    }
	

	public ArrayList<ObjectCall> getCallHistory() {
		return callList;
	}

	public void setCallHistory(ArrayList<ObjectCall> callList) {
		this.callList = callList;
	}
	
	public void addCall(ObjectCall call) {
		this.callList.add(call);
	}
	
}
