package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PropertyValuePairList {

	private ArrayList<PropertyValuePair> list;

	public PropertyValuePairList(Client client) {
		if(client != null) {
			list = new ArrayList<PropertyValuePair>();
			list.add(new PropertyValuePair(Client.Property.ID.toString(), client.getId()));
			list.add(new PropertyValuePair(Client.Property.NAME.toString(), client.getName()));
			list.add(new PropertyValuePair(Client.Property.PHONE_NUMER.toString(), client.getPhoneNumber()));
			list.add(new PropertyValuePair(Client.Property.TYPE.toString(), client.getType()));
		}
	}

	public PropertyValuePairList(Product product) {
		if(product != null) {
			list = new ArrayList<PropertyValuePair>();

			SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
			if(product.getdEntry() != null)
				list.add(new PropertyValuePair(dateTimeFormat.format(product.getdEntry()), product.getLegal() ? "Legal" : "Illegal") );

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

			list.add(new PropertyValuePair(Product.Property.ID.toString(), product.getProductID()));
			list.add(new PropertyValuePair(Product.Property.NAME.toString(), product.getName()));
			list.add(new PropertyValuePair(Product.Property.COMPOSITION.toString(), product.getComposition()));
			list.add(new PropertyValuePair(Product.Property.TYPE.toString(), product.getType()));
			list.add(new PropertyValuePair(Product.Property.PACKING.toString(), product.getPacking()));
			list.add(new PropertyValuePair(Product.Property.SUPPLIER.toString(), product.getSupplier()));
			list.add(new PropertyValuePair(Product.Property.COMPANY.toString(), product.getCompany()));
			if(product.getdExpiry() != null)
				list.add(new PropertyValuePair(Product.Property.EXPIRY.toString(), dateFormat.format(product.getdExpiry())) );
			list.add(new PropertyValuePair("*PACKAGING", ""));
			list.add(new PropertyValuePair(Product.Property.BOXES_P_CARTON.toString(), "" + product.getBoxPCarton()));
			list.add(new PropertyValuePair(Product.Property.BP_P_BOX.toString(), "" + product.getBPPBox()));
			list.add(new PropertyValuePair(Product.Property.PILLS_P_BP.toString(), "" + product.getPillPBP()));
			list.add(new PropertyValuePair("*QUANTITY", ""));
			list.add(new PropertyValuePair(Product.Property.QTY_CARTONS.toString(), "" + product.getQtyCartons()));
			list.add(new PropertyValuePair(Product.Property.QTY_BOXES.toString(), "" + product.getQtyBoxes()));
			list.add(new PropertyValuePair(Product.Property.QTY_BP.toString(), "" + product.getQtyBP()));
			list.add(new PropertyValuePair(Product.Property.QTY_PILLS.toString(), "" + product.getQtyPills()));
			list.add(new PropertyValuePair("*PRICES", ""));
			list.add(new PropertyValuePair(Product.Property.QTY_CARTONS.toString(), "" + product.getPriceCarton()));
			list.add(new PropertyValuePair(Product.Property.QTY_BOXES.toString(), "" + product.getPriceBox()));
			list.add(new PropertyValuePair(Product.Property.QTY_BP.toString(), "" + product.getPriceBP()));
			list.add(new PropertyValuePair(Product.Property.QTY_PILLS.toString(), "" + product.getPricePill()));
			list.add(new PropertyValuePair("*CUT-RATE PRICES", ""));
			list.add(new PropertyValuePair(Product.Property.QTY_CARTONS.toString(), "" + product.getPriceCartonCR()));
			list.add(new PropertyValuePair(Product.Property.QTY_BOXES.toString(), "" + product.getPriceBoxCR()));
			list.add(new PropertyValuePair(Product.Property.QTY_BP.toString(), "" + product.getPriceBPCR()));
			list.add(new PropertyValuePair(Product.Property.QTY_PILLS.toString(), "" + product.getPricePillCR()));


			list.add(new PropertyValuePair(Product.Property.DESCRIPTION.toString(), product.getDescription()));
		}
	}

	public ArrayList<PropertyValuePair> getList(){ return list; }

}
