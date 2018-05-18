package com.sm.net.apc.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.format.DateTimeFormatter;

import com.sm.net.apc.Main;
import com.sm.net.apc.enumeration.PriceStatus;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AmazonPrice {

	private IntegerProperty id;
	private ObjectProperty<Date> creationDate;
	private ObjectProperty<BigDecimal> price;
	private ObjectProperty<BigDecimal> priceOld;
	private IntegerProperty idProduct;
	private IntegerProperty status;
	private ObjectProperty<BigDecimal> percentage;
	private StringProperty percentageString;
	private ObjectProperty<ImageView> imageViewStatus;

	public AmazonPrice(int id, Date creationDate, BigDecimal price, BigDecimal priceOld, int idProduct, int status) {
		super();
		this.id = new SimpleIntegerProperty(id);
		this.creationDate = new SimpleObjectProperty<Date>(creationDate);
		this.price = new SimpleObjectProperty<BigDecimal>(price.setScale(2, RoundingMode.HALF_UP));
		this.priceOld = new SimpleObjectProperty<BigDecimal>(priceOld.setScale(2, RoundingMode.HALF_UP));
		this.id = new SimpleIntegerProperty(idProduct);
		this.status = new SimpleIntegerProperty(status);
		this.percentage = setPercentage();
		this.percentageString = setPercentageString();
		this.imageViewStatus = setImage();
	}

	private StringProperty setPercentageString() {
		return new SimpleStringProperty(this.percentage.get().toString() + " %");
	}

	private ObjectProperty<ImageView> setImage() {

		int id = this.status.get();
		PriceStatus status = PriceStatus.getFromId(id);
		Image image = null;

		switch (status) {
		case DETECTED:
			image = new Image(Main.PRICE.toURI().toString());
			break;
		case INCREASED:
			image = new Image(Main.UP.toURI().toString());
			break;
		case REDUCED:
			image = new Image(Main.DOWN.toURI().toString());
			break;
		}

		if (image != null) {

			ImageView imageView = new ImageView(image);
			imageView.setFitWidth(50);
			imageView.setFitHeight(50);

			return new SimpleObjectProperty<ImageView>(imageView);
		}
		return null;
	}

	private ObjectProperty<BigDecimal> setPercentage() {

		BigDecimal price = this.price.get();
		BigDecimal priceOld = this.priceOld.get();

		if (price.compareTo(priceOld) != 0) {

			BigDecimal value = null;

			value = price.subtract(priceOld);
			value = value.multiply(BigDecimal.valueOf(100));
			value = value.divide(priceOld, 2, RoundingMode.HALF_UP);
			value = value.setScale(2, RoundingMode.HALF_UP);

			return new SimpleObjectProperty<BigDecimal>(value);
		} else
			return new SimpleObjectProperty<BigDecimal>(BigDecimal.ZERO);
	}

	public IntegerProperty getId() {
		return id;
	}

	public void setId(IntegerProperty id) {
		this.id = id;
	}

	public ObjectProperty<Date> getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(ObjectProperty<Date> creationDate) {
		this.creationDate = creationDate;
	}

	public ObjectProperty<BigDecimal> getPrice() {
		return price;
	}

	public void setPrice(ObjectProperty<BigDecimal> price) {
		this.price = price;
	}

	@Override
	public String toString() {
		String text = "";

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		text = "[" + dtf.format(getCreationDate().get().toLocalDate()) + "]";
		text += "  -->  ";

		PriceStatus priceStatus = PriceStatus.getFromId(getStatus().get());
		text += "[" + priceStatus.toString() + "]";

		text += "  -->  ";
		text += getPrice().get().toString();

		return text;
	}

	public IntegerProperty getIdProduct() {
		return idProduct;
	}

	public void setIdProduct(IntegerProperty idProduct) {
		this.idProduct = idProduct;
	}

	public IntegerProperty getStatus() {
		return status;
	}

	public void setStatus(IntegerProperty status) {
		this.status = status;
	}

	public ObjectProperty<BigDecimal> getPriceOld() {
		return priceOld;
	}

	public void setPriceOld(ObjectProperty<BigDecimal> priceOld) {
		this.priceOld = priceOld;
	}

	public ObjectProperty<BigDecimal> getPercentage() {
		return percentage;
	}

	public void setPercentage(ObjectProperty<BigDecimal> percentage) {
		this.percentage = percentage;
	}

	public ObjectProperty<ImageView> getImageViewStatus() {
		return imageViewStatus;
	}

	public void setImageViewStatus(ObjectProperty<ImageView> imageViewStatus) {
		this.imageViewStatus = imageViewStatus;
	}

	public StringProperty getPercentageString() {
		return percentageString;
	}

	public void setPercentageString(StringProperty percentageString) {
		this.percentageString = percentageString;
	}

}
