package com.sm.net.apc.model;

import java.io.File;
import java.math.BigDecimal;

import com.sm.net.apc.Main;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AmazonProduct {

	private IntegerProperty id;
	private StringProperty code;
	private StringProperty productName;
	private ObjectProperty<ImageView> imageUrl;
	private IntegerProperty idList;
	private ObjectProperty<BigDecimal> priceAlert;

	public AmazonProduct(int id, String code, String productName, String imageUrl, int idList, BigDecimal priceAlert) {
		super();
		this.id = new SimpleIntegerProperty(id);
		this.code = new SimpleStringProperty(code);
		this.productName = new SimpleStringProperty(productName);
		this.imageUrl = new SimpleObjectProperty<ImageView>(getImageView(id, imageUrl));
		this.idList = new SimpleIntegerProperty(idList);
		this.priceAlert = new SimpleObjectProperty<BigDecimal>(priceAlert);
	}

	private ImageView getImageView(int id, String imageUrl) {

		String path = Main.getImagePath(id);
		File imageFile = new File(path);
		ImageView imageView = null;

		if (imageFile.exists()) {
			Image image = new Image(imageFile.toURI().toString());
			imageView = new ImageView(image);
			imageView.setPreserveRatio(true);
			if (image.getWidth() > image.getHeight())
				imageView.setFitWidth(100);
			else
				imageView.setFitHeight(100);

			return imageView;
		} else {
			return new ImageView();
		}
	}

	public IntegerProperty getId() {
		return id;
	}

	public void setId(IntegerProperty id) {
		this.id = id;
	}

	public StringProperty getCode() {
		return code;
	}

	public void setCode(StringProperty code) {
		this.code = code;
	}

	public StringProperty getProductName() {
		return productName;
	}

	public void setProductName(StringProperty productName) {
		this.productName = productName;
	}

	public ObjectProperty<ImageView> getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(ObjectProperty<ImageView> imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Override
	public String toString() {
		return getProductName().get();
	}

	public IntegerProperty getIdList() {
		return idList;
	}

	public void setIdList(IntegerProperty idList) {
		this.idList = idList;
	}

	public ObjectProperty<BigDecimal> getPriceAlert() {
		return priceAlert;
	}

	public void setPriceAlert(ObjectProperty<BigDecimal> priceAlert) {
		this.priceAlert = priceAlert;
	}

}
