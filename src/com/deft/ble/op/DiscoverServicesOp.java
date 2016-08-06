package com.deft.ble.op;

import java.util.List;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

public class DiscoverServicesOp extends WaitOp<List<BluetoothGattService>> {
	@Override
	boolean execuOpInner() {
		return getGatt().discoverServices();
	}

	@Override
	public boolean isMatch(BluetoothGatt prmGatt, List<BluetoothGattService> prmObject) {
		boolean isSucc = false;
		if (getGatt().getDevice().equals(prmGatt.getDevice())){
			isSucc = true;
		}
		return isSucc;
	}

}
