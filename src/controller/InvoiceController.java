package controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import database.InvoiceManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterAttributes;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
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
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Invoice;
import model.Invoice.Property;
import model.Line;

public class InvoiceController {

	@FXML public TableView<Invoice> tableInvoice;
	@FXML private TableColumn<Invoice, Integer> colInvoiceID;
	@FXML private TableColumn<Invoice, String> colClientName, colClientID;
	@FXML private TableColumn<Invoice, Date> colEntry;
	@FXML private TableColumn<Invoice, Double> colTotal;
	@FXML private TableColumn<Invoice, Boolean> colLock;

	@FXML private Button btnFilter;

	@FXML public TableView<Line> tableLine;
	@FXML private TableColumn<Line, Integer> colLineNumber, colQuantity;
	@FXML private TableColumn<Line, String> colName, colPacking, colPriceBy;
	@FXML private TableColumn<Line, Double> colPrice, colDiscount;
	@FXML TextField tfSearch;
	@FXML ComboBox<Property> cbSearchBy;

	private Stage newStage;
	private Stage editStage;

	public static final int PRODUCT_RANGE = 600;

	public ObservableList<Invoice> invoiceList;

	private GUIWindowController guiController;
	private ProductController productController;

	public void setReferences(GUIWindowController controller, ProductController productController) {
		guiController = controller;
		this.productController = productController;
	}

