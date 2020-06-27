package org.altbeacon.beaconreference;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import android.content.SharedPreferences;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.mqtt.MqttMessagePayload;
import org.altbeacon.mqtt.MqttPublisher;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dyoung on 12/13/13.
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private MonitoringActivity monitoringActivity = null;
    private String cumulativeLog = "";
    private Set<String> beaconIDsInRegion = new HashSet<>();
    private Set<String> beaconIDsOutRegion = new HashSet<>();
    MqttPublisher mqttPublisher;

    public void onCreate() {

        super.onCreate();
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManager.setDebug(true);
        this.mqttPublisher = new MqttPublisher();

        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        backgroundPowerSaver = new BackgroundPowerSaver(this);

    }

    public void disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
            regionBootstrap = null;
        }
    }

    public void enableMonitoring() {
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }


    @Override
    public void didEnterRegion(Region arg0) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "did enter region.");
        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity");

            // The very first time since boot that we detect an beacon, we launch the
            // MainActivity
            Intent intent = new Intent(this, MonitoringActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
            this.startActivity(intent);
            this.beaconIDsInRegion = readBeaconIDsFromSharedPrefs();
            //TODO add mqttData
            publishToBroker(MqttMessagePayload.ENTER, this.beaconIDsInRegion);
            haveDetectedBeaconsSinceBoot = true;
        } else {
            if (monitoringActivity != null) {
                // If the Monitoring Activity is visible, we log info about the beacons we have
                // seen on its display
                logToDisplay("I see a beacon again");
            } else {
                // If we have already seen beacons before, but the monitoring activity is not in
                // the foreground, we send a notification to the user on subsequent detections.
                Log.d(TAG, "Sending notification.");
                sendNotification();
            }
        }


    }

    @Override
    public void didExitRegion(Region region) {
        this.beaconIDsOutRegion = readBeaconIDsFromSharedPrefs();
        Collection<String> subtract = CollectionUtils.subtract(this.beaconIDsOutRegion, this.beaconIDsInRegion);
        Set<String> substractSet = new HashSet<>();
        substractSet.addAll(subtract);
        //TODO send mqtt data;
        logToDisplay("I no longer see a beacon.");
        publishToBroker(MqttMessagePayload.EXIT, substractSet);
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        this.mqttPublisher.connect(BeaconReferenceApplication.this);
        this.beaconIDsInRegion = readBeaconIDsFromSharedPrefs();
        Log.d(TAG, this.beaconIDsInRegion.toString());
        logToDisplay("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE (" + state + ")"));
        publishToBroker(MqttMessagePayload.EXIST, this.beaconIDsInRegion);

    }

    private void publishToBroker(String status, Set<String> beaconIds) {
        MqttMessagePayload payload = new MqttMessagePayload();
        payload.setProperties(status, beaconIds);
        this.mqttPublisher.publishToBroker(payload);
    }

    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Beacon Reference Application")
                        .setContentText("An beacon is nearby.")
                        .setSmallIcon(R.drawable.ic_launcher);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MonitoringActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    public void setMonitoringActivity(MonitoringActivity activity) {
        this.monitoringActivity = activity;
    }

    private void logToDisplay(String line) {
        cumulativeLog += (line + "\n");
        if (this.monitoringActivity != null) {
            this.monitoringActivity.updateLog(cumulativeLog);
        }
    }

    public String getLog() {
        return cumulativeLog;
    }

    private Set<String> readBeaconIDsFromSharedPrefs() {
        SharedPreferences beaconsPref = getSharedPreferences("beaconsDB", Context.MODE_PRIVATE);
        return beaconsPref.getStringSet("beacon_ids", new HashSet<String>());
    }
}
