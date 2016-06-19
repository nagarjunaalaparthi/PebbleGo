package com.pebblego;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Arjun.
 */
public class DataSynchService extends Service {
    private PebbleKit.PebbleDataReceiver pebbleDataReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        //TODO do something useful
        startServiceinForeGround();
        final UUID appUuid = UUID.fromString("30a113fd-7937-4ca3-8b18-dce66da2979f");
        if (pebbleDataReceiver == null) {
            pebbleDataReceiver = new PebbleKit.PebbleDataReceiver(appUuid) {
                @Override
                public void receiveData(Context context, int id, PebbleDictionary data) {
                    // Message received, over!
                    int stepcount = 0;
                    int newStepCount = -1;
                    PebbleKit.sendAckToPebble(context, id);
//                    Toast.makeText(MainActivity.this,"data "+data.toJsonString(),Toast.LENGTH_LONG).show();
                    Log.i("datais", "int: " + id + " dic : " + data.toString());
                    try {
                        JSONArray array = new JSONArray(data.toJsonString());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(0);
                            if (object.has("key")) {
                                if (object.optInt("key") == 1) {
                                    stepcount = object.getInt("value");
                                    SharedPreferenceUtils.writeInteger(DataSynchService.this, "step_count", stepcount);
                                    Intent broadCastIntent = new Intent("com.pebblego.steps");
                                    broadCastIntent.putExtra("value", stepcount);
                                    sendBroadcast(broadCastIntent);
                                    startServiceinForeGround();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (newStepCount != stepcount) {
                        int goalCount = Integer.parseInt(SharedPreferenceUtils.readString(context, "count", "0"));
                        if (goalCount < stepcount) {
                            sendNotificationToPebble();
                            sendNotificationToUser();
                        }
                        newStepCount = stepcount;
                        PebbleDictionary dic = new PebbleDictionary();
                        dic.addString(0, "values");
                        PebbleKit.sendDataToPebble(DataSynchService.this, appUuid, dic);
                    }
//            PebbleKit.sendAckToPebble(context, transactionId);
                }


            };
        }
        if (PebbleKit.isWatchConnected(DataSynchService.this)) {
            PebbleKit.registerReceivedDataHandler(DataSynchService.this, pebbleDataReceiver);
        }
        return Service.START_STICKY;
    }

    private void sendNotificationToUser() {
        int smallIcon = 0;
        smallIcon = R.drawable.pebble_go;
        NotificationManager mNotificationManager = (NotificationManager) DataSynchService.this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(DataSynchService.this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(DataSynchService.this, 5, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(DataSynchService.this).setSmallIcon(smallIcon).setContentTitle(getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle()).setContentText("Hurray!!! Goal Completed");
        mBuilder.setContentIntent(contentIntent);
//        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setSound(null);
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        mBuilder.getNotification().flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        mNotificationManager.notify(7, mBuilder.build());
    }

    private void startServiceinForeGround() {
        int smallIcon = 0;
        smallIcon = R.drawable.pebble_go;
        String stepCount = SharedPreferenceUtils.readInteger(DataSynchService.this, "step_count", 0) + " steps";

        Intent notificationIntent = new Intent();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(DataSynchService.this, 5, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(DataSynchService.this).setSmallIcon(smallIcon).setContentTitle(getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle()).setContentText(stepCount);
        // mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setAutoCancel(true);
//        if(!isAppForeground(AlarmService.this)) {
//            mBuilder.setContentIntent(contentIntent);
//        }
        mBuilder.setPriority(Notification.PRIORITY_MIN);
        mBuilder.getNotification().flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        startForeground(9, mBuilder.build());
    }

    public void sendNotificationToPebble() {
        // Push a notification
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

        final Map data = new HashMap();
        data.put("title", "Hurray!");
        data.put("body", "You have reached the goal today!!! Keep going on...");
        final JSONObject jsonData = new JSONObject(data);
        final String notificationData = new JSONArray().put(jsonData).toString();

        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", "PebbleKit Android");
        i.putExtra("notificationData", notificationData);
        sendBroadcast(i);
    }
}
