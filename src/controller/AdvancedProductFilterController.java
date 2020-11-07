package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Product;
import model.Product.Property;

public class AdvancedProductFilterController {

	@FXML private CheckBox cbAll, cbTablets, cbSyrup, cbPowder, cbTube, cbInjectable;
	@FXML private DatePicker dbFrom, dbTo;
	@FXML private TextField tfDecimalTo, tfDecimalFrom;
	@FXML private TitledPane tpProductType, tpDateTime, tpQuantityPrice;
	@FXML private ComboBox<String>  cbTimeFromHour, cbTimeFromMinute, cbTimeToHour, cbTimeToMinute;
	@FXML private ToggleButton tbTimeFromMeridiem, tbTimeToMeridiem;
	@FXML private Accordion accordion;
	@FXML private VBox checkBoxGroup;

	private Property selected;

	private ProductController productController;
	private Stage currentStage;

	public void setReferences(Stage stage, ProductController controller) {
		currentStage = stage;
		productController = controller;

		selected = controller.cbSearchBy.getSelectionModel().getSelectedItem();
		expandTitlePane();
	}


	@FXML private void initialize() {
		boolean[] cbValues = {false, false, false, false, false};

		try (FileReader fr = new FileReader("FilterProduct.txt");
				BufferedReader br = new BufferedReader(fr);){
			String line;
			int i = 0;
			while((line = br.readLine()) != null) {
				cbValues[i++] = Boolean.parseBoolean(line);
			}

		} catch (IOException ex) { /* DO NOTHING */ }

		int i = 0;
		for(Node node: checkBoxGroup.getChildren()){
			CheckBox cb = (CheckBox) node;
			cb.setSelected(cbValues[i++]);
			cb.selectedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					cbAll.setIndeterminate(true);
				}
			});
		}

		cbAll.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {

				for(Node node: checkBoxGroup.getChildren()){
					CheckBox cb = (CheckBox) node;
					cb.setSelected(newVal);
				}
			}
		});

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
		boolean[] filter = {cbTablets.isSelected(), cbSyrup.isSelected(), cbTube.isSelected(), cbPowder.isSelected(), cbInjectable.isSelected()};
		switch(selected) {
		case TYPE:
			StringBuilder sb = new StringBuilder();
			if(filter[0])
				sb.append(Product.Type.TABLETS.toString() + ",");
			if(filter[1])
				sb.append(Product.Type.SYRUP.toString() + ",");
			if(filter[2])
				sb.append(Product.Type.TUBE.toString() + ",");
			if(filter[3])
				sb.append(Product.Type.POWDER.toString() + ",");
			if(filter[4])
				sb.append(Product.Type.INJECTABLE.toString() + ",");

			String text = sb.toString();
			if(!text.isEmpty()) {
				if(text.charAt(text.length()-1) == ',');
				text = text.substring(0, text.length()-1);
			}
			if(text.isEmpty()) {
				productController.tfSearch.clear();
			} else {
				productController.tfSearch.setText(text);
				productController.tfSearch.requestFocus();
				productController.filterNavTable(text);
			}

			try (FileWriter fw = new FileWriter("FilterProduct.txt", false);
					BufferedWriter bw = new BufferedWriter(fw);){
				for(int i = 0; i < filter.length; i++) {
					bw.write(String.valueOf(filter[i]));
					bw.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;
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
			productController.filterNavTable(entryDateFrom, entryDateTo);
			break;
		case EXPIRY:
			Date dFrom = Date.valueOf(dbFrom.getValue());
			Date dTo = Date.valueOf(dbTo.getValue());
			productController.filterNavTable(dFrom, dTo);
			break;
		case QTY_CARTONS:
		case PRICE_CARTON:
		case PRICE_CARTON_CR:
		case PRICE_BOX:
		case PRICE_BOX_CR:
		case PRICE_BP:
		case PRICE_BP_CR:
		case PRICE_PILL:
		case PRICE_PILL_CR:
			String valFrom = tfDecimalFrom.getText();
			String valTo = tfDecimalTo.getText();
			productController.filterNavTable(valFrom, valTo);
			break;
		default:
			break;
		}
		currentStage.close();
	}

	private void expandTitlePane() {
		switch(selected) {
		case TYPE:
			cbAll.setDisable(false);
			cbTablets.setDisable(false);
			cbSyrup.setDisable(false);
			cbPowder.setDisable(false);
			cbTube.setDisable(false);
			cbInjectable.setDisable(false);
			accordion.setExpandedPane(tpProductType);
			break;
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
			break;
		case EXPIRY:
			dbFrom.setDisable(false);
			dbTo.setDisable(false);
			accordion.setExpandedPane(tpDateTime);
			break;
		default:
			tfDecimalFrom.setDisable(false);
			tfDecimalTo.setDisable(false);
			accordion.setExpandedPane(tpQuantityPrice);
			break;
		}
	}

	@FXML void handleTBFromMeridiem() {
		System.out.println("WORKING");
		tbTimeFromMeridiem.setText(tbTimeFromMeridiem.isSelected() ? "PM" : "AM");
	}

	@FXML void handleTBToMeridiem() {
		tbTimeToMeridiem.setText(tbTimeToMeridiem.isSelected() ? "PM" : "AM");
	}

}
