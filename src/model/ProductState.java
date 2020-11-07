package model;

public enum ProductState {
	LOCKED("Locked"), UNLOCKED("Unlocked"), CRITICAL("Critical"), LOCKED_CRITICAL("Locked_Critical");
	private String value;

	ProductState(String v){
		value = v;
	}

	@Override
	public String toString() {
		return value;
	}
}
