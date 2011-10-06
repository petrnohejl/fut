package fut.android.contact;

public class ObjectEmail {
	
	public final String [] types = {"Unknown", "Home", "Work", "Other", "Mobile"};
	
 	private String address;
 	private int type;
 	
 	public String getAddress() {
 		return address;
 	}
 	
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	
 	public int getType() {
 		return type;
 	}
 	
 	public void setType(String t) {
 		this.type = Integer.parseInt(t);
 	}
 	
 	public ObjectEmail(String a, String t) {
 		this.address = a;
 		this.type = Integer.parseInt(t);
 	}
 }

