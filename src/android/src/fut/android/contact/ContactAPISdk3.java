package fut.android.contact;

import java.util.ArrayList; 

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Contacts.People;

public class ContactAPISdk3 extends ContactAPI {

	private Cursor cur;
	private ContentResolver cr;
	
	public Cursor getCur() {
		return cur;
	}

	public void setCur(Cursor cur) {
		this.cur = cur;
	}

	public ContentResolver getCr() {
		return cr;
	}

	public void setCr(ContentResolver cr) {
		this.cr = cr;
	}

	public Intent getContactIntent() {
		return(new Intent(Intent.ACTION_PICK, People.CONTENT_URI));
	}
	
	public ObjectContactList newContactList() {
		ObjectContactList contacts = new ObjectContactList();
		String id;
		
		/*
		this.cur = this.cr.query(People.CONTENT_URI, 
				null, null, null, null);
		*/
		
		// Run query
        Uri uri = People.CONTENT_URI;       
        String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        this.cur = this.cr.query(uri, projection, selection, selectionArgs, sortOrder);
        
		
		if (this.cur.getCount() > 0) {
			while (cur.moveToNext()) {
				ObjectContact c = new ObjectContact();
				id = cur.getString(cur.getColumnIndex(People._ID));
				c.setId(id);
				c.setDisplayName(cur.getString(cur.getColumnIndex(People.DISPLAY_NAME)));
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(People.PRIMARY_PHONE_ID))) > 0) 
				{
					c.setPhone(this.getPhoneNumbers(id));
				}
				else
				{
					c.setPhone(null);
				}
				c.setEmail(this.getEmailAddresses(id));
				ArrayList<String> notes = new ArrayList<String>();
				notes.add(cur.getString(cur.getColumnIndex(People.NOTES)));
				c.setNotes(notes);
				c.setAddresses(this.getContactAddresses(id));
				c.setImAddresses(this.getIM(id));
				c.setOrganization(this.getContactOrg(id));
				contacts.addContact(c);
			}
		}
		return(contacts);
	}
	
	public ArrayList<ObjectPhone> getPhoneNumbers(String id) {
		ArrayList<ObjectPhone> phones = new ArrayList<ObjectPhone>();
		
		Cursor pCur = this.cr.query(
				Contacts.Phones.CONTENT_URI, 
				null, 
				Contacts.Phones.PERSON_ID +" = ?", 
				new String[]{id}, null);
		while (pCur.moveToNext()) {
			phones.add(new ObjectPhone(
					pCur.getString(pCur.getColumnIndex(Contacts.Phones.NUMBER))
					, pCur.getString(pCur.getColumnIndex(Contacts.Phones.TYPE))
			));

		} 
		pCur.close();
		return(phones);
	}
	
	public ArrayList<ObjectEmail> getEmailAddresses(String id) {
		ArrayList<ObjectEmail> emails = new ArrayList<ObjectEmail>();
		
		Cursor emailCur = this.cr.query( 
				Contacts.ContactMethods.CONTENT_EMAIL_URI, 
				null,
				Contacts.ContactMethods.PERSON_ID + " = ?", 
				new String[]{id}, null); 
		while (emailCur.moveToNext()) { 
		    // This would allow you get several email addresses
			ObjectEmail e = new ObjectEmail(emailCur.getString(emailCur.getColumnIndex(Contacts.ContactMethods.DATA))
					,emailCur.getString(emailCur.getColumnIndex(Contacts.ContactMethods.CONTENT_EMAIL_TYPE))  
					);
			emails.add(e);
		} 
		emailCur.close();
		return(emails);
	}
	
	public ArrayList<ObjectAddress> getContactAddresses(String id) {
		ArrayList<ObjectAddress> addrList = new ArrayList<ObjectAddress>();
		
		String where = Contacts.ContactMethods.PERSON_ID + " = ? AND " + Contacts.ContactMethods.KIND + " = ?"; 
		String[] whereParameters = new String[]{id, 
				Contacts.ContactMethods.CONTENT_POSTAL_ITEM_TYPE}; 
		
		Cursor addrCur = this.cr.query(Contacts.ContactMethods.CONTENT_URI, null, where, whereParameters, null); 
		while(addrCur.moveToNext()) {
			String addr = addrCur.getString(addrCur.getColumnIndex(Contacts.ContactMethodsColumns.DATA));
			String type = addrCur.getString(addrCur.getColumnIndex(Contacts.ContactMethodsColumns.TYPE));
			ObjectAddress a = new ObjectAddress(addr, type);
			addrList.add(a);
		} 
		addrCur.close();
		return(addrList);
	}
	
	public ArrayList<ObjectIM> getIM(String id) {
		ArrayList<ObjectIM> imList = new ArrayList<ObjectIM>();
		String where = Contacts.ContactMethods.PERSON_ID + " = ? AND " + Contacts.ContactMethods.KIND + " = ?"; 
		String[] whereParameters = new String[]{id, 
				Contacts.ContactMethods.CONTENT_IM_ITEM_TYPE}; 
		
		Cursor imCur = this.cr.query(Contacts.ContactMethods.CONTENT_URI, null, where, whereParameters, null); 
		if (imCur.moveToFirst()) { 
			String imName = imCur.getString(imCur.getColumnIndex(Contacts.ContactMethodsColumns.DATA));
			String imType = imCur.getString(imCur.getColumnIndex(Contacts.ContactMethodsColumns.TYPE));
			String imProtocol = "0";
			if (imName.length() > 0) {
				ObjectIM im = new ObjectIM(imName, imType, imProtocol);
				imList.add(im);
			}
		} 
		imCur.close();
		return(imList);
	}
	
	public ObjectOrganization getContactOrg(String id) {
		ObjectOrganization org = new ObjectOrganization();
		String where = Contacts.ContactMethods.PERSON_ID + " = ?"; 
		String[] whereParameters = new String[]{id}; 
		
		Cursor orgCur = this.cr.query(Contacts.Organizations.CONTENT_URI, null, where, whereParameters, null);

		if (orgCur.moveToFirst()) { 
			String orgName = orgCur.getString(orgCur.getColumnIndex(Contacts.Organizations.COMPANY));
			String title = orgCur.getString(orgCur.getColumnIndex(Contacts.Organizations.TITLE));
			if (orgName.length() > 0) {
				org.setOrganization(orgName);
				org.setTitle(title);
			}
		} 
		orgCur.close();
		return(org);
	}
}

