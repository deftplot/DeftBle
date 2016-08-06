package com.deft.ble.op;

import com.deft.ble.InnerBleCallback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

public class ConnectOp extends WaitOp<Integer> {
	private BluetoothDevice bluetoothDevice;
	private final Context context;
	private final InnerBleCallback callback;
	private final boolean isConnect;
	
	public ConnectOp(Context prmCtx, BluetoothDevice prmDevice, InnerBleCallback prmCallback, boolean prmIsConnect) {
		bluetoothDevice = prmDevice;
		context = prmCtx;
		callback = prmCallback;
		isConnect = prmIsConnect;
	}

	@Override
	boolean execuOpInner() {
		boolean succ = false;
		BluetoothGatt gatt = getGatt();
		if (!isConnect && gatt != null) {
			// disconnect
			gatt.disconnect();
			succ = true;
		} else if (isConnect) {
			if (gatt != null && gatt.getDevice().getAddress().equals(bluetoothDevice.getAddress())) {
				//gatt.disconnect();
				succ = gatt.connect();
			} else {
				setGatt(bluetoothDevice.connectGatt(context, false, callback));
				succ = getGatt() != null;
			}
			callback.connectBulid(getGatt());
		}
		return succ;
	}

	@Override
	public boolean isMatch(BluetoothGatt prmGatt, Integer prmObject) {
		boolean isSucc = false;
		if (getGatt().getDevice().equals(prmGatt.getDevice())) {
			isSucc = true;
		}
		return isSucc;
	}

}
