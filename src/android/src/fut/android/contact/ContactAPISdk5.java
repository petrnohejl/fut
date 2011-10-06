package fut.android.contact;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.content.ContentResolver;

public class ContactAPISdk5 extends ContactAPI {

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
		return(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI));
	}
	
	public ObjectContactList newContactList() {
		ObjectContactList contacts = new ObjectContactList();
		String id;

        /*
		this.cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
		*/
		
		// Run query
        Uri uri = ContactsContract.Contacts.CONTENT_URI;       
        String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        this.cur = this.cr.query(uri, projection, selection, selectionArgs, sortOrder);
        
		
		if (this.cur.getCount() > 0) {
			while (cur.moveToNext()) {
				ObjectContact c = new ObjectContact();
				id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				c.setId(id);
				c.setDisplayName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
				{
					c.setPhone(this.getPhoneNumbers(id));
				}
				else
				{
					c.setPhone(null);
				}
				c.setEmail(this.getEmailAddresses(id));
				c.setNotes(this.getContactNotes(id));
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
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
				null, 
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
				new String[]{id}, null);
		while (pCur.moveToNext()) {
			phones.add(new ObjectPhone(
					pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
					, pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
			));

		} 
		pCur.close();
		return(phones);
	}
	
	public ArrayList<ObjectEmail> getEmailAddresses(String id) {
		ArrayList<ObjectEmail> emails = new ArrayList<ObjectEmail>();
		
		Cursor emailCur = this.cr.query( 
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
				null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
				new String[]{id}, null); 
		while (emailCur.moveToNext()) { 
		    // This would allow you get several email addresses
			ObjectEmail e = new ObjectEmail(emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
					,emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))  
					);
			emails.add(e);
		} 
		emailCur.close();
		return(emails);
	}
	
	public ArrayList<String> getContactNotes(String id) {
		ArrayList<String> notes = new ArrayList<String>();
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
		String[] whereParameters = new String[]{id, 
			ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE}; 
		Cursor noteCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null); 
		if (noteCur.moveToFirst()) { 
			String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
			if (note.length() > 0) {
				notes.add(note);
			}
		} 
		noteCur.close();
		return(notes);
	}
	
	public ArrayList<ObjectAddress> getContactAddresses(String id) {
		ArrayList<ObjectAddress> addrList = new ArrayList<ObjectAddress>();
		
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
		String[] whereParameters = new String[]{id, 
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE}; 
		
		Cursor addrCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null); 
		while(addrCur.moveToNext()) {
			String poBox = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
			String street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
			String city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
			String state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
			String postalCode = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
			String country = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
			String type = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
			ObjectAddress a = new ObjectAddress(poBox, street, city, state, postalCode, country, type);
			addrList.add(a);
		} 
		addrCur.close();
		return(addrList);
	}
	
	public ArrayList<ObjectIM> getIM(String id) {
		ArrayList<ObjectIM> imList = new ArrayList<ObjectIM>();
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
		String[] whereParameters = new String[]{id, 
				ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE}; 
		
		Cursor imCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null); 
		if (imCur.moveToFirst()) { 
			String imName = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
			String imType;
			imType = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
			String imProtocol = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
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
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
		String[] whereParameters = new String[]{id, 
				ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}; 
		
		Cursor orgCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null);

		if (orgCur.moveToFirst()) { 
			String orgName = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
			String title = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
			if (orgName.length() > 0) {
				org.setOrganization(orgName);
				org.setTitle(title);
			}
		} 
		orgCur.close();
		return(org);
	}
	
}

