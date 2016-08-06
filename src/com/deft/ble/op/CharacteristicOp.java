package com.deft.ble.op;

import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class CharacteristicOp extends WaitOp<BluetoothGattCharacteristic> {
	private BluetoothGattCharacteristic characteristic;
	private final UUID serviceUuid;
	private final UUID characteristicUuid;
	private final WriteOp writeOp; 
	
	static public class WriteOp{
		public WriteOp(byte[] prmValue){
			this(prmValue, null);
		}
		public WriteOp(byte[] prmValue, Integer prmWriteType){
			value = prmValue;
			writeType = prmWriteType;
		}
		private byte[] value;
		/*
	    * @param writeType The write type to for this characteristic. Can be one
	    *                  of:
	    *                  {@link #WRITE_TYPE_DEFAULT},
	    *                  {@link #WRITE_TYPE_NO_RESPONSE} or
	    *                  {@link #WRITE_TYPE_SIGNED}.
	    */
		private Integer writeType;
		public byte[] getValue() {
			return value;
		}
		public void setValue(byte[] value) {
			this.value = value;
		}
		public Integer getWriteType() {
			return writeType;
		}
		public void setWriteType(Integer writeType) {
			this.writeType = writeType;
		}
	}

	public CharacteristicOp(UUID prmServiceUuid, UUID prmCharacteristicUuid, WriteOp prmWriteOp) {
		serviceUuid = prmServiceUuid;
		characteristicUuid = prmCharacteristicUuid;
		writeOp = prmWriteOp;
	}

	@Override
	boolean execuOpInner() {
		boolean isSucc = false;
		if (getGatt() != null 
				&& (characteristic=getGatt().getService(serviceUuid).getCharacteristic(characteristicUuid))!= null) {
			if (writeOp != null) {
				if (writeOp.getWriteType() != null){
					characteristic.setWriteType(writeOp.getWriteType());
				}
				characteristic.setValue(writeOp.getValue());
				isSucc = getGatt().writeCharacteristic(characteristic);
			} else {
				isSucc = getGatt().readCharacteristic(characteristic);
			}
		}
		return isSucc;
	}

	@Override
	public boolean isMatch(BluetoothGatt prmGatt, BluetoothGattCharacteristic prmObject) {
		boolean isSucc = false;
		if (characteristic == prmObject) { //not equal
			isSucc = true;
		}
		return isSucc;
	}

}
