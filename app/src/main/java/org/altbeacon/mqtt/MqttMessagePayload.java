package org.altbeacon.mqtt;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class MqttMessagePayload {
    private Set<String> beaconIds;
    private String status;

    public MqttMessagePayload() {
        this.beaconIds = new HashSet<>();
    }

    public static final String EXIT = "exit";
    public static final String ENTER = "enter";
    public static final String EXIST = "exist";

    public void setProperties(String status, Set<String> beaconIds) {
        this.beaconIds.addAll(beaconIds);
        this.status = status;
    }

    public Set<String> getBeaconIds() {
        return beaconIds;
    }

    public String getStatus() {
        return status;
    }

}
