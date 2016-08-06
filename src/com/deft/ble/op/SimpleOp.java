package com.deft.ble.op;

import android.bluetooth.BluetoothGatt;

public abstract class SimpleOp extends BleOp<Boolean> {
	private boolean isSucc;
	abstract boolean execuOpInner();

	@Override
	public boolean unsuccessful() {
		return !isSucc;
	}

	@Override
	final public OpResult<Boolean> call() throws Exception {
		isSucc = execuOpInner();
		OpResult<Boolean> opResut = new OpResult<Boolean>(this,
				isSucc ? BluetoothGatt.GATT_SUCCESS : BluetoothGatt.GATT_FAILURE, isSucc);
		return opResut;
	}
}
