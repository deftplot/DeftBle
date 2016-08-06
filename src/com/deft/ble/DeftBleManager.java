package com.deft.ble;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class DeftBleManager {
	private static final int MSG_CHECK_DEFTBLE = 0;
	private static final int CHECK_DEFTBLE_DELAY = 60 * 1000;
	private static DeftBleManager sInstance;

	static public synchronized void init(Context prmCtx) {
		if (sInstance == null) {
			sInstance = new DeftBleManager(prmCtx);
		}
	}

	public static DeftBle getOneDeftBle(DeftBleCallback prmCallback) {
		DeftBle deftBle = new DeftBle(sInstance.mContext, prmCallback);
		sInstance.addOneDeftBle(deftBle);
		return deftBle;
	}

	public static void releaseOneDeftBle(DeftBle prmDeftBle) {
		if (prmDeftBle != null){
			sInstance.removeOneDeftBle(prmDeftBle);
		}
	}
	
	final private Context mContext;
	final private Handler mMangerHandler;
	
	private final ArrayList<DeftBle> mDeftBles = new ArrayList<DeftBle>();

	private DeftBleManager(Context prmCtx) {
		mContext = prmCtx.getApplicationContext();
        if (mContext == null) {
            throw new IllegalArgumentException("context not associated with any application");
        }
		HandlerThread handlerThread = new HandlerThread("DeftBleManager");
		handlerThread.start();
		mMangerHandler = new MangerHandler(handlerThread.getLooper());
		mMangerHandler.sendEmptyMessageDelayed(MSG_CHECK_DEFTBLE, CHECK_DEFTBLE_DELAY);
	}

	private void addOneDeftBle(DeftBle prmDeftBle) {
		if (prmDeftBle == null) {
			throw new NullPointerException("can't add null DeftBle");
		}
		synchronized (this) {
			mDeftBles.add(prmDeftBle);
		}
	}

	private void removeOneDeftBle(DeftBle prmDeftBle) {
		synchronized (this) {
			mDeftBles.remove(prmDeftBle);
			prmDeftBle.destory();			
		}
	}

	private DeftBle[] getDeftBleArray() {
		DeftBle contents[] = null;
		synchronized (this) {
			if (!mDeftBles.isEmpty()) {
				contents = new DeftBle[mDeftBles.size()];
				contents = mDeftBles.toArray(contents);
			} else {
				contents = new DeftBle[0];
			}
		}
		return contents;
	}

	private class MangerHandler extends Handler {
		private MangerHandler(Looper looper) {
			super(looper);
		}

		private void checkDeftBle() {
			DeftBle contents[] = getDeftBleArray();
			for (DeftBle item:contents){
				item.checkCallBackException();
			}
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CHECK_DEFTBLE:
				checkDeftBle();
				sendEmptyMessageDelayed(MSG_CHECK_DEFTBLE, CHECK_DEFTBLE_DELAY);
				break;

			default:
				break;
			}
		}
	}

}
