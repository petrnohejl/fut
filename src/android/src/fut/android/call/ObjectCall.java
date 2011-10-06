package fut.android.call;

public class ObjectCall {
	
	public final String [] types = {"Unknown", "Incoming", "Outgoing", "Missed"};
	
	private String number;
	private String name;
 	private int type;	
 	private long duration;
 	private long date;
 	
 	
 	public String getNumber() {
 		return number;
 	}
 
 	public void setNumber(String number) {
 		this.number = number;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 	
 	public long getDuration() {
 		return duration;
 	}
 
 	public void setDuration(long duration) {
 		this.duration = duration;
 	}
 	
 	public long getDate() {
 		return date;
 	}
 
 	public void setDate(long date) {
 		this.date = date;
 	}
 

 	public ObjectCall(String number, String name, int type, long duration, long date) {
 		this.number = number;
 		this.name = name;
 		this.type = type;
 		this.duration = duration;
 		this.date = date;
 	}
}
