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
    public static final String TIMEOUT = "timeout";
    public static final String DELAY = "delay";
    public static final String RETRY = "retry";

    SharedPreferences sharedPreferences;
    private EditText delay;
    private EditText timeout;
    private EditText retry;
    private Button btnSave;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences = getApplicationContext().getSharedPreferences("com.vogo.vogobletest",MODE_PRIVATE);
        context = this;
        delay = (EditText) findViewById(R.id.delayET);
        timeout = (EditText)findViewById(R.id.timeoutET);
        retry = (EditText) findViewById(R.id.retryET);
        btnSave = (Button) findViewById(R.id.saveBtn);


        delay.setText(String.valueOf(sharedPreferences.getInt(DELAY,100)));
        timeout.setText(String.valueOf(sharedPreferences.getInt(TIMEOUT,1000*20)));
        retry.setText(String.valueOf(sharedPreferences.getInt(RETRY,1)));


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putInt(DELAY, Integer.parseInt(delay.getText().toString().trim().replaceAll("[^0-9]", ""))).apply();
                sharedPreferences.edit().putInt(TIMEOUT, Integer.parseInt(timeout.getText().toString().trim().replaceAll("[^0-9]", ""))).apply();
                sharedPreferences.edit().putInt(RETRY,Integer.parseInt(retry.getText().toString().trim().replaceAll("[^0-9]", ""))).apply();
                Toast.makeText(context,"Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
