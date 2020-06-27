package org.altbeacon.mqtt;

import android.content.Context;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class MqttPublisher implements MqttCallback {
    String TAG = this.getClass().getSimpleName();
    private MqttClient client;
    private String clientId;

    public MqttPublisher() {
        this.clientId = MqttClient.generateClientId();
    }


    public void connect(Context applicationContext) {
//        String mqtt = "mqtt://";
//        this.client =
//                new MqttAndroidClient(applicationContext, "tcp://192.168.1.141:1883",
//                        clientId, new MemoryPersistence());
        try {
            this.client = new MqttClient("tcp://192.168.1.141:1883", this.clientId, new MemoryPersistence());
            client.setCallback(this);
            client.connect();
            client.subscribe("/floors/1/");
        } catch (MqttException e) {
            e.printStackTrace();
        }


//        try {
//            IMqttToken token = client.connect();
//            token.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    // We are connected
//                    Log.d(TAG, "successfully connected");
//                    Log.d(TAG, clientId + "     *****************************************   ");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    // Something went wrong e.g. connection timeout or firewall problems
//                    Log.d(TAG, "connection failed");
//
//                }
//            });
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
    }

    //TODO attach if necessary
//    public void subscribeToBroker() {
//        String topic = "/floors/1/";
//        int qos = 1;
//        try {
//            IMqttToken subToken = client.subscribe(topic, qos);
//            subToken.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    // The message was published
//                    Log.d(TAG, "Sbuscribed Successfully");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken,
//                                      Throwable exception) {
//                    // The subscription could not be performed, maybe the user was not
//                    // authorized to subscribe on the specified topic e.g. using wildcards
//                    Log.d(TAG, "Sbuscription failed");
//                }
//            });
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }


    public void publishToBroker(MqttMessagePayload msgPayload) {
        String payload = jsonMsg(msgPayload, msgPayload.getBeaconIds());
        try {
            String topic = "/floors/1/";
            byte[] bytes = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(bytes);
            this.client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    private String jsonMsg(MqttMessagePayload payload, Set<String> beaconIds) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", this.clientId);
            jsonObject.put("status", payload.getStatus());
            jsonObject.put("beaconIds", beaconIds);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
