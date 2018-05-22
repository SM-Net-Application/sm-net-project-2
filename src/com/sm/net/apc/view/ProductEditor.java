package com.sm.net.apc.view;

import com.sm.net.apc.Main;
import com.sm.net.apc.model.AmazonList;
import com.sm.net.apc.model.AmazonProduct;
import com.sm.net.simple.h2.OperationBuilder;
import com.sm.net.simple.h2.SimpleH2Database;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

	private SimpleH2Database database;
	private AmazonProduct product;
	private String bufferName;
	private MainView mainView;

	@FXML
	private void initialize() {

		this.textFieldID.setEditable(false);
		this.textFieldID.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.textFieldName.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");
		this.comboBoxList.setStyle("-fx-alignment: center-left; -fx-font: 15px System;");

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
	}

	private void loadProduct() {
		imageView.setImage(this.product.getImageUrl().get().getImage());
		textFieldID.setText(this.product.getCode().get());
		textFieldName.setText(this.product.getProductName().get());
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
