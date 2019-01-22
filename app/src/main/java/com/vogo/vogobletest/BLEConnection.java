package com.vogo.vogobletest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEConnection extends AppCompatActivity {
    String boxNumber;
    String macAddress;
    String blePass;
    String cmdIgnitionOn;
    String cmdIgnitionOff;
    String cmdSeatLockOpen;
    String cmdEndRide;
    TextView tvBox, tvMac, tvStatus;
    Button btnStart,btnEnd,btnStop,btnSeat;
    private ProgressDialog dialog;



    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int mConnectionState = STATE_DISCONNECTED;

    SharedPreferences sharedPreferences;
    BluetoothGattCharacteristic characteristic;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private int REQUEST_ENABLE_BT = 1;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH
    };
    //Button toggle;
    boolean isunlocked = true;
    private String cmdBLE;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private ScanCallback mScanCallback;


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            String intentAction;
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction);
                    Log.i("BLE", "Connected to GATT server.");
                    Log.i("BLE", "Attempting to start service discovery:" +
                            gatt.discoverServices());
                    Log.i("gattCallback", "STATE_CONNECTED");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i("BLE", "Disconnected from GATT server.");
                    broadcastUpdate(intentAction);
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService services = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
            Log.i("onServicesDiscovered", services.toString());
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            characteristic = services.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
            notifyService(true);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", new String(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("onCharacteristic", new String(characteristic.getValue()));
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleconnection);
        Bundle bundle = getIntent().getExtras();
        boxNumber = bundle.getString("mBoxNo");
        macAddress = bundle.getString("mAddress");
        tvBox = (TextView) findViewById(R.id.boxTv);
        tvMac = (TextView) findViewById(R.id.macTv);
        tvStatus = (TextView) findViewById(R.id.statusTv);
        btnStart = (Button)findViewById(R.id.start);
        btnEnd = (Button) findViewById(R.id.end);
        btnStop = (Button) findViewById(R.id.stop);
        btnSeat = (Button) findViewById(R.id.seat);
        dialog = new ProgressDialog(this);
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        tvBox.setText(boxNumber);
        tvMac.setText(macAddress);
        sharedPreferences = getApplicationContext().getSharedPreferences("com.vogo.vogobletest",MODE_PRIVATE);
        blePass = sharedPreferences.getString(Constants.BLE_PASS,Config.DEFAULT_PASS);
        cmdIgnitionOn = sharedPreferences.getString(Constants.IGNITION_ON,Config.DEFAULT_IGNITION_ON);
        cmdIgnitionOff = sharedPreferences.getString(Constants.IGNITION_OFF,Config.DEFAULT_IGNITION_OFF);
        cmdSeatLockOpen = sharedPreferences.getString(Constants.SEAT_OPEN,Config.DEFAULT_SEAT_OPEN);
        cmdEndRide = sharedPreferences.getString(Constants.END_RIDE,Config.DEFAULT_END_RIDE);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BLE","start");
                cmdBLE = cmdIgnitionOn;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();
                scanLeDevice(true);
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdBLE = cmdIgnitionOff;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();
                scanLeDevice(true);
            }
        });

        btnSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            cmdBLE = cmdSeatLockOpen;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();
            scanLeDevice(true);
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdBLE = cmdEndRide;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();
                scanLeDevice(true);
            }
        });


    }

    private void prepareBLE() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_GATT_CONNECTED);
            intentFilter.addAction(ACTION_DATA_AVAILABLE);
            intentFilter.addAction(ACTION_GATT_DISCONNECTED);
            intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
            intentFilter.addAction(EXTRA_DATA);
            intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);

            intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
            registerReceiver(mGattUpdateReceiver, intentFilter);
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
                ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(macAddress).build();
                filters.add(filter);

                mScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        Log.i("callbackType", String.valueOf(callbackType));
                        Log.i("result", result.toString());
                        BluetoothDevice btDevice = result.getDevice();
                        Log.d("device", btDevice.getAddress());
                        if (btDevice.getAddress().equalsIgnoreCase(macAddress)) {
                            connectToDevice(btDevice);
                        }
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        for (ScanResult sr : results) {
                            Log.i("ScanResult - Results", sr.toString());
                        }
                    }
                    @Override
                    public void onScanFailed(int errorCode) {
                        Log.e("Scan Failed", "Error Code: " + errorCode);
                    }
                };
            }
            else{
                // For old version of android.
                mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi,
                                         byte[] scanRecord) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("onLeScan", device.toString());
                                if (device.getAddress().equalsIgnoreCase(macAddress)) {
                                    connectToDevice(device);
                                }
                            }
                        });
                    }
                };
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("result", "On resume");
        prepareBLE();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Toast.makeText(this,"Searching",Toast.LENGTH_SHORT).show();
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (Build.VERSION.SDK_INT < 21) {
//                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    } else {
//                        mLEScanner.stopScan(mScanCallback);
//                    }
//                }
//            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            Toast.makeText(this,"Searching Stopped",Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_LONG).show();
                tvStatus.setText("Connected");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                         // disconnect from BLE
                    }
                },20000);
                //  toggle.setEnabled(true);
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(),"Disconnected",Toast.LENGTH_LONG).show();
                tvStatus.setText("Disconnected");
                deleteBondInformation(mGatt.getDevice());
                close();
                //  toggle.setEnabled(false);
            }  else if (ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(EXTRA_DATA));
            }
            else if(ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                Toast.makeText(getApplicationContext(),"Service Found",Toast.LENGTH_LONG).show();
                Log.d("BLE read",""+send(cmdBLE.getBytes()));
            }
            else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
                Log.d("Pairing Request", "Pairing");
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);


                device.setPin(blePass.getBytes());
                device.setPairingConfirmation(true);
                Log.d("BLE","Setting pin");

            }
        }
    };
    public static void deleteBondInformation(BluetoothDevice device)
    {
        try
        {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        }
        catch (Exception e)
        {
            Log.e("BLE",e.getMessage());
        }
    }
    public void close() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }
    private void displayData(String stringExtra) {

        Log.d("BLE readings", stringExtra
        );
         if(stringExtra.equalsIgnoreCase("401")) {
            Toast.makeText(this,stringExtra + "Success",Toast.LENGTH_LONG).show();
            showDialog("Success");
            Log.d("BLE", "done");
        } else if(stringExtra.equalsIgnoreCase("400")) {
             showDialog("Please Properly Lock Dicky");
         }
         else if(stringExtra.equalsIgnoreCase("411")){
             showDialog("Please Turn off ignition");

         }
         else if(stringExtra.equalsIgnoreCase("410")){
             showDialog("Please Turn off ignition and lock the seat");
         }
    }
    Runnable r = new Runnable() {
        @Override
        public void run() {
            deleteBondInformation(mGatt.getDevice());
            close();
        }
    };
    private void action(String stringExtra, int delayMillis, String s) {
        Toast.makeText(this, stringExtra + " Failed", Toast.LENGTH_LONG).show();
        mHandler.postDelayed(r, delayMillis);  // setting Time out to 30 Sec
        showDialog(s);
    }
    private void showDialog(String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert").setMessage(message).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }
    public boolean send(byte[] data) {
        if (mGatt == null || characteristic == null) {
            Log.w("BLE", "BluetoothGatt not initialized");
            return false;
        }


        if (characteristic == null) {
            Log.w("BLE", "Send characteristic not found");
            return false;
        }

        characteristic.setValue(data);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        return mGatt.writeCharacteristic(characteristic);
    }
    /**
     * Subscribing to BLE services
     * @param yes
     * @return
     */
    public boolean notifyService(boolean yes) {
        if (mGatt == null || characteristic == null) {
            Log.w("BLE", "BluetoothGatt not initialized");
            return false;
        }


        if (characteristic == null) {
            Log.w("BLE", "Send characteristic not found");
            return false;
        }
        Log.d("Charac",String.valueOf(characteristic.getDescriptors().size()));
        for(BluetoothGattDescriptor d:characteristic.getDescriptors()){
            Log.d("BT",d.getUuid().toString());
            d.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            break;
        }
        return mGatt.setCharacteristicNotification(characteristic,yes);
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data));

        }
        sendBroadcast(intent);
    }
}