	@FXML public void initialize() {

		colInvoiceID.setCellValueFactory(new PropertyValueFactory<Invoice, Integer>("invoiceID"));
		colClientID.setCellValueFactory(new PropertyValueFactory<Invoice, String>("clientID"));
		colClientName.setCellValueFactory(new PropertyValueFactory<Invoice, String>("clientName"));
		colEntry.setCellValueFactory(new PropertyValueFactory<Invoice, Date>("entry"));
		colTotal.setCellValueFactory(new PropertyValueFactory<Invoice, Double>("totalAmount"));
		colLock.setCellValueFactory(new PropertyValueFactory<Invoice, Boolean>("locked"));

		colLineNumber.setCellValueFactory(new PropertyValueFactory<Line, Integer>("lineNumber"));
		colPrice.setCellValueFactory(new PropertyValueFactory<Line, Double>("price"));
		colDiscount.setCellValueFactory(new PropertyValueFactory<Line, Double>("discount"));
		colQuantity.setCellValueFactory(new PropertyValueFactory<Line, Integer>("quantity"));
		colPriceBy.setCellValueFactory(new PropertyValueFactory<Line, String>("priceBy"));
		colPacking.setCellValueFactory(new PropertyValueFactory<Line, String>("packing"));
		colName.setCellValueFactory(new PropertyValueFactory<Line, String>("name"));

		// Disable column sort either DESC or ASC
		colLock.setSortable(false);

		// Necessary for highlight of searchText
		tableInvoice.setFixedCellSize(29);

		colLock.setCellFactory( column -> {
			return new TableCell<Invoice, Boolean>() {

				@Override
				protected void updateItem(Boolean item, boolean empty) {
					super.updateItem(item, empty);
					if(empty) {
						setGraphic(null);
						setText(null);
						setStyle("");
					} else {
						ToggleButton btn = new ToggleButton();
						btn.selectedProperty().setValue(item);
						btn.setOnAction(new EventHandler<ActionEvent>() {

							@Override
							public void handle(ActionEvent e) {
								try {
									Invoice in = getTableView().getItems().get(getIndex());
									// Invert the state
									InvoiceManager.updateLock(in.getInvoiceID(), btn.isSelected());
								} catch (SQLException ex) {
									ex.printStackTrace();
								}
							}
						});
						setGraphic(btn);
						getStyleClass().remove(getStyleClass().size()-1);
						this.getStyleClass().add("stateLock");
						setText(null);
					}
				}
			};
		});

		colInvoiceID.setCellFactory( column -> {
			return new TableCell<Invoice, Integer>(){

				@Override
				protected void updateItem(Integer arg, boolean empty) {
					super.updateItem(arg, empty);
					String item = String.valueOf(arg);
					if(item == null || empty) {
						setGraphic(null);
						setText(null);
						setStyle("");
					} else {
						setGraphic(null);
						String searchVal = tfSearch.getText();
						if(!searchVal.isEmpty() &&
								cbSearchBy.getSelectionModel().getSelectedItem() ==
								Property.INVOICE_ID &&
								item.contains(searchVal)) {
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

		colClientID.setCellFactory( column -> {
			return new TableCell<Invoice, String>() {

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
								Property.CLIENT_ID &&
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

		colClientName.setCellFactory( column -> {
			return new TableCell<Invoice, String>() {

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
								Property.CLIENT_NAME &&
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

		colEntry.setCellFactory( column -> {
			return new TableCell<Invoice, Date>() {

				@Override
				protected void updateItem(Date item, boolean empty) {
					super.updateItem(item, empty);
					if(item == null || empty) {
						setGraphic(null);
						setText(null);
						setStyle("");
					} else {
						setGraphic(null);
						SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
						setText(f.format(item));
						setTextFill(javafx.scene.paint.Color.BLACK);
						setStyle("");
						setContentDisplay(ContentDisplay.TEXT_ONLY);

					}
				}
			};
		});

		populateList();

		tableInvoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Invoice>() {
			@Override
			public void changed(ObservableValue<? extends Invoice> observable, Invoice oldValue, Invoice newValue) {
				updateLineTable(newValue);
			}
		});

		tableInvoice.getItems().addListener(new ListChangeListener<Invoice>() {
			@Override
			public void onChanged(Change<? extends Invoice> in) {
				if(in.next()) {
					tableInvoice.getSelectionModel().select(tableInvoice.getItems().size()-1);
				}
			}
		});

		cbSearchBy.getItems().addAll(Property.INVOICE_ID, Property.CLIENT_ID, Property.CLIENT_NAME, Property.ENTRY, Property.TOTAL_AMOUNT);

		cbSearchBy.getSelectionModel().select(0);

		cbSearchBy.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Property>() {

			@Override
			public void changed(ObservableValue<? extends Property> observable, Property oldValue, Property newValue) {
				switch(newValue) {
				case INVOICE_ID:
				case ENTRY:
				case TOTAL_AMOUNT:
					btnFilter.setDisable(false);
					break;
					default:
						btnFilter.setDisable(true);
				}
				switch(newValue) {
				case ENTRY:
					tfSearch.setEditable(false);
					break;
				default:
					tfSearch.setEditable(true);
				}
			}

		});

		tfSearch.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent ke) {
				filterInvoiceTable(tfSearch.getText());
			}
		});

	}

	private void populateList() {
		invoiceList = FXCollections.observableArrayList();
		try {
			invoiceList.addAll(InvoiceManager.getInvoiceList());
			tableInvoice.getItems().addAll(invoiceList);
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

	public void updateLineTable(Invoice invoice) {
		if(invoice != null) {
			try {
				ArrayList<Line> list = InvoiceManager.getLinesList(invoice.getInvoiceID());
				if(list != null) {
					tableLine.getItems().clear();
					tableLine.getItems().addAll(list);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void filterInvoiceTable(String value) {
		value = value.toLowerCase();
		boolean statusGreen = false;
		tableInvoice.getItems().clear();
		tableLine.getItems().clear();
		Property searchBy = cbSearchBy.getSelectionModel().getSelectedItem();
		ArrayList<Invoice> filteredList = new ArrayList<>();
		try {
			if(searchBy.equals(Property.INVOICE_ID)) {
				filteredList = InvoiceManager.searchInvoice(value, InvoiceManager.Fields.INVOICE_ID);
				for(Invoice in : filteredList) {
					String id = String.valueOf(in.getInvoiceID());
					if(id.contains(value)) {
						statusGreen = true;
						break;
					}
				}
			} else if(searchBy.equals(Property.CLIENT_ID)) {
				filteredList = InvoiceManager.searchInvoice(value, InvoiceManager.Fields.CLIENT_ID);
				for(Invoice in : filteredList) {
					if(in.getClientID().contains(value)){
						statusGreen = true;
						break;
					}
				}
			}
			else if(searchBy.equals(Property.CLIENT_NAME)) {
				filteredList = InvoiceManager.searchInvoice(value, InvoiceManager.Fields.CLIENT_NAME);
				for(Invoice in : filteredList) {
					if(in.getClientName().contains(value)){
						statusGreen = true;
						break;
					}
				}
			}
			// Remove the last property before adding another -which is (text-field)
			tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);

			if(statusGreen)
				tfSearch.getStyleClass().add("tfFocusGreen");
			else
				tfSearch.getStyleClass().add("tfFocusRed");
			if(!filteredList.isEmpty()) {
				tableInvoice.getItems().clear();
				tableInvoice.getItems().addAll(filteredList);
			}
			if(value.isEmpty()) {
				tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);
				tfSearch.getStyleClass().add("tfFocusNormal");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void filterNavTable(Date dFrom, Date dTo) {
		boolean statusGreen = false;
		tableInvoice.getItems().clear();
		tableLine.getItems().clear();
		Property searchBy = cbSearchBy.getSelectionModel().getSelectedItem();
		ArrayList<Invoice> filteredList = new ArrayList<>();

		try {
			if(searchBy == Property.ENTRY) {
				SimpleDateFormat entryFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String entryFrom = entryFormat.format(dFrom);
				String entryTo = entryFormat.format(dTo);
				tfSearch.setText("From(" + entryFrom + ") To(" + entryTo + ")");

				//Since I didn't added any input field for seconds, so I'm concatenating ':00' manually
				String entryQuery = new String("\'" + entryFrom + ":00\' AND \'" + entryTo + ":00\'");

				filteredList = InvoiceManager.searchInvoice(entryQuery, InvoiceManager.Fields.ENTRY);
				for(Invoice in : filteredList) {
					Date date = in.getEntry();
					// Since >= AND <= is not supported that is why used that else if block
					if(date.after(dFrom) && date.before(dTo)){
						statusGreen = true;
						break;
					} else if(date.compareTo(dFrom) == 0 || date.compareTo(dTo) == 0) {
						statusGreen = true;
						break;
					}
				}
			}
			tfSearch.requestFocus();

			for(Invoice in : filteredList) {
				if(!in.isLocked()){
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
				tableInvoice.getItems().clear();
				tableInvoice.getItems().addAll(filteredList);
			}
			if(tableInvoice.getItems().isEmpty()) {
				tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);
				tfSearch.getStyleClass().add("tfFocusNormal");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void filterNavTable(String from, String to) {
		boolean statusGreen = false;
		tableInvoice.getItems().clear();
		tableLine.getItems().clear();
		Property searchBy = cbSearchBy.getSelectionModel().getSelectedItem();
		ArrayList<Invoice> filteredList = new ArrayList<>();

		String value = new String(from + " AND " + to);
		double fromDouble = Double.parseDouble(from);
		double toDouble = Double.parseDouble(to);
		tfSearch.setText(from + "-" + to);
		tfSearch.requestFocus();

		try {
			switch(searchBy) {
			case INVOICE_ID:
				filteredList = InvoiceManager.searchInvoice(value, InvoiceManager.Fields.INVOICE_ID_RANGE);
				for(Invoice in : filteredList) {
					if(in.getInvoiceID() >= fromDouble && in.getInvoiceID() <= toDouble){
						statusGreen = true;
						break;
					}
				}
				break;
			case TOTAL_AMOUNT:
				filteredList = InvoiceManager.searchInvoice(value, InvoiceManager.Fields.TOTAL_AMOUNT_RANGE);
				for(Invoice in : filteredList) {
					if(in.getTotalAmount() >= fromDouble && in.getTotalAmount() <= toDouble){
						statusGreen = true;
						break;
					}
				}
				break;
			default:
			}

			for(Invoice in : filteredList) {
				if(!in.isLocked()){
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
				tableInvoice.getItems().clear();
				tableInvoice.getItems().addAll(filteredList);
			}
			if(value.isEmpty()) {
				tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);
				tfSearch.getStyleClass().add("tfFocusNormal");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@FXML private void handleBtnNew() {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NewInvoiceView.fxml"));
			AnchorPane pane = loader.load();
			Scene scene = new Scene(pane);
			newStage = new Stage();
			newStage.setScene(scene);
			newStage.setTitle("Create Invoice");
			newStage.initModality(Modality.APPLICATION_MODAL);
			newStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					try {
						InvoiceManager.rollBack();
						InvoiceManager.setAutoCommitMode(true);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
			NewInvoiceController controller = loader.getController();
			controller.setReferences(newStage, this, guiController, productController);
			newStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML private void handleBtnEdit() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditInvoiceView.fxml"));
			AnchorPane pane = loader.load();
			Scene scene = new Scene(pane);
			editStage = new Stage();
			editStage.setScene(scene);
			editStage.setTitle("Edit Invoice");
			editStage.initModality(Modality.APPLICATION_MODAL);
			editStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					try {
						InvoiceManager.rollBack();
						InvoiceManager.setAutoCommitMode(true);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
			int selectedIndex = tableInvoice.getSelectionModel().getSelectedIndex();
			if(selectedIndex == -1) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Row Selection Warning");
				alert.setHeaderText("Row not selected!");
				alert.setContentText("Please select a row from table and then hit edit to customize invoice.");
				alert.showAndWait();
			}else {
				EditInvoiceController controller = loader.getController();
				controller.setReferences(editStage, this, selectedIndex);
				editStage.show();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML private void handleBtnDelete() {
		int selectedIndex = tableInvoice.getSelectionModel().getSelectedIndex();
		if(selectedIndex == -1)
			return;

		try {
			int id = tableInvoice.getItems().get(selectedIndex).getInvoiceID();
			InvoiceManager.deleteInvoice(id);

			if(!tableInvoice.getItems().isEmpty()) {
				tableInvoice.getItems().remove(selectedIndex);

				if(tableInvoice.getItems().size() == 0) {
					tableInvoice.getItems().clear();
					tableLine.getItems().clear();
				}
			}

		} catch ( SQLException ex ) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Deleting Invoice failed");
			alert.setHeaderText("Error: " + ex.getMessage());
			alert.setContentText("Cause: " + ex.getCause());
			alert.showAndWait();
		}
	}


	@FXML private void handleBtnFilter() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdvancedInvoiceFilterView.fxml"));
			GridPane pane = loader.load();
			Scene scene = new Scene(pane);
			newStage = new Stage();
			newStage.setScene(scene);
			newStage.setTitle("Filter Invoice");
			newStage.initModality(Modality.APPLICATION_MODAL);
			AdvancedInvoiceFilterController controller = loader.getController();
			controller.setReferences(newStage, this);
			newStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML private void handleBtnPrint() {
		Invoice in = tableInvoice.getSelectionModel().getSelectedItem();
		if(in != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/InvoicePreviewView.fxml"));
				AnchorPane pane = loader.load();
				Scene scene = new Scene(pane);

				pane.getStyleClass().add("white-bg");

				Stage printStage = new Stage();
				printStage.setScene(scene);
				printStage.setTitle("Print Invoice");
				printStage.initModality(Modality.APPLICATION_MODAL);

				PrintPreviewController ppc = loader.getController();
				ppc.setReferences(printStage, this);

				printStage.show();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}



}
