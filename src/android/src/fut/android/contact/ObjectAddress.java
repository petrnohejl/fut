package fut.android.contact;

public class ObjectAddress {
	
	public final String [] types = {"Unknown", "Home", "Work", "Other"};
	
 	private String poBox;
 	private String street;
 	private String city;
 	private String state;
 	private String postalCode;
 	private String country;
 	private int type;
 	private String asString = "";
 	
 	public int getType() {
 		return type;
 	}
 	public void setType(String t) {
 		this.type = Integer.parseInt(t);
 	}
 	public String getPoBox() {
 		return poBox;
 	}
 	public void setPoBox(String poBox) {
 		this.poBox = poBox;
 	}
 	public String getStreet() {
 		return street;
 	}
 	public void setStreet(String street) {
 		this.street = street;
 	}
 	public String getCity() {
 		return city;
 	}
 	public void setCity(String city) {
 		this.city = city;
 	}
 	public String getState() {
 		return state;
 	}
 	public void setState(String state) {
 		this.state = state;
 	}
 	public String getPostalCode() {
 		return postalCode;
 	}
 	public void setPostalCode(String postalCode) {
 		this.postalCode = postalCode;
 	}
 	public String getCountry() {
 		return country;
 	}
 	public void setCountry(String country) {
 		this.country = country;
 	}
 	public String toString() {
 		if (this.asString.length() > 0) {
 			return(this.asString);
 		} else {
 			String addr = "";
 			if (this.getPoBox() != null) {
 				addr = addr + this.getPoBox() + ", ";
 			}
 			if (this.getStreet() != null) {
 				addr = addr + this.getStreet() + ", ";
 			}
 			if (this.getCity() != null) {
 				addr = addr + this.getCity() + ", ";
 			}
 			if (this.getState() != null) {
 				addr = addr + this.getState() + ", ";
 			}
 			if (this.getPostalCode() != null) {
 				addr = addr + this.getPostalCode() + " ";
 			}
 			if (this.getCountry() != null) {
 				addr = addr + this.getCountry();
 			}
 			return(addr);
 		}
 	}
 	
 	public ObjectAddress(String asString, String type) {
 		this.asString = asString;
 		this.type = Integer.parseInt(type);
 	}
 	
 	public ObjectAddress(String poBox, String street, String city, String state, String postal, String country, String type) {
 		this.setPoBox(poBox);
  		this.setStreet(street);
 		this.setCity(city);
 		this.setState(state);
 		this.setPostalCode(postal);
 		this.setCountry(country);
 		this.setType(type);
 	}
 }

