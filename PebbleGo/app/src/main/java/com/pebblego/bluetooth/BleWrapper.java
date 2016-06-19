package com.pebblego.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;

import java.io.File;
import java.util.List;

public class BleWrapper {
	/* defines (in milliseconds) how often RSSI should be updated */
	private static final int RSSI_UPDATE_TIME_INTERVAL = 1500; // 1.5 seconds

	/* callback object through which we are returning results to the caller */
	private BleWrapperUiCallbacks mUiCallback = null;

	/* define NULL object for UI callbacks */
	private static final BleWrapperUiCallbacks NULL_CALLBACK = new BleWrapperUiCallbacks.Null();

	/* creates BleWrapper object, set its parent activity and callback object */
	public BleWrapper(Context parent, BleWrapperUiCallbacks callback) {
		this.mParent = parent;
		mUiCallback = callback;
		if (mUiCallback == null)
			mUiCallback = NULL_CALLBACK;
	}

	public BluetoothManager getManager() {
		return mBluetoothManager;
	}

	public BluetoothAdapter getAdapter() {
		return mBluetoothAdapter;
	}

	public BluetoothDevice getDevice() {
		return mBluetoothDevice;
	}

	public BluetoothGatt getGatt() {
		return mBluetoothGatt;
	}

	public BluetoothGattService getCachedService() {
		return mBluetoothSelectedService;
	}

	public List<BluetoothGattService> getCachedServices() {
		return mBluetoothGattServices;
	}

	public boolean isConnected() {
		return mConnected;
	}

	/* run test and check if this device has BT and BLE hardware available */
	public boolean checkBleHardwareAvailable() {
		// First check general Bluetooth Hardware:
		// get BluetoothManager...
		final BluetoothManager manager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
		if (manager == null)
			return false;
		// .. and then get adapter from manager
		final BluetoothAdapter adapter = manager.getAdapter();
		if (adapter == null)
			return false;

		// and then check if BT LE is also available
		boolean hasBle = mParent.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
		return hasBle;
	}

	/*
	 * before any action check if BT is turned ON and enabled for us call this
	 * in onResume to be always sure that BT is ON when Your application is put
	 * into the foreground
	 */
	public boolean isBtEnabled() {
		final BluetoothManager manager = (BluetoothManager) mParent.getSystemService(Context.BLUETOOTH_SERVICE);
		if (manager == null)
			return false;

		final BluetoothAdapter adapter = manager.getAdapter();
		if (adapter == null)
			return false;

		return adapter.isEnabled();
	}

	/* start scanning for BT LE devices around */
	public void startScanning() {
		IntentFilter filter = new IntentFilter();

		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		mParent.registerReceiver(mReceiver, filter);
		mBluetoothAdapter.startDiscovery();
	}

	/* stops current scanning */
	public void stopScanning() {
		mBluetoothAdapter.stopLeScan(mDeviceFoundCallback);
	}

	private static Context appContext = null;

