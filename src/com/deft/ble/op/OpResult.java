package com.deft.ble.op;

public class OpResult<V> {
	private final BleOp<V> srcOp;
	private int status;
	private V arg;
	public OpResult(BleOp<V> srcOp){
		this.srcOp = srcOp;
	}
	public OpResult(BleOp<V> srcOp, int status){
		this(srcOp);
		this.status = status;
	}
	public OpResult(BleOp<V> srcOp, int status, V arg){
		this(srcOp, status);
		this.arg = arg;
	}
	
	public BleOp<V> getSrcOp() {
		return srcOp;
	}
	public void setArg(V arg) {
		this.arg = arg;
	}
	public V getArg() {
		return arg;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}
