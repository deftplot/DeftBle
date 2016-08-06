package com.deft.ble.op;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.FutureTask;

import android.bluetooth.BluetoothGatt;

/*this operate is need wait reply */
public abstract class WaitOp<V> extends BleOp<V> {
	private static Method sRunAndReset;
	private boolean isSucc;
	static {
		try {
			sRunAndReset = FutureTask.class.getDeclaredMethod("runAndReset");
			sRunAndReset.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public void execuOp() {
		try {
			sRunAndReset.invoke(getOpFuture());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean unsuccessful() {
		return !isSucc;
	}

	@Override
	final public OpResult<V> call() throws Exception {
		isSucc = execuOpInner();
		return null;
	}
	
	//op ,succ return true, else fail
	abstract boolean execuOpInner();

	public void setFutrueResult(int status, V arg){
		OpResult<V> opResult = new OpResult<V>(this, status, arg);
		getOpFuture().setResult(opResult);
	}
	
	public boolean isMatch(BluetoothGatt prmGatt, V prmObject){
		return true;
	}
}
