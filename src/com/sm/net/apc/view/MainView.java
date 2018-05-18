package com.sm.net.apc.view;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sm.net.amazon.util.Html;
import com.sm.net.apc.Main;
import com.sm.net.apc.interfaces.TaskCheckPrice;
import com.sm.net.apc.model.AmazonPrice;
import com.sm.net.apc.model.AmazonProduct;
import com.sm.net.apc.task.CheckPrice;
import com.sm.net.simple.h2.OperationBuilder;
import com.sm.net.simple.h2.SimpleH2Database;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainView implements TaskCheckPrice {

	@FXML
	private TextField textFieldLink;
	@FXML
	private Button buttonAdd;

	@FXML
	private TableView<AmazonProduct> tableViewProducts;
	@FXML
	private TableColumn<AmazonProduct, ImageView> tableColumnImage;
	@FXML
	private TableColumn<AmazonProduct, String> tableColumnName;

	@FXML
	private TableView<AmazonPrice> tableViewPrice;
	@FXML
	private TableColumn<AmazonPrice, ImageView> tableColumnPriceImage;
	@FXML
	private TableColumn<AmazonPrice, BigDecimal> tableColumnPriceValue;
	@FXML
	private TableColumn<AmazonPrice, String> tableColumnPricePercentage;

	@FXML
	private Label labelCheck;
	@FXML
	private Button buttonCheck;

	private SimpleH2Database database;
	private ScheduledExecutorService executorService;
	private boolean status;
	private CheckPrice checkPriceTask;

	@FXML
	private void initialize() {

		this.executorService = null;
		this.status = false;

		setImageButtonPlus();
		setImageButtonStart();
		setLabelCheck(1);

		tableColumnImage.setCellValueFactory(cellData -> cellData.getValue().getImageUrl());
		tableColumnName.setCellValueFactory(cellData -> cellData.getValue().getProductName());

		tableColumnPriceImage.setCellValueFactory(cellData -> cellData.getValue().getImageViewStatus());
		tableColumnPriceValue.setCellValueFactory(cellData -> cellData.getValue().getPrice());
		tableColumnPricePercentage.setCellValueFactory(cellData -> cellData.getValue().getPercentageString());

		tableColumnImage.setStyle("-fx-alignment: center; -fx-font: 15px System;");
		tableColumnName.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");

		tableColumnPriceImage.setStyle("-fx-alignment: center; -fx-font: 15px System;");
		tableColumnPriceValue.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		tableColumnPricePercentage.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");

		tableViewProducts.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue.intValue() > -1)
					loadPrice();
			}
		});

	}

	private void setLabelCheck(Integer min) {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
		this.labelCheck.setText(dtf.format(getNextCheck(min)));
	}

	private void setLabelCheckInProgress() {
		this.labelCheck.setText("Check in progress...");
	}

	private Instant getNextCheck(Integer min) {
		Instant now = Instant.now();
		now = now.plus(min, ChronoUnit.MINUTES);

		return now;
	}

	private void setImageButtonStart() {

		String path = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "images"
				+ File.separatorChar + "start.png";

		ImageView imageView = new ImageView(new Image(new File(path).toURI().toString()));

		imageView.setFitHeight(50);
		imageView.setFitWidth(50);

		this.buttonCheck.setGraphic(imageView);
	}

	private void setImageButtonStop() {

		String path = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "images"
				+ File.separatorChar + "stop.png";

		ImageView imageView = new ImageView(new Image(new File(path).toURI().toString()));

		imageView.setFitHeight(50);
		imageView.setFitWidth(50);

		this.buttonCheck.setGraphic(imageView);
	}

	private void setImageButtonPlus() {

		String path = System.getProperty("user.dir") + File.separatorChar + "resources" + File.separatorChar + "images"
				+ File.separatorChar + "add.png";

		ImageView imageView = new ImageView(new Image(new File(path).toURI().toString()));

		imageView.setFitHeight(50);
		imageView.setFitWidth(50);

		this.buttonAdd.setGraphic(imageView);
	}

	protected void loadPrice() {
		AmazonProduct item = tableViewProducts.getSelectionModel().getSelectedItem();
		if (item != null)
			tableViewPrice.setItems(Main.getListPrice(this.database, item.getId().get()));
		else
			tableViewPrice.setItems(null);
	}

	public void init() {
		loadListProduct();

		checkPriceTask = new CheckPrice(database, this);

		this.executorService = Executors.newScheduledThreadPool(1);
		executorService.scheduleAtFixedRate(checkPriceTask, 1, 60, TimeUnit.MINUTES);
	}

	public void buttonAddOnClick() {

		String link = textFieldLink.getText();
		if (!link.isEmpty()) {
			String productCode = Html.getProductCodeFromUrl(link);
			if (!productCode.isEmpty())
				checkProduct(productCode);
		}
	}

	public void buttonCheckOnClick() {

		if (status)
			status = false;
		else
			startManualCheck();
	}

	private void startManualCheck() {
		executorService.execute(new Thread(checkPriceTask));
	}

	private void checkProduct(String productCode) {

		String productUrl = Html.getAmazonProductSimpleUrl("de", productCode);
		String sourceCode = com.sm.net.util.Html.getSourceCode(productUrl);

		if (!sourceCode.isEmpty()) {

			String productTitle = "";
			String imageUrl = "";

			productTitle = com.sm.net.util.Html.getSubsourceCode(sourceCode, Html.productTitleStart,
					com.sm.net.util.Html.tagSpanEnd);
			String imageUrlTag = com.sm.net.util.Html.getSubsourceCode(sourceCode, Html.imageTagWrapperStart,
					com.sm.net.util.Html.tagDivEnd);

			imageUrl = com.sm.net.util.Html.getSubsourceCode(imageUrlTag, com.sm.net.util.Html.attrSrcStart,
					com.sm.net.util.Html.backslash);

			addProduct(productCode, com.sm.net.util.Html.encodingForeignChars(productTitle), imageUrl);
		}
	}

	private void addProduct(String productCode, String productTitle, String imageUrl) {

		if (!productCode.isEmpty() && !productTitle.isEmpty()) {

			productTitle = productTitle.replace("'", "");

			OperationBuilder op = new OperationBuilder("apc", "product");
			op.setColumnValue("code", productCode);
			op.setColumnValue("product_name", productTitle);

			if (imageUrl.isEmpty())
				op.setColumnValue("image_url", "");
			else
				op.setColumnValue("image_url", imageUrl);

			database.runOperation(op.buildInsert());

			textFieldLink.setText("");
			loadListProduct();
		}
	}

	private void loadListProduct() {
		tableViewProducts.setItems(Main.getListProduct(this.database));
	}

	public SimpleH2Database getDatabase() {
		return database;
	}

	public void setDatabase(SimpleH2Database database) {
		this.database = database;
	}

	public ScheduledExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public void startCheck() {
		status = true;
		setLabelCheckInProgress();
		setImageButtonStop();
	}

	@Override
	public void stopCheck() {
		status = false;
		setLabelCheck(60);
		setImageButtonStart();
	}

	@Override
	public boolean getStatus() {
		return status;
	}

	public CheckPrice getCheckPriceTask() {
		return checkPriceTask;
	}

	public void setCheckPriceTask(CheckPrice checkPriceTask) {
		this.checkPriceTask = checkPriceTask;
	}
}
