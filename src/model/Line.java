package model;

public class Line extends Product implements Cloneable {
	int lineNumber;
	double price;
	int quantity;
	String priceBy;
	double discount;
	double subTotal;
	double netTotal;

	public Line(){

	}

	public Line(int lineNumber, double price, int quantity, String priceBy, String productID , double discount) {

		this.lineNumber = lineNumber;
		this.price = price;
		this.quantity = quantity;
		this.priceBy = priceBy;
		this.productID = productID;
		this.discount = discount;
	}

	public double getSelectedPrice() {
		if(priceBy.equals(PriceType.CARTON.toString()))
			return priceCarton;
		else if(priceBy.equals(PriceType.CARTON_CR.toString()))
			return priceCartonCR;
		else if(priceBy.equals(PriceType.BOX.toString()))
			return priceBox;
		else if(priceBy.equals(PriceType.BOX_CR.toString()))
			return priceBoxCR;
		else if(priceBy.equals(PriceType.BP.toString()))
			return priceBP;
		else if(priceBy.equals(PriceType.BP_CR.toString()))
			return priceBPCR;
		else if(priceBy.equals(PriceType.PILL.toString()))
			return pricePill;
		else if(priceBy.equals(PriceType.PILL_CR.toString()))
			return pricePillCR;
		return -1;
	}

	public String getSubTotalFormated() {
		return String.format("%.2f", subTotal);
	}

	public double getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	public String getNetTotalFormated() {
		return String.format("%.2f", netTotal);
	}

	public double getNetTotal() {
		return netTotal;
	}

	public void setNetTotal(double netTotal) {
		this.netTotal = netTotal;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getPriceBy() {
		return priceBy;
	}

	public void setPriceBy(String priceBy) {
		this.priceBy = priceBy;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	@Override
	public Line clone() throws CloneNotSupportedException {
		return (Line)super.clone();
	}


}
