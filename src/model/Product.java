package model;

import java.sql.Date;

public class Product {

	private ProductState state;

	protected String productID;
	protected String name;
	protected String type;
	protected String composition;
	protected String description;
	protected String supplier;
	protected String company;
	protected String packing;
	protected boolean legal;
	protected Date dExpiry;
	protected Date dEntry;
	protected int qtyCartons;
	protected int qtyBoxes;
	protected int qtyBP;
	protected int qtyPills;
	protected int boxPCarton;
	protected int bpPBox;
	protected int pillPBP;
	protected double priceCarton;
	protected double priceBox;
	protected double priceBP;
	protected double pricePill;
	protected double priceCartonCR;
	protected double priceBoxCR;
	protected double priceBPCR;
	protected double pricePillCR;

	public enum PriceType {

		CARTON("Carton"), BOX("Box"), BP("BP"), PILL("Pill"),
		CARTON_CR("Carton CR"), BOX_CR("Box CR"), BP_CR("BP CR"), PILL_CR("Pill CR");

		private String value;
		PriceType(String value) {this.value = value;}

		@Override
		public String toString() {	return value;	}
	}

	public enum Type {

		TABLETS("Tablets"), SYRUP("Syrup"), TUBE("Tube"), POWDER("Powder"), INJECTABLE("Injectable");
		private String value;
		Type(String value) {this.value = value;}

		@Override
		public String toString() {	return value;	}
	}

	public enum Property {
		ID("Product ID"), NAME("Name"), COMPOSITION("Composition"), DESCRIPTION("Description"), LEGAL("Legal"),
		PACKING("Packing"), TYPE("Type"), SUPPLIER("Supplier"), COMPANY("Company"), EXPIRY("Expiry Date"), ENTRY("Entry Time"),
		QTY_CARTONS("Cartons"), QTY_BOXES("Boxes"), QTY_BP("BP"), QTY_PILLS("Pills"),
		PRICE_CARTON("Carton Price"), PRICE_CARTON_CR("Carton Price CR"), PRICE_BOX("Box Price"), PRICE_BOX_CR("Box Price CR"),
		PRICE_BP("BP Price"), PRICE_BP_CR("BP Price CR"), PRICE_PILL("Pill"), PRICE_PILL_CR("Pill Price CR"),
		BOXES_P_CARTON("Boxes/Carton"), BP_P_BOX("BP/Box"), PILLS_P_BP("Pills/BP");

		private String value;

		Property(String value){ this.value = value; }

		@Override
		public String toString() {
			return value;
		}
	}

	public double getFieldByProperty(Property searchBy) {
		switch(searchBy) {
		case QTY_CARTONS:
			return qtyCartons;
		case QTY_BOXES:
			return qtyBoxes;
		case QTY_BP:
			return qtyBP;
		case QTY_PILLS:
			return qtyPills;
		case PRICE_CARTON:
			return priceCarton;
		case PRICE_CARTON_CR:
			return priceCartonCR;
		case PRICE_BOX:
			return priceBox;
		case PRICE_BOX_CR:
			return priceBoxCR;
		case PRICE_BP:
			return priceBP;
		case PRICE_BP_CR:
			return priceBPCR;
		case PRICE_PILL:
			return pricePill;
		case PRICE_PILL_CR:
			return pricePillCR;
			default:
				return -1;
		}
	}


	public Product() {
		legal = true;
		qtyCartons = 0;
		qtyBoxes = 0;
		qtyBP = 0;
		qtyPills = 0;
		priceCarton = 0.0;
		priceBox = 0.0;
		priceBP = 0.0;
	}

	public Product( String id, String name, String type, String composition, String description, String supplier, String company,
		boolean legal,String packing, Date dExpiry, Date dEntry, int qtyCartons, int qtyBoxes, int qtyBP,
		int qtyPills, int boxPCarton, int bpPBox, int pillPBP, double priceCarton, double priceBox, double priceBP, double pricePill, double priceCartonCR, double priceBoxCR,
		double priceBPCR, double pricePillCR, ProductState state) {

		this.productID = id;
		this.name = name;
		this.type = type;
		this.composition = composition;
		this.description = description;
		this.supplier = supplier;
		this.company = company;
		this.legal = legal;
		this.packing = packing;
		this.dExpiry = dExpiry;
		this.dEntry = dEntry;
		this.qtyCartons = qtyCartons;
		this.qtyBoxes = qtyBoxes;
		this.qtyBP = qtyBP;
		this.qtyPills = qtyPills;
		this.boxPCarton = boxPCarton;
		this.bpPBox = bpPBox;
		this.pillPBP = pillPBP;
		this.priceCarton = priceCarton;
		this.priceBox = priceBox;
		this.priceBP = priceBP;
		this.pricePill = pricePill;
		this.priceCartonCR = priceCartonCR;
		this.priceBoxCR = priceBoxCR;
		this.priceBPCR = priceBPCR;
		this.pricePillCR = pricePillCR;
		this.state = state;
	}

