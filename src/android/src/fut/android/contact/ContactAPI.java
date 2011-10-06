package fut.android.contact;

//TODO kontrolovat carky v CSV a pripadne uvozovat do uvozovek
//TODO redukovat duplicitni cisla (napr. z facebooku)
//http://higherpass.com/Android/Tutorials/Working-With-Android-Contacts/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

/**
 * @author Petr Nohejl
 * @version 1.0
 * @since 1.0 - 22.11.2010
 */
public abstract class ContactAPI {
	
	public static final String CSV_SEPARATOR = ",";
	public static final String CSV_HEADER = "Name,Given Name,Additional Name,Family Name,Yomi Name,Given Name Yomi,Additional Name Yomi,Family Name Yomi,Name Prefix,Name Suffix,Initials,Nickname,Short Name,Maiden Name,Birthday,Gender,Location,Billing Information,Directory Server,Mileage,Occupation,Hobby,Sensitivity,Priority,Subject,Notes,Group Membership,E-mail 1 - Type,E-mail 1 - Value,E-mail 2 - Type,E-mail 2 - Value,E-mail 3 - Type,E-mail 3 - Value,IM 1 - Type,IM 1 - Service,IM 1 - Value,IM 2 - Type,IM 2 - Service,IM 2 - Value,IM 3 - Type,IM 3 - Service,IM 3 - Value,Phone 1 - Type,Phone 1 - Value,Phone 2 - Type,Phone 2 - Value,Phone 3 - Type,Phone 3 - Value,Address 1 - Type,Address 1 - Formatted,Address 1 - Street,Address 1 - City,Address 1 - PO Box,Address 1 - Region,Address 1 - Postal Code,Address 1 - Country,Address 1 - Extended Address";
	public static final String CSV_GROUP = "FUT Contacts";

	private static ContactAPI api;
	
	public abstract Intent getContactIntent();	
	public abstract ObjectContactList newContactList();	
	public abstract Cursor getCur();
	public abstract void setCur(Cursor cur);
	public abstract ContentResolver getCr();
	public abstract void setCr(ContentResolver cr);
	
