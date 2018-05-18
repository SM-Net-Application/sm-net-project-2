package com.sm.net.apc.enumeration;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public enum PriceStatus {

	INCREASED(0, "INCREASED"), REDUCED(1, "REDUCED"), DETECTED(2, "DETECTED");

	private Integer id;
	private StringProperty name;

	private PriceStatus(int id, String name) {
		this.id = new Integer(id);
		this.name = new SimpleStringProperty(name);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public StringProperty getName() {
		return name;
	}

	public void setName(StringProperty name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name.get();
	}

	public static PriceStatus getFromId(int id) {

		for (PriceStatus enu : PriceStatus.values()) {
			if (enu.getId().compareTo(new Integer(id)) == 0) {
				return enu;
			}
		}

		return null;
	}
}
