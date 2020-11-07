package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

import database.InvoiceManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
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

public class EditInvoiceController {

	private Stage currentStage, newStage;
	private InvoiceController controller;
	private int selectedIndex = -1;
	private Invoice selectedInvoice = null;
	private Product selectedProduct = null;
	private Client selectedClient = null;
	private int nextLineNumber;

	@FXML TextField tfInvoiceID, tfClientName, tfProductName, tfQuantity, tfDiscount;
	@FXML ComboBox<String> cbPriceBy;
	@FXML TableView<Line> lineTable;
	@FXML TableColumn<Line, String> colProductID, colProductName, colPacking, colPriceBy;
	@FXML TableColumn<Line, Integer> colLineNumber, colQuantity;
	@FXML TableColumn<Line, Double> colPrice, colDiscount, colSubTotal, colNetTotal;


	public void setReferences(Stage stage, InvoiceController controller, int selectedIndex) {
		currentStage = stage;
		this.controller = controller;
		this.selectedIndex = selectedIndex;
		selectedInvoice = controller.tableInvoice.getItems().get(selectedIndex);
		tfInvoiceID.setText(String.valueOf(selectedInvoice.getInvoiceID()));
		tfClientName.setText(selectedInvoice.getClientName());
		selectedClient = new Client();
		selectedClient.setId(selectedInvoice.getClientID());
		selectedClient.setName(selectedInvoice.getClientName());

		try {
			ArrayList<Line> list = InvoiceManager.getLinesList(selectedInvoice.getInvoiceID());
			InvoiceManager.setAutoCommitMode(false);
			InvoiceManager.setSavepoint();
			if(list != null) {
				lineTable.getItems().addAll(list);
				nextLineNumber = list.size() + 1;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Invoice fetching failed!");
			alert.setHeaderText("Error: " + ex.getMessage());
			alert.setContentText("Cause: " + ex.getCause());
			alert.showAndWait();
		}
	}

	@FXML private void initialize() {
		for(Product.PriceType t : Product.PriceType.values()) {
			cbPriceBy.getItems().add(t.toString());
		}
		cbPriceBy.getSelectionModel().select(0);

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


		lineTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Line>() {
			@Override
			public void changed(ObservableValue<? extends Line> observable, Line oldValue, Line newValue) {
				selectedProduct = newValue;
				updateEditor(newValue);
			}
		});
	}

	private void updateEditor(Line line) {
		if(line != null) {
			tfProductName.setText(line.getName());
			cbPriceBy.getSelectionModel().select(line.getPriceBy());
			tfQuantity.setText(String.valueOf(line.getQuantity()));
			tfDiscount.setText(String.valueOf(line.getDiscount()));
		}
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

	@FXML private void handleBtnChange() {
		Line newLine = lineTable.getSelectionModel().getSelectedItem();
		Line oldLine = null;
		try {
			oldLine = newLine.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		int selectedLine = lineTable.getSelectionModel().getSelectedIndex();
		if(newLine != null && oldLine != null && selectedProduct != null) {

			newLine.setQuantity(Integer.parseInt(tfQuantity.getText()));
			newLine.setDiscount(Double.parseDouble(tfDiscount.getText()));
			newLine.setProductID(selectedProduct.getProductID());
			newLine.setPriceBy(cbPriceBy.getSelectionModel().getSelectedItem());

			try {
				Line updatedLine = InvoiceManager.updateLine(oldLine, newLine, selectedInvoice.getInvoiceID());
				if(updatedLine != null && !lineTable.getItems().isEmpty()) {
					lineTable.getItems().remove(selectedLine);
					lineTable.getItems().add(selectedLine, updatedLine);
					lineTable.getSelectionModel().select(selectedLine);
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
				alert.setTitle("Editing Product Error");
				alert.setHeaderText("Error: " + ex.getMessage());
				alert.setContentText("Cause: " + ex.getCause());
				alert.showAndWait();
			}
		}
	}

	@FXML private void handleBtnSave() {
		try {
			if(selectedClient != null) {
				// In case when user deletes all the lines, prompt if he would proceed with deleting the invoice
				Optional<ButtonType> result = null;
				if(InvoiceManager.getLineCount(selectedInvoice.getInvoiceID()) <= 0) {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Invoice Delete Confirmation");
					alert.setHeaderText("Invoice with no rows!" );
					alert.setContentText("Are you sure want to delete invoice?");
					result = alert.showAndWait();
					if(result.get() == ButtonType.OK) {
						InvoiceManager.deleteInvoice(selectedInvoice.getInvoiceID());
						controller.tableInvoice.getItems().remove(selectedIndex);
						controller.tableLine.getItems().clear();
						InvoiceManager.commit();
						InvoiceManager.setAutoCommitMode(true);
						currentStage.close();
					}
				} else {

					InvoiceManager.updateInvoiceClient(selectedClient.getId(), selectedInvoice.getInvoiceID());
					InvoiceManager.updateInvoiceAmount(selectedInvoice.getInvoiceID());
					InvoiceManager.commit();
					InvoiceManager.setAutoCommitMode(true);
					Invoice in = InvoiceManager.getInvoice(selectedInvoice.getInvoiceID());
					if(in != null) {

						controller.tableInvoice.getItems().remove(selectedIndex);
						controller.tableInvoice.getItems().add(selectedIndex, in);
						controller.tableInvoice.getSelectionModel().select(selectedIndex);
						controller.updateLineTable(in);
						currentStage.close();
					}

				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	@FXML private void handleBtnDelete() {
		try {
			Line line = lineTable.getSelectionModel().getSelectedItem();
			if(line != null) {
				InvoiceManager.deleteLine(line, selectedInvoice.getInvoiceID());
				ArrayList<Line> list = InvoiceManager.getLinesList(selectedInvoice.getInvoiceID());
				if(list != null) {
					lineTable.getItems().clear();
					lineTable.getItems().addAll(list);
					tfProductName.setText("");
					tfDiscount.setText("");
					tfQuantity.setText("");
					cbPriceBy.getSelectionModel().select(0);
					selectedProduct = null;
				}
				int val = InvoiceManager.getNextLineNumber(selectedInvoice.getInvoiceID());
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
			newStage.setTitle("Product Selection");
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
