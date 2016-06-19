package com.pebblego;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.pebblego.bluetooth.BleWrapper;
import com.pebblego.bluetooth.BleWrapperUiCallbacks;

import java.util.ArrayList;

/**
 * Created by Arjun.
 */
public class PebbleConnectActivity extends BaseActivity {
    ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private ListView listView;
    private PebbleAdapter listAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private int ENABLE_BT_REQUEST_ID = 537;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }

        if (mBluetoothAdapter == null)
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
        }
        registerReciever();
        listView = (ListView) findViewById(R.id.listview);
        listAdapter = new PebbleAdapter();
        listView.setAdapter(listAdapter);
        initializeBlueTooth();
        PebbleKit.registerPebbleConnectedReceiver(PebbleConnectActivity.this,pebbleReciever);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO
                showProgressDialogue(true);
                devices.get(i).connectGatt(PebbleConnectActivity.this, true, gattCallBack);
            }
        });
        showProgressDialogue(true);
        startScanning();
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialogue(true);
                startScanning();
            }
        });
    }

    BroadcastReceiver pebbleReciever= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("device",""+(intent!=null));
        }
    };

    protected BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("device",""+newState+" sta"+status);
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    private void registerReciever() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }

    private void startScanning() {

        mBluetoothAdapter.startDiscovery();
        // initialize BleWrapper object
        devices.clear();
    }

    private void stopScanning() {
        mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BT_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {
                startScanning();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Please Turn on bluetooth", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initializeBlueTooth() {

        // on every Resume check if BT is enabled (user could turn it off while
        // app was in background etc.)
        if (!isBtEnabled()) {
            // BT is not turned on - ask user to make it enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
            // see onActivityResult to check what is the status of our request
        } else {
            startScanning();
        }
    }

    public boolean isBtEnabled() {
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null)
            return false;

        final BluetoothAdapter adapter = manager.getAdapter();
        if (adapter == null)
            return false;

        return adapter.isEnabled();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                dismissProgressDialogue();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device!=null && device.getName().contains("Pebble")) {
                    Log.i("deviceUUID", device.getName()+"   and ");
                    if(device.getUuids()!=null){
                        Log.i("deviceUUID", device.getName() + "   and " + device.getUuids().toString());
                    }
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);


                    if(device != null && uuidExtra != null)
                        Log.i("deviceUUID", device.getName() + "   and " + uuidExtra.toString());
                        devices.add(device);
                        listAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    class PebbleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (devices != null) {
                return devices.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                view = PebbleConnectActivity.this.getLayoutInflater().inflate(R.layout.device_list_item, viewGroup, false);
                holder = new ViewHolder();
                holder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
                if (holder == null) {
                    holder = new ViewHolder();
                }
            }
            if (devices != null) {
                BluetoothDevice device = devices.get(i);
                holder.deviceName.setText(device.getName());
            }
            return view;
        }

        class ViewHolder {
            TextView deviceName;
        }
    }
}
