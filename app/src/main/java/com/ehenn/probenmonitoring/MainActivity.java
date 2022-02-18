package com.ehenn.probenmonitoring;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    //variables
    private static final String TAG = "MainActivity";
    private Button button_connect;
    private Button button_scan_pkid;
    private Button button_commit_pkid;
    private Button button_Reset;
    private Button button_Apply_Changes;
    private Button button_to_Detact;
    private RadioButton radioButton_Mobile1;
    private RadioButton radioButton_Mobile2;
    private RadioButton radioButton_Mobile3;
    private RadioButton radioButton_Mobile4;
    private RadioButton radioButton_Mobile5;
    private RadioButton radioButton_Mobile6;
    private RadioButton radioButton_Mobile7;
    private RadioButton radioButton_Mobile8;
    private RadioButton radioButton_Mobile9;
    private RadioButton radioButton_Mobile10;
    private RadioButton radioButton_Mobile11;
    private RadioButton radioButton_Mobile12;
    private RadioButton radioButton_Terminal1;
    private RadioButton radioButton_Terminal2;
    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private RadioGroup radioGroup3;
    private TextView textView_status;
    private EditText editText_PKID;
    private EditText editText_IP;
    private EditText editText_Port;
    private EditText editText_Username;
    private EditText editText_Password;
    private NestedScrollView nestedScrollView_status;

    MqttAndroidClient client;

    public String default_IP_Adress = "192.168.178.53";
    public String default_Port = "1883";
    String default_MQTT_Broker = "tcp://192.168.178.53:1883";
    String default_MQTT_User = "detact";
    String default_MQTT_PassKey = "detact#1234";

    public String IP_Adress = "192.168.178.53";
    public String Port = "1883";
    String MQTT_Broker = "tcp://192.168.178.53:1883";
    String MQTT_User = "detact";
    String MQTT_PassKey = "detact#1234";

    String PKID = "";
    String actual_Station_ID = "";
    public boolean reachable = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //TODO: integrate Login Information
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GUI Assignment
        button_connect = findViewById(R.id.Button_Connect);
        button_scan_pkid = findViewById(R.id.Button_Scan_PKID);
        button_commit_pkid = findViewById(R.id.Button_Commit_PKID);
        button_Apply_Changes = findViewById(R.id.Button_Apply_Changes);
        button_Reset = findViewById(R.id.Button_Reset);
        button_to_Detact = findViewById(R.id.Button_to_Detact);

        radioButton_Mobile1 = findViewById(R.id.RadioButton_Mobile1);
        radioButton_Mobile2 = findViewById(R.id.RadioButton_Mobiel2);
        radioButton_Mobile3 = findViewById(R.id.RadioButton_Mobiel3);
        radioButton_Mobile4 = findViewById(R.id.RadioButton_Mobiel4);
        radioButton_Mobile5 = findViewById(R.id.RadioButton_Mobiel5);
        radioButton_Mobile6 = findViewById(R.id.RadioButton_Mobiel6);
        radioButton_Mobile7 = findViewById(R.id.RadioButton_Mobiel7);
        radioButton_Mobile8 = findViewById(R.id.RadioButton_Mobiel8);
        radioButton_Mobile9 = findViewById(R.id.RadioButton_Mobiel9);
        radioButton_Mobile10 = findViewById(R.id.RadioButton_Mobiel10);
        radioButton_Mobile11 = findViewById(R.id.RadioButton_Mobiel11);
        radioButton_Mobile12 = findViewById(R.id.RadioButton_Mobiel12);
        radioButton_Terminal1 = findViewById(R.id.RadioButton_Terminal1);
        radioButton_Terminal2 = findViewById(R.id.RadioButton_Terminal2);

        radioGroup1 = findViewById(R.id.RadioGroup1);
        radioGroup2 = findViewById(R.id.RadioGroup2);
        radioGroup3 = findViewById(R.id.RadioGroup3);

        textView_status = findViewById(R.id.TextView_Status);

        editText_PKID = findViewById(R.id.EditText_PKID);
        editText_IP = findViewById(R.id.EditText_IP);
        editText_Port = findViewById(R.id.EditText_Port);
        editText_Username = findViewById(R.id.EditText_Username);
        editText_Password = findViewById(R.id.EditText_Password);

        nestedScrollView_status = findViewById(R.id.nestedScrollView_status);

        //set OnClickListeners
        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mqtt_connect(MQTT_Broker, MQTT_User, MQTT_PassKey);

            }
        });

        button_scan_pkid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QRScanActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        button_commit_pkid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PKID = editText_PKID.getText().toString();

                if (actual_Station_ID != "") {
                    if (PKID != "") {
                        String topic = actual_Station_ID + "/PKID";
                        mqtt_publish_message(topic, editText_PKID.getText().toString());
                    } else {
                        textView_status.append(get_timestamp() + " No PKID has been selected. Please scan or enter a PKID");
                        scrollToBottom_textView_status();
                    }
                } else {
                    textView_status.append(get_timestamp() + " No Station has been selected. Please select a station from above!");
                    scrollToBottom_textView_status();

                }
            }
        });

        button_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restore_default_values();
            }
        });

        button_Apply_Changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply_Changes();
            }
        });

        button_to_Detact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebURL("https://stackoverflow.com/");
            }
        });

        radioButton_Mobile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup2.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile1.isChecked()) {
                    actual_Station_ID = radioButton_Mobile1.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup2.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile2.isChecked()) {
                    actual_Station_ID = radioButton_Mobile2.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup2.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile3.isChecked()) {
                    actual_Station_ID = radioButton_Mobile3.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup2.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile4.isChecked()) {
                    actual_Station_ID = radioButton_Mobile4.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup2.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile5.isChecked()) {
                    actual_Station_ID = radioButton_Mobile5.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup2.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile6.isChecked()) {
                    actual_Station_ID = radioButton_Mobile6.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup1.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile7.isChecked()) {
                    actual_Station_ID = radioButton_Mobile7.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup1.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile8.isChecked()) {
                    actual_Station_ID = radioButton_Mobile8.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup1.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile9.isChecked()) {
                    actual_Station_ID = radioButton_Mobile9.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup1.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile10.isChecked()) {
                    actual_Station_ID = radioButton_Mobile10.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup1.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile11.isChecked()) {
                    actual_Station_ID = radioButton_Mobile11.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Mobile12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup1.clearCheck();
                radioGroup3.clearCheck();
                if (radioButton_Mobile12.isChecked()) {
                    actual_Station_ID = radioButton_Mobile12.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Terminal1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup1.clearCheck();
                radioGroup2.clearCheck();
                if (radioButton_Terminal1.isChecked()) {
                    actual_Station_ID = radioButton_Terminal1.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        radioButton_Terminal2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroup1.clearCheck();
                radioGroup2.clearCheck();
                if (radioButton_Terminal2.isChecked()) {
                    actual_Station_ID = radioButton_Terminal2.getText().toString();
                    textView_status.append(get_timestamp() + " " + actual_Station_ID + " has been chosen as active station.\n");
                    scrollToBottom_textView_status();
                }
            }
        });

        //check connection and start online monitor
        startPing();
        (new Handler()).postDelayed(this::start_online_Monitors, 5000);

        //write default values in UI:
        editText_IP.setText(IP_Adress);
        editText_Port.setText(Port);
        editText_Username.setText(MQTT_User);
        editText_Password.setText(MQTT_PassKey);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2)
        {
            if (resultCode != Activity.RESULT_CANCELED) {
                PKID = data.getStringExtra("PKID");
                textView_status.append(get_timestamp() + " PKID: " + PKID + " has been scanned.\n");
                editText_PKID.setText(PKID);
            }
        }
    }


    public void mqtt_connect(String mqtt_broker, String mqtt_user, String mqtt_passkey) {

        //TODO: stürtzt ab nach einiger zeit

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), mqtt_broker,
                        clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mqtt_user);
        options.setPassword(mqtt_passkey.toCharArray());

        try {
            IMqttToken token = client.connect(options);

            token.setActionCallback(new IMqttActionListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess in mqtt_connect()");
                    Log.i(TAG, "Connected to the " + MQTT_Broker + " MQTT Broker.");
                    textView_status.append((get_timestamp() + " Connected to the " + MQTT_Broker + " MQTT Broker." + "\n"));
                    scrollToBottom_textView_status();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure in mqtt_connect()");
                    Log.i(TAG, "Error while connecting to the " + MQTT_Broker + " MQTT Broker.");
                    textView_status.append(("Error while connecting to the " + MQTT_Broker + " MQTT Broker." + "\n"));
                    scrollToBottom_textView_status();
                }
            });
        } catch (MqttException e){
            Log.e(TAG, "Try Catch engaged in mqtt_connect()");
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void mqtt_publish_message(String topic, String payload) {

        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);

            client.publish(topic, message);

            Log.i(TAG, "Error while connecting to the " + MQTT_Broker + " MQTT Broker.");
            textView_status.append(get_timestamp() + " " + "Message " +message.toString() + " has been published in " + topic + ".\n");
            scrollToBottom_textView_status();

        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }


    public void mqtt_subscribe_topic(String topic) {

        try{
            client.subscribe(topic, 1);

            Log.i(TAG, "subscribed to Topic " + topic + " on " + MQTT_Broker);
            textView_status.append("subscribed to Topic " + topic + " on " + MQTT_Broker + "\n");
            scrollToBottom_textView_status();

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "connection to " + topic + " on " + MQTT_Broker + " has been lost");
                    textView_status.append("connection to " + topic + " on " + MQTT_Broker + " has been lost" + "\n");
                    scrollToBottom_textView_status();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.i(TAG, "a message has arrived");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i(TAG, "message delivery completed");
                }
            });
        }catch(MqttException e){
            Log.i(TAG, "something happened");
        }
    }


    public void online_status(RadioButton element) {

        try {
            String topic = element.getText().toString() + "/online";

            client.subscribe(topic, 1);

            Log.i(TAG, "subscribed to Topic " + topic + " on " + MQTT_Broker);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "connection to " + topic + " on " + MQTT_Broker + " has been lost");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String Payload = new String(message.getPayload());

                    Log.i(TAG, "a message has arrived");
                    if (Payload.equals("True")) {
                        if (topic.equals("Mobile 1/online")){
                            radioButton_Mobile1.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 2/online")){
                            radioButton_Mobile2.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 3/online")){
                            radioButton_Mobile3.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 4/online")){
                            radioButton_Mobile4.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 5/online")){
                            radioButton_Mobile5.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 6/online")){
                            radioButton_Mobile6.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 7/online")){
                            radioButton_Mobile7.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 8/online")){
                            radioButton_Mobile8.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 9/online")){
                            radioButton_Mobile9.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 10/online")){
                            radioButton_Mobile10.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 11/online")){
                            radioButton_Mobile11.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Mobile 12/online")){
                            radioButton_Mobile12.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Terminal 1/online")){
                            radioButton_Terminal1.setTextColor(getResources().getColor(R.color.light_green));
                        }
                        if (topic.equals("Terminal 2/online")){
                            radioButton_Terminal2.setTextColor(getResources().getColor(R.color.light_green));
                        }
                    }

                    if (Payload.equals("False")) {
                        if (topic.equals("Mobile 1/online")){
                            radioButton_Mobile1.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile1.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 2/online")){
                            radioButton_Mobile2.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile2.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 3/online")){
                            radioButton_Mobile3.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile3.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 4/online")){
                            radioButton_Mobile4.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile4.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 5/online")){
                            radioButton_Mobile5.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile5.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 6/online")){
                            radioButton_Mobile6.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile6.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 7/online")){
                            radioButton_Mobile7.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile7.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 8/online")){
                            radioButton_Mobile8.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile8.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 9/online")){
                            radioButton_Mobile9.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile9.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 10/online")){
                            radioButton_Mobile10.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile10.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 11/online")){
                            radioButton_Mobile11.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile11.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Mobile 12/online")){
                            radioButton_Mobile12.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Mobile12.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Terminal 1/online")){
                            radioButton_Terminal1.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Terminal1.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                        if (topic.equals("Terminal 2/online")){
                            radioButton_Terminal2.setTextColor(getResources().getColor(R.color.light_red));
                            textView_status.append(get_timestamp() + radioButton_Terminal2.getText().toString()
                                    + " has lost connection."  + "\n");
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i(TAG, "message delivery completed");
                }
            });
        } catch (MqttException e) {
            Log.i(TAG, "something happened");
        }
    }


    public void startPing () {
        Connection_Check_IP ping = new Connection_Check_IP(this);
        ping.execute(IP_Adress);
    }


    private static class Connection_Check_IP extends AsyncTask<String,String,String> {
        //class for running ping operation outside the main thread.
        private WeakReference<MainActivity> activityWeakReference;

        Connection_Check_IP(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.textView_status.append(activity.get_timestamp() + " trying to reach the Server at "
                    + activity.IP_Adress + ". Please wait..." + "\n");

        }


        @Override
        protected String doInBackground(String... strings) {

            boolean reachable = false;
            String Message = "";


            try {
                reachable = InetAddress.getByName(strings[0]).isReachable(5000);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (reachable) {
                Message = "online";
            } else {
                Message = "offline";
            }

            return Message;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            if (s.equals("online")) {
                activity.textView_status.append(activity.get_timestamp() + " Server " +
                        activity.IP_Adress + " is online." + "\n");

                //connect to MQTT broker
                activity.mqtt_connect(activity.MQTT_Broker, activity.MQTT_User, activity.MQTT_PassKey);

                activity.reachable = true;



            } else {
                activity.textView_status.append(activity.get_timestamp() + " Server " +
                        activity.IP_Adress + " is offline! Please check the configuration." + "\n");
            }
        }
    }


    private void restore_default_values() {
        //resort values
        IP_Adress = default_IP_Adress;
        Port = default_Port;
        MQTT_Broker = default_MQTT_Broker;
        MQTT_User = default_MQTT_User;
        MQTT_PassKey = default_MQTT_PassKey;

        //display restored values
        editText_IP.setText(IP_Adress);
        editText_Port.setText(Port);
        editText_Username.setText(MQTT_User);
        editText_Password.setText(MQTT_PassKey);
    }


    private void apply_Changes() {
        // deconstruct old client
        client.close();
        reachable = false;

        //assign new values to key variables
        IP_Adress = editText_IP.getText().toString();
        Port = editText_Port.getText().toString();
        MQTT_User = editText_Username.getText().toString();
        MQTT_PassKey = editText_Password.getText().toString();

        //construct Broker address
        MQTT_Broker = "tcp://" + IP_Adress + ":" + Port;

        //reconnect using new data
        startPing();
        (new Handler()).postDelayed(this::start_online_Monitors, 5000);
    }


    public void start_online_Monitors() {
        if (reachable) {
            online_status(radioButton_Mobile1);
            online_status(radioButton_Mobile2);
            online_status(radioButton_Mobile3);
            online_status(radioButton_Mobile4);
            online_status(radioButton_Mobile5);
            online_status(radioButton_Mobile6);
            online_status(radioButton_Mobile7);
            online_status(radioButton_Mobile8);
            online_status(radioButton_Mobile9);
            online_status(radioButton_Mobile10);
            online_status(radioButton_Mobile11);
            online_status(radioButton_Mobile12);
            online_status(radioButton_Terminal1);
            online_status(radioButton_Terminal2);
        }
    }

    public void openWebURL( String inURL ) {
        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( inURL ) );

        startActivity( browse );
    }

    private void scrollToBottom_textView_status() {

        nestedScrollView_status.post(new Runnable() {
            @Override
            public void run() {nestedScrollView_status.fullScroll(View.FOCUS_DOWN);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private String get_timestamp() {
        String timeStamp = new SimpleDateFormat("dd/MM/yy_HH:mm:ss").format(Calendar.getInstance().getTime());
        return timeStamp + ":";
    }
}
