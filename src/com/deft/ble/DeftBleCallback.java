package com.deft.ble;

import android.bluetooth.BluetoothAdapter.LeScanCallback;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public abstract class DeftBleCallback implements LeScanCallback {
	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
	}

	/**
	 * call when connect is end,this will be 
	 * called whatever gatt status is connected or not.
	 */
	public abstract void onConnectEnd();

	/**
	 * @param status
	 *            Status of the connect or disconnect operation.
	 *            {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
	 * @param newState
	 *            Returns the new connection state. Can be one of
	 *            {@link BluetoothProfile#STATE_DISCONNECTED} or
	 *            {@link BluetoothProfile#STATE_CONNECTED}
	 */
	public void onConnectionStateChange(int status, int newState) {
	}

	/**
	 * @param status
	 *            {@link BluetoothGatt#GATT_SUCCESS} if the remote device has
	 *            been explored successfully.
	 * @param services
	 *            List<BluetoothGattService> when status is successfully.
	 */
	public void onServicesDiscovered(int status, List<BluetoothGattService> services) {
	}

	/**
	 * Callback reporting the result of a characteristic read operation.
	 * 
	 * @param characteristic
	 *            Characteristic that was read from the associated remote
	 *            device.
	 * @param status
	 *            {@link BluetoothGatt#GATT_SUCCESS} if the read operation was
	 *            completed successfully.
	 */
	public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
	}

	/**
	 * Callback indicating the result of a characteristic write operation.
	 *
	 * <p>
	 * If this callback is invoked while a reliable write transaction is in
	 * progress, the value of the characteristic represents the value reported
	 * by the remote device. An application should compare this value to the
	 * desired value to be written. If the values don't match, the application
	 * must abort the reliable write transaction.
	 *
	 * @param characteristic
	 *            Characteristic that was written to the associated remote
	 *            device.
	 * @param status
	 *            The result of the write operation
	 *            {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
	 */
	public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
	}

	/**
	 * Callback triggered as a result of a remote characteristic notification.
	 *
	 * @param characteristic
	 *            Characteristic that has been updated as a result of a remote
	 *            notification event.
	 */
	public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
	}

	/**
	 * Callback reporting the result of a descriptor read operation.
	 *
	 * @param descriptor
	 *            Descriptor that was read from the associated remote device.
	 * @param status
	 *            {@link BluetoothGatt#GATT_SUCCESS} if the read operation was
	 *            completed successfully
	 */
	public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
	}

	/**
	 * Callback indicating the result of a descriptor write operation.
	 *
	 * @param descriptor
	 *            Descriptor that was writte to the associated remote device.
	 * @param status
	 *            The result of the write operation
	 *            {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
	 */
	public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
	}

	/**
	 * Callback reporting the RSSI for a remote device connection.
	 *
	 * This callback is triggered in response to the
	 * {@link BluetoothGatt#readRemoteRssi} function.
	 *
	 * @param rssi
	 *            The RSSI value for the remote device
	 * @param status
	 *            {@link BluetoothGatt#GATT_SUCCESS} if the RSSI was read
	 *            successfully
	 */
	public void onReadRemoteRssi(int rssi, int status) {
	}
}
