package com.vogo.vogobletest;

import android.content.SharedPreferences;

public class Config {

    public static String DEFAULT_IGNITION_ON = "bikeon";
    public static String DEFAULT_IGNITION_OFF = "bikeoff";
    public static String DEFAULT_SEAT_OPEN = "dickyopen";
    public static String DEFAULT_END_RIDE = "endride";
    public static String DEFAULT_ACK_IGN_ON = "1";
    public static String DEFAULT_ACK_IGN_OFF = "3";
    public static String DEFAULT_ACK_SEAT_OPEN = "2";
    public static String DEFAULT_ACK_END_RIDE = "401";

    public static String DEFAULT_ACK_IG_ON = "410";
    public static String DEFAULT_ACK_IG_OFF = "0";
    public static String DEFAULT_ACK_SEAT_LOCK = "411";
    public static String DEFAULT_ACK_SEAT_UNLOCK = "400";

    public static String DEFAULT_PASS = "123456";


    public static void resetToDefaults(SharedPreferences sharedPreferences){
        sharedPreferences.edit().putString(Constants.IGNITION_ON,DEFAULT_IGNITION_ON).apply();
        sharedPreferences.edit().putString(Constants.IGNITION_OFF,DEFAULT_IGNITION_OFF).apply();
        sharedPreferences.edit().putString(Constants.END_RIDE,DEFAULT_END_RIDE).apply();
        sharedPreferences.edit().putString(Constants.SEAT_OPEN,DEFAULT_SEAT_OPEN).apply();
        sharedPreferences.edit().putString(Constants.ACK_IGN_ON,DEFAULT_ACK_IGN_ON).apply();
        sharedPreferences.edit().putString(Constants.ACK_IGN_OFF,DEFAULT_ACK_IGN_OFF).apply();
        sharedPreferences.edit().putString(Constants.ACK_SEAT_OPEN,DEFAULT_ACK_SEAT_OPEN).apply();
        sharedPreferences.edit().putString(Constants.ACK_END_RIDE,DEFAULT_ACK_END_RIDE).apply();
        sharedPreferences.edit().putString(Constants.DEFAULT_ACK_IG_ON1,DEFAULT_ACK_IG_ON).apply();
        sharedPreferences.edit().putString(Constants.DEFAULT_ACK_IG_OFF1,DEFAULT_ACK_IG_OFF).apply();
        sharedPreferences.edit().putString(Constants.DEFAULT_ACK_SEAT_LOCK1,DEFAULT_ACK_SEAT_LOCK).apply();
        sharedPreferences.edit().putString(Constants.DEFAULT_ACK_SEAT_UNLOCK1,DEFAULT_ACK_SEAT_UNLOCK).apply();
        sharedPreferences.edit().putString(Constants.BLE_PASS,DEFAULT_PASS).apply();
    }


}
