package com.example.bluetoothcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeviceScanActivity extends AppCompatActivity {
    private static final String TAG = DeviceScanActivity.class.getSimpleName();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothGatt bluetoothGatt;
    private boolean mScanning;
    private Handler handler;
    private TextView bleTopText;
    private ArrayAdapter<String> leAdapter;
    private boolean mCharacteristicWritten = true;
    private BluetoothGattCharacteristic customCharacteristic;


    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);
        this.handler = new Handler();
        this.bleTopText = findViewById(R.id.bleTopText);

        //Do not go to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothManager = bluetoothManager;
        bluetoothAdapter = bluetoothManager.getAdapter();

        int REQUEST_ENABLE_BT = 3;

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        this.leAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(leAdapter);

        scanLeDevice(true);

        initializeButtons();
    }

    private void initializeButtons() {
        Button buttonUp = (Button) findViewById(R.id.moveForward);
        buttonUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sendMoveForwardSignal(buttonUp);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    sendStopMoveForwardSignal(buttonUp);
                }
                return true;
            }
        });
        Button buttonDown = (Button) findViewById(R.id.moveBackward);
        buttonDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sendMoveBackwardSignal(buttonDown);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    sendStopMoveBackwardSignal(buttonDown);
                }
                return true;

            }
        });
        Button buttonLeft = (Button) findViewById(R.id.moveLeft);
        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sendMoveLeftSignal(buttonLeft);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    sendStopMoveLeftSignal(buttonLeft);
                }
                return true;
            }
        });
        Button buttonRight = (Button) findViewById(R.id.moveRight);
        buttonRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sendMoveRightSignal(buttonRight);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    sendStopMoveRightSignal(buttonRight);
                }
                return true;

            }
        });

    }

    private DeviceScanActivity getActivity() {
        return this;
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
//            leAdapter.clear();
//            if (leAdapter.getItem())
//            if (result.getDevice().getName().contains("BT-BLACK-RAM")) {
//                leAdapter.clear();
            if (result.getDevice() != null) {
                if (result.getDevice().getName() != null) {
                    if (result.getDevice().getName().contains("BLACK-RAM")) {
                        leAdapter.add(result.getDevice().getName());
                        leAdapter.notifyDataSetChanged();
                        ListView listView = (ListView) findViewById(R.id.listview);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Snackbar.make(view, "Connecting to " + result.getDevice().getName(), Snackbar.LENGTH_LONG).setAction("No action", null).show();
                                if (bluetoothGatt == null) {
                                    bluetoothGatt = result.getDevice().connectGatt(getActivity(), false, gattCallback);
                                }
                            }
                        });
                    }
                }
            }

//            }


            bleTopText.setText("Scan done; result:");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);


            List<String> resultsStrings = results.stream()
                                                 .map((result) -> result.getDevice().toString())
                                                 .collect(Collectors.toList());
            leAdapter.clear();
            leAdapter.addAll(resultsStrings);
            leAdapter.notifyDataSetChanged();

            ListView listView = (ListView) findViewById(R.id.listview);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Snackbar.make(view, "Connecting to " + results.get(position).getDevice().getName(), Snackbar.LENGTH_LONG).setAction("No action", null).show();
                    if (bluetoothGatt == null) {
                        bluetoothGatt = results.get(position).getDevice().connectGatt(getActivity(), false, gattCallback);
                    }
                }
            });

            bleTopText.setText("Scan done; batch results:");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            leAdapter.clear();
            leAdapter.notifyDataSetChanged();

            bleTopText.setText("Scan has failed!");
        }
    };

    private void scanLeDevice(final boolean enable) {
        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    //        leAdapter.clear();
    //        leAdapter.notifyDataSetChanged();
        if (enable) {
            bleTopText.setText("Scanning...");
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    bleTopText.setText("Scan stopped");
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Snackbar.make(findViewById(android.R.id.content), "Connected to " + gatt.getDevice().getName(), Snackbar.LENGTH_LONG).setAction("No action", null).show();
//                        intentAction = ACTION_GATT_CONNECTED;
//                        connectionState = STATE_CONNECTED;
//                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                bluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                        intentAction = ACTION_GATT_DISCONNECTED;
//                        connectionState = STATE_DISCONNECTED;
                        Snackbar.make(findViewById(android.R.id.content), "Disconnected from " + gatt.getDevice().getName(), Snackbar.LENGTH_LONG).setAction("No action", null).show();

                        Log.i(TAG, "Disconnected from GATT server.");
//                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status){
                    //TODO: Get these out in a const
                    Snackbar.make(findViewById(android.R.id.content), "Discovered services" , Snackbar.LENGTH_LONG).setAction("No action", null).show();

                    customCharacteristic =
                            gatt.getService(UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB"))
                                    .getCharacteristic(UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB"));
                    if (customCharacteristic == null) {
                        Snackbar.make(findViewById(android.R.id.content), "Characteristic not found" , Snackbar.LENGTH_LONG).setAction("No action", null).show();
                        return;
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        mCharacteristicWritten = true;
                    }
                }

            };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSettings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.actionDisconnect:
                close();

                return true;

            case R.id.actionScan:
                scanLeDevice(true);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ble_menu, menu);
        return true;
    }

    public void close() {
        if (bluetoothGatt == null) {
            Snackbar.make(findViewById(android.R.id.content), "Nothing connected" , Snackbar.LENGTH_LONG).setAction("No action", null).show();
            return;
        }
        Snackbar.make(findViewById(android.R.id.content), "Disconnecting from " + bluetoothGatt.getDevice().getName(), Snackbar.LENGTH_LONG).setAction("No action", null).show();

        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
        Snackbar.make(findViewById(android.R.id.content), "Disconnected" , Snackbar.LENGTH_LONG).setAction("No action", null).show();
    }

    private void sendCharacteristic(String toSend) {
        if (bluetoothGatt == null) {
            Snackbar.make(findViewById(android.R.id.content), "Nothing connected" , Snackbar.LENGTH_LONG).setAction("No action", null).show();
            return;
        }

        if (mCharacteristicWritten) {
            if (customCharacteristic == null) {
                Snackbar.make(findViewById(android.R.id.content), "Characteristic not found" , Snackbar.LENGTH_LONG).setAction("No action", null).show();
                return;
            }

            customCharacteristic.setValue(toSend);
            mCharacteristicWritten = false;
            bluetoothGatt.writeCharacteristic(customCharacteristic);
        }
    }

    public void sendWeaponOnSignal(View view) {
        sendCharacteristic("8");
    }
    public void sendWeaponOffSignal(View view) {
        sendCharacteristic("9");
    }

    public void sendMoveForwardSignal(View view) { sendCharacteristic("0"); }
    public void sendStopMoveForwardSignal(View view) { sendCharacteristic("1"); }

    public void sendMoveLeftSignal(View view) { sendCharacteristic("2"); }
    public void sendStopMoveLeftSignal(View view) { sendCharacteristic("3"); }

    public void sendMoveBackwardSignal(View view) { sendCharacteristic("4"); }
    public void sendStopMoveBackwardSignal(View view) { sendCharacteristic("5"); }

    public void sendMoveRightSignal(View view) { sendCharacteristic("6"); }
    public void sendStopMoveRightSignal(View view) {sendCharacteristic("7");}

//    private class LeDeviceListAdapter extends ArrayAdapter<String> {

//    }
/*
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (RecyclerView.ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }*/
}
