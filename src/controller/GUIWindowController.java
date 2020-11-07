package controller;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.controlsfx.control.Notifications;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class GUIWindowController {

	@FXML Tab tabClient, tabProduct, tabInvoice;

	@FXML
	private void initialize() {
		try {
			FXMLLoader clientLoader = new FXMLLoader(getClass().getResource("/view/ClientView.fxml"));
			FXMLLoader productLoader = new FXMLLoader(getClass().getResource("/view/ProductView.fxml"));
			FXMLLoader invoiceLoader = new FXMLLoader(getClass().getResource("/view/InvoiceView.fxml"));
			BorderPane clientPane = clientLoader.load();
			BorderPane productPane = productLoader.load();
			BorderPane invoicePane = invoiceLoader.load();

			tabClient.setContent(clientPane);
			tabProduct.setContent(productPane);
			tabInvoice.setContent(invoicePane);

			ClientController clientController = clientLoader.getController();
			clientController.setReferences(this);

			ProductController productController = productLoader.getController();
			productController.setReferences(this);

			InvoiceController invoiceController = invoiceLoader.getController();
			invoiceController.setReferences(this, productController);

		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void showWarningProduct(boolean shown) {
		if(shown) {
			if(tabProduct.getGraphic() != null)
				return;
			try {
				Image img = new Image(new FileInputStream(new File( System.getProperty("user.dir") + "/icons/warning.png")));
				ImageView iconWarning = new ImageView(img);
				iconWarning.setFitHeight(24);
				iconWarning.setFitWidth(24);
				if(iconWarning != null) {
					tabProduct.setGraphic(iconWarning);

					Notifications notification = Notifications.create().title("Product quantitiy warning!")
							.text("Some of the products quantity is in critical shape.").position(Pos.BOTTOM_RIGHT)
							.hideAfter(javafx.util.Duration.seconds(10)).darkStyle();

					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							notification.showWarning();
							Toolkit.getDefaultToolkit().beep();
						}
					});
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			tabProduct.setGraphic(null);
			Notifications notification = Notifications.create().title("Product quantities updated!")
					.text("All of the products are in good shape now").position(Pos.BOTTOM_RIGHT)
					.hideAfter(javafx.util.Duration.seconds(10)).darkStyle();

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					notification.showInformation();
					Toolkit.getDefaultToolkit().beep();
				}
			});
		}
	}

}
