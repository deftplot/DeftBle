package com.deft.ble.op;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class OpFuture<V> extends FutureTask<OpResult<V>> {
	public OpFuture(Callable<OpResult<V>> callable) {
		super(callable);
	}
	
	void setResult(OpResult<V> prmResult){
		super.set(prmResult);
	}
}
