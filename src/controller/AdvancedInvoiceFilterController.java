package controller;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Invoice.Property;

public class AdvancedInvoiceFilterController {

	@FXML private CheckBox cbAll, cbTablets, cbSyrup, cbPowder, cbTube, cbInjectable;
	@FXML private DatePicker dbFrom, dbTo;
	@FXML private TextField tfDecimalTo, tfDecimalFrom;
	@FXML private TitledPane tpProductType, tpDateTime, tpQuantityPrice;
	@FXML private ComboBox<String>  cbTimeFromHour, cbTimeFromMinute, cbTimeToHour, cbTimeToMinute;
	@FXML private ToggleButton tbTimeFromMeridiem, tbTimeToMeridiem;
	@FXML private Accordion accordion;
	@FXML private VBox checkBoxGroup;

	private Property selected;

	private InvoiceController invoiceController;
	private Stage currentStage;

	public void setReferences(Stage stage, InvoiceController controller) {
		currentStage = stage;
		invoiceController = controller;
		selected = controller.cbSearchBy.getSelectionModel().getSelectedItem();
		expandTitlePane();
	}


	@FXML private void initialize() {

		ArrayList<String> hours = new ArrayList<>();
		for(int j = 1; j <= 12; j++) {
			hours.add(String.format("%02d", j));
		}
		cbTimeFromHour.getItems().addAll(hours);
		cbTimeToHour.getItems().addAll(hours);

		ArrayList<String> minutes = new ArrayList<>();
		for(int j = 0; j <= 59; j++) {
			minutes.add(String.format("%02d", j));
		}
		cbTimeFromMinute.getItems().addAll(minutes);
		cbTimeToMinute.getItems().addAll(minutes);

		cbTimeFromHour.getSelectionModel().select(0);
		cbTimeToHour.getSelectionModel().select(0);
		cbTimeFromMinute.getSelectionModel().select(0);
		cbTimeToMinute.getSelectionModel().select(0);

	}

	@FXML void handleBtnCancel() {
		currentStage.close();
	}

	@FXML void handleBtnFilter() {
		switch(selected) {
		case ENTRY:
			Date entryDateFrom = Date.valueOf(dbFrom.getValue());
			Date entryDateTo = Date.valueOf(dbTo.getValue());

			String timeFromHour = cbTimeFromHour.getSelectionModel().getSelectedItem();
			String timeFromMinute = cbTimeFromMinute.getSelectionModel().getSelectedItem();
			String timeFromMeridiem = tbTimeFromMeridiem.isSelected() ? "PM" : "AM";
			String timeToHour = cbTimeToHour.getSelectionModel().getSelectedItem();
			String timeToMinute = cbTimeToMinute.getSelectionModel().getSelectedItem();
			String timeToMeridiem = tbTimeToMeridiem.isSelected() ? "PM" : "AM";

			// For converting initial dates to String format
			DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
			// For the actual output format
			// Watch out for "hh" (12-Hours) and "HH" (24-Hours)
			DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
			String sDateFrom = new String(inputFormat.format(entryDateFrom) + " " + timeFromHour + ":" + timeFromMinute +
					" " + timeFromMeridiem);
			String sDateTo = new String(inputFormat.format(entryDateTo) + " " + timeToHour + ":" + timeToMinute +
					" " + timeToMeridiem);
			try {
				entryDateFrom = new Date(outputFormat.parse(sDateFrom).getTime());
				entryDateTo = new Date(outputFormat.parse(sDateTo).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			invoiceController.filterNavTable(entryDateFrom, entryDateTo);
			break;
		case INVOICE_ID:
		case TOTAL_AMOUNT:
			String valFrom = tfDecimalFrom.getText();
			String valTo = tfDecimalTo.getText();
			invoiceController.filterNavTable(valFrom, valTo);
			break;
		default:
			break;
		}
		currentStage.close();
	}

	private void expandTitlePane() {
		switch(selected) {
		case ENTRY:
			dbFrom.setDisable(false);
			dbTo.setDisable(false);
			cbTimeFromHour.setDisable(false);
			cbTimeFromMinute.setDisable(false);
			tbTimeFromMeridiem.setDisable(false);
			cbTimeToHour.setDisable(false);
			cbTimeToMinute.setDisable(false);
			tbTimeToMeridiem.setDisable(false);
			accordion.setExpandedPane(tpDateTime);
			System.out.println("ENTRY");
			break;
		default:
			tfDecimalFrom.setDisable(false);
			tfDecimalTo.setDisable(false);
			accordion.setExpandedPane(tpQuantityPrice);
		}
	}

	@FXML void handleTBFromMeridiem() {
		tbTimeFromMeridiem.setText(tbTimeFromMeridiem.isSelected() ? "PM" : "AM");
	}

	@FXML void handleTBToMeridiem() {
		tbTimeToMeridiem.setText(tbTimeToMeridiem.isSelected() ? "PM" : "AM");
	}

}
