package com.sm.net.apc.model;

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

	public AmazonProduct(int id, String code, String productName, String imageUrl) {
		super();
		this.id = new SimpleIntegerProperty(id);
		this.code = new SimpleStringProperty(code);
		this.productName = new SimpleStringProperty(productName);
		this.imageUrl = new SimpleObjectProperty<ImageView>(getImageView(imageUrl));
	}

	private ImageView getImageView(String imageUrl) {

		Image image = new Image(imageUrl);
		ImageView imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		if (image.getWidth() > image.getHeight())
			imageView.setFitWidth(100);
		else
			imageView.setFitHeight(100);

		return imageView;
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

}
