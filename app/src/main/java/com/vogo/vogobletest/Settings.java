package com.vogo.vogobletest;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Settings extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private static final String HEX_PREFIX = "0x";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences = getApplicationContext().getSharedPreferences("com.vogo.vogobletest3",MODE_PRIVATE);



    }
}
