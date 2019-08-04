package com.rover.bigbywolf.rovercontroller.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

public class FlightControlMessage implements DroneMessage {

    public final static String messageId = "flight";


    /**
     * All values must be between -1 to 1
     * @param throttle
     * @param rudder
     * @param elevator
     * @param aileron
     * @return
     * @throws JSONException
     */
    public static String sendFlightControls(double throttle, double rudder, double elevator,
                                            double aileron)
            throws JSONException {

        JSONObject object = new JSONObject();
        object.put("messageId", messageId);
        object.put("throttle", throttle);
        object.put("rudder", rudder);
        object.put("elevator", elevator);
        object.put("aileron", aileron);
        return object.toString() + MESSAGE_TERMINATOR;
    }

    public static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static String joyStickToFlightControl(int leftAngle, int leftStrength, int rightAngle,
                                                 int rightStrength) throws JSONException {

        double leftRad = Math.toRadians(leftAngle);
        double rightRad = Math.toRadians(rightAngle);

        double throttle = (Math.sin(leftRad) * leftStrength) / 100.0;
        double rudder = (Math.cos(leftRad) * leftStrength) / 100.0;

        double elevator = (Math.sin(rightRad) * rightStrength) / 100.0;
        double aileron = (Math.cos(rightRad) * rightStrength) / 100.0;

        return sendFlightControls(roundAvoid(throttle, 3), roundAvoid(rudder, 3),
                roundAvoid(elevator, 3), roundAvoid(aileron, 3));
    }
}
