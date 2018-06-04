package com.sm.net.apc.interfaces;

import java.math.BigDecimal;

import com.sm.net.apc.model.AmazonProduct;

public interface TaskCheckPrice {

	public abstract void startCheck();

	public abstract void stopCheck();

	public abstract void showAlert(AmazonProduct amazonProduct, BigDecimal newPrice);

	public abstract void setProductName(AmazonProduct amazonProduct, int size);

	public abstract boolean getStatus();
}
