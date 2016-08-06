package com.deft.ble;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.deft.ble.op.BleOp;
import com.deft.ble.op.CharacteristicOp;
import com.deft.ble.op.ConnectOp;
import com.deft.ble.op.DescriptorOp;
import com.deft.ble.op.DiscoverServicesOp;
import com.deft.ble.op.NotificationSetOp;
import com.deft.ble.op.OpFuture;
import com.deft.ble.op.OpResult;
import com.deft.ble.op.ReadRssiOp;
import com.deft.ble.op.ScanOp;
import com.deft.ble.op.WaitOp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class DeftBle {
	private static final String TAG = "DeftBle";
	private static final boolean INFO = true;
	private static int sCount = 0;
	final private static int MSG_ADD_OP = 0;
	final private static int MSG_DEAL_NEXT_OP = 1;
	final private static int MSG_WAIT_OP_DONE = 2;
	final private static int MSG_OP_FAILED = 3;
	final private static int MSG_CHECK_ERROR = 4;

	final private static int IDLE = 0;
	final private static int CONNECTING = 1;
	final private static int CONNECTED = 2;
	final private static int DISCONNECTING = 3;
	final private static int DISCONNECTED = 4;
	private final DeftBleCallback mCallback;
	private Context mContext;
	private final Handler mHandler;
	private final BluetoothManager mBluetoothManager;
	private final BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mGatt;

	private int mConnectStatus = IDLE;// connect status
	private final Object mLockObject = new Object();
	private volatile WaitOp<?> mWaitingOp = null;
	private final AtomicInteger mCurrOpSeq = new AtomicInteger();
	private final InnerBleCallback mInnerCallback = new InnerCallback();
	private final Callable<Boolean> mCheckCallback = new Callable<Boolean>() {
		private boolean hasWaitOp = false;
		private int lastWaitSeq;

		@Override
		public Boolean call() throws Exception {
			boolean isOk = true;
			/*
			 * this callable deal in handler, not require lock
			 */
			if (mWaitingOp != null) {
				if (hasWaitOp && lastWaitSeq == mCurrOpSeq.get()) {
					isOk = false;
				} else {
					lastWaitSeq = mCurrOpSeq.get();
					hasWaitOp = true;
				}
			}
			return isOk;
		}
	};

	DeftBle(Context prmCtx, DeftBleCallback prmCallback) {
		mCallback = prmCallback;
		mContext = prmCtx.getApplicationContext();
		// mHandler = new BleHandler(mContext.getMainLooper());
		HandlerThread handlerThread = new HandlerThread("DeftBle-" + sCount++);
		handlerThread.start();
		mHandler = new BleHandler(handlerThread.getLooper());
		mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
	}

	// call by DeftBleManager
	void checkCallBackException() {
		FutureTask<Boolean> checkFuture = new FutureTask<Boolean>(mCheckCallback);
		mHandler.obtainMessage(MSG_CHECK_ERROR, 0, 0, checkFuture).sendToTarget();
		boolean isOk = false;
		try {
			isOk = checkFuture.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (!isOk) {
			if (INFO)
				Log.i(TAG, "checkCallBackException MSG_OP_FAILED");
			mHandler.sendEmptyMessage(MSG_OP_FAILED);
		}

	}

	void destory() {
		mHandler.sendEmptyMessage(MSG_OP_FAILED);
		mHandler.getLooper().quitSafely();
	}

	private  void addOp(BleOp<?> prmOp) {
		if (!mHandler.sendMessage(mHandler.obtainMessage(MSG_ADD_OP, 0, 0, prmOp))){
			prmOp.getOpFuture().cancel(false);
		}
	}

	public OpFuture<Boolean> startScan() {
		ScanOp op = new ScanOp(mBluetoothAdapter, mInnerCallback, true);
		addOp(op);
		return op.getOpFuture();
	}

	public OpFuture<Boolean> stopScan() {
		ScanOp op = new ScanOp(mBluetoothAdapter, mInnerCallback, false);
		addOp(op);
		return op.getOpFuture();
	}

	public OpFuture<Integer> connectGatt(BluetoothDevice prmDevice) {
		ConnectOp op = new ConnectOp(mContext, prmDevice, mInnerCallback, true);
		addOp(op);
		return op.getOpFuture();
	}

	public OpFuture<Integer> disconnectGatt() {
		ConnectOp op = new ConnectOp(mContext, null, mInnerCallback, false);
		addOp(op);
		return op.getOpFuture();
	}

	public OpFuture<BluetoothGattCharacteristic> readCharacteristic(UUID prmServiceUuid, UUID prmCharacteristicUuid) {
		// if (mGatt == null) {
		// throw new NullPointerException("mGatt null, connectGatt first");
		// }
		CharacteristicOp op = new CharacteristicOp(prmServiceUuid, prmCharacteristicUuid, null);
		addOp(op);
		return op.getOpFuture();
	}

	/*
	 * @param prmWriteType The write type to for this characteristic. Can be one
	 * of: {@link #WRITE_TYPE_DEFAULT}, {@link #WRITE_TYPE_NO_RESPONSE} or
	 * {@link #WRITE_TYPE_SIGNED}.
	 */
	public OpFuture<BluetoothGattCharacteristic> writeCharacteristic(UUID prmServiceUuid, UUID prmCharacteristicUuid,
			byte prmValue[], Integer prmWriteType) {
		CharacteristicOp op = new CharacteristicOp(prmServiceUuid, prmCharacteristicUuid,
				new CharacteristicOp.WriteOp(prmValue, prmWriteType));
		addOp(op);
		return op.getOpFuture();
	}

	public OpFuture<BluetoothGattCharacteristic> writeCharacteristic(UUID prmServiceUuid, UUID prmCharacteristicUuid,
			byte prmValue[]) {
		return writeCharacteristic(prmServiceUuid, prmCharacteristicUuid, prmValue, null);
	}

	public OpFuture<BluetoothGattDescriptor> readDescriptor(UUID prmServiceUuid, UUID prmCharacteristicUuid,
			UUID prmDescriptorUuid) {
		DescriptorOp op = new DescriptorOp(prmServiceUuid, prmCharacteristicUuid, prmDescriptorUuid, null, false);
		addOp(op);
		return op.getOpFuture();
	}

	public OpFuture<BluetoothGattDescriptor> writeDescriptor(UUID prmServiceUuid, UUID prmCharacteristicUuid,
			UUID prmDescriptorUuid, byte prmValue[]) {
		DescriptorOp op = new DescriptorOp(prmServiceUuid, prmCharacteristicUuid, prmDescriptorUuid, prmValue, true);
		addOp(op);
		return op.getOpFuture();
	}

	public OpFuture<Boolean> setCharacteristicNotification(UUID prmServiceUuid, UUID prmCharacteristicUuid,
			boolean prmEnable) {
		NotificationSetOp op = new NotificationSetOp(prmServiceUuid, prmCharacteristicUuid, prmEnable);
		addOp(op);
		return op.getOpFuture();
	}

	public OpFuture<List<BluetoothGattService>> discoverServices() {
		DiscoverServicesOp op = new DiscoverServicesOp();
		addOp(op);
		return op.getOpFuture();
	}

	public OpFuture<Integer> readRemoteRssi() {
		ReadRssiOp op = new ReadRssiOp();
		addOp(op);
		return op.getOpFuture();
	}

	// wait future done,return true if result is ok
	public boolean waitFuture(OpFuture<?> prmOpFuture) {
		boolean isOk = false;
		try {
			if (prmOpFuture != null){
				OpResult<?> opResult = prmOpFuture.get();
				if (opResult.getStatus() == BluetoothGatt.GATT_SUCCESS) {
					isOk = true;
				}
			}
		} catch (Throwable e) {
			if (INFO)
				e.printStackTrace();
		}
		return isOk;
	}
	
	public void waitFutureExcept(OpFuture<?> prmOpFuture) {
		if (!waitFuture(prmOpFuture)){
			throw new CancellationException();
		}
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}

	public BluetoothDevice getRemoteDevice(String address) {
		return mBluetoothAdapter.getRemoteDevice(address);
	}

	private class BleHandler extends Handler {
		private LinkedList<BleOp<?>> mOpQueue = new LinkedList<BleOp<?>>();
		private boolean mOpDealIsRunning = false;

		public BleHandler(Looper prmLooper) {
			super(prmLooper);
		}

		private boolean dealOneOp(BleOp<?> oneOp) {
			boolean isSucc = true;
			if (INFO)
				Log.i(TAG, "deal op seq:" + mCurrOpSeq.get() + " op:" + oneOp);
			mCurrOpSeq.incrementAndGet();
			oneOp.setGatt(mGatt);
			oneOp.execuOp();
			if (oneOp.unsuccessful()) {
				// WaitOp must cancel in here
				oneOp.cancel();
				// failed, realease all
				opFailed();
				isSucc = false;
			} else if (oneOp instanceof WaitOp) {
				synchronized (mLockObject) {
					mWaitingOp = (WaitOp<?>) oneOp;
				}
				if (INFO)
					Log.i(TAG, "begin mWaitingOp " + oneOp);
			}
			return isSucc;
		}

		private void dealOp() {
			boolean isSucc = false;
			BleOp<?> oneOp = mOpQueue.poll();
			if (oneOp != null) {
				isSucc = dealOneOp(oneOp);
			}
			if (isSucc && mWaitingOp == null) {
				sendEmptyMessage(MSG_DEAL_NEXT_OP);
			} else if (!isSucc) {
				mOpDealIsRunning = false;
			}
		}

		private void addOp(Message msg) {
			if (msg.obj instanceof BleOp) {
				mOpQueue.add((BleOp<?>) msg.obj);
				if (!mOpDealIsRunning) {
					sendEmptyMessage(MSG_DEAL_NEXT_OP);
					mOpDealIsRunning = true;
				}
			}
		}

		private void waitOpDone() {
			boolean needNextOp = false;
			synchronized (mLockObject) {
				if (mWaitingOp != null) {
					mWaitingOp.cancel();
					mWaitingOp = null;
					needNextOp = true;
				}
			}
			if (needNextOp && !hasMessages(MSG_DEAL_NEXT_OP)) {
				sendEmptyMessage(MSG_DEAL_NEXT_OP);
			}
		}

		private void opFailed() {
			if (INFO)
				Log.i(TAG, "opFailed");
			waitOpDone();
			BleOp<?> oneOp = null;
			while ((oneOp = mOpQueue.poll()) != null) {
				oneOp.cancel();
			}

			if (mGatt != null) {
				mGatt.disconnect();
				mGatt = null;
			}
			mConnectStatus = IDLE;
			if (mCallback != null) {
				mCallback.onConnectEnd();
			}
		}

		private void checkError(Message msg) {
			if (INFO)
				Log.i(TAG, "checkError");
			if (msg.obj instanceof RunnableFuture) {
				RunnableFuture<?> future = (RunnableFuture<?>) msg.obj;
				future.run();
			}
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ADD_OP:
				addOp(msg);
				break;
			case MSG_DEAL_NEXT_OP:
				dealOp();
				break;
			case MSG_WAIT_OP_DONE:
				if (INFO)
					Log.i(TAG, "end mWaitingOp");
				waitOpDone();
				break;
			case MSG_OP_FAILED:
				opFailed();
				break;
			case MSG_CHECK_ERROR:
				checkError(msg);
				break;
			default:
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> boolean setWaitResult(Class<?> targetCls, BluetoothGatt gatt, int status, T prmArg) {
		boolean isSucc = false;
		synchronized (mLockObject) {
			if (targetCls.isInstance(mWaitingOp) && ((WaitOp<T>) mWaitingOp).isMatch(gatt, prmArg)) {
				((WaitOp<T>) mWaitingOp).setFutrueResult(status, prmArg);
				mHandler.sendEmptyMessage(MSG_WAIT_OP_DONE);
				if (status != BluetoothGatt.GATT_SUCCESS) {
					mHandler.sendEmptyMessage(MSG_OP_FAILED);
				}
			}
		}
		return isSucc;
	}

	private class InnerCallback extends InnerBleCallback {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (mCallback != null) {
				mCallback.onLeScan(device, rssi, scanRecord);
			}
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (mCallback != null) {
				mCallback.onConnectionStateChange(status, newState);
			}
			setWaitResult(ConnectOp.class, gatt, status, newState);
			if (BluetoothGatt.GATT_SUCCESS == status && newState == BluetoothProfile.STATE_DISCONNECTED) {
				// disconnet remove all
				if (mHandler.getLooper().getThread().isAlive()){
					mHandler.sendEmptyMessage(MSG_OP_FAILED);
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (mCallback != null) {
				mCallback.onServicesDiscovered(status, gatt.getServices());
			}
			setWaitResult(DiscoverServicesOp.class, gatt, status, gatt.getServices());
			List<BluetoothGattService> services = gatt.getServices();
			for (BluetoothGattService service : services) {
				List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
				for (BluetoothGattCharacteristic characteristic : characteristics) {
					String msg = service.getUuid() + " charuuid:" + characteristic.getUuid().toString() + " permission:"
							+ characteristic.getPermissions() + " property:" + characteristic.getProperties();
					Log.i(TAG, msg);
				}
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (mCallback != null) {
				mCallback.onCharacteristicRead(characteristic, status);
			}
			setWaitResult(CharacteristicOp.class, gatt, status, characteristic);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (mCallback != null) {
				mCallback.onCharacteristicWrite(characteristic, status);
			}
			setWaitResult(CharacteristicOp.class, gatt, status, characteristic);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if (mCallback != null) {
				mCallback.onCharacteristicChanged(characteristic);
			}
			// data recv data
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			if (mCallback != null) {
				mCallback.onDescriptorRead(descriptor, status);
			}
			setWaitResult(DescriptorOp.class, gatt, status, descriptor);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			if (mCallback != null) {
				mCallback.onDescriptorWrite(descriptor, status);
			}
			setWaitResult(DescriptorOp.class, gatt, status, descriptor);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			if (mCallback != null) {
				mCallback.onReadRemoteRssi(rssi, status);
			}
			setWaitResult(ReadRssiOp.class, gatt, status, rssi);
		}

		@Override
		public void connectBulid(BluetoothGatt prmGatt) {
			mGatt = prmGatt;
		}

	}
}
