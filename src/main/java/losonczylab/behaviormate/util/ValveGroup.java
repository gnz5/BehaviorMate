package losonczylab.behaviormate.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public final class ValveGroup {
    private final class Valve {
        private int pin; // arduino pin the valve is controlled by
        private int state = Int.VALVE_OFF; // describes whether the valve is on or off
        private int count = 0; // number of times the valve has been opened
        private String id; // the id of the context list

        private Valve(int pin) {
            this.pin = pin;
        }

        private Valve(int pin, String id) {
            this.pin = pin;
            this.id = id;
        }

        private int getPin() {
            return pin;
        }

        private int getState() {
            return state;
        }

        private int getCount() {
            return count;
        }

        private String getId() {
            return id;
        }

        /**
         *
         * @param state 1 for on and -1 for off. Use the constants Int.VALVE_ON and Int.VALVE_OFF.
         */
        public void setState(int state) {
            this.state = state;
        }

        public void incrementCount() {
            count += 1;
        }
    }

    private ArrayList<Valve> valves;

    public ValveGroup() {
        valves = new ArrayList<Valve>();
    }

    public void addValve(int pin, String id) {
        Valve valve = getValveAtPin(pin);
        if (valve == null || valve.id.compareTo(id) != 0) {
            valve = new Valve(pin, id);
        }
        valves.add(valve);
    }

    private Valve getValveAtPin(int pin) {
        for (Valve valve : valves) {
            if (valve.getPin() == pin) {
                return valve;
            }
        }
        return null;
    }

    public void setValve(int pin, int state) {
        Valve valveToBeSet = getValveAtPin(pin);
        if (valveToBeSet == null) {
            valveToBeSet = new Valve(pin);
        }
        valveToBeSet.setState(state);

        if (state == Int.VALVE_ON) {
            valveToBeSet.incrementCount();
        }

        if (valves.contains(valveToBeSet)) {
            valves.remove(valveToBeSet);
        }
        valves.add(0, valveToBeSet);
    }

    public JSONArray getValves() {
        JSONArray valvesJSON = new JSONArray();
        for(int i = 0; i < valves.size(); i++) {
            Valve valve = valves.get(i);
            JSONObject valveJSON = new JSONObject();
            try {
                valveJSON.put(Str.PIN, valve.pin);
                valveJSON.put(Str.STATE, valve.state);
                valveJSON.put(Str.COUNT, valve.count);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            valvesJSON.put(valveJSON);
        }
        return  valvesJSON;
    }

    public void printValveInfo() {
        String output = "valves = [ ";
        for (ValveGroup.Valve valve : valves) {
            output += "<" + valve.getPin() + ", " + valve.getState() + ", " + valve.getCount() + ", " + valve.getId() + ">, ";
        }
        output += "] ";
        System.out.println(output);
    }
}

//    public HashMap<Integer, Integer> getStates() {
//        HashMap<Integer, Integer> valve_states = new HashMap<>();
//        for (Valve valve : valves) {
//            valve_states.put(valve.pin, valve.state);
//        }
//        return valve_states;
//    }