	public ProductState getProductState() {
		return state;
	}

	public void setProductState(ProductState state) {
		this.state = state;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}



	public int getBoxPCarton() {
		return boxPCarton;
	}

	public void setBoxPCarton(int boxPCarton) {
		this.boxPCarton = boxPCarton;
	}

	public int getBPPBox() {
		return bpPBox;
	}

	public void setBPPBox(int bpPBox) {
		this.bpPBox = bpPBox;
	}

	public int getPillPBP() {
		return pillPBP;
	}

	public void setPillPBP(int pillPBP) {
		this.pillPBP = pillPBP;
	}

	public double getPricePill() {
		return pricePill;
	}

	public void setPricePill(double pricePill) {
		this.pricePill = pricePill;
	}

	public double getPriceBPCR() {
		return priceBPCR;
	}

	public void setPriceBPCR(double priceBPCR) {
		this.priceBPCR = priceBPCR;
	}

	public double getPricePillCR() {
		return pricePillCR;
	}

	public void setPricePillCR(double pricePillCR) {
		this.pricePillCR = pricePillCR;
	}

	public double getPriceCartonCR() {
		return priceCartonCR;
	}

	public void setPriceCartonCR(double priceCartonCR) {
		this.priceCartonCR = priceCartonCR;
	}

	public double getPriceBoxCR() {
		return priceBoxCR;
	}

	public void setPriceBoxCR(double priceBoxCR) {
		this.priceBoxCR = priceBoxCR;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComposition() {
		return composition;
	}

	public void setComposition(String composition) {
		this.composition = composition;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getdExpiry() {
		return dExpiry;
	}

	public void setdExpiry(Date dExpiry) {
		this.dExpiry = dExpiry;
	}

	public Date getdEntry() {
		return dEntry;
	}

	public void setdEntry(Date dEntry) {
		this.dEntry = dEntry;
	}

	public String getPacking() {
		return packing;
	}

	public void setPacking(String packing) {
		this.packing = packing;
	}

	public int getQtyCartons() {
		return qtyCartons;
	}

	public void setQtyCartons(int qtyCartons) {
		this.qtyCartons = qtyCartons;
	}

	public int getQtyBoxes() {
		return qtyBoxes;
	}

	public void setQtyBoxes(int qtyBoxes) {
		this.qtyBoxes = qtyBoxes;
	}

	public int getQtyBP() {
		return qtyBP;
	}

	public void setQtyBP(int qtyBP) {
		this.qtyBP = qtyBP;
	}

	public int getQtyPills() {
		return qtyPills;
	}

	public void setQtyPills(int qtyPills) {
		this.qtyPills = qtyPills;
	}

	public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public double getPriceCarton() {
		return priceCarton;
	}

	public void setPriceCarton(double priceCarton) {
		this.priceCarton = priceCarton;
	}

	public double getPriceBox() {
		return priceBox;
	}

	public void setPriceBox(double priceBox) {
		this.priceBox = priceBox;
	}

	public double getPriceBP() {
		return priceBP;
	}

	public void setPriceBP(double priceBP) {
		this.priceBP = priceBP;
	}

	public boolean getLegal() {
		return legal;
	}

	public void setLegal(boolean legal) {
		this.legal = legal;
	}

	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}

	@Override
	public String toString() {
		String s = getProductID() + " " + getName() + " " +
				getType() + " " + getLegal() + " " +
				getComposition() + " " + getdExpiry() +" " +
				getDescription() + " " + getdEntry() + " " +
				getQtyCartons() + " " + getQtyBoxes() + " " +
				getQtyPills() + " " + getQtyBP() + " " +
				getPriceCarton() + " " +  getPriceBox() + " " +
				getPriceCartonCR() + " " +  getPriceBoxCR() + " " +
				getPriceBP() + " " + getSupplier();

		return s;
	}
}
