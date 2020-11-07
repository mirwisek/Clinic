package model;

import java.sql.Date;

public class Invoice {
	int invoiceID;
	String clientID;
	String clientName;
	Date entry;
	double totalAmount;
	boolean locked;

	public enum Property {
		INVOICE_ID("Invoice ID"), CLIENT_ID("Client ID"), CLIENT_NAME("Client Name"), ENTRY("Entry"), TOTAL_AMOUNT("Total Amount");

		private String value;

		Property(String value){ this.value = value; }

		@Override
		public String toString() {
			return value;
		}
	}

	public Invoice(int invoiceID, String clientID, String clientName, Date entry, double totalAmount, boolean locked) {

		this.invoiceID = invoiceID;
		this.clientID = clientID;
		this.clientName = clientName;
		this.entry = entry;
		this.totalAmount = totalAmount;
		this.locked = locked;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public int getInvoiceID() {
		return invoiceID;
	}
	public void setInvoiceID(int invoiceID) {
		this.invoiceID = invoiceID;
	}
	public String getClientID() {
		return clientID;
	}
	public void setClientID(String clientID) {
		this.clientID = clientID;
	}
	public Date getEntry() {
		return entry;
	}
	public void setEntry(Date entry) {
		this.entry = entry;
	}
	public double getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}
}
