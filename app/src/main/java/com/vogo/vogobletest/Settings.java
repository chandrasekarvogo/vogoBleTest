package com.vogo.vogobletest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Settings extends AppCompatActivity {
    public static final String START_BYTE = "startByte";
    public static final String END_BYTE = "endByte";
    public static final String COMMAND_BYTE = "commandByte";
    public static final String DATA_BYTE = "dataByte";
    public static final String DELAY = "delay";
    public static final String TIMEOUT = "timeout";
    public static final String DATA_LENGTH = "dataLength";
    public static final String RETRY = "retry";
    public static final String START_TX = "start_tx";
    public static final String START_RX = "start_rx";
    public static final String END_TX="end_tx";
    public static final String END_RX="end_rx";
    public static final String STOP_TX = "stop_tx";
    public static final String STOP_RX = "stop_rx";
    public static final String SEAT_TX = "seat_tx";
    public static final String SEAT_RX = "seat_rx";
    public static final String BOOT_TX = "boot_tx";
    public static final String BOOT_RX = "boot_rx";
    public static final java.lang.reflect.Type TYPE = new TypeToken<HashMap<String, FeildData>>() {
    }.getType();
    SharedPreferences sharedPreferences;
    private static final String HEX_PREFIX = "0x";
    private EditText startByte;
    private EditText endByte;
    private EditText commandByte;
    private EditText dataByte;
    private EditText delay;
    private EditText timeout;
    private EditText datalength;
    private EditText retry;
    private Button btnSave;
    EditText etStartTx, etStartRx;
    EditText etEndTx, etEndRx;
    EditText etStopTx, etStopRx;
    EditText etSeatTx, etSeatRx;
    EditText etBootTx, etBootRx;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences = getApplicationContext().getSharedPreferences("com.vogo.vogobletest3",MODE_PRIVATE);
        startByte = (EditText) findViewById(R.id.startByteET);
        endByte = (EditText) findViewById(R.id.endByteET);
        commandByte = (EditText) findViewById(R.id.commandByteET);
        dataByte = (EditText) findViewById(R.id.dataByteET);
        delay = (EditText) findViewById(R.id.delayET);
        timeout = (EditText)findViewById(R.id.timeoutET);
        datalength = (EditText)findViewById(R.id.dataLengthET);
        datalength.setFilters(new InputFilter[]{new InputFilterMinMax("1","250")});
        retry = (EditText) findViewById(R.id.retryET);
        etStartTx = getView(R.id.etStartTx);
        etStartRx = getView(R.id.etStartRx);

        etEndRx = getView(R.id.etEndRx);
        etEndTx = getView(R.id.etEndTx);

        etBootRx = getView(R.id.etBootRx);
        etBootTx = getView(R.id.etBootTx);

        etSeatRx = getView(R.id.etSeatRx);
        etSeatTx = getView(R.id.etSeatTx);

        etStopTx = getView(R.id.etStopTx);
        etStopRx = getView(R.id.etStopRx);
        btnSave = (Button) findViewById(R.id.saveBtn);
        context = this;
        loadValues(sharedPreferences);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putString(START_BYTE,startByte.getText().toString()).apply();
                sharedPreferences.edit().putString(END_BYTE,endByte.getText().toString()).apply();
                sharedPreferences.edit().putString(COMMAND_BYTE,commandByte.getText().toString()).apply();
                sharedPreferences.edit().putString(DATA_BYTE,dataByte.getText().toString()).apply();
                sharedPreferences.edit().putInt(DELAY, Integer.parseInt(delay.getText().toString().trim().replaceAll("[^0-9]", ""))).apply();
                sharedPreferences.edit().putInt(TIMEOUT, Integer.parseInt(timeout.getText().toString().trim().replaceAll("[^0-9]", ""))).apply();
                sharedPreferences.edit().putInt(DATA_LENGTH,Integer.parseInt(datalength.getText().toString().trim().replaceAll("[^0-9]", ""))).apply();
                sharedPreferences.edit().putInt(RETRY,Integer.parseInt(retry.getText().toString().trim().replaceAll("[^0-9]", ""))).apply();
                sharedPreferences.edit().putString(START_TX,etStartTx.getText().toString()).apply();
                sharedPreferences.edit().putString(START_RX,etStartRx.getText().toString()).apply();

                sharedPreferences.edit().putString(END_TX,etEndTx.getText().toString()).apply();
                sharedPreferences.edit().putString(END_RX,etEndRx.getText().toString()).apply();

                sharedPreferences.edit().putString(BOOT_TX,etBootTx.getText().toString()).apply();
                sharedPreferences.edit().putString(BOOT_RX,etBootRx.getText().toString()).apply();

                sharedPreferences.edit().putString(SEAT_TX,etSeatTx.getText().toString()).apply();
                sharedPreferences.edit().putString(SEAT_RX,etSeatRx.getText().toString()).apply();

                sharedPreferences.edit().putString(STOP_TX,etStopTx.getText().toString()).apply();
                sharedPreferences.edit().putString(STOP_RX,etStopRx.getText().toString()).apply();


                LinearLayout viewGroup = (LinearLayout) findViewById(R.id.feildsGroup);
                String feildsJson = sharedPreferences.getString("Feilds", "{}");
                Map<String, FeildData> feildDataMap = new Gson().fromJson(feildsJson, TYPE);
                for(int i =0;i<viewGroup.getChildCount();i++){
                    View view = viewGroup.getChildAt(i);
                    String tag = view.getTag().toString();
                    EditText txEt = (EditText) view.findViewById(R.id.temp_tx_et);
                    EditText rxEt = (EditText) view.findViewById(R.id.temp_rx_et);
                    FeildData feildData = feildDataMap.get(tag);
                    if(feildData!=null){
                        feildData.setFeildTx(txEt.getText().toString());
                        feildData.setFeildRx(rxEt.getText().toString());
                    }
                    else {
                        feildData = new FeildData();
                        feildData.setFeildTx(txEt.getText().toString());
                        feildData.setFeildRx(rxEt.getText().toString());
                    }
                    feildDataMap.put(tag,feildData);
                }
                sharedPreferences.edit().putString("Feilds",new Gson().toJson(feildDataMap,TYPE)).apply();
                Toast.makeText(context,"Saved",Toast.LENGTH_SHORT).show();
            }
        });


    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addFields:
                showAddDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadValues(SharedPreferences sharedPreferences) {

        startByte.setText(sharedPreferences.getString(START_BYTE,"24"));
        endByte.setText(sharedPreferences.getString(END_BYTE,"3B"));
        commandByte.setText(sharedPreferences.getString(COMMAND_BYTE,"63"));
        dataByte.setText(sharedPreferences.getString(DATA_BYTE,"62"));
        delay.setText(String.valueOf(sharedPreferences.getInt(DELAY,100)));
        timeout.setText(String.valueOf(sharedPreferences.getInt(TIMEOUT,1000*20)));
        datalength.setText(String.valueOf(sharedPreferences.getInt(DATA_LENGTH,4)));
        retry.setText(String.valueOf(sharedPreferences.getInt(RETRY,1)));

        etStartTx.setText(sharedPreferences.getString(START_TX,"41"));
        etStartRx.setText(sharedPreferences.getString(START_RX,"2A"));
        etStopTx.setText(sharedPreferences.getString(STOP_TX,"42"));
        etStopRx.setText(sharedPreferences.getString(STOP_RX,"2B"));
        etSeatTx.setText(sharedPreferences.getString(SEAT_TX,"44"));
        etSeatRx.setText(sharedPreferences.getString(SEAT_RX,"2D"));
        etEndTx.setText(sharedPreferences.getString(END_TX,"45"));
        etEndRx.setText(sharedPreferences.getString(END_RX,"2E"));
        etBootTx.setText(sharedPreferences.getString(BOOT_TX,"43"));
        etBootRx.setText(sharedPreferences.getString(BOOT_RX,"2C"));

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFeilds();

    }

    private void loadFeilds() {
        String feildsJson = sharedPreferences.getString("Feilds", "{}");
        if (!feildsJson.equalsIgnoreCase("{}")){
            Map<String, FeildData> feildDataMap = new Gson().fromJson(feildsJson, TYPE);
            View v ;
            LinearLayout viewGroup = (LinearLayout) findViewById(R.id.feildsGroup);
            if(viewGroup.getChildCount()>0) viewGroup.removeAllViews();
            // Layout inflater
            LayoutInflater layoutInflater = getLayoutInflater();
            for(Map.Entry<String,FeildData> feildDataEntry:feildDataMap.entrySet()){
                v = layoutInflater.inflate(R.layout.feild_templatee,viewGroup,false);
                v.setTag(feildDataEntry.getKey());
                TextView txTv = (TextView) v.findViewById(R.id.temp_tx);
                txTv.setText(feildDataEntry.getKey() + " Tx");

                EditText txEt = (EditText) v.findViewById(R.id.temp_tx_et);
                txEt.setText(feildDataEntry.getValue().getFeildTx());

                TextView rxTv = (TextView) v.findViewById(R.id.temp_rx);
                rxTv.setText(feildDataEntry.getKey() + " Tx");

                EditText rxEt = (EditText) v.findViewById(R.id.temp_rx_et);
                rxEt.setText(feildDataEntry.getValue().getFeildRx());

                viewGroup.addView(v);
            }

        }
    }

    private EditText getView(int id) {
        return (EditText) findViewById(id);
    }
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Mapping");
        final View viewInflated = LayoutInflater.from(this).inflate(R.layout.add_feilds, null);
        builder.setView(viewInflated);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText feildName = (EditText)viewInflated.findViewById(R.id.feildNameTv);
                EditText feildRx = (EditText)viewInflated.findViewById(R.id.feildRx);
                EditText feildTx = (EditText)viewInflated.findViewById(R.id.feildTx);
                String feild_Name = feildName.getText().toString();
                String feildRxValue = feildRx.getText().toString();
                String feildTxValue = feildTx.getText().toString();

                if(!feild_Name.equalsIgnoreCase("") && !feildTxValue.equalsIgnoreCase("") && !feildRxValue.equalsIgnoreCase("")) {
                    FeildData feildData = new FeildData();
                    feildData.setFeildName(feild_Name);
                    feildData.setFeildTx(feildTxValue);
                    feildData.setFeildRx(feildRxValue);
                    String feildsJson = sharedPreferences.getString("Feilds", "{}");
                    if (!feildsJson.equalsIgnoreCase("{}")){
                        Map<String,FeildData> feildDataMap = new Gson().fromJson(feildsJson, TYPE);
                        feildDataMap.put(feild_Name,feildData);
                        sharedPreferences.edit().putString("Feilds",new Gson().toJson(feildDataMap,TYPE)).apply();
                    }
                    else{
                            Map<String,FeildData> feildDataMap = new HashMap<>();
                            feildDataMap.put(feild_Name,feildData);
                            sharedPreferences.edit().putString("Feilds",new Gson().toJson(feildDataMap,TYPE)).apply();
                    }
                    loadFeilds();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });



        final AlertDialog dialog = builder.create();
        dialog.show();
        final EditText feildName = (EditText)viewInflated.findViewById(R.id.feildNameTv);
        final EditText feildRx = (EditText)viewInflated.findViewById(R.id.feildRx);
        final EditText feildTx = (EditText)viewInflated.findViewById(R.id.feildTx);

        feildName.addTextChangedListener(getWatcher(dialog));
        feildTx.addTextChangedListener(getWatcher(dialog));
        feildRx.addTextChangedListener(getWatcher(dialog));

        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
    }

    private TextWatcher getWatcher(final AlertDialog dialog) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    // Disable ok button
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        };
    }
}
