package com.sm.net.apc.view;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import com.sm.net.amazon.util.Html;
import com.sm.net.apc.Main;
import com.sm.net.apc.model.AmazonList;
import com.sm.net.apc.model.AmazonProduct;
import com.sm.net.simple.h2.SimpleH2Database;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class Alert {

	@FXML
	private ImageView imageView;
	@FXML
	private TextField textFieldID;
	@FXML
	private TextField textFieldName;
	@FXML
	private TextField textFieldList;
	@FXML
	private Label labelNewPrice;
	@FXML
	private Button buttonOpenAmazon;

	private SimpleH2Database database;
	private AmazonProduct product;
	private ObservableList<AmazonList> list;
	private BigDecimal newPrice;

	@FXML
	private void initialize() {

		this.textFieldID.setEditable(false);
		this.textFieldName.setEditable(false);
		this.textFieldList.setEditable(false);
		this.textFieldID.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.textFieldName.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.textFieldList.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
	}

	public void init() {

		list = Main.getListList(this.database);
		list.add(0, new AmazonList(-1, "Wishlist"));

		loadProduct();
		labelNewPrice.setText(newPrice.toString() + " €");
	}

	public void openAmazon() {

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

	private void loadProduct() {
		imageView.setImage(this.product.getImageUrl().get().getImage());
		textFieldID.setText(this.product.getCode().get());
		textFieldName.setText(this.product.getProductName().get());
		textFieldList.setText(getListProduct());
	}

	private String getListProduct() {

		int idList = this.product.getIdList().get();
		for (AmazonList amazonList : this.list) {
			if (amazonList.getId().intValue() == idList)
				return amazonList.getName().get();
		}
		return "";
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

	public BigDecimal getNewPrice() {
		return newPrice;
	}

	public void setNewPrice(BigDecimal newPrice) {
		this.newPrice = newPrice;
	}

}
