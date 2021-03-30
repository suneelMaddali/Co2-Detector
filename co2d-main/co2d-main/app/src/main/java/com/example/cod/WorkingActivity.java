package com.example.cod;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkingActivity extends AppCompatActivity {

    TextView textView2, textView4;
    Call<ResponseBody> call;
    Timer updateCo2Timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.working_activity);
        // String clientId = MqttClient.generateClientId();
        //  MqttAndroidClient mqttAndroidClient =  new MqttAndroidClient(getApplicationContext(), "tcp://broker.hivemq.com:1883",
        //         clientId);
        textView2 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.testView4);

        startTimerCo2Update();

    }

    private void startTimerCo2Update() {
        updateCo2Timer = new Timer();

        updateCo2Timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Run every 3 seconds
                getHeroes();
                Log.d("RESPONSE", "CALLS EVERY 3 SECONDS");
            }
        }, 3000, 2000);
    }


    private void getHeroes() {
        // String afterTime = Utils.getInstance().getTheTimeNow(24L * 60L * 60L);

        call = RetrofitClient.getInstance().getMyApi().getCensorData(25, "-received_at", "up.uplink_message.decoded_payload");
        Log.d("RESPONSE", "CALLING API RESPONSE");
        
        call.clone().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String resp = response.body().string();
                    List<String> respList = Arrays.asList(resp.split("\\n\\n"));
                    resp = String.join(",", respList);
                    resp = "[" + resp + "]";

                    //Creating an String array for the ListView
                    TypeToken<List<Result>> resultToken = new TypeToken<List<Result>>() {
                    };

                    List<Result> list = new Gson().fromJson(resp, resultToken.getType());
                    Log.d("RESONSE", list.toString());
                    //looping through all the heroes and inserting the names inside the string array
                    if (!list.isEmpty()) {
                        Double temperature = 0.0;

                        for (Result result : list) {
                            temperature = result.getResult().getUplink_message().getDecoded_payload().getTemperature();
                            textView2.setText("current co2 value " + temperature);
                        }

                        if (temperature >= 30.0) {
                            Toast.makeText(getApplicationContext(), "CHECKING...", Toast.LENGTH_SHORT).show();
                            ringAlarm();
                        }

                        textView4.setText(String.valueOf(temperature));

                    } else {
                        Toast.makeText(WorkingActivity.this, "Value Returned is Empty!", Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    Log.d("RESPONSE", "Something WENT WRONG");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //play alaram sound
    //call this method for rining the alaram sound when co2 levels are reached...
    void ringAlarm() {
        Toast.makeText(getApplicationContext(), "DANGER", Toast.LENGTH_LONG).show();
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startTimerCo2Update();
    }

    @Override
    protected void onStop() {
        super.onStop();

        updateCo2Timer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateCo2Timer.cancel();
    }

    //connect to mqtt
    void connectToMqtt(final MqttAndroidClient client) {
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    subscribeToMqttChannel(client);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void subscribeToMqttChannel(MqttAndroidClient client) {
        String topic = "demo";
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos, iMqttMessageListener);

            /*subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Timber.d("Mqtt channel subscribe success");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Timber.d("Mqtt channel subscribe error %s",exception.getMessage());
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                }
            });
            */
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    // this is the main method which reads data from mqtt, co2 values
    IMqttMessageListener iMqttMessageListener = new IMqttMessageListener() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            JSONObject signallStatus = new JSONObject(message.toString());

        }
    };

    void unSubscribeMqttChannel(MqttAndroidClient client) {
        final String topic = "demo";
        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                    Toast.makeText(getApplicationContext(), "The subscription could successfully be removed from the client", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                    Toast.makeText(getApplicationContext(), "some error occurred", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //disconect mqtt
    void disconnectMqtt(MqttAndroidClient client) {
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(getApplicationContext(), "something went wrong", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}

