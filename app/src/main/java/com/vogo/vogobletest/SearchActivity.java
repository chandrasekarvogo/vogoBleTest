package com.vogo.vogobletest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {
    Map<String,String> mapping = new HashMap<>();
    SharedPreferences sharedPreferences;
    ListView mapList;
    ArrayAdapter<String> mapAdapter;

    List<String> items;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search); sharedPreferences = getApplicationContext().getSharedPreferences("com.vogo.vogobletest3",MODE_PRIVATE);
        mapList = (ListView) findViewById(R.id.search_list);
        mapList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("BLE","list click");

                String selectedItem = String.valueOf(mapList.getItemAtPosition(position));
                String macAddress = mapping.get(selectedItem);
                if(macAddress!=null && !macAddress.equalsIgnoreCase("") && validate(macAddress)){
                    Intent i = new Intent(SearchActivity.this, BLEConnection.class);
                    i.putExtra("mAddress",macAddress);
                    i.putExtra("mBoxNo",selectedItem);
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i);
                }
                else{
                    showAlert("Mapped MAC address is not valid.Please re-map in valid format");
                }
            }
        });
        final EditText editText = (EditText) findViewById(R.id.searchET);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = editText.getText().toString();
                mapAdapter.getFilter().filter(text);
            }
        });
        try{
            populateList();}
        catch (JSONException e){
            finish();
        }

    }
    public boolean validate(String mac) {
        Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
        Matcher m = p.matcher(mac);
        return m.find();
    }
    void populateList() throws JSONException {
        String listItems = sharedPreferences.getString("mappings","{}");
        JSONObject obj = new JSONObject(listItems);

        Iterator<String> itr = obj.keys();
        while (itr.hasNext()) {

            String k = itr.next();
            String v = (String) obj.get(k);
            mapping.put(k,v);
        }
        items = new ArrayList<>(mapping.keySet());
        mapAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,items);
        mapList.setAdapter(mapAdapter);
    }
    private void showAlert(String message) {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);

        builder.setTitle("Error")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Whatever...
                        dialog.cancel();
                    }
                })
                .show();
    }
}