	/**
     * Create instance of ContactAPI, depending on used version of SDK.<br>
     * Android 2.0 and newer uses another methods of getting contacts from phone, then older versions.
     *
     * @return api, instance of ContactAPI		 
     */
	public static ContactAPI getAPI() {
		if (api == null) {
			String apiClass;
			if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ECLAIR) {
				apiClass = "fut.android.contact.ContactAPISdk5";
			} else {
				apiClass = "fut.android.contact.ContactAPISdk3";
			}
			
			try {
				Class<? extends ContactAPI> realClass = Class.forName(apiClass).asSubclass(ContactAPI.class);
				api = realClass.newInstance();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}	
		}
		return api;
	}
	
	/**
     * Create CSV file with contacs in Google format.<br>
     * Contacts including phone numbers (max 3), e-mails (max 3), notes (max 1), addresses (max 1), instant messangers (max 3).
     *
     * @param contactList
     *            contact list with data
     * @param directoryPath
     *            directory path of CSV file
     * @param fileName
     *            name of CSV file
     * @return true, if CSV file was created successfully
     */
	public boolean createCsvFile(ObjectContactList contactList, String directoryPath, String fileName)
    {
    	// ukazatele na instance
        ArrayList<ObjectContact> contacts;
        ObjectContact contact;
        ArrayList<ObjectPhone> phones;
        ObjectPhone phone;
        ArrayList<ObjectEmail> emails;
        ObjectEmail email;
        ArrayList<String> notes;
        String note;
        ArrayList<ObjectAddress> addresses;
        ObjectAddress address;
        ArrayList<ObjectIM> ims;
        ObjectIM im;
        
        // pocitadlo cyklu
        int j = 0;
        
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
			
			// prochazeni kontaktu
			contacts = contactList.getContacts();
	        for(int i=0; i < contacts.size(); i++)
	        {        		        	
	        	// ziskani jednoho kontaktu
	        	contact = contacts.get(i);	
	        	      	
	        	// zapis jmena
	        	writer.write(contact.getDisplayName());
	        	for(j=0; j < 25; j++) writer.write(CSV_SEPARATOR);
	        		        	
	        	// zapis poznamky
	        	notes = contact.getNotes();
	        	if(notes != null && notes.size() > 0)
	        	{	        		
	        		note = notes.get(0);	 
		        	writer.write(note);
	        	}
	        	writer.write(CSV_SEPARATOR);
	        		        	
	        	// zapis skupiny
	        	writer.write(CSV_GROUP);
	        	writer.write(CSV_SEPARATOR);
	        		        	
	        	// prochazeni emailu
	        	emails = contact.getEmail();
	        	if(emails != null)
	        	for(j=0; j < emails.size() && j < 3; j++)
	        	{	        		
	        		email = emails.get(j);	 
	        		int index = email.getType();
	        		if(index < 0 || index >= email.types.length) index = 0;
	        		writer.write(email.types[index]);
		        	writer.write(CSV_SEPARATOR);
		        	writer.write(email.getAddress());
		        	writer.write(CSV_SEPARATOR);	        	
	        	}
	        	// doplneni carek
	        	for(int k = j; k < 3; k++)
	        	{
	        		writer.write(CSV_SEPARATOR);
	        		writer.write(CSV_SEPARATOR);
	        	}
	        		        	
	        	// prochazeni IM
	        	ims = contact.getImAddresses();
	        	if(ims != null)
	        	for(j=0; j < ims.size() && j < 3; j++)
	        	{	        		
	        		im = ims.get(j);	 
	        		int index = im.getType();
	        		if(index < 0 || index >= im.types.length) index = 0;
	        		writer.write(im.types[index]);
		        	writer.write(CSV_SEPARATOR);
		        	int index2 = im.getProtocol() + 1;
	        		if(index2 < 0 || index2 >= im.protocols.length) index2 = 0;
		        	writer.write(im.protocols[index2]);
		        	writer.write(CSV_SEPARATOR);
		        	writer.write(im.getName());
		        	writer.write(CSV_SEPARATOR);	        	
	        	}
	        	// doplneni carek
	        	for(int k = j; k < 3; k++)
	        	{
	        		writer.write(CSV_SEPARATOR);
	        		writer.write(CSV_SEPARATOR);
	        		writer.write(CSV_SEPARATOR);
	        	}
	        		        	
	        	// prochazeni telefonu
	        	phones = contact.getPhone();
	        	if(phones != null)
	        	for(j=0; j < phones.size() && j < 3; j++)
	        	{        		
	        		phone = phones.get(j);
	        		int index = phone.getType();
	        		if(index < 0 || index >= phone.types.length) index = 0;
	        		writer.write(phone.types[index]);
		        	writer.write(CSV_SEPARATOR);
		        	writer.write(phone.getNumber());
		        	writer.write(CSV_SEPARATOR);		        	
	        	}
	        	// doplneni carek
	        	for(int k = j; k < 3; k++)
	        	{
	        		writer.write(CSV_SEPARATOR);
	        		writer.write(CSV_SEPARATOR);
	        	}
	        		        	
	        	// prochazeni adres
	        	addresses = contact.getAddresses();
	        	if(addresses != null && addresses.size() > 0)
	        	{	        		
	        		address = addresses.get(0);	 
	        		int index = address.getType();
	        		if(index < 0 || index >= address.types.length) index = 0;
	        		writer.write(address.types[index]);
		        	writer.write(CSV_SEPARATOR);
		        	writer.write("\"" + address.toString() + "\"");
		        	writer.write(CSV_SEPARATOR);	        	
	        	}
	        	else
	        	{
	        		writer.write(CSV_SEPARATOR);
	        		writer.write(CSV_SEPARATOR);
	        	}
	        	// doplneni carek
	        	for(j=0; j < 6; j++) writer.write(CSV_SEPARATOR);	        	
	        	
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
     * Create CSV string with contacs in Google format.<br>
     * Contacts including phone numbers (max 3), e-mails (max 3), notes (max 1), addresses (max 1), instant messangers (max 3).
     *
     * @param contactList
     *            contact list with data
     * @return string with content
     */
	public String createCsvString(ObjectContactList contactList)
    {
    	// ukazatele na instance
        ArrayList<ObjectContact> contacts;
        ObjectContact contact;
        ArrayList<ObjectPhone> phones;
        ObjectPhone phone;
        ArrayList<ObjectEmail> emails;
        ObjectEmail email;
        ArrayList<String> notes;
        String note;
        ArrayList<ObjectAddress> addresses;
        ObjectAddress address;
        ArrayList<ObjectIM> ims;
        ObjectIM im;
        
        // pocitadlo cyklu
        int j = 0;
        
        // textovy vystup do stringu
        StringBuffer output = new StringBuffer("");
		String newline = System.getProperty("line.separator");		
    	
		output = output.append(CSV_HEADER);
		output = output.append(newline);
		
		// prochazeni kontaktu
		contacts = contactList.getContacts();
        for(int i=0; i < contacts.size(); i++)
        {        		        	
        	// ziskani jednoho kontaktu
        	contact = contacts.get(i);	
        	      	
        	// zapis jmena
        	output = output.append(contact.getDisplayName());
        	for(j=0; j < 25; j++) output = output.append(CSV_SEPARATOR);
        		        	
        	// zapis poznamky
        	notes = contact.getNotes();
        	if(notes != null && notes.size() > 0)
        	{	        		
        		note = notes.get(0);	 
        		output = output.append(note);
        	}
        	output = output.append(CSV_SEPARATOR);
        		        	
        	// zapis skupiny
        	output = output.append(CSV_GROUP);
        	output = output.append(CSV_SEPARATOR);
        		        	
        	// prochazeni emailu
        	emails = contact.getEmail();
        	if(emails != null)
        	for(j=0; j < emails.size() && j < 3; j++)
        	{	        		
        		email = emails.get(j);	 
        		int index = email.getType();
        		if(index < 0 || index >= email.types.length) index = 0;
        		output = output.append(email.types[index]);
        		output = output.append(CSV_SEPARATOR);
        		output = output.append(email.getAddress());
        		output = output.append(CSV_SEPARATOR);	        	
        	}
        	// doplneni carek
        	for(int k = j; k < 3; k++)
        	{
        		output = output.append(CSV_SEPARATOR);
        		output = output.append(CSV_SEPARATOR);
        	}
        		        	
        	// prochazeni IM
        	ims = contact.getImAddresses();
        	if(ims != null)
        	for(j=0; j < ims.size() && j < 3; j++)
        	{	        		
        		im = ims.get(j);	 
        		int index = im.getType();
        		if(index < 0 || index >= im.types.length) index = 0;
        		output = output.append(im.types[index]);
        		output = output.append(CSV_SEPARATOR);
	        	int index2 = im.getProtocol() + 1;
        		if(index2 < 0 || index2 >= im.protocols.length) index2 = 0;
        		output = output.append(im.protocols[index2]);
        		output = output.append(CSV_SEPARATOR);
        		output = output.append(im.getName());
        		output = output.append(CSV_SEPARATOR);	        	
        	}
        	// doplneni carek
        	for(int k = j; k < 3; k++)
        	{
        		output = output.append(CSV_SEPARATOR);
        		output = output.append(CSV_SEPARATOR);
        		output = output.append(CSV_SEPARATOR);
        	}
        		        	
        	// prochazeni telefonu
        	phones = contact.getPhone();
        	if(phones != null)
        	for(j=0; j < phones.size() && j < 3; j++)
        	{        		
        		phone = phones.get(j);
        		int index = phone.getType();
        		if(index < 0 || index >= phone.types.length) index = 0;
        		output = output.append(phone.types[index]);
        		output = output.append(CSV_SEPARATOR);
        		output = output.append(phone.getNumber());
        		output = output.append(CSV_SEPARATOR);		        	
        	}
        	// doplneni carek
        	for(int k = j; k < 3; k++)
        	{
        		output = output.append(CSV_SEPARATOR);
        		output = output.append(CSV_SEPARATOR);
        	}
        		        	
        	// prochazeni adres
        	addresses = contact.getAddresses();
        	if(addresses != null && addresses.size() > 0)
        	{	        		
        		address = addresses.get(0);	 
        		int index = address.getType();
        		if(index < 0 || index >= address.types.length) index = 0;
        		output = output.append(address.types[index]);
        		output = output.append(CSV_SEPARATOR);
        		output = output.append("\"" + address.toString() + "\"");
        		output = output.append(CSV_SEPARATOR);	        	
        	}
        	else
        	{
        		output = output.append(CSV_SEPARATOR);
        		output = output.append(CSV_SEPARATOR);
        	}
        	// doplneni carek
        	for(j=0; j < 6; j++) output = output.append(CSV_SEPARATOR);	        	
        	
        	// zalomeni radku
        	output = output.append(newline);
        }
        
        return output.toString();
    }
}

