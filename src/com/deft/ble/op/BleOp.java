package com.deft.ble.op;

import java.util.concurrent.Callable;

import android.bluetooth.BluetoothGatt;

public abstract class BleOp<V> implements Callable<OpResult<V>> {
	private BluetoothGatt gatt;
	private final OpFuture<V> opFuture;

	BleOp() {
		opFuture = new OpFuture<V>(this);
	}

	public void execuOp() {
		opFuture.run();
	}

	public OpFuture<V> getOpFuture() {
		return opFuture;
	}

	final public boolean isDone() {
		return opFuture.isDone();
	}

	final public boolean cancel() {
		return opFuture.cancel(false);
	}

	public BluetoothGatt getGatt() {
		return gatt;
	}

	public void setGatt(BluetoothGatt gatt) {
		this.gatt = gatt;
	}

	abstract public boolean unsuccessful();

	@Override
	public OpResult<V> call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
