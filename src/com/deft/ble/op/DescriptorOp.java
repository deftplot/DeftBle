package com.deft.ble.op;

import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

public class DescriptorOp extends WaitOp<BluetoothGattDescriptor> {
	private BluetoothGattDescriptor descriptor;
	private final boolean isWrite;
	private final UUID serviceUuid;
	private final UUID characteristicUuid;
	private final UUID descriptorUuid;
	private final byte[] writeValue;

	public DescriptorOp(UUID prmServiceUuid, UUID prmCharacteristicUuid, UUID prmDescriptorUuid, byte prmValue[],
			boolean prmIsWrite) {
		serviceUuid = prmServiceUuid;
		characteristicUuid = prmCharacteristicUuid;
		descriptorUuid = prmDescriptorUuid;
		writeValue = prmValue;
		isWrite = prmIsWrite;
	}

	@Override
	boolean execuOpInner() {
		boolean isSucc = false;
		if (getGatt() != null
				&& (descriptor = getGatt().getService(serviceUuid).getCharacteristic(characteristicUuid)
				.getDescriptor(descriptorUuid)) != null){
			if (isWrite ) {
				descriptor.setValue(writeValue);
				isSucc = getGatt().writeDescriptor(descriptor);
			} else {
				isSucc = getGatt().readDescriptor(descriptor);
			}
		}
		return isSucc;
	}

	@Override
	public boolean isMatch(BluetoothGatt prmGatt, BluetoothGattDescriptor prmObject) {
		boolean isSucc = false;
		if (descriptor == prmObject) { // not equal
			isSucc = true;
		}
		return isSucc;
	}
}
