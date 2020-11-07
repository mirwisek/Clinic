package controller;

import java.sql.SQLException;

import database.ClientManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Client;

public class NewClientController {

	private Stage currentStage;
	private ClientController clientController;

	@FXML private TextField tfClientName, tfPhoneNumber;
	@FXML private ComboBox<String> cbClientType;

	public void setReferences(Stage stage, ClientController controller) {
		currentStage = stage;
		clientController = controller;
	}

	@FXML private void initialize() {
		Client.Type[] typeList = Client.Type.values();
		for(int i = 0; i < typeList.length; i++) {
			cbClientType.getItems().add(typeList[i].toString());
		}
		cbClientType.getSelectionModel().selectFirst();
	}

	@FXML private void handleBtnAdd() {
		String name = tfClientName.getText();
		String phone = tfPhoneNumber.getText();
		String type = cbClientType.getSelectionModel().getSelectedItem();
		Client client = new Client(name, phone, type);
		try {
			ClientManager.addClient(client);
			clientController.clientList.add(client);
			clientController.tableNavClient.setItems(clientController.clientList);
			currentStage.close();
		} catch( SQLException ex ) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Add Client Error");
			alert.setHeaderText("Error: " + ex.getMessage());
			alert.setContentText("Cause: " + ex.getCause());
			alert.showAndWait();
		}

	}

	@FXML private void handleBtnCancel() {
		currentStage.close();
	}

}
