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
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class BLEConnection extends AppCompatActivity {
    String boxNumber;
    String macAddress;
    String blePass;
    String cmdIgnitionOn;
    String cmdIgnitionOff;
    String cmdSeatLockOpen;
    String cmdEndRide;
    TextView tvBox, tvMac, tvStatus;
    Button btnStart;
    EditText etStartTx,etStartRx;
    Button btnEnd;
    EditText etEndTx,etEndRx;
    Button btnStop;
    EditText etStopTx,etStopRx;
    Button btnSeat;
    EditText etSeatTx,etSeatRx;
    Button bootSpace;
    EditText etBootTx,etBootRx;
    Button dataSend;
    EditText etData;

    TextView tvAssembled,tvReply,tvDatalimit,tvDiscovered;
//    EditText etDelay;
    private ProgressDialog dialog;
    int millis = 100; //default


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
    int MODE = 0;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH
    };
    //Button toggle;
    boolean isunlocked = true;
    private String cmdBLE,cmdBLERx="";
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private ScanCallback mScanCallback;
    int retry = 1,retryCount = 0;
    boolean isRetry = true;
    private ToggleButton toggleButton;
    private LinearLayout dataModeLayout;



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
        Log.d("check mac",macAddress);
        tvBox = (TextView) findViewById(R.id.boxTv);
        tvMac = (TextView) findViewById(R.id.macTv);
        tvStatus = (TextView) findViewById(R.id.statusTv);
        btnStart = (Button)findViewById(R.id.start);
        btnEnd = (Button) findViewById(R.id.end);
        btnStop = (Button) findViewById(R.id.stop);
        btnSeat = (Button) findViewById(R.id.seat);
        bootSpace = (Button) findViewById(R.id.boot);
        dataSend = (Button) findViewById(R.id.btn_send);
        tvDiscovered = (TextView) findViewById(R.id.tvDiscovered);
        //etDelay = (EditText) findViewById(R.id.etDelay);
        dialog = new ProgressDialog(this);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        dataModeLayout =  (LinearLayout) findViewById(R.id.layoutData);

        etStartTx = getView(R.id.etStartTx);
        etStartRx = getView(R.id.etStartRx);

        etEndRx = getView(R.id.etEndRx);
        etEndTx = getView(R.id.etEndTx);

        etBootRx = getView(R.id.etBootRx);
        etBootTx = getView(R.id.etBootTx);

        etSeatRx = getView(R.id.etSeatRx);
        etSeatTx = getView(R.id.etSeatTx);

        etStopTx = getView(R.id.etStopTx);
        etStopRx  =getView(R.id.etStopRx);

        etData = getView(R.id.etData);
        tvAssembled = (TextView) findViewById(R.id.assembleTv);
        tvReply = (TextView) findViewById(R.id.replyTv);
        tvDatalimit = (TextView) findViewById(R.id.datalimitTV);



        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dataModeLayout.setVisibility(View.VISIBLE);
                    MODE =1;
                } else {
                    // The toggle is disabled
                    dataModeLayout.setVisibility(View.INVISIBLE);
                    MODE =0;
                }
            }
        });

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
      //  etDelay.setText(String.valueOf(millis));
        sharedPreferences = getApplicationContext().getSharedPreferences("com.vogo.vogobletest3",MODE_PRIVATE);
        blePass = sharedPreferences.getString(Constants.BLE_PASS,Config.DEFAULT_PASS);
        cmdIgnitionOn = sharedPreferences.getString(Constants.IGNITION_ON,Config.DEFAULT_IGNITION_ON);
        cmdIgnitionOff = sharedPreferences.getString(Constants.IGNITION_OFF,Config.DEFAULT_IGNITION_OFF);
        cmdSeatLockOpen = sharedPreferences.getString(Constants.SEAT_OPEN,Config.DEFAULT_SEAT_OPEN);
        cmdEndRide = sharedPreferences.getString(Constants.END_RIDE,Config.DEFAULT_END_RIDE);
        retry = sharedPreferences.getInt(Settings.RETRY,1);
        intialize();
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tx = etStartTx.getText().toString();
                String rx = etStartRx.getText().toString();
                if(!tx.contentEquals("") && !rx.contentEquals("")){
                Log.d("BLE","start");
                cmdBLE = assembleString(MODE,tx,rx);
                cmdBLERx  = String.valueOf(toAscii(rx));
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();
                isRetry = true;
                retryCount = 0;
                scanLeDevice(true);
            }
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tx = etStopTx.getText().toString();
                String rx = etStopRx.getText().toString();
                if(!tx.contentEquals("") && !rx.contentEquals("")){
                    cmdBLE = assembleString(MODE,tx,rx);
                    cmdBLERx  = String.valueOf(toAscii(rx));
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();
                    isRetry = true;
                    retryCount = 0;
                scanLeDevice(true);
            }}
        });

        btnSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tx = etSeatTx.getText().toString();
                String rx = etSeatRx.getText().toString();
                if(!tx.contentEquals("") && !rx.contentEquals("")){
                    cmdBLE = assembleString(MODE,tx,rx);
                    cmdBLERx  = String.valueOf(toAscii(rx));
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();
                isRetry = true;
                retryCount = 0;
            scanLeDevice(true);
            }}
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tx = etEndTx.getText().toString();
                String rx = etEndRx.getText().toString();
                if(!tx.contentEquals("") && !rx.contentEquals("")){
                    cmdBLE = assembleString(MODE,tx,rx);
                    cmdBLERx  = String.valueOf(toAscii(rx));
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Connecting");
                dialog.show();
                isRetry = true;
                retryCount = 0;
                scanLeDevice(true);
            }}
        });

        bootSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tx = etBootTx.getText().toString();
                String rx = etBootRx.getText().toString();
                if(!tx.contentEquals("") && !rx.contentEquals("")){
                    cmdBLE = assembleString(MODE,tx,rx);
                    cmdBLERx  = String.valueOf(toAscii(rx));
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    dialog.setMessage("Connecting");
                    dialog.show();
                    isRetry = true;
                    retryCount = 0;
                    scanLeDevice(true);
                }
            }
        });

        dataSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tx = etData.getText().toString();
                String rx="";
                int datacount = sharedPreferences.getInt(Settings.DATA_LENGTH, 4);
                if(datacount<9){
                    rx="0"+datacount;
                }
                else{
                    rx=""+datacount;
                }
                if(!tx.contentEquals("") && !rx.contentEquals("")){
                    cmdBLE = assembleString(MODE,tx,rx);
                    //cmdBLERx  = rx;
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    dialog.setMessage("Connecting");
                    dialog.show();
                    isRetry = true;
                    retryCount = 0;
                    scanLeDevice(true);
                }
            }
        });
    }

    private char toAscii(String in){
        return (char)Integer.parseInt(in,16);
    }

    private String assembleString(int mode, String txData, String rxData){
        StringBuilder sb = new StringBuilder();
        sb.append(toAscii(sharedPreferences.getString(Settings.START_BYTE,"24")));

        if(mode==0){
            sb.append(toAscii(sharedPreferences.getString(Settings.COMMAND_BYTE,"63")));
            sb.append(toAscii(txData));
        }
        else{
            sb.append(toAscii(sharedPreferences.getString(Settings.DATA_BYTE,"62")));
            sb.append(toAscii(rxData));
            for(String s: txData.split(" "))
                sb.append(toAscii(s));
        }


        if(mode==0)
            sb.append(toAscii(rxData));
        sb.append(toAscii(sharedPreferences.getString(Settings.END_BYTE,"3B")));


        tvAssembled.setText(sb.toString());
        return sb.toString();
    }

    private void intialize() {
        etStartTx.setText("41");
        etStartRx.setText("2A");
        etStopTx.setText("42");
        etStopRx.setText("2B");
        etSeatTx.setText("44");
        etSeatRx.setText("2D");
        etEndTx.setText("45");
        etEndRx.setText("2E");
        etBootTx.setText("43");
        etBootRx.setText("2C");
        tvDatalimit.setText(String.format("You can send upto %s data", String.valueOf(sharedPreferences.getInt(Settings.DATA_LENGTH, 4))));
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; ++i)
                {
                    if (!Pattern.compile("[ABCDEF1234567890 ]*").matcher(String.valueOf(source.charAt(i))).matches())
                    {
                        return "";
                    }
                }

                return null;
            }
        };
        etData.setFilters(new InputFilter[]{filter,new InputFilter.LengthFilter(sharedPreferences.getInt(Settings.DATA_LENGTH, 4)*2 + (sharedPreferences.getInt(Settings.DATA_LENGTH, 4)-1))});
    }

    private EditText getView(int id) {
        return (EditText) findViewById(id);
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
                            tvDiscovered.setText("Discovered:"+result.getRssi());
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
                    public void onLeScan(final BluetoothDevice device, final int rssi,
                                         byte[] scanRecord) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("onLeScan", device.toString());
                                if (device.getAddress().equalsIgnoreCase(macAddress)) {
                                    tvDiscovered.setText("Discovered:"+rssi);
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
            /**
             * Reset the reply textview for new connection
             */
            tvReply.setText("");
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
                isRetry = false;
                retryCount = retry;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mGatt!=null) {
                            // disconnect from BLE
                            deleteBondInformation(mGatt.getDevice());
                            close();
                        }
                    }
                },sharedPreferences.getInt(Settings.TIMEOUT,1000*20));
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
    public static void deleteBondInformation(BluetoothDevice device)
    {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("BLE", e.getMessage());
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

        Log.d("BLE readings", stringExtra
        );
        tvReply.setText(stringExtra);

        if(cmdBLERx.contentEquals(stringExtra)){
            showDialog("Success");
        }
         if(stringExtra.equalsIgnoreCase("401")) {
            Toast.makeText(this,stringExtra + "Success",Toast.LENGTH_LONG).show();
            showDialog("Success");
            Log.d("BLE", "done");
        }
         else if(stringExtra.equalsIgnoreCase("40")) {
             Toast.makeText(this,stringExtra + "Success",Toast.LENGTH_LONG).show();
             showDialog("Success");
             Log.d("BLE", "done");
         }
         else if(stringExtra.equalsIgnoreCase("400")) {
             // showDialog("Please Properly Lock Dicky");
             showDialog("Success");
         }
         else if(stringExtra.equalsIgnoreCase("411")){
             showDialog("Please Turn off ignition");

         }
         else if(stringExtra.equalsIgnoreCase("41")){
             showDialog("Please Turn off ignition");

         }
         else if(stringExtra.equalsIgnoreCase("410")){
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
            return false;
        }


        if (characteristic == null) {
            Log.w("BLE", "Send characteristic not found");
            return false;
        }
        try{
             millis =sharedPreferences.getInt(Settings.DELAY,100);
            Thread.sleep(millis);}
        catch(InterruptedException e){

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
                stringBuilder.append(String.format("%02X", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data));

        }
        sendBroadcast(intent);
    }
}
