package test.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import com.deft.ble.DeftBle;
import com.deft.ble.DeftBleCallback;
import com.deft.ble.DeftBleManager;
import com.deft.ble.op.OpFuture;
import com.deft.ble.op.OpResult;
import com.fc.test.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	static private final String TAG = "MainActivity";
	private boolean mIsBle = true;
	ScheduledExecutorService mExecutor = Executors.newScheduledThreadPool(4);

	private static class BlDevice {
		private DeftBle deftBle;
		private BluetoothDevice device;
		private int rssi;
		private int maxRssi = Integer.MIN_VALUE;
		private int count;
		private String extStr;
		private String commStr;

		public BluetoothDevice getDevice() {
			return device;
		}

		public void setDevice(BluetoothDevice device) {
			this.device = device;
		}

		public int getRssi() {
			return rssi;
		}

		public void setRssi(int rssi) {
			this.rssi = rssi;
			if (rssi > getMaxRssi()) {
				setMaxRssi(rssi);
			}
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public String toString() {
			String name = device.getName() != null ? "(" + device.getName() + ") " : "";
			name = device.getAddress() + name + "  " + rssi + "dBm" + " maxRssi " + maxRssi + "  count " + count;
			return name;
		}

		public int getMaxRssi() {
			return maxRssi;
		}

		public void setMaxRssi(int maxRssi) {
			this.maxRssi = maxRssi;
		}

		public String getExtStr() {
			return extStr;
		}

		public void setExtStr(String extStr) {
			this.extStr = extStr;
		}

		public String getCommStr() {
			return commStr;
		}

		public void setCommStr(String commStr) {
			this.commStr = commStr;
		}

		public DeftBle getDeftBle() {
			return deftBle;
		}

		public void setDeftBle(DeftBle deftBle) {
			this.deftBle = deftBle;
		}
	}

	ListView mListView;
	ArrayAdapter<BlDevice> mAdapter;
	List<BlDevice> mDeviceList = new ArrayList<BlDevice>();
	HashMap<String, BlDevice> mDeviceMap = new HashMap<String, MainActivity.BlDevice>();
	DeftBle mDeftBle;

	DeftBleCallback mBleCallback = new DeftBleCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String str = " " + device.getAddress() + " " + rssi + "dBm";
			Log.i(TAG, "onLeScan： " + str + " " + Utils.toString(scanRecord));
			BlDevice blDevice = mDeviceMap.get(device.getAddress());
			if (blDevice == null) {
				blDevice = new BlDevice();
				blDevice.setDevice(device);
				blDevice.setRssi(rssi);
				mDeviceMap.put(device.getAddress(), blDevice);
				mDeviceList.add(blDevice);
			} else {
				blDevice.setRssi(rssi);
				blDevice.setCount(blDevice.getCount() + 1);
			}
			runOnUiThread(new Runnable() {
				public void run() {
					mAdapter.notifyDataSetChanged();
				}
			});

		}

		@Override
		public void onConnectionStateChange(int status, int newState) {
			toast("onConnectionStateChange" + newState);
			Log.i(TAG, "onConnectionStateChange " + newState);
			if (BluetoothProfile.STATE_CONNECTED == newState) {
			}
		}

		@Override
		public void onServicesDiscovered(int status, List<BluetoothGattService> services) {
			// toast("onServicesDiscovered");
			Log.i(TAG, "onServicesDiscovered: " + status);
		}

		@Override
		public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
			// toast("onCharacteristicRead");
			Log.i(TAG, "onCharacteristicRead");
		}

		@Override
		public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
			// toast("onCharacteristicWrite");
			Log.i(TAG, "onCharacteristicWrite " + status);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
			// toast("onCharacteristicChanged");
			Log.i(TAG, "onCharacteristicChanged:0x" + Utils.toString(characteristic.getValue()) + "  uuid:"
					+ characteristic.getUuid().toString());
		}

		@Override
		public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
			// toast("onDescriptorRead");
			Log.i(TAG, "onDescriptorRead:0x" + Utils.toString(descriptor.getValue()));
		}

		@Override
		public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
			// toast("onDescriptorWrite");
			Log.i(TAG, "onDescriptorWrite:0x" + Utils.toString(descriptor.getValue()));
		}

		@Override
		public void onReadRemoteRssi(int rssi, int status) {
			// toast("onReadRemoteRssi");
			Log.i(TAG, "onReadRemoteRssi rssi: " + rssi + " status" + status);
		}

		@Override
		public void onConnectEnd() {

		}
	};

	private void setExtStr(BlDevice device, String extStr) {
		device.setExtStr(extStr);
		runOnUiThread(new Runnable() {
			public void run() {
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	private void setCommStr(BlDevice device, String commStr) {
		device.setCommStr(commStr);
		runOnUiThread(new Runnable() {
			public void run() {
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	private String getCharacteristicProperties(int properties) {
		String ret = "";
		if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
			ret += getString(R.string.read) + "|";
		}
		if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
			ret += getString(R.string.write_no_resp) + "|";
		}
		if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
			ret += getString(R.string.write) + "|";
		}
		if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
			ret += getString(R.string.notify) + "|";
		}
		if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
			ret += getString(R.string.indicate) + "|";
		}
		if (ret.length() > 0) {
			ret = ret.substring(0, ret.length() - 1);
		}
		return ret;
	}

	final String WECHAT_SEND_CHARACTERISTIC = "0000fec7-0000-1000-8000-00805f9b34fb";// WRITE
																						// 0x08
	final String WECHAT_RECV_CHARACTERISTIC = "0000fec8-0000-1000-8000-00805f9b34fb";// INDICATE
																						// 0x20
	final String WECHAT_READ_CHARACTERISTIC = "0000fec9-0000-1000-8000-00805f9b34fb";// READ
																						// 0x02
	final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"; // INDICATE
																						// descriptor

	private void testBle(DeftBle prmDeftBle, BlDevice device) throws InterruptedException, ExecutionException {
		// mDeftBle.stopScan();
		int i = 0;
		setExtStr(device, getString(R.string.connect_begin));
		setCommStr(device, "");
		while (i++ < 3) {
			if (prmDeftBle.waitFuture(prmDeftBle.connectGatt(device.getDevice()))) {
				Log.i(TAG, "connectGatt succ");
				break;
			}
		}
		if (i >= 3) {
			setExtStr(device, getString(R.string.connect_failed));
			return;
		}
		setExtStr(device, getString(R.string.discover_services));
		OpFuture<List<BluetoothGattService>> servicesFuture = prmDeftBle.discoverServices();
		if (!prmDeftBle.waitFuture(servicesFuture)) {
			setExtStr(device, getString(R.string.discover_services_failed));
		}
		OpResult<List<BluetoothGattService>> servicesResult = servicesFuture.get();
		List<BluetoothGattService> services = servicesResult.getArg();
		StringBuilder strBuilder = new StringBuilder();

		for (BluetoothGattService service : services) {
			List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
			strBuilder.append(service.getUuid() + "\n");
			for (BluetoothGattCharacteristic characteristic : characteristics) {
				strBuilder.append("  " + characteristic.getUuid().toString() + " property:"
						+ getCharacteristicProperties(characteristic.getProperties()) + "\n");
				for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
					strBuilder.append("      " + descriptor.getUuid().toString() + " "
							+ Utils.toString(descriptor.getValue()) + "\n");
				}
			}
			strBuilder.append("\n");
		}
		setExtStr(device, strBuilder.toString());

		UUID serviceUuid = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb");
		UUID recvCharacteristicUuid = UUID.fromString(WECHAT_RECV_CHARACTERISTIC);
		UUID descriptorUuid = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG);
		UUID sendCharacteristicUuid = UUID.fromString(WECHAT_SEND_CHARACTERISTIC);

		prmDeftBle.setCharacteristicNotification(serviceUuid, recvCharacteristicUuid, true);
		OpFuture<BluetoothGattDescriptor> wechatEnable = prmDeftBle.writeDescriptor(serviceUuid, recvCharacteristicUuid,
				descriptorUuid, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		if (prmDeftBle.waitFuture(wechatEnable)) {
			setCommStr(device, getString(R.string.support_wechat));
		} else {
			setCommStr(device, getString(R.string.no_support_wechat));
		}

		byte pack[] = { (byte) 0xFE, 0x01, 0x00, 0x3C, 0x27, 0x11, 0x00, 0x01, 0x0A, 0x00, 0x12, 0x10, 0x37, 0x60,
				(byte) 0x8C, 0x00, 0x79, 0x22, (byte) 0xBA, 0x35, (byte) 0x80, 0x21, 0x53, 0x22, (byte) 0x91, 0x6D,
				0x35, 0x45, 0x18, (byte) 0x84, (byte) 0x80, 0x04, 0x20, 0x01, 0x28, 0x01, 0x62, 0x16, 0x57, 0x65, 0x43,
				0x68, 0x61, 0x74, 0x42, 0x6C, 0x75, 0x65, 0x74, 0x6F, 0x6F, 0x74, 0x68, 0x44, 0x65, 0x76, 0x69, 0x63,
				0x65, 0x00 };
		for (i = 0; i < pack.length; i += 20) {
			int len = 20;
			if (pack.length - i < len) {
				len = pack.length - i;
			}
			byte sendV[] = new byte[len];
			System.arraycopy(pack, i, sendV, 0, len);
			for (int k = 0; k < 10000; k++) {
				setCommStr(device, getString(R.string.wechat_send) + k);
				if (!prmDeftBle
						.waitFuture(prmDeftBle.writeCharacteristic(serviceUuid, sendCharacteristicUuid, sendV))) {
					setCommStr(device, getString(R.string.wechat_send_failed));
					return;
				}
			}
		}
		setCommStr(device, getString(R.string.wechat_send_succ));
		prmDeftBle.readRemoteRssi();
		Log.i(TAG, "gatt disconnet " + prmDeftBle.waitFuture(prmDeftBle.disconnectGatt()));

	}

	void testSSP(BlDevice device) {
		BluetoothSocket bleSocket = null;
		try {
			setExtStr(device, "connect ssp");
			bleSocket = device.getDevice()
					.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			bleSocket.connect();
			final InputStream input = bleSocket.getInputStream();
			final OutputStream out = bleSocket.getOutputStream();
			// byte buffer[] = new byte[200];
			// int len;
			// try {
			// do {
			// len = input.read(buffer);
			// Log.d(TAG, "read: "+len+" "+ Utils.toString(buffer, 0,
			// len));
			// } while (len > 0);
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			int n = 0;
			while (++n < 10) {
				out.write(("deft spp test " + n).getBytes());
				Thread.sleep(1000);
				setExtStr(device, "write to spp" + n);
			}
			setExtStr(device, "end");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setExtStr(device, "IOException");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setExtStr(device, "InterruptedException");
		}
	}

	private void testBalance() {
		DeftBle deftBle = DeftBleManager.getOneDeftBle(mBleCallback);
		try {
			UUID serviceFat = UUID.fromString("0000ffcc-0000-1000-8000-00805f9b34fb");
			UUID notifyFatCharacteristic = UUID.fromString("0000ffc3-0000-1000-8000-00805f9b34fb");
			UUID readFatCharacteristic = UUID.fromString("0000ffc2-0000-1000-8000-00805f9b34fb");
			UUID writeFatCharacteristic = UUID.fromString("0000ffc1-0000-1000-8000-00805f9b34fb");
			UUID descriptorUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
			int i = 0;
			while (i++ < 3) {
				if (deftBle.waitFuture(deftBle.connectGatt(deftBle.getRemoteDevice("18:7A:93:3D:C9:C3")))) {
					Log.i(TAG, "connectGatt succ");
					break;
				}
			}
			if (i>=3){
				return;
			}
			deftBle.waitFutureExcept(deftBle.discoverServices());
//			byte arrayOfByte3[] = new byte[3];
//			arrayOfByte3[0] = -126;
//			arrayOfByte3[1] = 1;
//			arrayOfByte3[2] = 0;
//			deftBle.waitFutureExcept(deftBle.writeCharacteristic(serviceFat,
//					writeFatCharacteristic, arrayOfByte3, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE));
			deftBle.waitFutureExcept(deftBle.setCharacteristicNotification(serviceFat, notifyFatCharacteristic, true));
			deftBle.waitFutureExcept(deftBle.writeDescriptor(serviceFat, notifyFatCharacteristic,
					descriptorUuid, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE));
			
//			deftBle.waitFutureExcept(deftBle.setCharacteristicNotification(serviceFat, readFatCharacteristic, true));
//			deftBle.waitFutureExcept(deftBle.writeDescriptor(serviceFat, readFatCharacteristic,
//					descriptorUuid, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE));
//			
//			deftBle.waitFutureExcept(deftBle.setCharacteristicNotification(serviceFat, writeFatCharacteristic, true));
//			deftBle.waitFutureExcept(deftBle.writeDescriptor(serviceFat, writeFatCharacteristic,
//					descriptorUuid, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE));
			
//			cmdWriteFuture = deftBle.writeCharacteristic(serviceFat,
//					writeFatCharacteristic,  new byte[]{75}, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//			deftBle.waitFutureExcept(cmdWriteFuture);

			Log.i(TAG, "balance is ok");
			Thread.sleep(20*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DeftBleManager.releaseOneDeftBle(deftBle);
		}

	}

	final OnItemClickListener mItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final BlDevice device = mDeviceList.get(position);
			Object object = view.getTag();
			if (object != null && object instanceof Future<?>) {
				Future<?> future = (Future<?>) object;
				future.cancel(true);
				DeftBleManager.releaseOneDeftBle(device.getDeftBle());
				device.setDeftBle(null);
				view.setTag(null);
				setExtStr(device, "");
				setCommStr(device, "");
			} else {
				Future<?> future = mExecutor.submit(new Runnable() {
					public void run() {
						try {
							if (device.getCount() < 0) {
								MainActivity.this.testSSP(device);
							} else {
								DeftBle deftBle = DeftBleManager.getOneDeftBle(mBleCallback);
								device.setDeftBle(deftBle);
								MainActivity.this.testBle(deftBle, device);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						} catch (Throwable e) {
							e.printStackTrace();
						} finally {
						}
					}
				});
				view.setTag(future);
			}

		}
	};

	void toast(final String prmStr) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(MainActivity.this, prmStr, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		setContentView(R.layout.activity_main);
		mListView = (ListView) findViewById(R.id.list_view);

		mAdapter = new UserAdapter(this, R.layout.list_line, R.id.title, mDeviceList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mItemClickListener);

		DeftBleManager.init(this);
		mDeftBle = DeftBleManager.getOneDeftBle(mBleCallback);

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		DeftBleManager.releaseOneDeftBle(mDeftBle);
		mDeftBle = null;
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			Log.i(TAG, "ACTION_FOUND： " + action);
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
				Log.i(TAG, "ACTION_FOUND： " + device.getName() + "     " + device.getAddress() + "rssi " + rssi);
				BlDevice blDevice = mDeviceMap.get(device.getAddress());
				if (blDevice == null) {
					blDevice = new BlDevice();
					blDevice.setDevice(device);
					blDevice.setRssi(rssi);
					blDevice.setCount(-1);
					mDeviceMap.put(device.getAddress(), blDevice);
					mDeviceList.add(blDevice);
				} else {
					blDevice.setRssi(rssi);
					blDevice.setCount(-1);
				}

				mAdapter.notifyDataSetChanged();
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_bluetooth_on:
			if (!mDeftBle.getBluetoothAdapter().enable()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 0);
			} else {
				toast("bluetooth on");
			}
			break;
		case R.id.action_bluetooth_off:
			if (mDeftBle.getBluetoothAdapter().isEnabled()) {
				mDeftBle.getBluetoothAdapter().disable();
				toast("bluetooth off");
			}
			break;
		case R.id.action_change_ble:
			mIsBle = !mIsBle;
			toast(mIsBle ? "is ble" : "normal bluetooth");
			break;
		case R.id.action_beginscan:
			if (mIsBle) {
				OpFuture<Boolean> future = mDeftBle.startScan();
			} else {
				mDeftBle.getBluetoothAdapter().startDiscovery();
			}
			toast("begin scan");
			break;
		case R.id.action_endscan:
			if (mIsBle) {
				mDeftBle.stopScan();
			} else {
				mDeftBle.getBluetoothAdapter().cancelDiscovery();
			}
			toast("end scan");
			break;
		case R.id.action_clear_all:
			mDeviceList.clear();
			mDeviceMap.clear();
			mAdapter.notifyDataSetChanged();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	class UserAdapter extends ArrayAdapter<BlDevice> {
		public UserAdapter(Context context, int resourceId, int textViewResourceId, List<BlDevice> users) {
			super(context, resourceId, textViewResourceId, users);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View view = super.getDropDownView(position, convertView, parent);
			String extStr = getItem(position).getExtStr();
			TextView textView = (TextView) view.findViewById(R.id.service);
			textView.setText(extStr != null ? extStr : "");
			return view;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getDropDownView(position, convertView, parent);
			String extStr = getItem(position).getExtStr();
			TextView textView = (TextView) view.findViewById(R.id.service);
			textView.setText(extStr != null ? extStr : "");

			String commStr = getItem(position).getCommStr();
			textView = (TextView) view.findViewById(R.id.wechat);
			textView.setText(commStr != null ? commStr : "");
			return view;
		}
	}

	// You should register BroadcastReceiver for
	// android.bluetooth.device.action.PAIRING_REQUEST
	// BluetoothDevice.ACTION_PAIRING_REQUEST
	// Call createBond()
	// Wait for BroadcastReceiver to trigger
	// In BroadcastReceiver if action is
	// android.bluetooth.device.action.PAIRING_REQUEST call this method
	// public void setBluetoothPairingPin(BluetoothDevice device)
	// {
	// byte[] pinBytes = convertPinToBytes("0000");
	// try {
	// Log.d(TAG, "Try to set the PIN");
	// Method m = device.getClass().getMethod("setPin", byte[].class);
	// m.invoke(device, pinBytes);
	// Log.d(TAG, "Success to add the PIN.");
	// try {
	// device.getClass().getMethod("setPairingConfirmation",
	// boolean.class).invoke(device, true);
	// Log.d(TAG, "Success to setPairingConfirmation.");
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// Log.e(TAG, e.getMessage());
	// e.printStackTrace();
	// }
	// } catch (Exception e) {
	// Log.e(TAG, e.getMessage());
	// e.printStackTrace();
	// }
	// }
	// IntentFilter filter2 = new IntentFilter(
	// "android.bluetooth.device.action.PAIRING_REQUEST");
	// mActivity.registerReceiver(
	// pairingRequest, filter2);
	//
	// private final BroadcastReceiver pairingRequest = new BroadcastReceiver()
	// {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	//
	// if
	// (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST"))
	// {
	// mBluetoothDevice = needed;
	// try {
	// byte[] pin = (byte[])
	// BluetoothDevice.class.getMethod("convertPinToBytes",
	// String.class).invoke(BluetoothDevice.class, "1234");
	// Method m = mBluetoothDevice.getClass().getMethod("setPin", byte[].class);
	// m.invoke(mBluetoothDevice, pin);
	// mBluetoothDevice.getClass().getMethod("setPairingConfirmation",
	// boolean.class).invoke(mBluetoothDevice, true);
	// }
	// catch(Exception e)
	// {
	//
	// e.printStackTrace();
	//
	// }
}
