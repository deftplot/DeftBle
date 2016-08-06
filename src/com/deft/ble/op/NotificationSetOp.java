package com.deft.ble.op;

import java.util.UUID;

import android.bluetooth.BluetoothGattCharacteristic;

public class NotificationSetOp extends SimpleOp {
	private final boolean enable;
	private final UUID serviceUuid;
	private final UUID characteristicUuid;

	public NotificationSetOp(UUID prmServiceUuid, UUID prmCharacteristicUuid, boolean prmEnable) {
		enable = prmEnable;
		serviceUuid = prmServiceUuid;
		characteristicUuid = prmCharacteristicUuid;
	}

	@Override
	boolean execuOpInner() {
		BluetoothGattCharacteristic characteristic = getGatt().getService(serviceUuid).getCharacteristic(characteristicUuid);
		return getGatt().setCharacteristicNotification(characteristic, enable);
	}

}
