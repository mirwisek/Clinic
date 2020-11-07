package controller;

import java.sql.Date;
import java.sql.SQLException;

import database.ProductManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import model.Product;
import model.ProductState;

public class NewProductController {

	private Stage currentStage;
	private ProductController productController;

	@FXML TextField tfProductID, tfName, tfSupplier, tfPacking, tfCompany,
	tfQtyCarton, tfPriceCarton, tfQtyBox, tfPriceBox, tfQtyBP, tfPriceBP, tfQtyPill, tfPricePill,
	tfPriceCartonCR, tfPriceBoxCR, tfPriceBPCR, tfPricePillCR,
	tfBoxPCarton, tfBPPBox, tfPillPBP;
	@FXML TextArea taComposition, taDescription;
	@FXML ToggleButton tbLegal;
	@FXML DatePicker datePickerExpiry;
	@FXML ComboBox<String> cbProductType;


	public void setReferences(Stage stage, ProductController controller) {
		currentStage = stage;
		productController = controller;
	}

	@FXML private void initialize() {
		for(Product.Type t : Product.Type.values()) {
			cbProductType.getItems().add(t.toString());
		}
		cbProductType.getSelectionModel().select(0);
	}

	@FXML private void handleBtnAdd() {
		Product product = getEditorValues();
		try {
			ProductManager.addProduct(product);
			productController.tableNavProduct.getItems().add(product);
			currentStage.close();
		} catch( SQLException ex ) {
			ex.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Add Product Error");
			alert.setHeaderText("Error: " + ex.getMessage());
			alert.setContentText("Cause: " + ex.getCause());
			alert.showAndWait();
		}
	}

	@FXML private void handleBtnLegal() {
		tbLegal.setText(tbLegal.isSelected() ? "Legal" : "Illegal");
	}

	@FXML private void handleBtnCancel() {
		currentStage.close();
	}

	private Product getEditorValues() {
		String id = tfProductID.getText();
		String name = tfName.getText();
		String type = cbProductType.getSelectionModel().getSelectedItem();
		String supplier = tfSupplier.getText();
		String company = tfCompany.getText();
		String packing = tfPacking.getText();
		String composition = taComposition.getText();
		String description = taDescription.getText();
		int qtyCarton = Integer.parseInt(tfQtyCarton.getText());
		int qtyBox = Integer.parseInt(tfQtyBox.getText());
		int qtyBP = Integer.parseInt(tfQtyBP.getText());
		int qtyPill = Integer.parseInt(tfQtyPill.getText());
		int boxPCarton = Integer.parseInt(tfBoxPCarton.getText());
		int bpPBox = Integer.parseInt(tfBPPBox.getText());
		int pillPBP = Integer.parseInt(tfPillPBP.getText());
		double priceCarton = Double.parseDouble(tfPriceCarton.getText());
		double priceBox = Double.parseDouble(tfPriceBox.getText());
		double priceBP = Double.parseDouble(tfPriceBP.getText());
		double pricePill = Double.parseDouble(tfPricePill.getText());
		double priceCartonCR = Double.parseDouble(tfPriceCartonCR.getText());
		double priceBoxCR = Double.parseDouble(tfPriceBoxCR.getText());
		double priceBPCR = Double.parseDouble(tfPriceBPCR.getText());
		double pricePillCR = Double.parseDouble(tfPricePillCR.getText());
		boolean legal = tbLegal.isSelected();
		Date expiry = Date.valueOf(datePickerExpiry.getValue());
		Date entry = new Date(System.currentTimeMillis());

		Product product = new Product(id, name, type, composition, description, supplier, company, legal, packing, expiry, entry, qtyCarton,
				qtyBox, qtyBP, qtyPill, boxPCarton, bpPBox, pillPBP, priceCarton, priceBox, priceBP, pricePill, priceCartonCR, priceBoxCR,
				priceBPCR, pricePillCR, ProductState.UNLOCKED);

		return product;
	}

}
