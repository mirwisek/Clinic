package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import database.InvoiceManager;
import database.ProductManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Client;
import model.Invoice;
import model.Line;
import model.Product;

public class NewInvoiceController {

	private Stage currentStage, newStage;
	private InvoiceController controller;
	private GUIWindowController guiController;
	private ProductController productController;
	private Product selectedProduct = null;
	private Client selectedClient = null;
	private int nextLineNumber, invoiceID;

	@FXML TextField tfInvoiceID, tfClientName, tfProductName, tfQuantity, tfDiscount;
	@FXML ComboBox<String> cbPriceBy;
	@FXML TableView<Line> lineTable;
	@FXML TableColumn<Line, String> colProductID, colProductName, colPacking, colPriceBy;
	@FXML TableColumn<Line, Integer> colLineNumber, colQuantity;
	@FXML TableColumn<Line, Double> colPrice, colDiscount, colSubTotal, colNetTotal;


	public void setReferences(Stage stage, InvoiceController controller, GUIWindowController guiController, ProductController pController) {
		currentStage = stage;
		this.controller = controller;
		this.guiController = guiController;
		productController = pController;
	}

	@FXML private void initialize() {

		nextLineNumber = 1;
		for(Product.PriceType t : Product.PriceType.values()) {
			cbPriceBy.getItems().add(t.toString());
		}
		cbPriceBy.getSelectionModel().select(0);
		try {
			InvoiceManager.setAutoCommitMode(false);
			InvoiceManager.setSavepoint();
			invoiceID = InvoiceManager.createInvoice();
			tfInvoiceID.setText(String.valueOf(invoiceID));
		} catch (SQLException ex) {
			ex.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Creating invoice failed!");
			alert.setHeaderText("Error: " + ex.getMessage());
			alert.setContentText("Cause: " + ex.getCause());
			alert.showAndWait();
		}

		colProductID.setCellValueFactory(new PropertyValueFactory<Line, String>("productID"));
		colProductName.setCellValueFactory(new PropertyValueFactory<Line, String>("name"));
		colPacking.setCellValueFactory(new PropertyValueFactory<Line, String>("packing"));
		colPriceBy.setCellValueFactory(new PropertyValueFactory<Line, String>("priceBy"));
		colLineNumber.setCellValueFactory(new PropertyValueFactory<Line, Integer>("lineNumber"));
		colQuantity.setCellValueFactory(new PropertyValueFactory<Line, Integer>("quantity"));
		colDiscount.setCellValueFactory(new PropertyValueFactory<Line, Double>("discount"));
		colPrice.setCellValueFactory(new PropertyValueFactory<Line, Double>("price"));
		colSubTotal.setCellValueFactory(new PropertyValueFactory<Line, Double>("subTotal"));
		colNetTotal.setCellValueFactory(new PropertyValueFactory<Line, Double>("netTotal"));

	}

	@FXML private void handleBtnAdd() {

		String priceBy = cbPriceBy.getSelectionModel().getSelectedItem();
		int quantity = Integer.parseInt(tfQuantity.getText());
		int invoiceID = Integer.parseInt(tfInvoiceID.getText());
		double discount = Double.parseDouble(tfDiscount.getText());

		if(selectedProduct != null) {
			Line line = new Line(nextLineNumber, -1, quantity, priceBy, selectedProduct.getProductID(), discount);

			try {
				Line newLine = InvoiceManager.addLineToInvoice(line, invoiceID);
				if(newLine != null) {
					lineTable.getItems().add(newLine);
					nextLineNumber++;
					// Reset the product
					tfProductName.setText("");
					tfDiscount.setText("");
					tfQuantity.setText("");
					cbPriceBy.getSelectionModel().select(0);
					selectedProduct = null;
				}
			} catch( Exception ex ) {
				ex.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Add Product Error");
				alert.setHeaderText("Error: " + ex.getMessage());
				alert.setContentText("Cause: " + ex.getCause());
				alert.showAndWait();
			}
		}
	}

	@FXML private void handleBtnCreate() {
		try {
			InvoiceManager.updateInvoiceClient(selectedClient.getId(), invoiceID);
			InvoiceManager.updateInvoiceAmount(invoiceID);
			InvoiceManager.updateEntry();
			Invoice in = InvoiceManager.getInvoice(invoiceID);
			if(in != null) {
				InvoiceManager.commit();
				InvoiceManager.setAutoCommitMode(true);
				controller.tableInvoice.getItems().add(in);
			}
			/*
			 * There are multiple options here
			 *
			 * 1. Update product state with each line manipulated in InvoiceManger
			 * 2. Update all of them once committed (SELCETED FOR NOW)
			 *
			 */
			// Since quantities have been updated, now update product states
			ProductManager.updateProductStates(InvoiceController.PRODUCT_RANGE);

			// Update the tableNavProduct
			productController.tableNavProduct.getItems().clear();
			productController.tableNavProduct.getItems().addAll(
					ProductManager.getUpdatedProducts(productController.tableNavProduct.getItems()));

			currentStage.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	@FXML private void handleBtnDelete() {
		try {
			Line line = lineTable.getSelectionModel().getSelectedItem();
			if(line != null) {
				InvoiceManager.deleteLine(line, invoiceID);
				ArrayList<Line> list = InvoiceManager.getLinesList(invoiceID);
				if(list != null) {
					lineTable.getItems().clear();
					lineTable.getItems().addAll(list);
					tfProductName.setText("");
					tfDiscount.setText("");
					tfQuantity.setText("");
					cbPriceBy.getSelectionModel().select(0);
					selectedProduct = null;
				}
				int val = InvoiceManager.getNextLineNumber(invoiceID);
				if(val != -1)
					nextLineNumber = val;
			}
		} catch( Exception ex ) {
			ex.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Delete Product Error");
			alert.setHeaderText("Error: " + ex.getMessage());
			alert.setContentText("Cause: " + ex.getCause());
			alert.showAndWait();
		}
	}

	@FXML private void handleBtnCancel() {
		try {
			InvoiceManager.rollBack();
			InvoiceManager.setAutoCommitMode(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		currentStage.close();
	}

	@FXML private void handleBtnProductSelect() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProductSelectView.fxml"));
			HBox pane = loader.load();
			Scene scene = new Scene(pane);
			newStage = new Stage();
			newStage.setScene(scene);
			newStage.setTitle("Product Selection");
			newStage.initModality(Modality.APPLICATION_MODAL);
			ProductSelectController controller = loader.getController();
			controller.setReferences(newStage, this);
			newStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML private void handleBtnClientSelect() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClientSelectView.fxml"));
			HBox pane = loader.load();
			Scene scene = new Scene(pane);
			newStage = new Stage();
			newStage.setScene(scene);
			newStage.setTitle("Client Selection");
			newStage.initModality(Modality.APPLICATION_MODAL);
			ClientSelectController controller = loader.getController();
			controller.setReferences(newStage, this);
			newStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Product getSelectedProduct() {
		return selectedProduct;
	}

	public void setSelectedProduct(Product selectedProduct) {
		this.selectedProduct = selectedProduct;
	}

	public Client getSelectedClient() {
		return selectedClient;
	}

	public void setSelectedClient(Client selectedClient) {
		this.selectedClient = selectedClient;
	}

}
