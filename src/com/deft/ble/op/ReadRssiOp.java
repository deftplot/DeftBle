package com.deft.ble.op;

import android.bluetooth.BluetoothGatt;

public class ReadRssiOp extends WaitOp<Integer> {

	@Override
	boolean execuOpInner() {
		return getGatt().readRemoteRssi();
	}
	
	@Override
	public boolean isMatch(BluetoothGatt prmGatt, Integer prmObject) {
		boolean isSucc = false;
		if (getGatt().getDevice().equals(prmGatt.getDevice())){
			isSucc = true;
		}
		return isSucc;
	}
}
