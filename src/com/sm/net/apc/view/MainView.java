package com.sm.net.apc.view;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sm.net.amazon.util.Html;
import com.sm.net.apc.Main;
import com.sm.net.apc.enumeration.PriceStatus;
import com.sm.net.apc.interfaces.TaskCheckPrice;
import com.sm.net.apc.model.AmazonList;
import com.sm.net.apc.model.AmazonPrice;
import com.sm.net.apc.model.AmazonProduct;
import com.sm.net.apc.task.CheckPrice;
import com.sm.net.simple.h2.OperationBuilder;
import com.sm.net.simple.h2.SimpleH2Database;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

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
	private TableColumn<AmazonPrice, Date> tableColumnPriceDate;

	@FXML
	private Label labelCheck;
	@FXML
	private Button buttonCheck;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Button buttonAddList;
	@FXML
	private Button buttonDeleteProduct;
	@FXML
	private Button buttonDeleteList;
	@FXML
	private Button buttonSettings;

	@FXML
	private ListView<AmazonList> listView;

	private SimpleH2Database database;
	private ScheduledExecutorService executorService;
	private boolean status;
	private int index;
	private CheckPrice checkPriceService;
	private Stage mainViewStage;

	@FXML
	private void initialize() {

		this.executorService = null;
		this.status = false;
		this.index = 0;
		this.checkPriceService = null;

		setImageButtonPlus();
		setImageButtonStop();
		setLabelCheck(1);

		tableColumnImage.setCellValueFactory(cellData -> cellData.getValue().getImageUrl());
		tableColumnName.setCellValueFactory(cellData -> cellData.getValue().getProductName());

		tableColumnPriceImage.setCellValueFactory(cellData -> cellData.getValue().getImageViewStatus());
		tableColumnPriceValue.setCellValueFactory(cellData -> cellData.getValue().getPrice());
		tableColumnPricePercentage.setCellValueFactory(cellData -> cellData.getValue().getPercentageString());
		tableColumnPriceDate.setCellValueFactory(cellData -> cellData.getValue().getCreationDate());

		tableColumnImage.setStyle("-fx-alignment: center; -fx-font: 15px System;");
		tableColumnName.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");

		tableColumnPriceImage.setStyle("-fx-alignment: center; -fx-font: 15px System;");
		tableColumnPriceValue.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		tableColumnPricePercentage.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		tableColumnPriceDate.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");

		progressBar.setStyle("-fx-font: 14px System;");

		listView.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		listView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue.intValue() > -1) {
					loadListProduct();
				}
			}
		});

		tableViewProducts.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue.intValue() > -1)
					loadPrice();
			}
		});

		tableViewProducts.setRowFactory(tv -> {
			TableRow<AmazonProduct> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty()) {
					AmazonProduct product = row.getItem();
					createEditor(product);
				}
			});
			return row;
		});
	}

	public void init() {
		loadListList();
		loadListProduct();

		checkPriceService = new CheckPrice(database, this);
		runService();

		// this.executorService = Executors.newScheduledThreadPool(1);
		// executorService.scheduleAtFixedRate(checkPriceService, 1, time,
		// TimeUnit.MINUTES);
	}

	public void buttonSettingsOnClick() {

		try {

			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(Main.class.getResource("view/Settings.fxml"));

			Scene scene = new Scene((AnchorPane) fxmlLoader.load());
			Stage stage = new Stage();
			stage.setScene(scene);

			stage.setTitle("Amazon PriceCheck 1.0");
			stage.getIcons().add(new Image(Main.ICON.toURI().toString()));
			stage.initOwner(mainViewStage);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setResizable(false);

			Settings controller = (Settings) fxmlLoader.getController();
			controller.setStage(stage);
			controller.setDatabase(database);
			controller.setProductSize(tableViewProducts.getItems().size());
			controller.init();

			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void buttonDeleteListOnClick() {

		AmazonList list = listView.getSelectionModel().getSelectedItem();
		if (list != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete list");
			alert.setHeaderText("Do you really want to delete it?");
			alert.setContentText(list.getName().get());

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK)
				deleteList(list);
		}

	}

	private void deleteList(AmazonList list) {

		database.runOperation("DELETE FROM apc.list WHERE id=" + list.getId().intValue());
		database.runOperation("UPDATE apc.product SET id_list=-1 WHERE id_list=" + list.getId().intValue());
		loadListList();
		loadListProduct();
	}

	private void createEditor(AmazonProduct product) {

		try {

			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(Main.class.getResource("view/ProductEditor.fxml"));

			Scene scene = new Scene((AnchorPane) fxmlLoader.load());
			Stage stage = new Stage();
			stage.setScene(scene);

			stage.setTitle("Amazon PriceCheck 1.0");
			stage.getIcons().add(new Image(Main.ICON.toURI().toString()));
			stage.initOwner(mainViewStage);
			stage.initModality(Modality.WINDOW_MODAL);

			ProductEditor controller = (ProductEditor) fxmlLoader.getController();
			controller.setDatabase(database);
			controller.setProduct(product);
			controller.setMainView(this);
			controller.init();

			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void buttonDeleteProductOnClick() {

		AmazonProduct product = tableViewProducts.getSelectionModel().getSelectedItem();
		if (product != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete product");
			alert.setHeaderText("Do you really want to delete it?");
			alert.setContentText(product.getProductName().get());

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK)
				deleteProduct(product);
		}
	}

	private void deleteProduct(AmazonProduct product) {

		database.runOperation("DELETE FROM apc.product WHERE id=" + product.getId().intValue());
		database.runOperation("DELETE FROM apc.price_check WHERE id_product=" + product.getId().intValue());
		loadListProduct();
	}

	public void buttonNewOnClick() {

		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Amazon PriceCheck 1.0");
		dialog.setHeaderText("List-Name:");

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Main.ICON.toURI().toString()));

		ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

		HBox hbox = new HBox();

		TextField listName = new TextField();
		listName.setStyle("-fx-font: normal 15 System;");

		hbox.getChildren().add(listName);

		dialog.getDialogPane().setContent(hbox);
		dialog.setResultConverter(new Callback<ButtonType, String>() {

			@Override
			public String call(ButtonType param) {
				if (param.getButtonData().compareTo(ButtonData.OK_DONE) == 0) {
					return listName.getText();
				} else {
					return null;
				}
			}
		});

		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()) {
			String list = result.get();

			addNewList(list);
		}
	}

	private void addNewList(String list) {

		OperationBuilder op = new OperationBuilder("apc", "list");
		op.setColumnValue("name", list);
		database.runOperation(op.buildInsert());

		loadListList();
		loadListProduct();
	}

	private void loadListList() {

		ObservableList<AmazonList> list = Main.getListList(this.database);
		list.add(0, new AmazonList(-1, "Wishlist"));

		this.listView.setItems(list);
		this.listView.getSelectionModel().selectFirst();
	}

	private void setLabelCheck(Integer min) {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
		this.labelCheck.setText("Next verification at " + dtf.format(getNextCheck(min)));
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
			shutdownService();
		else
			runService();
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

			String price = "";

			price = com.sm.net.util.Html.getSubsourceCode(sourceCode, Html.salePriceStart,
					com.sm.net.util.Html.tagSpanEnd);

			if (price.isEmpty())
				price = com.sm.net.util.Html.getSubsourceCode(sourceCode, Html.ourPriceStart,
						com.sm.net.util.Html.tagSpanEnd);

			List<Integer> indexes = addProduct(productCode, com.sm.net.util.Html.encodingForeignChars(productTitle),
					imageUrl);
			addPrice(indexes, price);
		}
	}

	private void addPrice(List<Integer> indexes, String price) {

		if (!indexes.isEmpty()) {
			if (indexes.size() == 1) {
				OperationBuilder op = new OperationBuilder("apc", "price_check");
				op.setColumnValue("creation_date", Instant.now());
				op.setColumnValue("price", new BigDecimal(price.replace(",", ".")));
				op.setColumnValue("price_old", new BigDecimal(price.replace(",", ".")));
				op.setColumnValue("id_product", indexes.get(0));
				op.setColumnValue("status", PriceStatus.DETECTED.getId());

				database.runOperation(op.buildInsert());
			}
		}
	}

	private List<Integer> addProduct(String productCode, String productTitle, String imageUrl) {

		if (!productCode.isEmpty() && !productTitle.isEmpty()) {

			productTitle = productTitle.replace("'", "");

			AmazonList list = listView.getSelectionModel().getSelectedItem();

			OperationBuilder op = new OperationBuilder("apc", "product");
			op.setColumnValue("code", productCode);
			op.setColumnValue("product_name", productTitle);
			op.setColumnValue("id_list", list.getId().get());
			op.setColumnValue("price_alert", BigDecimal.ZERO);

			if (imageUrl.isEmpty())
				op.setColumnValue("image_url", "");
			else
				op.setColumnValue("image_url", imageUrl);

			List<Integer> indexes = database.runInsert(op.buildInsert());

			textFieldLink.setText("");
			loadListProduct();

			return indexes;
		}

		return null;
	}

	public void loadListProduct() {
		AmazonList list = this.listView.getSelectionModel().getSelectedItem();
		if (list != null) {
			tableViewProducts.setItems(Main.getListProduct(this.database, list.getId().get()));
			tableViewPrice.setItems(null);
			updateTime(Main.getListProduct(database, -2).size());
		}
	}

	private void updateTime(int size) {

		Integer atLeastTime = new Integer(size * 2);
		String minString = Main.min.getValue().get();
		if (atLeastTime.compareTo(new Integer(minString)) == 1) {
			String ext = Main.ext.getValue().get();
			Main.updateSettings(database, ext, atLeastTime.toString());
			reloadService();
		}
	}

	private void reloadService() {
		shutdownService();
		runService();
	}

	private void runService() {
		this.executorService = Executors.newScheduledThreadPool(1);
		executorService.scheduleAtFixedRate(checkPriceService, 1, new Integer(Main.min.getValue().get()).intValue(),
				TimeUnit.MINUTES);
		this.status = true;
		setImageButtonStop();
		setLabelCheck(1);
	}

	private void shutdownService() {
		if (executorService != null) {
			executorService.shutdown();
			this.status = false;
			setImageButtonStart();
			this.labelCheck.setText("Stopped...");
		}
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
		this.index = 0;
		this.progressBar.setProgress(0);
	}

	@Override
	public void stopCheck() {
		setLabelCheck(new Integer(Main.min.getValue().get()));
		this.progressBar.setProgress(0);
	}

	@Override
	public void showAlert(AmazonProduct amazonProduct, BigDecimal newPrice) {

		try {

			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(Main.class.getResource("view/Alert.fxml"));

			Scene scene = new Scene((AnchorPane) fxmlLoader.load());
			Stage stage = new Stage();
			stage.setScene(scene);

			stage.setTitle("Price alarm");
			stage.getIcons().add(new Image(Main.ICON.toURI().toString()));

			com.sm.net.apc.view.Alert controller = (com.sm.net.apc.view.Alert) fxmlLoader.getController();
			controller.setDatabase(database);
			controller.setProduct(amazonProduct);
			controller.setNewPrice(newPrice);
			controller.init();

			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean getStatus() {
		return status;
	}

	@Override
	public void setProductName(AmazonProduct amazonProduct, int size) {
		this.index += 1;
		this.labelCheck.setText(this.index + "/" + size + " " + amazonProduct.getProductName().get());
		this.progressBar.setProgress(getProgress(size));
	}

	private double getProgress(int size) {
		return BigDecimal.valueOf(this.index).divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP).doubleValue();
	}

	public Stage getMainViewStage() {
		return mainViewStage;
	}

	public void setMainViewStage(Stage mainViewStage) {
		this.mainViewStage = mainViewStage;
	}

}
