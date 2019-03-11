package com.vogo.vogobletest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends AppCompatActivity {
    public static final String START_BYTE = "startByte";
    public static final String END_BYTE = "endByte";
    public static final String COMMAND_BYTE = "commandByte";
    public static final String DATA_BYTE = "dataByte";
    public static final String DELAY = "delay";
    public static final String TIMEOUT = "timeout";
    public static final String DATA_LENGTH = "dataLength";
    SharedPreferences sharedPreferences;
    private static final String HEX_PREFIX = "0x";
    private EditText startByte;
    private EditText endByte;
    private EditText commandByte;
    private EditText dataByte;
    private EditText delay;
    private EditText timeout;
    private EditText datalength;
    private Button btnSave;
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
                Toast.makeText(context,"Saved",Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void loadValues(SharedPreferences sharedPreferences) {

        startByte.setText(sharedPreferences.getString(START_BYTE,"24"));
        endByte.setText(sharedPreferences.getString(END_BYTE,"3B"));
        commandByte.setText(sharedPreferences.getString(COMMAND_BYTE,"63"));
        dataByte.setText(sharedPreferences.getString(DATA_BYTE,"62"));
        delay.setText(String.valueOf(sharedPreferences.getInt(DELAY,100)));
        timeout.setText(String.valueOf(sharedPreferences.getInt(TIMEOUT,1000*20)));
        datalength.setText(String.valueOf(sharedPreferences.getInt(DATA_LENGTH,4)));

    }



}
