package com.ehenn.probenmonitoring;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    //variables
    private static final String TAG = "MainActivity";
    private Button button_connect;
    private Button button_publish;
    private Button button_subscribe;
    private Button button_scan_pkid;
    private Button button_commit_pkid;
    private Button button_start_online_Monitor;
    private TextView textView_status;
    private TextView textView_content;
    private TextView textView_DevPlatform;
    private EditText editText_topic;
    private NestedScrollView nestedScrollView_status;
    private NestedScrollView nestedScrollView_content;


    MqttAndroidClient client;

    String MQTT_Broker = "tcp://192.168.178.53:1883";
    String MQTT_User = null;
    String MQTT_PassKey = null;
    String PKID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //TODO: integrate Login Information
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GUI Assignment
        button_connect = findViewById(R.id.Button_Connect);
        button_publish = findViewById(R.id.Button_Publish);
        button_subscribe = findViewById(R.id.Button_Subscribe);
        button_scan_pkid = findViewById(R.id.Button_Scan_PKID);
        button_commit_pkid = findViewById(R.id.Button_Commit_PKID);
        button_start_online_Monitor = findViewById(R.id.Button_start_online_Monitor);
        textView_status = findViewById(R.id.TextView_Status);
        textView_content = findViewById(R.id.TextView_Content);
        textView_DevPlatform = findViewById(R.id.TextView_DevPlatform);
        editText_topic = findViewById(R.id.EditText_Topic);
        nestedScrollView_status = findViewById(R.id.nestedScrollView_status);
        nestedScrollView_content = findViewById(R.id.nestedScrollView_Content);

        //set OnClickListeners
        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqtt_connect(MQTT_Broker, MQTT_User, MQTT_PassKey);
            }
        });

        button_publish.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                String Topic = editText_topic.getText().toString();
                mqtt_publish_message(Topic, "Hallo Welt");
            }
        });

        button_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqtt_subscribe_topic("Steuerung/PKID");
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
                String topic = editText_topic.getText().toString();
                mqtt_publish_message(topic, PKID);
            }
        });

        button_start_online_Monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                online_status("DevPlatform/online");
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2)
        {
            PKID=data.getStringExtra("PKID");
            textView_status.append(get_timestamp() + " PKID: " + PKID + "\n");
        }
    }


    public void mqtt_connect(String mqtt_broker, String mqtt_user, String mqtt_passkey) {

        //TODO: stürtzt ab nach einiger zeit

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://192.168.178.53:1883",
                        clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("detact");
        options.setPassword("detact#1234".toCharArray());

        textView_status.append("aproching try block");

        try {
            IMqttToken token = client.connect(options);
            textView_status.append("in try block 1");

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
                    textView_content.append(get_timestamp() + " " + new String(message.getPayload())  + "\n");
                    scrollToBottom_textView_content();
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


    public void online_status(String topic) {

        try {
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
                    String Payload = new String(message.getPayload());
                    Log.i(TAG, "a message has arrived");
                    textView_content.append(get_timestamp() + " " + (Payload + "\n"));
                    scrollToBottom_textView_content();
                    if (Payload.equals("True")) {
                        textView_DevPlatform.setBackgroundColor(getResources().getColor(R.color.teal_700));}
                    else {
                        textView_DevPlatform.setBackgroundColor(getResources().getColor(R.color.dark_grey));
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

    private void scrollToBottom_textView_status() {

        nestedScrollView_status.post(new Runnable() {
            @Override
            public void run() {
                nestedScrollView_status.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void scrollToBottom_textView_content() {

        nestedScrollView_content.post(new Runnable() {
            @Override
            public void run() {
                nestedScrollView_content.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String get_timestamp() {
        String timeStamp = new SimpleDateFormat("dd/MM/yy_HH:mm:ss").format(Calendar.getInstance().getTime());
        return timeStamp + ":";
    }
}
