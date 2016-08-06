package com.deft.ble.op;

import com.deft.ble.InnerBleCallback;

import android.bluetooth.BluetoothAdapter;

//scan or stopScan Ble
public class ScanOp extends SimpleOp {
	private boolean isBeginScan;
	private BluetoothAdapter bluetoothAdapter;
	private InnerBleCallback callback;

	public ScanOp(BluetoothAdapter bluetoothAdapter, InnerBleCallback callback, boolean isBeginScan) {
		this.bluetoothAdapter = bluetoothAdapter;
		this.callback = callback;
		this.isBeginScan = isBeginScan;
	}

	@Override
	boolean execuOpInner() {
		boolean suc = false;
		if (isBeginScan) {
			bluetoothAdapter.stopLeScan(callback);
			suc = bluetoothAdapter.startLeScan(callback);
		} else {
			bluetoothAdapter.stopLeScan(callback);
			suc = true;
		}
		return suc;
	}

}
