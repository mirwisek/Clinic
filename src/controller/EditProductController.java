package controller;

import java.awt.Toolkit;
import java.sql.Date;
import java.sql.SQLException;

import javax.management.Notification;

import org.controlsfx.control.Notifications;
import org.hsqldb.lib.Notified;

import database.ProductManager;
import impl.org.controlsfx.skin.NotificationBar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import model.Product;
import model.ProductState;

public class EditProductController {

	private Stage currentStage;
	private int selectedIndex;
	private ProductState state;

	@FXML TextField tfProductID, tfName, tfSupplier, tfPacking, tfCompany,
	tfQtyCarton, tfPriceCarton, tfQtyBox, tfPriceBox, tfQtyBP, tfPriceBP, tfQtyPill, tfPricePill,
	tfPriceCartonCR, tfPriceBoxCR, tfPriceBPCR, tfPricePillCR,
	tfBoxPCarton, tfBPPBox, tfPillPBP;
	@FXML TextArea taComposition, taDescription;
	@FXML ToggleButton tbLegal;
	@FXML DatePicker datePickerExpiry;
	@FXML ComboBox<String> cbProductType;

	private TableView<Product> tableProduct;

	private ProductController productController;

	public void setReferences(Stage stage, ProductController productController, TableView<Product> tableProduct, int selection) {
		currentStage = stage;
		this.productController = productController;
		this.tableProduct = tableProduct;
		selectedIndex = selection;
		if(selectedIndex != -1) {
			String id = tableProduct.getItems().get(selectedIndex).getProductID();

			try {
				inflateEditor(ProductManager.getProductByID(id));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML private void initialize() {
		for(Product.Type t : Product.Type.values()) {
			cbProductType.getItems().add(t.toString());
		}
		cbProductType.getSelectionModel().select(0);
	}

	@FXML private void handleBtnSave() {
		if(selectedIndex != -1) {
			Product oldProduct = tableProduct.getItems().get(selectedIndex);
			Product product = getEditorValues();

			int oldQty = oldProduct.getQtyCartons();
			int newQty = product.getQtyCartons();

			try {
				// If quantity has increased than critical range
				if(oldQty <= InvoiceController.PRODUCT_RANGE && newQty > InvoiceController.PRODUCT_RANGE) {

					if(state == ProductState.LOCKED_CRITICAL)
							product.setProductState(ProductState.LOCKED);
					else if(state == ProductState.CRITICAL)
						product.setProductState(ProductState.UNLOCKED);

				} else if(oldQty > InvoiceController.PRODUCT_RANGE && newQty <= InvoiceController.PRODUCT_RANGE){
					if(state == ProductState.LOCKED)
						product.setProductState(ProductState.LOCKED_CRITICAL);
					else if(state == ProductState.UNLOCKED)
						product.setProductState(ProductState.CRITICAL);
				}

				ProductManager.updateProduct(product);
				tableProduct.getItems().remove(selectedIndex);
				tableProduct.getItems().add(selectedIndex, product);
				tableProduct.getSelectionModel().select(selectedIndex);

				try {
					if(ProductManager.getCriticalProductsList(InvoiceController.PRODUCT_RANGE, false).isEmpty()) {
						productController.showWarningProduct(false);
					}
					else {
						productController.showWarningProduct(true);
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
				}

				currentStage.close();
			} catch( SQLException ex ) {
				ex.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Update Failed");
				alert.setHeaderText("Error: " + ex.getMessage());
				alert.setContentText("Cause: " + ex.getCause());
				alert.showAndWait();
			}
		}
	}

	@FXML private void handleBtnCancel() {
		currentStage.close();
	}

	@FXML private void handleBtnLegal() {
		tbLegal.setText(tbLegal.isSelected() ? "Legal" : "Illegal");
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
				priceBPCR, pricePillCR, state);

		return product;
	}

	private void inflateEditor(Product p) {
		tfProductID.setText(p.getProductID());
		tfName.setText(p.getName());
		cbProductType.getSelectionModel().select(p.getType());
		tfSupplier.setText(p.getSupplier());
		tfCompany.setText(p.getCompany());
		tfPacking.setText(p.getPacking());
		taComposition.setText(p.getComposition());
		taDescription.setText(p.getDescription());
		tfQtyCarton.setText(String.valueOf(p.getQtyCartons()));
		tfQtyBox.setText(String.valueOf(p.getQtyBoxes()));
		tfQtyBP.setText(String.valueOf(p.getQtyBP()));
		tfQtyPill.setText(String.valueOf(p.getQtyPills()));
		tfBoxPCarton.setText(String.valueOf(p.getBoxPCarton()));
		tfBPPBox.setText(String.valueOf(p.getBPPBox()));
		tfPillPBP.setText(String.valueOf(p.getPillPBP()));
		tfPriceCarton.setText(String.valueOf(p.getPriceCarton()));
		tfPriceBox.setText(String.valueOf(p.getPriceBox()));
		tfPriceBP.setText(String.valueOf(p.getPriceBP()));
		tfPricePill.setText(String.valueOf(p.getPricePill()));
		tfPriceCartonCR.setText(String.valueOf(p.getPriceCartonCR()));
		tfPriceBoxCR.setText(String.valueOf(p.getPriceBoxCR()));
		tfPriceBPCR.setText(String.valueOf(p.getPriceBPCR()));
		tfPricePillCR.setText(String.valueOf(p.getPricePillCR()));
		tbLegal.setSelected(p.getLegal());
		datePickerExpiry.setValue(p.getdExpiry().toLocalDate());
		state = p.getProductState();
	}

}