	/* initialize BLE and get BT Manager & Adapter */
	public boolean initialize(Context appContext) {
		if (appContext == null) {
			throw new IllegalArgumentException("Cannot initialize with a null appContext");
		}
		BleWrapper.appContext = appContext;

		if (!checkBleHardwareAvailable() || !isBtEnabled()) {
			return false;
		}

		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) BleWrapper.appContext.getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				return false;
			}
		}

		if (mBluetoothAdapter == null)
			mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			return false;
		}

		File dir = BleWrapper.appContext.getExternalFilesDir(null);
		String path = BleWrapper.appContext.getFilesDir().getAbsolutePath();
		if (dir != null) {
			path = dir.getAbsolutePath();
		}


		return true;
	}

	/* connect to the device with specified address */
	public boolean connect(final String deviceAddress) {
		if (mBluetoothAdapter == null || deviceAddress == null)
			return false;
		mDeviceAddress = deviceAddress;

		// check if we need to connect from scratch or just reconnect to
		// previous device
		if (mBluetoothGatt != null && mBluetoothGatt.getDevice().getAddress().equals(deviceAddress)) {
			// just reconnect
			return mBluetoothGatt.connect();
		} else {
			// connect from scratch
			// get BluetoothDevice object for specified address
			mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
			if (mBluetoothDevice == null) {
				// we got wrong address - that device is not available!
				return false;
			}
			// connect with remote device
			mBluetoothGatt = mBluetoothDevice.connectGatt(mParent, false, mBleCallback);
		}
		return true;
	}

	/*
	 * disconnect the device. It is still possible to reconnect to it later with
	 * this Gatt client
	 */
	public void diconnect() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
		mUiCallback.uiDeviceDisconnected(mBluetoothGatt, mBluetoothDevice, mConnected);
		mConnected = false;
	}

	/* close GATT client completely */
	public void close() {
		if (mBluetoothGatt != null)
			mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/* request new RSSi value for the connection */
	public void readPeriodicalyRssiValue(final boolean repeat) {
		mTimerEnabled = repeat;
		// check if we should stop checking RSSI value
		if (mConnected == false || mBluetoothGatt == null || mTimerEnabled == false) {
			mTimerEnabled = false;
			return;
		}

		mTimerHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mBluetoothGatt == null || mBluetoothAdapter == null || mConnected == false) {
					mTimerEnabled = false;
					return;
				}

				// request RSSI value
				mBluetoothGatt.readRemoteRssi();
				// add call it once more in the future
				readPeriodicalyRssiValue(mTimerEnabled);
			}
		}, RSSI_UPDATE_TIME_INTERVAL);
	}

	/* starts monitoring RSSI value */
	public void startMonitoringRssiValue() {
		readPeriodicalyRssiValue(true);
	}

	/* stops monitoring of RSSI value */
	public void stopMonitoringRssiValue() {
		readPeriodicalyRssiValue(false);
	}

	/*
	 * request to discover all services available on the remote devices results
	 * are delivered through callback object
	 */
	public void startServicesDiscovery() {
		if (mBluetoothGatt != null)
			mBluetoothGatt.discoverServices();
	}

	/*
	 * gets services and calls UI callback to handle them before calling
	 * getServices() make sure service discovery is finished!
	 */
	public void getSupportedServices() {
		if (mBluetoothGattServices != null && mBluetoothGattServices.size() > 0)
			mBluetoothGattServices.clear();
		// keep reference to all services in local array:
		if (mBluetoothGatt != null)
			mBluetoothGattServices = mBluetoothGatt.getServices();
	}

	/*
	 * get all characteristic for particular service and pass them to the UI
	 * callback
	 */
	public void getCharacteristicsForService(final BluetoothGattService service) {
		if (service == null)
			return;
		List<BluetoothGattCharacteristic> chars = null;

		chars = service.getCharacteristics();
		mUiCallback.uiCharacteristicForService(mBluetoothGatt, mBluetoothDevice, service, chars);
		// keep reference to the last selected service
		mBluetoothSelectedService = service;
	}

	/* defines callback for scanning results */
	private BluetoothAdapter.LeScanCallback mDeviceFoundCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
				if(device.getName().contains("Pebble")){
				}
		}
	};

	/* callbacks called for any action on particular Ble Device */
	private final BluetoothGattCallback mBleCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mConnected = true;
				mUiCallback.uiDeviceConnected(mBluetoothGatt, mBluetoothDevice);

				// now we can start talking with the device, e.g.
				mBluetoothGatt.readRemoteRssi();
				// response will be delivered to callback object!

				// in our case we would also like automatically to call for
				// services discovery
				startServicesDiscovery();

				// and we also want to get RSSI value to be updated periodically
				startMonitoringRssiValue();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				mConnected = false;
				mUiCallback.uiDeviceDisconnected(mBluetoothGatt, mBluetoothDevice, mConnected);

			}

		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {}
		}

		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descr, int status) {
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		};

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// we got new value of RSSI of the connection, pass it to the UI
				mUiCallback.uiNewRssiAvailable(mBluetoothGatt, mBluetoothDevice, rssi);
			}
		};

	};


	public enum ConnectionState {
		DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING;
	}

	private Context mParent = null;
	private boolean mConnected = false;
	private String mDeviceAddress = "";

	private BluetoothManager mBluetoothManager = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothDevice mBluetoothDevice = null;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothGattService mBluetoothSelectedService = null;
	private List<BluetoothGattService> mBluetoothGattServices = null;


	private Handler mTimerHandler = new Handler();
	private boolean mTimerEnabled = false;
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				//discovery starts, we can show progress dialog or perform other tasks
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				//discovery finishes, dismis progress dialog
				mUiCallback.stopedScan();
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				//bluetooth device found
				BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getName().contains("Pebble")) {
					mUiCallback.uiDeviceFound(device);
				}
			}
		}
	};
}
