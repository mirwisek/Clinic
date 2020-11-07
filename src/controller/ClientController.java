package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import database.ClientManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Client;
import model.PropertyValuePair;
import model.PropertyValuePairList;

public class ClientController {

	@FXML private TableView<PropertyValuePair> tableDetailClient;
	@FXML public TableView<Client> tableNavClient;
	@FXML private TableColumn<PropertyValuePair, String> colPropertyClient, colValueClient;
	@FXML private TableColumn<Client, String> colClientID, colClientName;
	@FXML private TableColumn<Client, Boolean> colLock;
	@FXML private TextField tfSearch;
	@FXML private Button btnAdvancedFilter;
	@FXML private ComboBox<Client.Property> cbSearchBy;

	private Stage newStage;
	private Stage editStage;

	public ObservableList<Client> clientList;

//	private GUIWindowController guiController;

	public void setReferences(GUIWindowController controller) {
//		guiController = controller;
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
		colLock.setCellValueFactory(new PropertyValueFactory<Client, Boolean>("locked"));

		// Disable column sort either DESC or ASC
		colPropertyClient.setSortable(false);
		colValueClient.setSortable(false);

		// Necessary for highlight of searchText
		tableNavClient.setFixedCellSize(29);

		colLock.setCellFactory( column -> {
			return new TableCell<Client, Boolean>() {

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
									Client c = getTableView().getItems().get(getIndex());
									// Invert the state
									ClientManager.updateLock(c.getId(), btn.isSelected());
								} catch (SQLException ex) {
									ex.printStackTrace();
								}
							}
						});
						setGraphic(btn);
						getStyleClass().remove(getStyleClass().size()-1);
						getStyleClass().add("stateLock");
						setText(null);
					}
				}
			};
		});

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
								Client.Property.NAME &&
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
								Client.Property.ID &&
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

		populateList();

		tableNavClient.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Client>() {
			@Override
			public void changed(ObservableValue<? extends Client> observable, Client oldValue, Client newValue) {
				updateDetailTable(newValue);
			}
		});

		tableNavClient.getItems().addListener(new ListChangeListener<Client>() {
			@Override
			public void onChanged(Change<? extends Client> c) {
				if(c.next()) {
					tableNavClient.getSelectionModel().select(tableNavClient.getItems().size()-1);
				}
			}
		});
// For highlighting background - Advanced for now
//		PseudoClass lastRow = PseudoClass.getPseudoClass("last-row");
//		tableNavClient.setRowFactory(new Callback<TableView<Client>, TableRow<Client>>() {
//
//			@Override
//			public TableRow<Client> call(TableView<Client> arg0) {
//				return new TableRow<Client>() {
//
//					@Override
//					public void updateIndex(int index) {
//						super.updateIndex(index);
//						pseudoClassStateChanged(lastRow, index >=0 && index == tableNavClient.getItems().size()-1 );
//					}
//
//				};
//			}
//
//		});

		cbSearchBy.getItems().addAll(Client.Property.ID,
				Client.Property.NAME,
				Client.Property.TYPE);

		cbSearchBy.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Client.Property>() {

			@Override
			public void changed(ObservableValue<? extends Client.Property> observable, Client.Property oldValue, Client.Property newValue) {
				switch(newValue) {
				case TYPE:
					btnAdvancedFilter.setDisable(false);
					tfSearch.setEditable(false);
					tfSearch.clear();
					break;
				default:
					btnAdvancedFilter.setDisable(true);
					tfSearch.setEditable(true);
					tfSearch.clear();
				}
			}
		});

		btnAdvancedFilter.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				openAdvancedFilterWindow();
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
		clientList = FXCollections.observableArrayList();
		//don't populate just initialize
