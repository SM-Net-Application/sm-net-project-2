package com.sm.net.apc.view;

import com.sm.net.amazon.util.Html;
import com.sm.net.apc.Main;
import com.sm.net.apc.model.AmazonPrice;
import com.sm.net.apc.model.AmazonProduct;
import com.sm.net.simple.h2.OperationBuilder;
import com.sm.net.simple.h2.SimpleH2Database;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainView {

	@FXML
	private ListView<AmazonProduct> listProducts;
	@FXML
	private TextField textFieldLink;
	@FXML
	private Button buttonAdd;
	@FXML
	private ListView<AmazonPrice> listPriceCheck;
	@FXML
	private ImageView imageViewFoto;

	@FXML
	private TableView<AmazonProduct> tableViewProducts;
	@FXML
	private TableColumn<AmazonProduct, ImageView> tableColumnImage;
	@FXML
	private TableColumn<AmazonProduct, String> tableColumnName;

	private SimpleH2Database database;

	@FXML
	private void initialize() {

		tableColumnImage.setCellValueFactory(cellData -> cellData.getValue().getImageUrl());
		tableColumnName.setCellValueFactory(cellData -> cellData.getValue().getProductName());

		tableColumnImage.setStyle("-fx-alignment: center; -fx-font: 15px System;");
		tableColumnName.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");

		listProducts.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue.intValue() > -1) {
					loadPreview();
					loadPrice();
				}
			}
		});

	}

	protected void loadPrice() {
		AmazonProduct item = listProducts.getSelectionModel().getSelectedItem();
		if (item != null) {
			listPriceCheck.setItems(null);
			listPriceCheck.setItems(Main.getListPrice(this.database, item.getId().get()));
		} else
			listPriceCheck.setItems(null);
	}

	protected void loadPreview() {
		AmazonProduct item = listProducts.getSelectionModel().getSelectedItem();
		if (item != null) {
			Image image = item.getImageUrl().get().getImage();
			imageViewFoto.setImage(image);
			imageViewFoto.setPreserveRatio(true);

			if (image.getWidth() > image.getHeight())
				imageViewFoto.setFitWidth(500);
			else
				imageViewFoto.setFitHeight(200);

		} else
			imageViewFoto.setImage(null);
	}

	public void init() {
		loadListProduct();
	}

	public void buttonAddOnClick() {

		String link = textFieldLink.getText();
		if (!link.isEmpty()) {
			String productCode = Html.getProductCodeFromUrl(link);
			if (!productCode.isEmpty())
				checkProduct(productCode);
		}
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

		listProducts.setItems(null);
		ObservableList<AmazonProduct> listProduct = Main.getListProduct(this.database);

		listProducts.setItems(listProduct);
		tableViewProducts.setItems(listProduct);
	}

	public SimpleH2Database getDatabase() {
		return database;
	}

	public void setDatabase(SimpleH2Database database) {
		this.database = database;
	}
}
