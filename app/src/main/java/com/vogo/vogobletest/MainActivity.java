package com.vogo.vogobletest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsPromptResult;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    static Map<String,String> mappings = new HashMap<>();
    SharedPreferences sharedPreferences;
    ListView mapList;
    ArrayAdapter<String> mapAdapter;
    List<String> items;
    String json = "{\"sample\":[\n" +
            "  {\n" +
            "    \"mac\": \"7C:01:0A:5B:E5:2A\",\n" +
            "    \"BoardNumber\": \"L293\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:7B:C5:72\",\n" +
            "    \"BoardNumber\": \"L289\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"34:15:13:CF:53:98\",\n" +
            "    \"BoardNumber\": \"L294\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:7B:C5:21\",\n" +
            "    \"BoardNumber\": \"L284\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:79:C8:B8\",\n" +
            "    \"BoardNumber\": \"L291\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:7B:B8:E6\",\n" +
            "    \"BoardNumber\": \"K927\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:7B:B8:D3\",\n" +
            "    \"BoardNumber\": \"L286\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:7B:BC:C1\",\n" +
            "    \"BoardNumber\": \"L285\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:7B:B8:A8\",\n" +
            "    \"BoardNumber\": \"L288\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"34:15:13:CF:2B:DD\",\n" +
            "    \"BoardNumber\": \"L290\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"34:15:13:CF:39:EF\",\n" +
            "    \"BoardNumber\": \"L292\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"34:15:13:CF:41:FD\",\n" +
            "    \"BoardNumber\": \"L307\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"34:15:13:CF:50:F0\",\n" +
            "    \"BoardNumber\": \"L000\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"34:15:13:CF:47:D2\",\n" +
            "    \"BoardNumber\": \"L299\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3E:07:12\",\n" +
            "    \"BoardNumber\": \"L302\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3E:0A:F8\",\n" +
            "    \"BoardNumber\": \"L300\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3E:79:D2\",\n" +
            "    \"BoardNumber\": \"L295\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3D:DB:A8\",\n" +
            "    \"BoardNumber\": \"L303\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3E:1C:3B\",\n" +
            "    \"BoardNumber\": \"L298\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3F:D8:8E\",\n" +
            "    \"BoardNumber\": \"L305\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3E:07:26\",\n" +
            "    \"BoardNumber\": \"L304\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3E:0E:F7\",\n" +
            "    \"BoardNumber\": \"L311\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3E:0A:90\",\n" +
            "    \"BoardNumber\": \"L308\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3F:D8:92\",\n" +
            "    \"BoardNumber\": \"L309\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3E:71:41\",\n" +
            "    \"BoardNumber\": \"L310\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3E:0E:EC\",\n" +
            "    \"BoardNumber\": \"L306\"\n" +
            "  },\n" +
            "{\n" +
            "    \"mac\": \"0C:B2:B7:7B:B8:E6\",\n" +
            "    \"BoardNumber\": \"G163\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:7B:BC:C1\",\n" +
            "    \"BoardNumber\": \"L663\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:7B:C5:72\",\n" +
            "    \"BoardNumber\": \"L808\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:7B:C5:12\",\n" +
            "    \"BoardNumber\": \"G275\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"18:62:E4:3C:8E:CC\",\n" +
            "    \"BoardNumber\": \"L583\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"0C:B2:B7:79:CB:4C\",\n" +
            "    \"BoardNumber\": \"L279\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"34:03:DE:1C:60FE\",\n" +
            "    \"BoardNumber\": \"L236\"\n" +
            "  }]}";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getApplicationContext().getSharedPreferences("com.vogo.vogobletest3",MODE_PRIVATE);
        mapList = (ListView) findViewById(R.id.list_item);
        mapList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("BLE","list click");
                String selectedItem = items.get(position);
                String macAddress = mappings.get(selectedItem);
                if(macAddress!=null && !macAddress.equalsIgnoreCase("") && validate(macAddress)){
                    Intent i = new Intent(MainActivity.this, BLEConnection.class);
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

    public boolean validate(String mac) {
        Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
        Matcher m = p.matcher(mac);
        return m.find();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                showAddDialog();
                return true;
            case R.id.settting:
                showPasswordDialog();
                return true;
            case R.id.search:
                startActivity(new Intent(this, SearchActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void showPasswordDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");
        final View viewInflated = LayoutInflater.from(this).inflate(R.layout.password_layout, null);
        builder.setView(viewInflated);
        final Context context =this;
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText password = (EditText)viewInflated.findViewById(R.id.password);
                String pass = password.getText().toString();

                if(!pass.equalsIgnoreCase("") && pass.contentEquals("3299")) {
                    startActivity(new Intent(context, Settings.class));
                }
                else{
                    dialog.cancel();
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
        final EditText pass = (EditText)viewInflated.findViewById(R.id.password);

        pass.addTextChangedListener(new TextWatcher() {
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
                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else if(s.toString().contentEquals("3299")) {
                    // Something into edit text. Enable the button.
                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });


        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
    }
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Mapping");
       final View viewInflated = LayoutInflater.from(this).inflate(R.layout.add_mapping, null);
        builder.setView(viewInflated);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText box = (EditText)viewInflated.findViewById(R.id.boxNumber);
                EditText macAddress = (EditText)viewInflated.findViewById(R.id.macAddr);
                String boxNo = box.getText().toString();
                String mAddress = macAddress.getText().toString();

                if(!boxNo.equalsIgnoreCase("") && !mAddress.equalsIgnoreCase("")) {
                    String json = sharedPreferences.getString("mappings", "{}");
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        jsonObject.put(boxNo,mAddress);
                        sharedPreferences.edit().putString("mappings",jsonObject.toString()).apply();
                        mappings.put(boxNo,mAddress);
                        items.add(boxNo);
                        mapAdapter.notifyDataSetChanged();
                    }
                    catch (JSONException e){
                        dialog.cancel();
                    }
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
        final EditText box = (EditText)viewInflated.findViewById(R.id.boxNumber);
        final EditText macAddress = (EditText)viewInflated.findViewById(R.id.macAddr);

        box.addTextChangedListener(new TextWatcher() {
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
                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        macAddress.addTextChangedListener(new TextWatcher() {
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
                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    // Something into edit text. Enable the button.
                    ((AlertDialog) dialog).getButton(
                            AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sharedPreferences.getBoolean("firstRun", true)){

            try {
                JSONObject macMappings = new JSONObject(json);
                JSONArray mappingArray = macMappings.getJSONArray("sample");
                for(int i=0;i<mappingArray.length();i++){
                    JSONObject macObject = mappingArray.getJSONObject(i);
                    if(macObject!=null){
                        Iterator<String> keysItr = macObject.keys();
                        while(keysItr.hasNext()) {
                            String k = (String) macObject.get(keysItr.next());
                            String v = (String) macObject.get(keysItr.next());
                            mappings.put(v,k);
                        }
                    }
                }


                items = new ArrayList<>(mappings.keySet());
                mapAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,items);
                mapList.setAdapter(mapAdapter);
                sharedPreferences.edit().putBoolean("firstRun",false).apply();
                JSONObject finalMap = new JSONObject(mappings);
                sharedPreferences.edit().putString("mappings",finalMap.toString()).apply();
                Config.resetToDefaults(sharedPreferences);
            }
            catch(JSONException e){
                finish();
            }
        }
        else {
            try{
            populateList();}
            catch (JSONException e){
                finish();
            }
        }
    }

    void populateList() throws JSONException {
        String listItems = sharedPreferences.getString("mappings","{}");
        JSONObject obj = new JSONObject(listItems);

        Iterator<String> itr = obj.keys();
        while (itr.hasNext()) {

            String k = itr.next();
            String v = (String) obj.get(k);
            mappings.put(k,v);
        }
        items = new ArrayList<>(mappings.keySet());
        mapAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,items);
        mapList.setAdapter(mapAdapter);
    }


}
