package losonczylab.behaviormate.util;

import losonczylab.behaviormate.BehaviorMate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public final class JSONHelper {

    public static JSONObject createNewEndMessage(String context, String id) throws JSONException {
        JSONObject end_msg = new JSONObject();
        end_msg.put(context, id);
        end_msg.put("action", "clear");
        return end_msg;
    }

    public static int[] JSONArrayToIntArray(JSONArray JSONArray) {
        int[] intArray = new int[JSONArray.length()];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = JSONArray.optInt(i);
        }
        return intArray;
    }

    public static String[] JSONArrayToStringArray(JSONArray JSONArray) {
        String[] stringArray = new String[JSONArray.length()];
        for (int i = 0; i < stringArray.length; i++) {
            stringArray[i] = JSONArray.optString(i);
        }
        return stringArray;
    }

    public static String readFile(String filename) {
        StringBuilder jsonData = new StringBuilder();
        BufferedReader br;
        try {
            String line;
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                if (!line.trim().startsWith("//")) {
                    jsonData.append(line.split("//")[0]).append("\n");
                }
            }
        } catch (IOException e) {
            BehaviorMate.showError(String.format("Error reading file: \"%s\" \n", filename));
        }
        return jsonData.toString();
    }

    /**
     * Placeholder
     *
     * @param filename Name of the file to be parsed, including the extension.
     * @return ?
     */
    public static JSONObject parseJsonFile(String filename) {
        try {
            // What does this regular expression do?
            String extension = filename.replaceAll("^.*\\.(.*)$", "$1");
            if (extension.equals("tdml")) {
                return parseTdmlSettings(filename);
            }

            JSONObject jsonObj = null;
            try {
                jsonObj = new JSONObject(readFile(filename));
            } catch (JSONException e) {
                BehaviorMate.showError(String.format("Error reading file: \"%s\" \n", filename));
            }
            return jsonObj;
        } catch (Exception e) {
            BehaviorMate.showError(String.format("Error reading file: \"%s\" \n", filename));
            return null;
        }
    }

    /**
     * ?
     *
     * @param filename ?
     * @param tag ?
     * @return ?
     * @throws JSONException ?
     */
    public static JSONObject findSettings(String filename, String tag) throws JSONException {
        JSONObject settings = parseJsonFile(filename, tag);
        if (settings.isNull("uses")) {
            return settings;
        }

        JSONArray settings_names = null;
        try {
            settings_names = settings.getJSONArray("uses");
        } catch (JSONException ignored) { }

        if (settings_names != null) {
            for (int i = 0; i < settings_names.length(); i++) {
                JSONObject settings_update;
                try {
                    JSONObject settings_info = settings_names.getJSONObject(i);
                    settings_update = findSettings(settings_info.getString("file"), settings_info.getString("tag"));
                } catch (JSONException e) {
                    settings_update = findSettings(filename, settings_names.getString(i));
                }

                Iterator<String> key_itr = settings.keys();
                while (key_itr.hasNext()) {
                    String key = key_itr.next();
                    settings_update.put(key, settings.get(key));
                }
                settings = settings_update;
            }
        } else {
            JSONObject settings_update;
            try {
                JSONObject settings_info = settings.getJSONObject("uses");
                settings_update = findSettings(
                        settings_info.getString("file"), settings_info.getString("tag"));
            } catch (JSONException e) {
                settings_update = findSettings(filename, settings.getString("uses"));
            }

            Iterator<String> key_itr = settings.keys();
            while (key_itr.hasNext()) {
                String key = key_itr.next();
                settings_update.put(key, settings.get(key));
            }
            settings = settings_update;
        }

        return settings;
    }

    /**
     * Extracts the JSON literal with the key <tt>tag</tt> from the file named <tt>filename</tt>.
     *
     * @param filename Name of the file to be parsed, including the extension.
     * @param tag Key of the JSON literal to be extracted.
     * @return The JSON literal as <code>JSONObject</code> if the key is found, otherwise
     * <code>null</code>.
     */
    static JSONObject parseJsonFile(String filename, String tag) {
        try {
            JSONObject json  = parseJsonFile(filename);
            try {
                json = json.getJSONObject(tag);
            } catch (JSONException e) {
                String message = "Failed to find tag: " + tag + "\n" + e;
                BehaviorMate.showError(message);
                json = null;
            }
            return json;
        } catch (Exception e) {
            BehaviorMate.showError(String.format("Error reading tag: \"%s\" from file: \"%s\" \n", tag, filename));
            return null;
        }
    }

    public static void loadSettings() throws JSONException {
        String settingsFileName = BehaviorMate.getSettingsFileName();
        JSONObject jsonObj = parseJsonFile(settingsFileName);

        String tag = "";
        Iterator<?> keys = jsonObj.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            //System.out.printf("SettingsLoader(): key = %s \n\n", key);
            if (!key.startsWith("_")) {
                tag = key;
                break;
            }
        }

        JSONObject settings = parseJsonFile(settingsFileName, tag);
        if (!settings.isNull("uses")) {
            JSONArray settings_names = null;
            try {
                settings_names = settings.getJSONArray("uses");
            } catch (JSONException ignored) {
            }

            if (settings_names != null) {
                for (int i = 0; i < settings_names.length(); i++) {
                    JSONObject settings_update;
                    try {
                        JSONObject settings_info = settings_names.getJSONObject(i);
                        settings_update = findSettings(settings_info.getString("file"), settings_info.getString("tag"));
                    } catch (JSONException e) {
                        settings_update = findSettings(settingsFileName, settings_names.getString(i));
                    }

                    Iterator<String> key_itr = settings.keys();
                    while (key_itr.hasNext()) {
                        String key = key_itr.next();
                        settings_update.put(key, settings.get(key));
                    }
                    settings = settings_update;
                }
            } else {
                JSONObject settings_update;
                try {
                    JSONObject settings_info = settings.getJSONObject("uses");
                    settings_update = findSettings(
                            settings_info.getString("file"), settings_info.getString("tag"));
                } catch (JSONException e) {
                    settings_update = findSettings(settingsFileName, settings.getString("uses"));
                }

                Iterator<String> key_itr = settings.keys();
                while (key_itr.hasNext()) {
                    String key = key_itr.next();
                    settings_update.put(key, settings.get(key));
                }
                settings = settings_update;
            }
        }

        JSONObject system_settings = parseJsonFile(settingsFileName, "_system");
        if (system_settings == null) {
            system_settings = parseJsonFile(BehaviorMate.DEFAULT_SETTINGS_FILE, "_system");
        }

        BehaviorMate.setMainBehaviorMateSettings(settings);
        BehaviorMate.setSystemBehaviorMateSettings(system_settings);
    }

    /**
     * ?
     *
     * @param filename Name of the file to be parsed, including the extension.
     * @return ?
     */
    private static JSONObject parseTdmlSettings(String filename) {
        String jsonData = "";
        BufferedReader br;
        try {
            String line;
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                if (!line.trim().startsWith("//")) {
                    jsonData = line.split("//")[0] + "\n";
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(jsonData);
                        if (!jsonObject.isNull("settings")) {
                            return jsonObject;
                        }
                    } catch(JSONException ignored) { }
                }
            }
        } catch (IOException e) {
            BehaviorMate.showError(String.format("Error reading file: \"%s\" \n", filename));
        }
        return null;
    }

    /**
     * ?
     *
     * @param fields ?
     * @return ?
     */
    private JSONObject parseJSONFields(JSONArray fields) throws JSONException {
        JSONObject value = new JSONObject();
        for (int i=0; i < fields.length(); i++) {
            JSONObject setting = fields.getJSONObject(i);
            String key = setting.getString("key");
            String type = setting.getString("type");

            switch (type) {
                case "String":
                    value.put(key, setting.getString("value"));
                    break;
                case "int":
                    value.put(key, setting.getInt("value"));
                    break;
                case "float":
                    value.put(key, setting.getDouble("value"));
                    break;
                case "JSONObject":
                    value.put(key, parseJSONFields(setting.getJSONArray("fields")));
                    break;
            }
        }

        return value;
    }



    /**
     * Generates the JSONObject necessary to create a valve.
     *
     * @param  pin Pin number to set up the valve on.
     * @return     JSONObject the arduino will use to configure the valve.
     */
    public static JSONObject setup_valve_json(int pin) throws JSONException {
        JSONObject valve_json = new JSONObject();
        JSONObject valve_subjson = new JSONObject();
        valve_subjson.put("pin", pin);
        valve_subjson.put("action", "create");
        valve_json.put("valves", valve_subjson);

        return valve_json;
    }

    /**
     * ?
     *
     * @param pin ?
     * @param inverted ?
     * @ ?
     */
    public static JSONObject setup_valve_json(int pin, boolean inverted) throws JSONException {
        JSONObject valve_json = new JSONObject();
        JSONObject valve_subjson = new JSONObject();
        valve_subjson.put("pin", pin);
        valve_subjson.put("action","create");
        if (inverted) {
            valve_subjson.put("inverted", true);
        }
        valve_json.put("valves", valve_subjson);

        return valve_json;
    }

    /**
     * ?
     * @param pin ?
     * @param frequency ?
     * @return ?
     */
    public static JSONObject setup_valve_json(int pin, int frequency) throws JSONException {
        JSONObject valve_json = new JSONObject();
        JSONObject valve_subjson = new JSONObject();
        valve_subjson.put("pin", pin);
        valve_subjson.put("frequency", frequency);
        valve_subjson.put("type", "tone");
        valve_subjson.put("action", "create");
        valve_json.put("valves", valve_subjson);

        return valve_json;
    }

    /**
     * Generates the JSONObject necessary to close a valve.
     *
     * @param  pin Pin number of the valve to close.
     * @return     JSONObject the arduino will use to close the valve.
     */
    public static JSONObject close_valve_json(int pin) throws JSONException {
        JSONObject valve_json = new JSONObject();
        JSONObject valve_subjson = new JSONObject();
        valve_subjson.put("pin", pin);
        valve_subjson.put("action","close");
        valve_json.put("valves", valve_subjson);

        return valve_json;
    }

    /**
     * Generates the JSONObject necessary to open a valve. Assumes valve has already been configured.
     *
     * @param  pin      Pin number of the valve to open.
     * @param  duration Amount of time to keep the valve open in milliseconds.
     * @return          JSONObject the arduino will use to open the valve.
     */
    public static JSONObject open_valve_json(int pin, int duration) throws JSONException {
        JSONObject valve_json = new JSONObject();
        JSONObject valve_subjson = new JSONObject();

        valve_subjson.put("pin", pin);
        valve_subjson.put("action","open");
        valve_subjson.put("duration",duration);
        valve_json.put("valves", valve_subjson);

        return valve_json;
    }

//    /**
//     * ?
//     *
//     * @param orig ?
//     * @param update_fields ?
//     * @return ?
//     */
//    private JSONObject mergeJSONFields(JSONObject orig, JSONArray update_fields) throws JSONException {
//        for (int i = 0; i < update_fields.length(); i++) {
//            JSONObject setting = update_fields.getJSONObject(i);
//            String key = setting.getString("key");
//            String type = setting.getString("type");
//
//            switch (type) {
//                case "String":
//                    orig.put(key, setting.getString("value"));
//                    break;
//                case "int":
//                    orig.put(key, setting.getInt("value"));
//                    break;
//                case "float":
//                    orig.put(key, setting.getDouble("value"));
//                    break;
//                case "JSONObject":
//                    if (!orig.isNull(key)) {
//                        orig.put(key, mergeJSONFields(orig.getJSONObject(key), setting.getJSONArray("fields")));
//                    } else {
//                        orig.put(key, parseJSONFields(setting.getJSONArray("fields")));
//                    }
//                    break;
//                case "JSONArray":
//                    orig.put(key, setting.getJSONArray("value"));
//                    break;
//            }
//        }
//
//        return orig;
//    }

}
