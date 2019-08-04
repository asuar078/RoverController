package com.rover.bigbywolf.rovercontroller.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

public class ControlMessage implements DroneMessage {


    public static String sendArmMessage() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("messageId", "arm");
        return object.toString() + MESSAGE_TERMINATOR;
    }

    public static String sendLandMessage() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("messageId", "land");
        return object.toString() + MESSAGE_TERMINATOR;
    }

    public static String sendAltCtlMessage() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("messageId", "alt_ctl");
        return object.toString() + MESSAGE_TERMINATOR;
    }

    public static String sendManualCtlMessage() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("messageId", "manual_ctl");
        return object.toString() + MESSAGE_TERMINATOR;
    }
}


