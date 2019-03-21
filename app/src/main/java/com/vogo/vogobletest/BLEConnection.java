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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
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
    int millis = 100; //default
TextView tvReceived;
TextView tvDiscovered;
    int retry = 1,retryCount = 0;
    boolean isRetry = true;

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
            Utils.sendLog(new LogInfo("On Bluetooth Connection Change ::::: Status: " + status, new Date(System.currentTimeMillis())),Utils.EVENTS);
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
                    Utils.sendLog(new LogInfo("Connected to GATT server", new Date(System.currentTimeMillis())),Utils.EVENTS);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i("BLE", "Disconnected from GATT server.");
                    Utils.sendLog(new LogInfo("Disconnected from GATT server.", new Date(System.currentTimeMillis())),Utils.EVENTS);
                    broadcastUpdate(intentAction);
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
                    Utils.sendLog(new LogInfo("Other Connection State", new Date(System.currentTimeMillis())),Utils.EVENTS);
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService services = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
            Log.i("onServicesDiscovered", services.toString());
            Utils.sendLog(new LogInfo("BLE Service Discovered.", new Date(System.currentTimeMillis())),Utils.EVENTS);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            characteristic = services.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
            notifyService(true);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Utils.sendLog(new LogInfo("Service Connection Success", new Date(System.currentTimeMillis())),Utils.SUCCESS);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", new String(characteristic.getValue()));
            Utils.sendLog(new LogInfo("Red value from BLE", new Date(System.currentTimeMillis())),Utils.EVENTS);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("onCharacteristic", new String(characteristic.getValue()));
            Utils.sendLog(new LogInfo("Red value from BLE", new Date(System.currentTimeMillis())),Utils.EVENTS);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleconnection);
        Utils.sendLog(new LogInfo("BLE Connection Activity Started", new Date(System.currentTimeMillis())),Utils.EVENTS);
        Bundle bundle = getIntent().getExtras();
        boxNumber = bundle.getString("mBoxNo");
        macAddress = bundle.getString("mAddress");
        Utils.sendLog(new LogInfo("Mac Address of BLE: "+macAddress, new Date(System.currentTimeMillis())),Utils.EVENTS);
        Log.d("check mac",macAddress);
        tvBox = (TextView) findViewById(R.id.boxTv);
        tvMac = (TextView) findViewById(R.id.macTv);
        tvStatus = (TextView) findViewById(R.id.statusTv);
        btnStart = (Button)findViewById(R.id.start);
        btnEnd = (Button) findViewById(R.id.end);
        btnStop = (Button) findViewById(R.id.stop);
        btnSeat = (Button) findViewById(R.id.seat);
        tvReceived = (TextView) findViewById(R.id.tvReceived);
        tvDiscovered = (TextView) findViewById(R.id.tvDiscovered);
        dialog = new ProgressDialog(this);
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.sendLog(new LogInfo("BLE Not Supported", new Date(System.currentTimeMillis())),Utils.ERRORS);
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();

            finish();
        }
        Utils.sendLog(new LogInfo("BLE Supported", new Date(System.currentTimeMillis())),Utils.SUCCESS);
        tvBox.setText(boxNumber);
        tvMac.setText(macAddress);
        sharedPreferences = getApplicationContext().getSharedPreferences("com.vogo.vogobletest",MODE_PRIVATE);
        blePass = sharedPreferences.getString(Constants.BLE_PASS,Config.DEFAULT_PASS);
        cmdIgnitionOn = sharedPreferences.getString(Constants.IGNITION_ON,Config.DEFAULT_IGNITION_ON);
        cmdIgnitionOff = sharedPreferences.getString(Constants.IGNITION_OFF,Config.DEFAULT_IGNITION_OFF);
        cmdSeatLockOpen = sharedPreferences.getString(Constants.SEAT_OPEN,Config.DEFAULT_SEAT_OPEN);
        cmdEndRide = sharedPreferences.getString(Constants.END_RIDE,Config.DEFAULT_END_RIDE);
        retry = sharedPreferences.getInt(Settings.RETRY,1);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.sendLog(new LogInfo("Start Button Clicked", new Date(System.currentTimeMillis())),Utils.EVENTS);
                Log.d("BLE","start");
                cmdBLE = cmdIgnitionOn;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();

                isRetry = true;
                retryCount = 0;
                scanLeDevice(true);
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.sendLog(new LogInfo("Stop Button Clicked", new Date(System.currentTimeMillis())),Utils.SUCCESS);
                cmdBLE = cmdIgnitionOff;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();

                isRetry = true;
                retryCount = 0;
                scanLeDevice(true);
            }
        });

        btnSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.sendLog(new LogInfo("Seat Button Clicked", new Date(System.currentTimeMillis())),Utils.SUCCESS);
            cmdBLE = cmdSeatLockOpen;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();

                isRetry = true;
                retryCount = 0;
            scanLeDevice(true);
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.sendLog(new LogInfo("End Button Clicked", new Date(System.currentTimeMillis())),Utils.SUCCESS);
                cmdBLE = cmdEndRide;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();

                isRetry = true;
                retryCount = 0;
                scanLeDevice(true);
            }
        });


    }

    private void prepareBLE() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Utils.sendLog(new LogInfo("Bluetooth Disabled", new Date(System.currentTimeMillis())),Utils.ERRORS);
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
                            tvDiscovered.setText("Device Discovered:"+ result.getRssi());
                            Utils.sendLog(new LogInfo("Device Discovered", new Date(System.currentTimeMillis())),Utils.SUCCESS);
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
                        Utils.sendLog(new LogInfo("Scan Failed", new Date(System.currentTimeMillis())),Utils.ERRORS);
                        Log.e("Scan Failed", "Error Code: " + errorCode);
                    }
                };
            }
            else{
                // For old version of android.
                Utils.sendLog(new LogInfo("Older Version of Android", new Date(System.currentTimeMillis())),Utils.EVENTS);
                mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, final int rssi,
                                         byte[] scanRecord) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("onLeScan", device.toString());
                                Utils.sendLog(new LogInfo("Scanning", new Date(System.currentTimeMillis())),Utils.EVENTS);
                                if (device.getAddress().equalsIgnoreCase(macAddress)) {
                                    tvDiscovered.setText("Device Discovered:"+ rssi);
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
        Utils.sendLog(new LogInfo("APP Resume", new Date(System.currentTimeMillis())),Utils.EVENTS);
        prepareBLE();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.sendLog(new LogInfo("APP Pause", new Date(System.currentTimeMillis())),Utils.EVENTS);
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.sendLog(new LogInfo("APP Destroy", new Date(System.currentTimeMillis())),Utils.EVENTS);
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
                Utils.sendLog(new LogInfo("BLUETOOTH NOT ENABLED", new Date(System.currentTimeMillis())),Utils.ERRORS);
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Toast.makeText(this,"Searching",Toast.LENGTH_SHORT).show();

            Utils.sendLog(new LogInfo("Searching", new Date(System.currentTimeMillis())),Utils.EVENTS);
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
            Utils.sendLog(new LogInfo("Searching Stopped", new Date(System.currentTimeMillis())),Utils.EVENTS);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    public void doRetry() {
        if (isRetry &&  retryCount < retry) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog.setMessage("Retrying");
            dialog.show();
            scanLeDevice(true);
            retryCount+=1;
        }
    }
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_LONG).show();
                Utils.sendLog(new LogInfo("BLE Device Connected", new Date(System.currentTimeMillis())),Utils.EVENTS);
                tvStatus.setText("Connected");
                isRetry = false;
                retryCount = retry;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                         // disconnect from BLE
                    }
                },(sharedPreferences.getInt(Settings.TIMEOUT,1000*20)>1000*20?sharedPreferences.getInt(Settings.TIMEOUT,1000*20):1000*20));
                //  toggle.setEnabled(true);
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(),"Disconnected",Toast.LENGTH_LONG).show();
                Utils.sendLog(new LogInfo("BLE Device Disconnected", new Date(System.currentTimeMillis())),Utils.EVENTS);
                tvStatus.setText("Disconnected");
                deleteBondInformation(mGatt.getDevice());
                close();
                //  toggle.setEnabled(false);
            }  else if (ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(EXTRA_DATA));
            }
            else if(ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                Toast.makeText(getApplicationContext(),"Service Found",Toast.LENGTH_LONG).show();
                Utils.sendLog(new LogInfo("BLE Device Service Found", new Date(System.currentTimeMillis())),Utils.EVENTS);
                Log.d("BLE read",""+send(cmdBLE.getBytes()));
            }
            else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
                Log.d("Pairing Request", "Pairing");
                Utils.sendLog(new LogInfo("BLE Device Paring Request", new Date(System.currentTimeMillis())),Utils.EVENTS);
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
            doRetry();
            return;
        }
        mGatt.close();
        mGatt = null;

        doRetry();
    }
    private void displayData(String stringExtra) {
        tvReceived.setText(stringExtra);
        Log.d("BLE readings", stringExtra
        );
         if(stringExtra.equalsIgnoreCase("401")) {
            Toast.makeText(this,stringExtra + "Success",Toast.LENGTH_LONG).show();
             Utils.sendLog(new LogInfo(stringExtra + "SUCCESS", new Date(System.currentTimeMillis())),Utils.EVENTS);
            showDialog("Success");
            Log.d("BLE", "done");
        }
         else if(stringExtra.equalsIgnoreCase("40")) {
             Toast.makeText(this,stringExtra + "Success",Toast.LENGTH_LONG).show();
             Utils.sendLog(new LogInfo(stringExtra + "SUCCESS", new Date(System.currentTimeMillis())),Utils.EVENTS);
             showDialog("Success");
             Log.d("BLE", "done");
         }
         else if(stringExtra.equalsIgnoreCase("400")) {
             // showDialog("Please Properly Lock Dicky");
             showDialog("Success");
             Utils.sendLog(new LogInfo(stringExtra + "SUCCESS", new Date(System.currentTimeMillis())),Utils.EVENTS);
         }
         else if(stringExtra.equalsIgnoreCase("411")){
             Utils.sendLog(new LogInfo(stringExtra + "FAILURE", new Date(System.currentTimeMillis())),Utils.EVENTS);
             showDialog("Please Turn off ignition");

         }
         else if(stringExtra.equalsIgnoreCase("41")){
             Utils.sendLog(new LogInfo(stringExtra + "FAILURE", new Date(System.currentTimeMillis())),Utils.EVENTS);
             showDialog("Please Turn off ignition");

         }
         else if(stringExtra.equalsIgnoreCase("410")){
             Utils.sendLog(new LogInfo(stringExtra + "FAILURE", new Date(System.currentTimeMillis())),Utils.EVENTS);
             showDialog("Please Turn off ignition");
//             showDialog("Please Turn off ignition and lock the seat");
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
            Utils.sendLog(new LogInfo("SEND :::: BluetoothGatt not initialized", new Date(System.currentTimeMillis())),Utils.ERRORS);
            return false;
        }


        if (characteristic == null) {
            Utils.sendLog(new LogInfo("SEND :::: Send characteristic not found", new Date(System.currentTimeMillis())),Utils.ERRORS);
            Log.w("BLE", "Send characteristic not found");
            return false;
        }
        try{
            millis = sharedPreferences.getInt(Settings.DELAY,100);
            Thread.sleep(millis);}
        catch(InterruptedException e){

        }
        Utils.sendLog(new LogInfo("SEND :::: Sending", new Date(System.currentTimeMillis())),Utils.EVENTS);
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
            Utils.sendLog(new LogInfo("REG_NOTIFY :::: BluetoothGatt not initialized", new Date(System.currentTimeMillis())),Utils.ERRORS);
            return false;
        }


        if (characteristic == null) {
            Log.w("BLE", "Send characteristic not found");
            Utils.sendLog(new LogInfo("REG_NOTIFY :::: characteristic not found", new Date(System.currentTimeMillis())),Utils.ERRORS);
            return false;
        }
        Log.d("Charac",String.valueOf(characteristic.getDescriptors().size()));
        for(BluetoothGattDescriptor d:characteristic.getDescriptors()){
            Log.d("BT",d.getUuid().toString());
            d.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            break;
        }
        Utils.sendLog(new LogInfo("REG_NOTIFY :::: done", new Date(System.currentTimeMillis())),Utils.SUCCESS);
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
        Utils.sendLog(new LogInfo("BROADCASTING :::: Received String", new Date(System.currentTimeMillis())),Utils.EVENTS);
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
