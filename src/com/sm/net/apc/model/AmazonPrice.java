package com.sm.net.apc.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.format.DateTimeFormatter;

import com.sm.net.apc.enumeration.PriceStatus;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class AmazonPrice {

	private IntegerProperty id;
	private ObjectProperty<Date> creationDate;
	private ObjectProperty<BigDecimal> price;
	private IntegerProperty idProduct;
	private IntegerProperty status;

	public AmazonPrice(int id, Date creationDate, BigDecimal price, int idProduct, int status) {
		super();
		this.id = new SimpleIntegerProperty(id);
		this.creationDate = new SimpleObjectProperty<Date>(creationDate);
		this.price = new SimpleObjectProperty<BigDecimal>(price.setScale(2, RoundingMode.HALF_UP));
		this.id = new SimpleIntegerProperty(idProduct);
		this.status = new SimpleIntegerProperty(status);
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

}