//		try {
//			clientList = ClientManager.getClientList();
			tableNavClient.getItems().addAll(clientList);
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
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

	@FXML private void handleBtnAdvancedFilter() {

	}

	@FXML private void handleBtnNew() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NewClientView.fxml"));
			AnchorPane pane = loader.load();
			Scene scene = new Scene(pane);
			newStage = new Stage();
			newStage.setScene(scene);
			newStage.setTitle("Add Client");
			newStage.initModality(Modality.APPLICATION_MODAL);
			NewClientController controller = loader.getController();
			controller.setReferences(newStage, this);
			newStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML private void handleBtnEdit() {
		int selectedIndex = tableNavClient.getSelectionModel().getSelectedIndex();
		if(selectedIndex == -1) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Invalid Selection");
			alert.setHeaderText("No Client Selected");
			alert.setContentText("A client must be selected before editing!");
			alert.showAndWait();

		} else {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditClientView.fxml"));
				AnchorPane pane = loader.load();
				Scene scene = new Scene(pane);
				editStage = new Stage();
				editStage.setScene(scene);
				editStage.setTitle("Edit Client");
				editStage.initModality(Modality.APPLICATION_MODAL);
				EditClientController controller = loader.getController();
				controller.setReferences(editStage, tableNavClient.getItems(), selectedIndex);
				editStage.show();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML private void handleBtnDelete() {
		int selectedIndex = tableNavClient.getSelectionModel().getSelectedIndex();
		if(selectedIndex == -1)
			return;

		try {
			String id = tableNavClient.getItems().get(selectedIndex).getId();
			ClientManager.deleteClient(id);

			if(!tableNavClient.getItems().isEmpty()) {
				tableNavClient.getItems().remove(selectedIndex);
			}
			if(tableNavClient.getItems().size() == 0 &&
					!tableDetailClient.getItems().isEmpty()) {
				tableDetailClient.getItems().clear();
			}

		} catch ( SQLException ex ) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Deleting client failed");
			alert.setHeaderText("Error: " + ex.getMessage());
			alert.setContentText("Cause: " + ex.getCause());
			alert.showAndWait();
		}
	}

	private void updateDetailTable(Client client) {
		if(client != null) {
			tableDetailClient.getItems().clear();

			PropertyValuePairList list = new PropertyValuePairList(client);
			tableDetailClient.getItems().addAll(list.getList());
		}
	}

	private void filterNavTable(String value) {
		boolean statusGreen = false;
		tableNavClient.getItems().clear();
		tableDetailClient.getItems().clear();
		String searchBy = cbSearchBy.getSelectionModel().getSelectedItem().toString();
		ArrayList<Client> filteredList = new ArrayList<Client>();
		try {
			if(searchBy.equals(Client.Property.ID.toString())) {
				value = value.toLowerCase();
				filteredList = ClientManager.searchClient(value, ClientManager.Fields.ID);
				for(Client c : filteredList) {
					if(c.getId().contains(value)){
						statusGreen = true;
						break;
					}
				}
			} else if(searchBy.equals(Client.Property.NAME.toString())) {
				value = value.toLowerCase();
				filteredList = ClientManager.searchClient(value, ClientManager.Fields.NAME);
				for(Client c : filteredList) {
					if(c.getName().toLowerCase().contains(value)) {
						statusGreen = true;
						break;
					}
				}
			} else if(searchBy.equals(Client.Property.TYPE.toString())) {
				filteredList = ClientManager.searchClient(value, ClientManager.Fields.TYPE);
				for(Client c : filteredList) {
					if(value.contains(c.getType())) {
						statusGreen = true;
						break;
					}
				}
			}

			tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);
			if(statusGreen)
				tfSearch.getStyleClass().add("tfFocusGreen");
			else
				tfSearch.getStyleClass().add("tfFocusRed");

			if(!filteredList.isEmpty()) {
				tableNavClient.getItems().clear();
				tableNavClient.getItems().addAll(filteredList);
				// Remove the last property before adding another -which is (text-field)
			}
			if(value.isEmpty()) {
				tfSearch.getStyleClass().remove(tfSearch.getStyleClass().size()-1);
				tfSearch.getStyleClass().add("tfFocusNormal");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void openAdvancedFilterWindow() {
		Stage stage = new Stage();
		VBox pane = new VBox(10);
		HBox hBox = new HBox(5);
		CheckBox cbTypical = new CheckBox(Client.Type.Typical.toString());
		CheckBox cbElite = new CheckBox(Client.Type.Elite.toString());
		CheckBox cbSpecial = new CheckBox(Client.Type.Special.toString());
		Button btnCancel = new Button("Cancel");
		Button btnFilter = new Button("Filter");

		boolean[] cbValues = {false, false, false};

		try (FileReader fr = new FileReader("FilterClient.txt");
				BufferedReader br = new BufferedReader(fr);){
			String line;
			int i = 0;
			while((line = br.readLine()) != null) {
				cbValues[i++] = Boolean.parseBoolean(line);
			}

		} catch (IOException ex) { /* DO NOTHING */ }

		cbTypical.setSelected(cbValues[0]);
		cbElite.setSelected(cbValues[1]);
		cbSpecial.setSelected(cbValues[2]);

		btnCancel.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				stage.close();
			}
		});

		btnFilter.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				boolean[] filter = {cbTypical.isSelected(), cbElite.isSelected(), cbSpecial.isSelected()};
				try (FileWriter fw = new FileWriter("FilterClient.txt", false);
						BufferedWriter bw = new BufferedWriter(fw);){
					bw.write(String.valueOf(filter[0]));
					bw.newLine();
					bw.write(String.valueOf(filter[1]));
					bw.newLine();
					bw.write(String.valueOf(filter[2]));
				} catch (IOException e) {
					e.printStackTrace();
				}

				StringBuilder sb = new StringBuilder();
				if(filter[0])
					sb.append(Client.Type.Typical.toString());

				if(filter[1]) {
					sb.append((filter[0] ? "," : ""));
					sb.append(Client.Type.Elite.toString());
				}

				if(filter[2]) {
					sb.append(((filter[0] || filter[1]) ? "," : ""));
					sb.append(Client.Type.Special.toString());
				}
				String text = sb.toString();
				if(text.isEmpty()) {
					tfSearch.clear();
				}else {
					tfSearch.setText(text);
					tfSearch.requestFocus();
					filterNavTable(text);
				}
				stage.close();
			}
		});

		VBox.setMargin(cbTypical, new Insets(30, 0, 0, 40));
		VBox.setMargin(cbElite, new Insets(0, 0, 0, 40));
		VBox.setMargin(cbSpecial, new Insets(0, 0, 0, 40));
		VBox.setMargin(hBox, new Insets(20, 0, 0, 30));

		hBox.getChildren().addAll(btnCancel, btnFilter);
		pane.getChildren().addAll(cbTypical, cbElite, cbSpecial, hBox);

		Scene scene = new Scene(pane, 150, 180);
		stage.setScene(scene);
		stage.show();
	}

}
