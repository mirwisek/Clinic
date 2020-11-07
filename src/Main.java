import java.sql.SQLException;

import database.ClientManager;
import database.DatabaseManager;
import database.InvoiceManager;
import database.ProductManager;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GUIWindowView.fxml"));
			VBox pane = loader.load();
			Scene scene = new Scene(pane);

			scene.getStylesheets().add(getClass().getResource("controller/application.css").toExternalForm());
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent arg0) {
					try {
						InvoiceManager.closeConnection();
						ProductManager.closeConnection();
						ClientManager.closeConnection();
						DatabaseManager.getInstance().close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					primaryStage.close();
				}
			});
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
