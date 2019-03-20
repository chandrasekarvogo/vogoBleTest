package com.vogo.vogobletest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class Utils {

    private static  DatabaseReference databaseReference;
    private static String deviceId;
    private static String todaysdate;
    private static Context context;
    public static final String EVENTS = "Events";
    public static final String ERRORS = "Failures";
    public static final String SUCCESS = "Success";

    public Utils(DatabaseReference databaseReference, String deviceId, Context context) {
        Utils.databaseReference = databaseReference;
        Utils.deviceId = deviceId;
        this.context = context;
        DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy");
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        todaysdate = dateFormat.format(date);
    }

    public static void sendDeviceInfo(DeviceInfo deviceInfo){
        DatabaseReference deviceInfoRef = databaseReference.child("DeviceInfo");

        deviceInfoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("com.vogo.vogobletest",MODE_PRIVATE);
                sharedPreferences.edit().putString("firstboot","firstboot").apply();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        deviceInfoRef.setValue(deviceInfo);
    }

    public static void sendLog(LogInfo logInfo,String type){
        databaseReference.child("Logs").child(todaysdate).child(type).push().setValue(logInfo);
    }
}