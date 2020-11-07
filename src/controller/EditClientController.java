package controller;

import java.sql.SQLException;

import database.ClientManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import model.Client;

public class EditClientController {

	private Stage currentStage;
	private ObservableList<Client> tableList;
	private int selectedIndex = -1;
	private boolean locked = false;

	@FXML private TextField tfClientName, tfPhoneNumber;
	@FXML ComboBox<String> cbClientType;

	public void setReferences(Stage stage, ObservableList<Client> navTableList, int selection) {
		currentStage = stage;
		tableList = navTableList;
		selectedIndex = selection;
		if(selectedIndex != -1) {
			Client client = tableList.get(selectedIndex);
			tfClientName.setText(client.getName());
			tfPhoneNumber.setText(client.getPhoneNumber());
			cbClientType.getSelectionModel().select(client.getType());
			locked = client.isLocked();
		}
	}

	@FXML private void initialize() {
		Client.Type[] typeList = Client.Type.values();
		for(int i = 0; i < typeList.length; i++) {
			cbClientType.getItems().add(typeList[i].toString());
		}
	}

	@FXML private void handleBtnSave() {
		if(selectedIndex != -1) {
			String name = tfClientName.getText();
			String phone = tfPhoneNumber.getText();
			String type = cbClientType.getSelectionModel().getSelectedItem();
			Client client = new Client(name, phone, type, locked);
			try {
				ClientManager.updateClient(client);
				tableList.remove(selectedIndex);
				tableList.add(selectedIndex, client);
				currentStage.close();
			} catch( SQLException ex ) {
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

}
