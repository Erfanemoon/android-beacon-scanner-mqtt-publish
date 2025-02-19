package org.altbeacon.beaconreference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.EditText;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

public class RangingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private SharedPreferences beaconPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences("beaconsDB", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {

        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  " + beacons.size());
                    Set<String> beaconSet = new HashSet<>();

                    Set<Beacon> tmp = new HashSet<>(beacons);
                    for (Beacon beacon : tmp)
                        beaconSet.add(beacon.getId1().toString());


                    //Beacon firstBeacon = beacons.iterator().next();
                    //TODO read data of all beacons
                    try {
                        beaconPreferences = getSharedPreferences("beaconsDB", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = beaconPreferences.edit();
                        if (!beaconSet.isEmpty()) {

                            edit.putStringSet("beacon_ids", beaconSet);
                            edit.apply();
                        }

                        logToDisplay(beacons.iterator().next().toString() + " is about " + beacons.iterator().next().getDistance() + "meters away");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }

        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);

        } catch (RemoteException e) {
        }
    }

    private void logToDisplay(final String line) {
        // runOnUiThread(new Runnable() {
        //   public void run() {
        EditText editText = (EditText) RangingActivity.this.findViewById(R.id.rangingText);
        editText.append(line + "\n");
    }
    // });
    //}
}
