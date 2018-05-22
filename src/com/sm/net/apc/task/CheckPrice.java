package com.sm.net.apc.task;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import com.sm.net.amazon.util.Html;
import com.sm.net.apc.Main;
import com.sm.net.apc.enumeration.PriceStatus;
import com.sm.net.apc.interfaces.TaskCheckPrice;
import com.sm.net.apc.model.AmazonPrice;
import com.sm.net.apc.model.AmazonProduct;
import com.sm.net.simple.h2.OperationBuilder;
import com.sm.net.simple.h2.SimpleH2Database;
import com.sm.net.simple.h2.SimpleH2ResultSet;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public class CheckPrice implements Runnable {

	private SimpleH2Database database;
	private BigDecimal priceTemp;
	private TaskCheckPrice callback;
	private boolean status;

	public CheckPrice(SimpleH2Database database, TaskCheckPrice callback) {
		super();
		this.database = database;
		this.callback = callback;
		this.status = true;
	}

	@Override
	public void run() {

		ObservableList<AmazonProduct> listProduct = Main.getListProduct(this.database, -2);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				callback.startCheck();
			}
		});

		for (AmazonProduct amazonProduct : listProduct) {

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					status = callback.getStatus();
				}
			});

			if (status) {

				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						callback.setProductName(amazonProduct, listProduct.size());
					}
				});

				checkPrice(amazonProduct);

			} else
				break;
		}

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				callback.stopCheck();
			}
		});
	}

	private void checkPrice(AmazonProduct amazonProduct) {

		String code = amazonProduct.getCode().get();
		String productUrl = Html.getAmazonProductSimpleUrl("de", code);

		String sourceCode = com.sm.net.util.Html.getSourceCode(productUrl);

		if (!sourceCode.isEmpty()) {

			String price = "";

			price = com.sm.net.util.Html.getSubsourceCode(sourceCode, Html.salePriceStart,
					com.sm.net.util.Html.tagSpanEnd);

			if (price.isEmpty())
				price = com.sm.net.util.Html.getSubsourceCode(sourceCode, Html.ourPriceStart,
						com.sm.net.util.Html.tagSpanEnd);

			if (!price.isEmpty())
				addPrice(amazonProduct.getId().get(), price.replaceAll(",", "."));
		}
	}

	private void addPrice(Integer id, String price) {

		int diff = isDifferentPrice(id, price);

		switch (diff) {
		case -2:
			addInDatabase(PriceStatus.DETECTED, id, price);
			break;
		case -1:
			addInDatabase(PriceStatus.INCREASED, id, price);
			break;
		case 1:
			addInDatabase(PriceStatus.REDUCED, id, price);
			break;
		}

	}

	private void addInDatabase(PriceStatus priceStatus, Integer id, String price) {

		OperationBuilder op = new OperationBuilder("apc", "price_check");
		op.setColumnValue("creation_date", Instant.now());
		op.setColumnValue("price", new BigDecimal(price));
		op.setColumnValue("price_old", this.priceTemp);
		op.setColumnValue("id_product", id);
		op.setColumnValue("status", priceStatus.getId());

		database.runOperation(op.buildInsert());
	}

	private int isDifferentPrice(Integer id, String price) {

		OperationBuilder selectionCriteria = new OperationBuilder("apc", "price_check");
		selectionCriteria.setSelection("price_check", "id");
		selectionCriteria.setSelection("price_check", "creation_date");
		selectionCriteria.setSelection("price_check", "price");
		selectionCriteria.setSelection("price_check", "price_old");
		selectionCriteria.setSelection("price_check", "id_product");
		selectionCriteria.setSelection("price_check", "status");
		selectionCriteria.setConditionEquals("id_product", id);

		String query = selectionCriteria.buildSelection() + " ORDER BY creation_date DESC, id DESC";

		SimpleH2ResultSet selection = database.runSelection(query);
		ResultSet resultSet = selection.getResultSet();
		AmazonPrice amazonPrice = null;

		this.priceTemp = new BigDecimal(price);

		try {
			while (resultSet.next()) {
				int idPrice = resultSet.getInt("id");
				Date creationData = resultSet.getDate("creation_date");
				BigDecimal priceProduct = resultSet.getBigDecimal("price");
				BigDecimal priceProductOld = resultSet.getBigDecimal("price_old");
				int idProduct2 = resultSet.getInt("id_product");
				int status = resultSet.getInt("status");

				amazonPrice = new AmazonPrice(idPrice, creationData, priceProduct, priceProductOld, idProduct2, status);

				this.priceTemp = priceProduct;

				break;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		selection.close();

		if (amazonPrice != null)
			return amazonPrice.getPrice().get().compareTo(new BigDecimal(price));

		return -2;
	}

	public SimpleH2Database getDatabase() {
		return database;
	}

	public void setDatabase(SimpleH2Database database) {
		this.database = database;
	}

	public BigDecimal getPriceTemp() {
		return priceTemp;
	}

	public void setPriceTemp(BigDecimal priceTemp) {
		this.priceTemp = priceTemp;
	}

	public TaskCheckPrice getCallback() {
		return callback;
	}

	public void setCallback(TaskCheckPrice callback) {
		this.callback = callback;
	}
}
