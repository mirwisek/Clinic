package model;

public class Client {
	String id;
	String name;
	String phoneNumber;
	String type;
	Boolean locked;

	public enum Property {
		ID("Client ID"),
		NAME("Name"),
		PHONE_NUMER("Phone Number"),
		TYPE("Type");

		private String value;

		Property(String value){ this.value = value; }

		@Override
		public String toString() {
			return value;
		}
	}

	public enum Type {
		Typical, Special, Elite
	}

	public Client() {

	}

	public Client(String name, String phoneNumber, String type){
		if(phoneNumber.length() == 11)
			this.id = phoneNumber.substring(7);

		this.name = name;
		this.phoneNumber = phoneNumber;
		this.type = type;
		this.locked = false;
	}

	public Client(String name, String phoneNumber, String type, Boolean locked){
		if(phoneNumber.length() == 11)
			this.id = phoneNumber.substring(7);

		this.name = name;
		this.phoneNumber = phoneNumber;
		this.type = type;
		this.locked = locked;
	}

	public static String getIdFromPhoneNo(String phone){
		String id = "";
		if(phone.length() == 11)
			id = phone.substring(7);
		return id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}

	@Override
	public String toString() {
		return "Client [id=" + id + ", name=" + name + ", phoneNumber=" + phoneNumber + ", type=" + type + ", locked="
				+ locked + "]";
	}



}
