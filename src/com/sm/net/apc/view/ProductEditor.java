package com.sm.net.apc.view;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;

import com.sm.net.amazon.util.Html;
import com.sm.net.apc.Main;
import com.sm.net.apc.model.AmazonList;
import com.sm.net.apc.model.AmazonPrice;
import com.sm.net.apc.model.AmazonProduct;
import com.sm.net.simple.h2.OperationBuilder;
import com.sm.net.simple.h2.SimpleH2Database;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class ProductEditor {

	@FXML
	private ImageView imageView;
	@FXML
	private TextField textFieldID;
	@FXML
	private TextField textFieldName;
	@FXML
	private ComboBox<AmazonList> comboBoxList;
	@FXML
	private TextField textFieldAlert;
	@FXML
	private TextField textFieldAverage;
	@FXML
	private TextField textFieldLowest;
	@FXML
	private TextField textFieldHigher;
	@FXML
	private Button buttonOpenAmazon;
	@FXML
	private LineChart<String, Number> lineChart;
	@FXML
	private CategoryAxis xAxis;
	@FXML
	private NumberAxis yAxis;

	private SimpleH2Database database;
	private AmazonProduct product;
	private String bufferName;
	private String bufferAlert;
	private MainView mainView;

	@FXML
	private void initialize() {

		this.textFieldID.setEditable(false);
		this.textFieldID.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.textFieldName.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.comboBoxList.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.textFieldAlert.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.textFieldAverage.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.textFieldLowest.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.textFieldHigher.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");

		this.textFieldAverage.setEditable(false);
		this.textFieldLowest.setEditable(false);
		this.textFieldHigher.setEditable(false);

		this.textFieldName.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					bufferName = textFieldName.getText();
				} else {
					String newName = textFieldName.getText();
					if (!newName.isEmpty() && newName.compareTo(bufferName) != 0) {
						OperationBuilder ob = new OperationBuilder("apc", "product");
						ob.setColumnValue("product_name", newName);
						ob.setConditionEquals("id", product.getId().get());

						database.runOperation(ob.buildUpdate());
						mainView.loadListProduct();
					}
				}
			}
		});

		this.textFieldAlert.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					bufferAlert = textFieldAlert.getText();
				} else {
					String newName = textFieldAlert.getText();
					if (!newName.isEmpty() && newName.compareTo(bufferAlert) != 0) {

						try {
							BigDecimal priceAlert = new BigDecimal(newName.replaceAll(",", "."));

							OperationBuilder ob = new OperationBuilder("apc", "product");
							ob.setColumnValue("price_alert", priceAlert);
							ob.setConditionEquals("id", product.getId().get());

							database.runOperation(ob.buildUpdate());
							mainView.loadListProduct();
						} catch (Exception e) {
							textFieldAlert.setText(bufferAlert);
						}
					} else {
						textFieldAlert.setText(bufferAlert);
					}
				}
			}
		});

		this.comboBoxList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue.intValue() > -1) {

					AmazonList list = comboBoxList.getSelectionModel().getSelectedItem();

					OperationBuilder ob = new OperationBuilder("apc", "product");
					ob.setColumnValue("id_list", list.getId().get());
					ob.setConditionEquals("id", product.getId().get());

					database.runOperation(ob.buildUpdate());
					mainView.loadListProduct();
				}
			}
		});
	}

	public void init() {

		ObservableList<AmazonList> list = Main.getListList(this.database);
		list.add(0, new AmazonList(-1, "Wishlist"));
		this.comboBoxList.setItems(list);

		loadProduct();
		loadChart();

	}

	private void loadChart() {

		lineChart.getData().clear();

		xAxis.setLabel("Date");
		yAxis.setLabel("Price");

		Series<String, Number> series = new XYChart.Series<>();
		series.setName(product.getCode().get());

		ObservableList<AmazonPrice> listPrice = Main.getListPrice(database, product.getId().get());

		BigDecimal summe = BigDecimal.ZERO;
		BigDecimal low = BigDecimal.ZERO;
		BigDecimal high = BigDecimal.ZERO;

		for (AmazonPrice amazonPrice : listPrice) {

			String date = amazonPrice.getCreationDate().get().toString();
			BigDecimal price = amazonPrice.getPrice().get();

			summe = summe.add(price);

			if (price.compareTo(low) == -1)
				low = price;

			if (price.compareTo(high) == 1)
				high = price;

			series.getData().add(new XYChart.Data<String, Number>(date, price));
		}

		double avr = summe.doubleValue() / listPrice.size();
		BigDecimal average = BigDecimal.valueOf(avr).setScale(2, RoundingMode.HALF_UP);

		textFieldAverage.setText(average.toString());
		textFieldLowest.setText(low.toString());
		textFieldHigher.setText(high.toString());

		lineChart.getData().add(series);
	}

	private void loadProduct() {
		imageView.setImage(this.product.getImageUrl().get().getImage());
		textFieldID.setText(this.product.getCode().get());
		textFieldName.setText(this.product.getProductName().get());
		textFieldAlert.setText(this.product.getPriceAlert().get().toString());
		setComboBox();
	}

	private void setComboBox() {

		int idList = this.product.getIdList().get();
		for (AmazonList amazonList : this.comboBoxList.getItems()) {
			if (amazonList.getId().intValue() == idList) {
				this.comboBoxList.getSelectionModel().select(amazonList);
				break;
			}
		}
	}

	public void buttonOpenAmazonOnClick() {

		String code = product.getCode().get();
		String productUrl = Html.getAmazonProductSimpleUrl(Main.ext.getValue().get(), code);

		Desktop desktop = Desktop.getDesktop();
		if (desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI(productUrl));
			} catch (IOException | URISyntaxException exc) {
				// ...
			}
		} else {
			// ...
		}
	}

	public SimpleH2Database getDatabase() {
		return database;
	}

	public void setDatabase(SimpleH2Database database) {
		this.database = database;
	}

	public AmazonProduct getProduct() {
		return product;
	}

	public void setProduct(AmazonProduct product) {
		this.product = product;
	}

	public MainView getMainView() {
		return mainView;
	}

	public void setMainView(MainView mainView) {
		this.mainView = mainView;
	}

}
