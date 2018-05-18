package com.sm.net.apc.interfaces;

import com.sm.net.apc.model.AmazonProduct;

public interface TaskCheckPrice {

	public abstract void startCheck();

	public abstract void stopCheck();

	public abstract void setProductName(AmazonProduct amazonProduct, int size);

	public abstract boolean getStatus();
}
