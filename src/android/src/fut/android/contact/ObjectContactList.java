package fut.android.contact;

import java.util.ArrayList;

public class ObjectContactList {

	private ArrayList<ObjectContact> contacts = new ArrayList<ObjectContact>();

	public ArrayList<ObjectContact> getContacts() {
		return contacts;
	}

	public void setContacts(ArrayList<ObjectContact> contacts) {
		this.contacts = contacts;
	}
	
	public void addContact(ObjectContact contact) {
		this.contacts.add(contact);
	}
 	
	public ObjectContactList() {
		
	}
	
}

