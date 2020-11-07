package model;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class PrintableInvoice {

	private int invoiceID;
	private String clientID;
	private String clientName;
	private String clientPhone;
	private Date entry;
	private double totalAmount;
	private List<Line> lines;

	public PrintableInvoice(int invoiceID, String clientID, String clientName, String clientPhone, Date entry,
			double totalAmount, List<Line> lines) {
		super();
		this.invoiceID = invoiceID;
		this.clientID = clientID;
		this.clientName = clientName;
		this.clientPhone = clientPhone;
		this.entry = entry;
		this.totalAmount = totalAmount;
		this.lines = lines;
	}

	public String getInvoiceID() {
		return "Invoice # " + String.valueOf(invoiceID);
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
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getClientPhone() {
		return clientPhone;
	}
	public void setClientPhone(String clientPhone) {
		this.clientPhone = clientPhone;
	}
	public String getEntry() {
		SimpleDateFormat format = new SimpleDateFormat("MMM dd, YYYY - hh:mm:ss a");
		return "Date: " + format.format(entry);
	}
	public void setEntry(Date entry) {
		this.entry = entry;
	}
	public String getTotalAmount() {
		return String.format("%.2f", totalAmount);
	}
	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public List<Line> getLines() {
		return lines;
	}
	public void setLines(List<Line> lines) {
		this.lines = lines;
	}
	public int getLinesCount() {
		return lines.size();
	}

}
