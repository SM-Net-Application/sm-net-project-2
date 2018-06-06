package com.sm.net.apc.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Setting {

	private IntegerProperty id;
	private StringProperty name;
	private StringProperty value;

	public Setting(Integer id, String name, String value) {
		super();
		this.id = new SimpleIntegerProperty(id);
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty(value);
	}

	public Setting(String name, String value) {
		super();
		this.id = null;
		this.name = new SimpleStringProperty(name);
		this.value = new SimpleStringProperty(value);
	}

	public IntegerProperty getId() {
		return id;
	}

	public void setId(IntegerProperty id) {
		this.id = id;
	}

	public StringProperty getName() {
		return name;
	}

	public void setName(StringProperty name) {
		this.name = name;
	}

	public StringProperty getValue() {
		return value;
	}

	public void setValue(StringProperty value) {
		this.value = value;
	}
}
