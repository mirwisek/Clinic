package controller;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import database.ProductManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Product;
import model.Product.Property;
import model.ProductState;
import model.PropertyValuePair;
import model.PropertyValuePairList;

public class ProductController {

	@FXML private TableView<PropertyValuePair> tableDetailProduct;
	@FXML public TableView<Product> tableNavProduct;
	@FXML private TableColumn<PropertyValuePair, String> colPropertyProduct, colValueProduct;
	@FXML private TableColumn<Product, String> colProductID, colProductName;
	@FXML private TableColumn<Product, ProductState> colState;
	@FXML TextField tfSearch;
	@FXML ComboBox<Property> cbSearchBy;
	@FXML private Button btnFilter;

	private Stage newStage;
	private Stage editStage;

	public ObservableList<Product> productList;

	private GUIWindowController guiController;

	public void setReferences(GUIWindowController controller) {
		guiController = controller;

		populateList();
	}

	@FXML public void initialize() {

		// Table
		colPropertyProduct.setCellValueFactory(
				new PropertyValueFactory<PropertyValuePair, String>("property"));
		colValueProduct.setCellValueFactory(
				new PropertyValueFactory<PropertyValuePair, String>("value"));

		colProductID.setCellValueFactory(new PropertyValueFactory<Product, String>("productID"));
		colProductName.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));
		colState.setCellValueFactory(new PropertyValueFactory<Product, ProductState>("state"));

		// Disable column sort either DESC or ASC
		colPropertyProduct.setSortable(false);
		colValueProduct.setSortable(false);

		colPropertyProduct.setCellFactory( column -> {
			return new TableCell<PropertyValuePair, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(empty) {
						setGraphic(null);
						setText(null);
						setStyle("");
					} else {
						if(item.charAt(0) == '*') {
							setGraphic(null);
							setTextFill(Color.DARKMAGENTA);
							setFont(Font.font("null", FontWeight.BOLD, 16));
							setText(item.replace("*", ""));
							setStyle("");
						}else {
							setGraphic(null);
							setFont(Font.font("null", FontWeight.NORMAL, 15));
							setTextFill(Color.BLACK);
//							setFont(Font.font("null", FontWeight.BOLD, 16));
							setText("  " + item);
							setStyle("");
						}
					}
				}
			};
		});

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
		tableDetailProduct.setFixedCellSize(29);

		colState.setCellValueFactory(state -> new ReadOnlyObjectWrapper<ProductState>(state.getValue().getProductState()));

		colState.setCellFactory( column -> {
			return new TableCell<Product, ProductState>() {

				@Override
				protected void updateItem(ProductState item, boolean empty) {
					super.updateItem(item, empty);
					if(item == null || empty) {
						setGraphic(null);
						setText(null);
						setStyle("");
					} else {
						ToggleButton btn = new ToggleButton();
//						btn.selectedProperty().setValue(item == ProductState.UNLOCKED ? false : true);
						if(item == ProductState.UNLOCKED || item == ProductState.LOCKED) {
							btn.selectedProperty().setValue(item == ProductState.LOCKED ? true : false);
							btn.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(ActionEvent e) {
									try {
										Product c = getTableView().getItems().get(getIndex());
										// Invert the state

										ProductManager.updateState(c.getProductID(),
												(c.getProductState() == ProductState.LOCKED) ? ProductState.UNLOCKED : ProductState.LOCKED);
									} catch (SQLException ex) {
										ex.printStackTrace();
									}
								}
							});
						}else {
							btn.setOnAction(null);
						}
						setGraphic(btn);
						// Previous styles must be removed or else it will stack
						getStyleClass().remove(getStyleClass().size()-1);
						switch(item) {
						case LOCKED:
						case UNLOCKED:
							getStyleClass().add("stateLock");
							break;
						case CRITICAL:
						case LOCKED_CRITICAL:
							getStyleClass().add("stateCritical");
							break;
						}
						setText(null);
					}
				}
			};
		});

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
								Property.NAME &&
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
								Property.ID &&
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

		cbSearchBy.getItems().addAll(Property.ID,
				Property.NAME,
				Property.TYPE,
				Property.COMPOSITION,
				Property.PACKING,
				Property.COMPANY,
				Property.SUPPLIER,
				Property.ENTRY,
				Property.EXPIRY,
				Property.LEGAL,
				Property.PRICE_CARTON,
				Property.PRICE_BOX,
				Property.PRICE_BP,
				Property.PRICE_PILL,
				Property.PRICE_CARTON_CR,
				Property.PRICE_BOX_CR,
				Property.PRICE_BP_CR,
				Property.PRICE_PILL_CR,
				Property.QTY_CARTONS,
				Property.QTY_BOXES,
				Property.QTY_BP,
				Property.QTY_PILLS
				);

		cbSearchBy.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Product.Property>() {

			@Override
			public void changed(ObservableValue<? extends Property> arg0, Property oldVal, Property newVal) {
				switch(newVal) {
				case TYPE:
				case ENTRY:
				case EXPIRY:
				case PRICE_CARTON:
				case PRICE_CARTON_CR:
				case PRICE_BOX:
				case PRICE_BOX_CR:
				case PRICE_BP:
				case PRICE_BP_CR:
				case PRICE_PILL:
				case PRICE_PILL_CR:
				case QTY_CARTONS:
				case QTY_BOXES:
				case QTY_BP:
				case QTY_PILLS:
					btnFilter.setDisable(false);
					tfSearch.setEditable(false);
					tfSearch.clear();
					break;
				default:
					btnFilter.setDisable(true);
					tfSearch.setEditable(true);
					tfSearch.clear();
				}
			}
		});

		cbSearchBy.getSelectionModel().select(0);

		tfSearch.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent ke) {
				filterNavTable(tfSearch.getText());
			}
		});

	}

	private void populateList() {
		productList = FXCollections.observableArrayList();
		try {
			productList.addAll(ProductManager.getCriticalProductsList(InvoiceController.PRODUCT_RANGE, true));
			if(!ProductManager.getCriticalProductsList(InvoiceController.PRODUCT_RANGE, false).isEmpty())
				showWarningProduct(true);
			tableNavProduct.getItems().addAll(productList);

		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	@FXML private void handleBtnNew() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NewProductView.fxml"));
			AnchorPane pane = loader.load();
			Scene scene = new Scene(pane);
			newStage = new Stage();
			newStage.setScene(scene);
			newStage.setTitle("Add Product");
			newStage.initModality(Modality.APPLICATION_MODAL);
			NewProductController controller = loader.getController();
			controller.setReferences(newStage, this);
			newStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML private void handleBtnEdit() {
		int selectedIndex = tableNavProduct.getSelectionModel().getSelectedIndex();
		if(selectedIndex == -1) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Invalid Selection");
			alert.setHeaderText("No Product Selected");
			alert.setContentText("A product must be selected before editing!");
			alert.showAndWait();
		} else {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditProductView.fxml"));
				AnchorPane pane = loader.load();
				Scene scene = new Scene(pane);
				editStage = new Stage();
				editStage.setScene(scene);
				editStage.setTitle("Edit Product");
				editStage.initModality(Modality.APPLICATION_MODAL);

				EditProductController controller = loader.getController();
				controller.setReferences(editStage, this, tableNavProduct, selectedIndex);
				editStage.show();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML private void handleBtnDelete() {
		int selectedIndex = tableNavProduct.getSelectionModel().getSelectedIndex();
		if(selectedIndex == -1)
			return;

		try {
			String id = tableNavProduct.getItems().get(selectedIndex).getProductID();
			ProductManager.deleteProduct(id);

			if(!tableNavProduct.getItems().isEmpty()) {
				tableNavProduct.getItems().remove(selectedIndex);
				tableDetailProduct.getItems().clear();
			}
			if(tableNavProduct.getItems().size() == 0 &&
					!tableNavProduct.getItems().isEmpty()) {
				tableNavProduct.getItems().clear();
			}

		} catch ( SQLException ex ) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Deleting product failed");
			alert.setHeaderText("Error: " + ex.getMessage());
			alert.setContentText("Cause: " + ex.getCause());
			alert.showAndWait();
		}
	}

	@FXML private void handleBtnFilter() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdvancedProductFilterView.fxml"));
			GridPane pane = loader.load();
			Scene scene = new Scene(pane);
			newStage = new Stage();
			newStage.setScene(scene);
			newStage.setTitle("Filter Product");
			newStage.initModality(Modality.APPLICATION_MODAL);
			AdvancedProductFilterController controller = loader.getController();
			controller.setReferences(newStage, this);
			newStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateDetailTable(Product product) {
		if(product != null) {
			tableDetailProduct.getItems().clear();

			PropertyValuePairList list = new PropertyValuePairList(product);
			tableDetailProduct.getItems().addAll(list.getList());
		}
	}

	public void filterNavTable(String value) {
		boolean statusGreen = false;
		tableNavProduct.getItems().clear();
		tableDetailProduct.getItems().clear();
		Property searchBy = cbSearchBy.getSelectionModel().getSelectedItem();
		ArrayList<Product> filteredList = new ArrayList<>();
		try {
			switch(searchBy) {
			case ID:
				value = value.toLowerCase();
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.ID);
				for(Product p : filteredList) {
					if(p.getProductID().contains(value)){
						statusGreen = true;
						break;
					}
				}
				break;
			case NAME:
				value = value.toLowerCase();
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.NAME);
				for(Product p : filteredList) {
					if(p.getName().toLowerCase().contains(value)){
						statusGreen = true;
						break;
					}
				}
				break;
			case TYPE:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.TYPE);
				for(Product p : filteredList) {
					if(value.contains(p.getType())){
						statusGreen = true;
						break;
					}
				}
				break;
			case COMPANY:
				value = value.toLowerCase();
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.COMPANY);
				for(Product p : filteredList) {
					if(p.getCompany().toLowerCase().contains(value)){
						statusGreen = true;
						break;
					}
				}
				break;
			case SUPPLIER:
				value = value.toLowerCase();
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.SUPPLIER);
				for(Product p : filteredList) {
					if(p.getSupplier().toLowerCase().contains(value)){
						statusGreen = true;
						break;
					}
				}
				break;
			case COMPOSITION:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.COMPOSITION);
				for(Product p : filteredList) {
					if(p.getComposition().toLowerCase().contains(value)){
						statusGreen = true;
						break;
					}
				}
				break;
			case PACKING:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.PACKING);
				for(Product p : filteredList) {
					if(p.getPacking().contains(value)){
						statusGreen = true;
						break;
					}
				}
				break;
			case LEGAL:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.LEGAL);
				if(value.equals("legal")) {
					for(Product p : filteredList) {
						if(p.getLegal()){
							statusGreen = true;
							break;
						}
					}
				}else {
					for(Product p : filteredList) {
						if(!p.getLegal()){
							statusGreen = true;
							break;
						}
					}
				}

			default:
			}


			for(Product p : filteredList) {
				if(p.getProductState() == ProductState.UNLOCKED){
					statusGreen = true;
					break;
				}
			}

			// Remove the last property before adding another -which is (text-field)
			tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);

			if(statusGreen)
				tfSearch.getStyleClass().add("tfFocusGreen");
			else
				tfSearch.getStyleClass().add("tfFocusRed");

			if(!filteredList.isEmpty()) {
				tableNavProduct.getItems().clear();
				tableNavProduct.getItems().addAll(filteredList);
			}
			if(value.isEmpty()) {
				tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);
				tfSearch.getStyleClass().add("tfFocusNormal");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void filterNavTable(String from, String to) {
		boolean statusGreen = false;
		tableNavProduct.getItems().clear();
		tableDetailProduct.getItems().clear();
		Property searchBy = cbSearchBy.getSelectionModel().getSelectedItem();

		ArrayList<Product> filteredList = new ArrayList<>();
		String value = new String(from + " AND " + to);
		double fromDouble = Double.parseDouble(from);
		double toDouble = Double.parseDouble(to);
		tfSearch.setText(from + "-" + to);
		tfSearch.requestFocus();

		try {
			switch(searchBy) {
			case QTY_CARTONS:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.QTY_CARTONS);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case QTY_BOXES:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.QTY_BOX);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case QTY_BP:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.QTY_BP);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case QTY_PILLS:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.QTY_PILLS);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case PRICE_CARTON:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.PRICE_CARTON);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case PRICE_CARTON_CR:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.PRICE_CARTON_CR);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case PRICE_BOX:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.PRICE_BOX);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case PRICE_BOX_CR:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.PRICE_BOX_CR);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case PRICE_BP:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.PRICE_BP);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case PRICE_BP_CR:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.PRICE_BP_CR);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case PRICE_PILL:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.PRICE_PILL);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			case PRICE_PILL_CR:
				filteredList = ProductManager.searchProduct(value, ProductManager.Fields.PRICE_PILL_CR);
				statusGreen = isStatusGreen(filteredList, searchBy, fromDouble, toDouble);
				break;
			default:
			}

			for(Product p : filteredList) {
				if(p.getProductState() == ProductState.UNLOCKED){
					statusGreen = true;
					break;
				}
			}

			// Remove the last property before adding another -which is (text-field)
			tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);

			if(statusGreen)
				tfSearch.getStyleClass().add("tfFocusGreen");
			else
				tfSearch.getStyleClass().add("tfFocusRed");

			if(!filteredList.isEmpty()) {
				tableNavProduct.getItems().clear();
				tableNavProduct.getItems().addAll(filteredList);
			}
			if(value.isEmpty()) {
				tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);
				tfSearch.getStyleClass().add("tfFocusNormal");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean isStatusGreen(ArrayList<Product> filteredList, Property searchBy, double from, double to) {
		for(Product p : filteredList) {
			double value = p.getFieldByProperty(searchBy);
			if(value >= from && value <= to){
				return true;
			}
		}
		return false;
	}


	public void filterNavTable(Date dFrom, Date dTo) {
		boolean statusGreen = false;
		tableNavProduct.getItems().clear();
		tableDetailProduct.getItems().clear();
		Property searchBy = cbSearchBy.getSelectionModel().getSelectedItem();
		ArrayList<Product> filteredList = new ArrayList<>();

		try {
			switch(searchBy) {

			case ENTRY:
				SimpleDateFormat entryFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String entryFrom = entryFormat.format(dFrom);
				String entryTo = entryFormat.format(dTo);
				tfSearch.setText("From(" + entryFrom + ") To(" + entryTo + ")");

				//Since I didn't added any input field for seconds, so I'm concatenating ':00' manually
				String entryQuery = new String("\'" + entryFrom + ":00\' AND \'" + entryTo + ":00\'");
				System.out.println(entryQuery);
				filteredList = ProductManager.searchProduct(entryQuery, ProductManager.Fields.ENTRY);
				for(Product p : filteredList) {
					Date date = p.getdEntry();
					// Since >= AND <= is not supported that is why used that else if block
					if(date.after(dFrom) && date.before(dTo)){
						statusGreen = true;
						break;
					} else if(date.compareTo(dFrom) == 0 || date.compareTo(dTo) == 0) {
						statusGreen = true;
						break;
					}
				}
				break;
			case EXPIRY:
				SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyy-MM-dd");
				String expiryFrom = expiryFormat.format(dFrom);
				String expiryTo = expiryFormat.format(dTo);
				tfSearch.setText("From(" + expiryFrom + ") To(" + expiryTo + ")");

				String expiryQuery = new String("\'" + expiryFrom + "\' AND \'" + expiryTo + "\'");
				filteredList = ProductManager.searchProduct(expiryQuery, ProductManager.Fields.EXPIRY);
				for(Product p : filteredList) {
					Date date = p.getdExpiry();
					// Since >= AND <= is not supported that is why used that else if block
					if(date.after(dFrom) && date.before(dTo)){
						statusGreen = true;
						break;
					} else if(date.compareTo(dFrom) == 0 || date.compareTo(dTo) == 0) {
						statusGreen = true;
						break;
					}
				}
				break;
			default:

			}
			tfSearch.requestFocus();

			for(Product p : filteredList) {
				if(p.getProductState() == ProductState.UNLOCKED){
					statusGreen = true;
					break;
				}
			}

			// Remove the last property before adding another -which is (text-field)
			tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);

			if(statusGreen)
				tfSearch.getStyleClass().add("tfFocusGreen");
			else
				tfSearch.getStyleClass().add("tfFocusRed");

			if(!filteredList.isEmpty()) {
				tableNavProduct.getItems().clear();
				tableNavProduct.getItems().addAll(filteredList);
			}
			if(tableNavProduct.getItems().isEmpty()) {
				tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);
				tfSearch.getStyleClass().add("tfFocusNormal");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void showWarningProduct(boolean shown) {
		guiController.showWarningProduct(shown);
	}

}
