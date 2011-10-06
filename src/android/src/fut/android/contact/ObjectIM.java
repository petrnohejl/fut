package fut.android.contact;

public class ObjectIM {
	
	public final String [] types = {"Unknown", "Home", "Work", "Other"};
	public final String [] protocols = {"Unknown", "AIM", "MSN", "Yahoo", "Skype", "QQ", "GTalk", "ICQ", "Jabber", "Net Meeting"};
	
 	private String name;
 	private int type;
 	private int protocol;
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public int getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = Integer.parseInt(type);
 	}
 	public int getProtocol() {
 		return protocol;
 	}
 	public void setProtocol(String protocol) {
 		this.protocol = Integer.parseInt(protocol);
 	}
 	
 	public ObjectIM(String name, String type, String protocol) {
 		this.name = name;
 		this.type = Integer.parseInt(type);
 		this.protocol = Integer.parseInt(protocol);
 	}
 }

