package fut.android.contact;

public class ObjectPhone {
	
	public final String [] types = {"Unknown", "Home", "Mobile", "Work", "Fax Work", "Fax Home", "Pager", 
		"Other", "Callback", "Car", "Company Main", "ISDN", "Main", "Other Fax", "Radio", "Telex", "TTY TDD", 
		"Work Mobile", "Work Pager", "Assistant", "MMS"};
	
 	private String number;
 	private int type;
 	
 	public String getNumber() {
 		return number;
 	}
 
 	public void setNumber(String number) {
 		this.number = number;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setType(String t) {
 		this.type = Integer.parseInt(t);
 	}
 
 	public ObjectPhone(String n, String t) {
 		this.number = n;
 		this.type = Integer.parseInt(t);
 	}
 	
 }

