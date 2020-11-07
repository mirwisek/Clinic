package controller;

import java.sql.SQLException;
import java.util.ArrayList;

import database.ClientManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
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
import model.Client;
import model.PropertyValuePair;
import model.PropertyValuePairList;

public class ClientSelectController {

	@FXML private TableView<PropertyValuePair> tableDetailClient;
	@FXML public TableView<Client> tableNavClient;
	@FXML private TableColumn<PropertyValuePair, String> colPropertyClient, colValueClient;
	@FXML private TableColumn<Client, String> colClientID, colClientName;
	@FXML private TextField tfSearch;
	@FXML private ComboBox<String> cbSearchBy;

	private Stage currentStage;
	private NewInvoiceController newInvoiceController = null;
	private EditInvoiceController editInvoiceController = null;
	private Client selectedClient = null;
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

	@FXML
	public void initialize() {

		// Table
		colPropertyClient.setCellValueFactory(
				new PropertyValueFactory<PropertyValuePair, String>("property"));
		colValueClient.setCellValueFactory(
				new PropertyValueFactory<PropertyValuePair, String>("value"));

		colClientID.setCellValueFactory(new PropertyValueFactory<Client, String>("id"));
		colClientName.setCellValueFactory(new PropertyValueFactory<Client, String>("name"));

		// Disable column sort either DESC or ASC
		colPropertyClient.setSortable(false);
		colValueClient.setSortable(false);

		// Necessary for highlight of searchText
		tableNavClient.setFixedCellSize(29);

		colClientName.setCellFactory( column -> {
			return new TableCell<Client, String>() {

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
								Client.Property.NAME.toString() &&
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
					this.getStyleClass().add("cellHighlight");
				}
			};
		});

		colClientID.setCellFactory( column -> {
			return new TableCell<Client, String>() {

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
								Client.Property.ID.toString() &&
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

		tableNavClient.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Client>() {
			@Override
			public void changed(ObservableValue<? extends Client> observable, Client oldValue, Client newValue) {
				selectedClient = newValue;
				updateDetailTable(newValue);
			}
		});

		cbSearchBy.getItems().addAll(Client.Property.ID.toString(),
				Client.Property.NAME.toString(),
				Client.Property.TYPE.toString());

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
		if(selectedClient == null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Client Selection Error!");
			alert.setHeaderText("Invalid selection!");
			alert.setContentText("No valid client has been selected.");
			alert.showAndWait();
		}else {
			switch(controllerType) {
			case NEW:
				newInvoiceController.setSelectedClient(selectedClient);
				newInvoiceController.tfClientName.setText(selectedClient.getName());
				break;
			case EDIT:
				editInvoiceController.setSelectedClient(selectedClient);
				editInvoiceController.tfClientName.setText(selectedClient.getName());
			}
			currentStage.close();
		}
	}

	@FXML private void handleBtnCancel() {
		currentStage.close();
	}

	private void updateDetailTable(Client client) {
		if(client != null) {
			tableDetailClient.getItems().clear();

			PropertyValuePairList list = new PropertyValuePairList(client);
			tableDetailClient.getItems().addAll(list.getList());
		}
	}

	private void filterNavTable(String value) {
		value = value.toLowerCase();
		boolean statusGreen = false;
		tableNavClient.getItems().clear();
		tableDetailClient.getItems().clear();
		String searchBy = cbSearchBy.getSelectionModel().getSelectedItem();
		ArrayList<Client> filteredList = new ArrayList<Client>();
		try {
			if(searchBy.equals(Client.Property.ID.toString())) {
				filteredList = ClientManager.searchClient(value, ClientManager.Fields.ID);
				for(Client c : filteredList) {
					if(c.getId().contains(value))
						statusGreen = true;
				}
			} else if(searchBy.equals(Client.Property.NAME.toString())) {
				filteredList = ClientManager.searchClient(value, ClientManager.Fields.NAME);
				for(Client c : filteredList) {
					if(c.getName().toLowerCase().contains(value))
						statusGreen = true;
				}
			} else {
				filteredList = ClientManager.searchClient(value, ClientManager.Fields.TYPE);
				for(Client c : filteredList) {
					if(c.getType().toLowerCase().contains(value))
						statusGreen = true;
				}
			}
			if(!filteredList.isEmpty()) {
				tableNavClient.getItems().clear();
				tableNavClient.getItems().addAll(filteredList);
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
