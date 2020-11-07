package controller;

import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import database.InvoiceManager;
import javafx.fxml.FXML;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import model.Line;
import model.PrintableInvoice;

public class PrintPreviewController {

	private Stage currentStage;
//	private InvoiceController controller;
	private int invoiceID;

	@FXML TableView<Line> table;
	@FXML TableColumn<Line, String> colProductName, colProductID, colPacking;
	@FXML TableColumn<Line, Integer> colLineNumber, colQty;
	@FXML TableColumn<Line, Double> colPrice, colDiscount, colNetTotal;
	@FXML TableColumn<Line, Date> colExpiry;
	@FXML ListView<String> lvInvoice, lvClient;
	@FXML Label lblTotal;
	@FXML HBox hBoxButtons;
	@FXML AnchorPane rootPane;

	public void setReferences(Stage stage, InvoiceController controller) {
		currentStage = stage;
//		this.controller = controller;
		invoiceID = controller.tableInvoice.getSelectionModel().getSelectedItem().getInvoiceID();

		inflate();

	}

	@FXML
	private void initialize() {

		colProductID.setCellValueFactory(new PropertyValueFactory<Line, String>("productID"));
		colProductName.setCellValueFactory(new PropertyValueFactory<Line, String>("name"));
		colPacking.setCellValueFactory(new PropertyValueFactory<Line, String>("packing"));
		colLineNumber.setCellValueFactory(new PropertyValueFactory<Line, Integer>("lineNumber"));
		colQty.setCellValueFactory(new PropertyValueFactory<Line, Integer>("quantity"));
		colPrice.setCellValueFactory(new PropertyValueFactory<Line, Double>("price"));
		colDiscount.setCellValueFactory(new PropertyValueFactory<Line, Double>("discount"));
		colNetTotal.setCellValueFactory(new PropertyValueFactory<Line, Double>("netTotal"));
		colExpiry.setCellValueFactory(new PropertyValueFactory<Line, Date>("dExpiry"));

		SimpleDateFormat expFormat = new SimpleDateFormat("dd/MM/yyyy");

		colExpiry.setCellFactory(column -> {
			return new TableCell<Line, Date>() {

				@Override
				protected void updateItem(Date item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
					} else {
						setText(expFormat.format(item));
					}

					setGraphic(null);
					setStyle("");
				}

			};
		});

	}

	private void inflate() {
		try {
			PrintableInvoice in = InvoiceManager.getPrintableInvoice(invoiceID);
			if (in != null) {
				lvInvoice.getItems().addAll(in.getInvoiceID(), in.getEntry());
				lvClient.getItems().addAll(in.getClientID(), in.getClientName(), in.getClientPhone());
				table.getItems().addAll(in.getLines());
				lblTotal.setText(in.getTotalAmount());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleBtnCancel() {
		currentStage.close();
	}

	@FXML
	private void handleBtnPrint() {

			hBoxButtons.setVisible(false);
			try {
				printNode(rootPane);
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
					| InvocationTargetException ex) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Print Error!");
				alert.setHeaderText("Error: " + ex.getMessage());
				alert.setContentText("Cause: " + ex.getCause());
				alert.showAndWait();
			}
	}

	private void printNode(Node node)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Printer printer = Printer.getDefaultPrinter();
		PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT,
				Printer.MarginType.HARDWARE_MINIMUM);
//		PrinterAttributes attr = printer.getPrinterAttributes();
		PrinterJob job = PrinterJob.createPrinterJob();
		double scaleX = pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth();
		double scaleY = pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight();
		Scale scale = new Scale(scaleX, scaleY);
		node.getTransforms().add(scale);

		if (job != null && job.showPrintDialog(node.getScene().getWindow())) {
			boolean success = job.printPage(pageLayout, node);
			if (success) {
//				NotificationBar nb = new Noti
				job.endJob();
				currentStage.close();
			}
		} else {
			// If cancel show the buttons again
			hBoxButtons.setVisible(true);
		}
		node.getTransforms().remove(scale);
	}
}
