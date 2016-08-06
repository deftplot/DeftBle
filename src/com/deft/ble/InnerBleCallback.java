package com.deft.ble;

import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

public abstract class InnerBleCallback extends BluetoothGattCallback  implements LeScanCallback{
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){};
	public abstract void connectBulid(BluetoothGatt prmGatt);
}
