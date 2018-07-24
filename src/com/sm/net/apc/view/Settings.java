package com.sm.net.apc.view;

import com.sm.net.apc.Main;
import com.sm.net.simple.h2.SimpleH2Database;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Settings {

	@FXML
	private TextField textFieldExtension;
	@FXML
	private TextField textFieldMinutes;
	@FXML
	private Button buttonSave;
	@FXML
	private Label labelProducts;
	@FXML
	private Label labelTime;

	private SimpleH2Database database;
	private Stage stage;
	private Integer productSize;
	private Integer time;

	public static final int minPerProd = 1;

	public void initialize() {

	}

	public void init() {
		textFieldExtension.setText(Main.ext.getValue().get());
		textFieldMinutes.setText(Main.min.getValue().get());

		time = new Integer(productSize.intValue() * minPerProd);

		labelProducts.setText(productSize.toString() + " Products");
		labelTime.setText("At least " + time.toString() + " minutes");
	}

	public void buttonSaveOnClick() {

		if (checkFields()) {
			Main.updateSettings(database, textFieldExtension.getText(), textFieldMinutes.getText());
			stage.close();
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.getIcons().add(new Image(Main.ICON.toURI().toString()));
			alert.setTitle(Main.appName + " " + Main.version);
			alert.setHeaderText("Check all fields");
			alert.setContentText(null);

			alert.show();
		}
	}

	private boolean checkFields() {

		boolean status = true;

		String ext = textFieldExtension.getText();
		String min = textFieldMinutes.getText();

		if (ext.isEmpty())
			status = false;

		if (min.isEmpty())
			status = false;
		else
			try {
				Integer minInt = new Integer(min);
				if (minInt.compareTo(time) == -1)
					status = false;
			} catch (Exception e) {
				status = false;
			}

		return status;
	}

	public SimpleH2Database getDatabase() {
		return database;
	}

	public void setDatabase(SimpleH2Database database) {
		this.database = database;
	}

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public Integer getProductSize() {
		return productSize;
	}

	public void setProductSize(Integer productSize) {
		this.productSize = productSize;
	}

}
