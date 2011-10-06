package fut.android.contact;

import java.util.ArrayList;

public class ObjectContact {
	private String id;
	private String displayName;
	private ArrayList<ObjectPhone> phone;
	private ArrayList<ObjectEmail> email;
	private ArrayList<String> notes;
	private ArrayList<ObjectAddress> addresses = new ArrayList<ObjectAddress>();
	private ArrayList<ObjectIM> imAddresses;
	private ObjectOrganization organization;
 	
	
	public ObjectOrganization getOrganization() {
		return organization;
	}
	public void setOrganization(ObjectOrganization organization) {
		this.organization = organization;
	}
	public ArrayList<ObjectIM> getImAddresses() {
		return imAddresses;
	}
	public void setImAddresses(ArrayList<ObjectIM> imAddresses) {
		this.imAddresses = imAddresses;
 	}
	public void addImAddresses(ObjectIM imAddr) {
		this.imAddresses.add(imAddr);
	}
	public ArrayList<String> getNotes() {
		return notes;
	}
	public void setNotes(ArrayList<String> notes) {
		this.notes = notes;
	}
	public void addNote(String note) {
		this.notes.add(note);
	}
	public ArrayList<ObjectAddress> getAddresses() {
		return addresses;
	}
	public void setAddresses(ArrayList<ObjectAddress> addresses) {
		this.addresses = addresses;
	}
	public void addAddress(ObjectAddress address) {
		this.addresses.add(address);
	}
	public ArrayList<ObjectEmail> getEmail() {
		return email;
	}
	public void setEmail(ArrayList<ObjectEmail> email) {
		this.email = email;
	}
	public void addEmail(ObjectEmail e) {
		this.email.add(e);
	}	
	public String getId() {
		return id;
	}
	public void setId(String id) {
 		this.id = id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String dName) {
		this.displayName = dName;
	}
	public ArrayList<ObjectPhone> getPhone() {
		return phone;
	}
	public void setPhone(ArrayList<ObjectPhone> phone) {
		this.phone = phone;
	}
	public void addPhone(ObjectPhone phone) {
		this.phone.add(phone);
	}
}

