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
            "    \"mac\": \"6C:C3:74:F3:BB:4C\",\n" +
            "    \"vehicle\": \"KA 05 AH 8829\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:02:0E:FC\",\n" +
            "    \"vehicle\": \"KA 03 AG 4825\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"A8:10:87:1B:77:DD\",\n" +
            "    \"vehicle\": \"KA 51 AC 1191\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:05:23:F5\",\n" +
            "    \"vehicle\": \"KA 05 AH 6546\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"B0:7E:11:FF:22:AF\",\n" +
            "    \"vehicle\": \"KA 05 AH 5952\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"6C:C3:74:F4:64:02\",\n" +
            "    \"vehicle\": \"KA 51 AC 6906\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"B0:7E:11:FF:1E:F3\",\n" +
            "    \"vehicle\": \"KA 03 AG 5001\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:02:05:80\",\n" +
            "    \"vehicle\": \"KA 51 AB 7604\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"6C:C3:74:F4:B4:A6\",\n" +
            "    \"vehicle\": \"KA 03 AG 4856\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:02:26:EF\",\n" +
            "    \"vehicle\": \"KA 03 AG 4998\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:04:A8:71\",\n" +
            "    \"vehicle\": \"KA 05 AJ 0528\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"D8:A9:8B:B0:D0:74\",\n" +
            "    \"vehicle\": \"KA 03 AG 4999\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"D8:A9:8B:B0:ED:E6\",\n" +
            "    \"vehicle\": \"KA 51 AC 0120\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:05:19:4B\",\n" +
            "    \"vehicle\": \"KA 51 AC 1848\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"6C:C3:74:F3:D3:2A\",\n" +
            "    \"vehicle\": \"KA 03 AG 4922\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"B0:7E:11:FF:1E:FB\",\n" +
            "    \"vehicle\": \"KA 05 AH 5660\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:04:6F:BF\",\n" +
            "    \"vehicle\": \"KA 51 AC 6873\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"6C:C3:74:FC:B1:C7\",\n" +
            "    \"vehicle\": \"KA 51 AC 6762\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"D8:A9:8B:B0:ED:DE\",\n" +
            "    \"vehicle\": \"KA 51 AC 1540\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:B8:01:02\",\n" +
            "    \"vehicle\": \"KA 05 AH 5980\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"6C:C3:74:FC:9F:49\",\n" +
            "    \"vehicle\": \"KA 51 AC 1767\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:04:87:35\",\n" +
            "    \"vehicle\": \"KA 05 AH 5321\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:04:BC:45\",\n" +
            "    \"vehicle\": \"KA 51 AB 7071\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"D8:A9:8B:B0:E2:C1\",\n" +
            "    \"vehicle\": \"KA 51 AC 8328\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"D8:A9:8B:B0:E6:D7\",\n" +
            "    \"vehicle\": \"KA 04 AH 5860\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:05:23:97\",\n" +
            "    \"vehicle\": \"KA 51 AC 0098\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"6C:C3:74:F4:5C:DA\",\n" +
            "    \"vehicle\": \"KA 51 AC 1554\"\n" +
            "  }\n" +
            ",{\n" +
            "    \"mac\": \"90:E2:02:05:51:A2\",\n" +
            "    \"vehicle\": \"KA 51 AC 6851\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"90:E2:02:05:19:59\",\n" +
            "    \"vehicle\": \"KA 51 AB 7060\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"B0:7E:11:FF:1E:CE\",\n" +
            "    \"vehicle\": \"KA 51 AC 1544\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"6C:C3:74:FC:9F:49\",\n" +
            "    \"vehicle\": \"KA 03 AG 4933\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"A8:10:87:1C:E9:60\",\n" +
            "    \"vehicle\": \"KA 03 AG 5002\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"A8:10:87:21:91:2C\",\n" +
            "    \"vehicle\": \"KA 05 AH 6303\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"mac\": \"D8:A9:8B:B0:DE:E8\",\n" +
            "    \"vehicle\": \"KA 51 AC 7275\"\n" +
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
