package controller;

import java.sql.SQLException;
import java.util.ArrayList;

import database.ProductManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import model.Product;
import model.PropertyValuePair;
import model.PropertyValuePairList;

public class ProductSelectController {

	@FXML private TableView<PropertyValuePair> tableDetailProduct;
	@FXML public TableView<Product> tableNavProduct;
	@FXML private TableColumn<PropertyValuePair, String> colPropertyProduct, colValueProduct;
	@FXML private TableColumn<Product, String> colProductID, colProductName;
	@FXML private TextField tfSearch;
	@FXML private ComboBox<String> cbSearchBy;

	private Stage currentStage;
	private NewInvoiceController newInvoiceController = null;
	private EditInvoiceController editInvoiceController = null;
	private Product selectedProduct = null;
	private ControllerType controllerType = null;

	private enum ControllerType {
		NEW, EDIT;
	}

	public void setReferences(Stage stage, NewInvoiceController controller) {
		currentStage = stage;
		newInvoiceController = controller;
		controllerType = ControllerType.NEW;
	}

	public void setReferences(Stage stage, EditInvoiceController controller) {
		currentStage = stage;
		editInvoiceController = controller;
		controllerType = ControllerType.EDIT;
	}

	@FXML public void initialize() {

		// Table
		colPropertyProduct.setCellValueFactory(
				new PropertyValueFactory<PropertyValuePair, String>("property"));
		colValueProduct.setCellValueFactory(
				new PropertyValueFactory<PropertyValuePair, String>("value"));

		colProductID.setCellValueFactory(new PropertyValueFactory<Product, String>("productID"));
		colProductName.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));

		// Disable column sort either DESC or ASC
		colPropertyProduct.setSortable(false);
		colValueProduct.setSortable(false);
		colValueProduct.setCellFactory( column -> {
			return new TableCell<PropertyValuePair, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(empty) {
						setGraphic(null);
						setText(null);
						setStyle("");
					} else {
						setGraphic(null);
						this.setPrefHeight(Control.USE_COMPUTED_SIZE);
						this.setWrapText(true);
						setText(item);
					}
				}
			};
		});

		// Necessary for highlight of searchText
		tableNavProduct.setFixedCellSize(29);

		colProductName.setCellFactory( column -> {
			return new TableCell<Product, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(item == null || empty) {
						setGraphic(null);
						setText(null);
						setStyle("");
					} else {
						setGraphic(null);
						String searchVal = tfSearch.getText();
						if(!searchVal.isEmpty() &&
								cbSearchBy.getSelectionModel().getSelectedItem() ==
								Product.Property.NAME.toString() &&
								item.toLowerCase().contains(searchVal.toLowerCase())) {
							setGraphic(buildFlowText(item, searchVal));
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						} else {
							setText(item);
							setTextFill(javafx.scene.paint.Color.BLACK);
							setStyle("");
							setContentDisplay(ContentDisplay.TEXT_ONLY);
						}
					}
//					this.getStyleClass().add("cellHighlight");
				}
			};
		});

		colProductID.setCellFactory( column -> {
			return new TableCell<Product, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(item == null || empty) {
						setGraphic(null);
						setText(null);
						setStyle("");
					} else {
						setGraphic(null);
						String searchVal = tfSearch.getText();
						if(!searchVal.isEmpty() &&
								cbSearchBy.getSelectionModel().getSelectedItem() ==
								Product.Property.ID.toString() &&
								item.toLowerCase().contains(searchVal.toLowerCase())) {
							setGraphic(buildFlowText(item, searchVal));
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}
						 else {
							setText(item);
							setTextFill(javafx.scene.paint.Color.BLACK);
							setStyle("");
							setContentDisplay(ContentDisplay.TEXT_ONLY);
						}
					}
				}
			};
		});

		tableNavProduct.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Product>() {
			@Override
			public void changed(ObservableValue<? extends Product> observable, Product oldValue, Product newValue) {
				selectedProduct = newValue;
				updateDetailTable(newValue);
			}
		});

		tableNavProduct.getItems().addListener(new ListChangeListener<Product>() {
			@Override
			public void onChanged(Change<? extends Product> p) {
				if(p.next()) {
					tableNavProduct.getSelectionModel().select(tableNavProduct.getItems().size()-1);
				}
			}
		});

		cbSearchBy.getItems().addAll(Product.Property.ID.toString(),
				Product.Property.NAME.toString(),
				Product.Property.TYPE.toString());

		cbSearchBy.getSelectionModel().select(0);

		tfSearch.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent ke) {
				filterNavTable(tfSearch.getText());
			}
		});
	}


	private TextFlow buildFlowText(String original, String filtered) {
		int filterIndex = original.toLowerCase().indexOf(filtered.toLowerCase());
		Text textBefore = new Text(original.substring(0, filterIndex));
		Text textAfter = new Text(original.substring(filterIndex + filtered.length()));
		Text textFilter = new Text(original.substring(filterIndex, filterIndex + filtered.length()));
		textFilter.setFill(Color.INDIANRED);
		textFilter.setFont(Font.font("null", FontWeight.BOLD, 16));
		return new TextFlow(textBefore, textFilter, textAfter);
	}

	@FXML private void handleBtnSelect() {
		if(selectedProduct == null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Product Selection Error!");
			alert.setHeaderText("Invalid selection!");
			alert.setContentText("No valid client has been selected.");
			alert.showAndWait();
		} else {
			switch(controllerType) {

			case NEW:
				newInvoiceController.setSelectedProduct(selectedProduct);
				newInvoiceController.tfProductName.setText(selectedProduct.getName());
				break;
			case EDIT:
				editInvoiceController.setSelectedProduct(selectedProduct);
				editInvoiceController.tfProductName.setText(selectedProduct.getName());
			}
			currentStage.close();
		}
	}



	@FXML private void handleBtnCancel() {
		currentStage.close();
	}

	private void updateDetailTable(Product product) {
		if(product != null) {
			tableDetailProduct.getItems().clear();

			PropertyValuePairList list = new PropertyValuePairList(product);
			tableDetailProduct.getItems().addAll(list.getList());
		}
	}

	private void filterNavTable(String value) {
		value = value.toLowerCase();
		boolean statusGreen = false;
		tableNavProduct.getItems().clear();
		tableDetailProduct.getItems().clear();
		String searchBy = cbSearchBy.getSelectionModel().getSelectedItem();
		ArrayList<Product> filteredList = new ArrayList<>();
		try {
			if(searchBy.equals(Product.Property.ID.toString())) {
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.ID);
				for(Product p : filteredList) {
					if(p.getProductID().contains(value))
						statusGreen = true;
				}
			} else if(searchBy.equals(Product.Property.NAME.toString())) {
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.NAME);
				for(Product p : filteredList) {
					if(p.getName().toLowerCase().contains(value))
						statusGreen = true;
				}
			}
			if(!filteredList.isEmpty()) {
				tableNavProduct.getItems().clear();
				tableNavProduct.getItems().addAll(filteredList);
				// Remove the last property before adding another -which is (text-field)
				tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);

				if(statusGreen)
					tfSearch.getStyleClass().add("tfFocusGreen");
				else
					tfSearch.getStyleClass().add("tfFocusRed");
			}
			if(value.isEmpty()) {
				tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);
				tfSearch.getStyleClass().add("tfFocusNormal");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
