package losonczylab.behaviormate.core;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.util.*;
import losonczylab.behaviormate.util.FileWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.lang.*;

import static losonczylab.behaviormate.BehaviorMate.main_settings;
import static losonczylab.behaviormate.BehaviorMate.system_settings;

/**
 * ?
 */
public final class TreadmillController {

    /**
     * Used to write to the behavior log and to the .tdml file.
     */
    public FileWriter fWriter;

    /**
     * Used to write to the behavior log of the previous trial
     */
    public FileWriter previousTrialfWriter;

    /**
     * ?
     */
    private final JSONObject[] msg_buffer = {null};

    /**
     * Used to receive position updates.
     */
    private UdpClient position_comm;

    /**
     * Used to receive behavior updates.
     */
    public UdpClient behavior_comm;

    /**
     * ?
     */
    public UdpClient reset_comm;

    /**
     * ?
     */
    private long last_reset_time;

    /**
     * ?
     */
    private ArrayList<UdpClient> comms;

    /**
     * ?
     */
    private long comms_check_time;

    /**
     * ?
     */
    private long comms_check_interval;

    /**
     * 1-D position of the mouse along track in millimeters.
     */
    private float position;

    /**
     * ?
     */
    private float offset_position;

    /**
     * ?
     */
    private boolean zero_position_boundary;

    /**
     * Distance run since last lap reset (allowed to be negative). Used to ensure animal is not
     * backing over reset tag.
     */
    private float distance;

    /**
     * ?
     */
    private float offset_distance;

    /**
     * Used to convert position updates from rotary encoder to millimeters traversed along the track.
     */
    private float position_scale;

    /**
     * ?
     */
    private float stored_position_scale;

    /**
     * The length of the track in millimeters. Set to the "track_length" property in the settings file.
     */
    private float track_length;

    /**
     * RFID tag string indicating that a lap has been completed and position should be reset to 0.
     */
    private String lap_tag;

    /**
     * Number of laps completed by the test animal.
     */
    private int lap_count;

    /**
     * ?
     */
    private boolean lock_lap;

    /**
     * ?
     */
    private int lick_count;

    /**
     * ?
     */
    private HashMap<Integer, Integer> sensor_counts;

    /**
     * Length of the trial in ?. Used to determine when the trial should end. Set to the value of the
     * <tt>trial_length</tt> property in the settings file.
     */
    // Todo: is this in seconds?
    private int trial_duration;

    /**
     * ?
     */
    private int lap_limit;

    /**
     * ?
     */
    private boolean position_reset;

    /**
     * ?
     */
    private boolean belt_calibration_mode;

    /**
     * ?
     */
    private float current_calibration;

    /**
     * ?
     */
    private int n_calibrations;

    /**
     * ?
     */
    private HashMap<Character, String> commentKeys;

    /**
     * ?
     */
    private int lap_offset;

    /**
     * A value of <code>indicates</code> the trial has started. Used to determine when contexts
     * should be enabled and if the file writer should be used.
     */
    private boolean started;

    /**
     * Pin number for the reward locations.
     */
    private int reward_valve;

    /**
     * Pin number lickport is attached to.
     */
    private int lickport_pin;

    /**
     * Read buffer for behavior messages.
     */
    private final JSONBuffer json_buffer = new JSONBuffer();

    /**
     * Date format for logging experiment start/stop
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * ?
     */
    public ArrayList<ContextList> contextLists;

    /**
     * ?
     */
    private int startTime;

    /**
     * ?
     */
    private float time;

    private boolean positionHasBeenZeroed = false;

    private boolean aContextHasBeenTriggered = false;

    private HashMap<String, Integer> rewardCounts;

    private int rewardCount;

    private float velocity;

    /**
     * This will be true if the TreadmillController has received a request to give a reward and the BehaviorMateController
     * has yet to display this reward. Once the reward has been displayed, this will be set to false.
     */
    private boolean rewardPendingDisplay;
    private boolean lickPendingDisplay;
    private ValveGroup valves;

    private JSONObject lastQuickComment;

    /**
     * ?
     *
     */
    public TreadmillController() {
        startTime = -1;
        trial_duration = -1;
        lap_limit = -1;
        position = -1;
        offset_position = -1;
        time = 0;
        distance = 0;
        offset_distance = 0;
        lap_count = 0;
        lick_count = 0;
        comms_check_time = 0;
        current_calibration = 0;
        lap_offset = 0;
        comms_check_interval = 60000;
        started = false;
        belt_calibration_mode = false;
        position_reset = false;
        zero_position_boundary = false;
        lock_lap = false;
        fWriter = null;
        lap_tag = "";
        sensor_counts = new HashMap<>();
        comms = new ArrayList<>();
        valves = new ValveGroup();
        prepareExitHandler();
        resetLastQuickComment();
    }

    /**
     * Starts a new experiment. Linked to clicking the "Start" button on the UI.
     * Creates a New Log file, makes the initial entries,
     * starts the timer, and triggers the sync pin to start
     * imaging.
     */
    public void Start(String mouse_name, String experiment_group) throws Exception {
        position_comm.clearQueuedMessages(); // clears all movement updates from before Start button was clicked

        if (mouse_name.equals("") || (experiment_group.equals(""))) {
            throw new Exception("TreadmillController.Start(): Error starting the experiment.");
        }
        position = offset_position;
        belt_calibration_mode = false;
        lick_count = 0;
        rewardCount = 0;
        lap_count = 0;

        if (fWriter != null) {
            fWriter.close();
        }

        String directory = system_settings.optString("data_directory", "data");
        fWriter = new FileWriter(directory, mouse_name);

        JSONObject info_msg = new JSONObject();
        JSONObject info_sub_msg = new JSONObject();
        info_sub_msg.put("action", "info");
        info_msg.put("communicator", info_sub_msg);

        addVersionAndSettingToTDMLFile(mouse_name, experiment_group);

        if (!main_settings.isNull("trial_startup")) {
            UdpHelper.sendMessages(main_settings.getJSONArray("trial_startup"), mouse_name);
        }

        for (ContextList context : contextLists) {
            context.trialStart();
            if (msg_buffer[0] != null) {
                JSONObject log_message = new JSONObject();
                log_message.put("behavior_mate", msg_buffer[0]);
                fWriter.write(log_message.toString().replace("\n", ""));
                msg_buffer[0] = null;
            }
        }

        testComms();
        startTime = ((int) System.currentTimeMillis());
        JSONObject valve_json = JSONHelper.open_valve_json(main_settings.getInt("sync_pin"), 100);
        behavior_comm.sendMessage(valve_json.toString());
        behavior_comm.sendMessage(info_msg.toString());
        started = true;
    }

    /**
     * Checks for changes in position of the mouse. Updates the class variables related to position and writes these
     * updates to the behavior file.
     */
    private void updatePosition() throws JSONException {
        if (position_comm == null) {
            System.out.println("position_comm is null");
            return;
        }

        boolean reset_lap = false;
        float dy = 0;
        JSONObject position_json = null;

        for (int i = 0; ( (i < 10) && position_comm.receiveMessage(json_buffer) ); i++) {
            if (dy != 0 && started && position_json != null) {
                fWriter.write(position_json.toString());
            }

            position_json = json_buffer.json.getJSONObject(position_comm.getId());

            if (!position_json.isNull("lap_reset")) {
                if (position_reset) {
                    reset_lap = true;
                    break;
                }
            }

            if (!position_json.isNull("position")) {
                dy += position_json.getJSONObject("position").optDouble("dy", 0);
            } else if (started) {
                json_buffer.json.put("time", time);
                fWriter.write(json_buffer.json.toString());
            }
        }

        dy /= position_scale;
        velocity = dy;

        if ((dy == 0) && !reset_lap) {
            return;
        }

        distance += dy;
        offset_distance += dy;
        if ((position != -1) && (!((zero_position_boundary) && (position + dy < 0)))) {
            if ((position + dy) < 0) {
                position += track_length;
            }
            position += dy;
            offset_position += dy;
            if (offset_position >= track_length) {
                offset_position -= track_length;
                if (offset_distance > track_length/2) {
                    resetLap("");
                    offset_distance = 0;
                }
            } else if (offset_position < 0 ) {
                offset_position += track_length;
            }
        }

        if (reset_lap) {
            if (position == -1) {
                position = 0;
                offset_position = lap_offset;
                distance = 0;
                // check that this is a legitimate lap reset read
            } else if (distance > track_length/2) {
                // position == -1 means that the lap reader has been initialized since BehviorMate was started yet.
                if (belt_calibration_mode) {
                    current_calibration = ( (current_calibration * n_calibrations) + position_scale
                            * (1+(distance-track_length)/track_length)) / (++n_calibrations);
                    position_scale = current_calibration;
                }

                // check to see if the lap number needs to be updated
                if (offset_distance >= track_length/2) {
                    resetLap("");
                    offset_distance = 0;
                }
                position = 0;
                offset_position = lap_offset;
                distance = 0;
            }
        } else if (position >= track_length) {
            position -= track_length;
        }

        if (started) {
            json_buffer.json.put("y", offset_position);
            json_buffer.json.put("time", time);
            fWriter.write(json_buffer.json.toString());
        }

    }

    /**
     * Checks for updates related to licking, valves, sensors, and tones.
     *
     */
    private void updateBehavior() throws JSONException {
        if (behavior_comm == null) {
            return;
        }

        for (int i = 0; i < 10 && behavior_comm.receiveMessage(json_buffer); i++) {
            if (!behavior_comm.getStatus()) {
                behavior_comm.setStatus(true);
            }

            // behavior_json will contain a json message from the arduino describing any behavior events.
            // For it example it may contain "{"valve":{"pin":12,"action":"open"}}"
            JSONObject behavior_json = json_buffer.json.getJSONObject(behavior_comm.getId());

            if (!behavior_json.isNull("lick")) {
                if (behavior_json.getJSONObject("lick").optString("action", "stop").equals("start")) {
                    //BehaviorMateController.registerLickOnUI();
                    lickPendingDisplay = true;
                    lick_count++;
                }
            }

            if (!behavior_json.isNull("valve")) {
                JSONObject valveJson = behavior_json.getJSONObject("valve");
                int valve_pin = valveJson.optInt("pin", -1);
                if (valve_pin == reward_valve) {
                    if (valveJson.optString("action", "close").equals("open")) {
                        rewardPendingDisplay = true;
                        rewardCount += 1;
                        valves.setValve(valve_pin, Int.VALVE_ON);
                    } else {
                        valves.setValve(valve_pin, Int.VALVE_OFF);
                    }
                } else if (valve_pin != -1) {
                    if (valveJson.optString("action", "close").equals("open")) {
                        //BehaviorMateController.setValveState(valve_pin, Int.VALVE_ON);
                        valves.setValve(valve_pin, Int.VALVE_ON);

                    } else {
                        //BehaviorMateController.setValveState(valve_pin, Int.VALVE_OFF);
                        valves.setValve(valve_pin, Int.VALVE_OFF);
                    }
                }

                if (valve_pin == reward_valve) {

                }
            } else if (!behavior_json.isNull("tone")) {
                JSONObject valveJson = behavior_json.getJSONObject("tone");
                int valve_pin = valveJson.optInt("pin", -1);
                if (valve_pin != -1) {
                    if (valveJson.optString("action", "close").equals("open")) {
                        //BehaviorMateController.setValveState(valve_pin, Int.VALVE_ON);
                        valves.setValve(valve_pin, Int.VALVE_ON);
                    } else {
                        //BehaviorMateController.setValveState(valve_pin, Int.VALVE_OFF);
                        valves.setValve(valve_pin, Int.VALVE_OFF);
                    }
                }
            } else if (!behavior_json.isNull("sensor")) {
                JSONObject sensorJson = behavior_json.getJSONObject("sensor");
                int sensor_pin = sensorJson.optInt("pin", -1);
                if (sensor_pin != -1) {
                    String action = sensorJson.optString("action", "stop");
                    switch (action) {
                        case "start":
                            sensor_counts.put(sensor_pin, sensor_counts.get(sensor_pin) + 1);
                            //BehaviorMateController.setSensorState(sensor_pin, Int.SENSOR_ON);
                            break;
                        case "stop":
                            //BehaviorMateController.setSensorState(sensor_pin, Int.SENSOR_OFF);
                            break;
                        case "created":
                            sensor_counts.put(sensor_pin, 0);
                            //BehaviorMateController.setSensorState(sensor_pin, Int.SENSOR_OFF);
                            break;
                    }
                }
            }

            if (!behavior_json.isNull("tag_reader") && !behavior_json.getJSONObject("tag_reader").isNull("tag")) {
                JSONObject tag = behavior_json.getJSONObject("tag_reader");
                String tag_id = tag.getString("tag");
                //BehaviorMate.display.setCurrentTag(tag_id, distance-track_length);
                if (tag_id.equals(lap_tag)) {
                    position = 0;
                    offset_position = lap_offset;
                    resetLap(lap_tag);
                }
            }

            if (!behavior_json.isNull("error")) {
                if (started) {
                    BehaviorMate.showError("Behavior Controller: " + behavior_json.getString("error"));
                } else {
                    BehaviorMate.showError("Behavior Controller: " + behavior_json.getString("error"));
                }
            }

            if (!behavior_json.isNull(Str.CONTEXT)) {
                JSONObject context_json = behavior_json.getJSONObject(Str.CONTEXT);
                if (!context_json.isNull("id")) {
                    String context_id = context_json.getString("id");
                    if (!context_json.isNull("action")) {
                        for (ContextList context : contextLists) {
                            if (context.getId().equals(context_id)) {
                                if (context_json.getString("action").equals("start")) {
                                    context.setStatus("started");
                                } else if (context_json.getString("action").equals("stop")) {
                                    context.setStatus("stopped");
                                }
                                break;
                            }
                        }
                    }
                }
            }

            if (!behavior_json.isNull("starting")) {
                System.out.println("RESETTING COMMS");
                try {
                    configure_sensors();
                    JSONObject valve_json = JSONHelper.setup_valve_json(main_settings.getInt("sync_pin")); // sync pin tells microscope to start recording
                    behavior_comm.sendMessage(valve_json.toString());
                    for (ContextList context : contextLists) {
                        if (context.getComm() == behavior_comm) {
                            context.sendCreateMessages();
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (started) {
                json_buffer.json.put("time", time);
                fWriter.write(json_buffer.json.toString());
            }
        }

        long time_in_millis = (long) time*1000;

        if ((!started) && (time_in_millis > (comms_check_time + comms_check_interval))) {
            testComms();
            comms_check_time = time_in_millis;
        }

        if (!behavior_comm.getStatus()) {
            if (time_in_millis > (last_reset_time + 3000))
            {
                last_reset_time = time_in_millis;
                BehaviorMate.ac.resetArduino(true);
                //resetArduino(true);
            }
        }

    }

    /**
     * Main loop of the program which gets position and behavior updates from the arduino.
     * @return True if the experiment is over, false otherwise.
     * @throws JSONException
     * @throws IOException
     */
    public boolean mainExperimentLoop() throws JSONException, IOException {
        time = (((int) System.currentTimeMillis()) - startTime)/1000.0f;
        if ( (trial_duration != -1 && time > trial_duration) || (lap_limit != -1 && lap_count > lap_limit) ) {
            //System.out.printf("time = %f, trial_duration = %d \n", time, trial_duration);
            endExperiment();
            return true;
        }

        if (started) {
            updatePosition();
            for (ContextList context : contextLists) {
                aContextHasBeenTriggered = context.check();

                if (msg_buffer[0] != null) {
                    this.writeLog(msg_buffer[0]);
                    msg_buffer[0] = null;
                }
            }
            updateBehavior();
        }

        //updateBehavior();
        for (UdpClient c: comms) {
            if ((c != behavior_comm) && (c != position_comm)) {
                checkMessages(c);
            }
        }
        return false;
    }

    /**
     * ?
     */
    public void ZeroPosition() {
        setPosition(lap_offset);
        positionHasBeenZeroed = true;
    }

    /**
     * ?
     */
    public void CalibrateBelt() {
        belt_calibration_mode = true;
        current_calibration = 0;
        n_calibrations = 0;
    }

    /**
     * ?
     */
    public void EndBeltCalibration() {
        try {
            main_settings.put("position_scale", position_scale);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        belt_calibration_mode = false;
    }

    /**
     * ?
     */
    public void ResetCalibration() {
        if (position_scale != stored_position_scale) {
            position_scale = stored_position_scale;
            try {
                main_settings.put("position_scale", position_scale);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ?
     *
     * @throws Exception ?
     */
    public void reconfigureExperiment() throws Exception {
        //System.out.printf("TreadmillController.reconfigureExperiment(): main_settings = \n%s\n\n", main_settings);

        //TODO: diff the new settings from the old and only make necessary updates
        contextLists = new ArrayList<>();
        valves = new ValveGroup();

        if (!main_settings.optString("lap_reset_tag", "").equals("")) {
            if (!main_settings.getString("lap_reset_tag").equals(lap_tag)) {
                position = -1;
                offset_position = -1;
            }
        } else if (!position_reset && main_settings.optBoolean("position_lap_reader", false)) {
            position = -1;
            offset_position = -1;

        } else if (position == -1 && (!main_settings.optBoolean("position_lap_reader", false))) {
            position = 0;
            offset_position = lap_offset;
        }

        zero_position_boundary = main_settings.optBoolean("zero_position_boundary", false);

        trial_duration = main_settings.optInt("trial_length", -1);
        lap_limit = main_settings.optInt("lap_limit", -1);
        stored_position_scale = (float) main_settings.getDouble("position_scale");

        if (Math.signum(current_calibration) == 0.0) {
            position_scale = (float) main_settings.getDouble("position_scale");
        } else {
            main_settings.put("position_scale", position_scale);
        }
        track_length = (float) main_settings.getDouble("track_length");
        lap_tag = main_settings.optString("lap_reset_tag", "");
        lap_offset = main_settings.optInt("lap_offset", 0);
        position_reset = main_settings.optBoolean("position_lap_reader", false);
        resetLastQuickComment();

        JSONObject controllers;
        if (!main_settings.isNull("controllers")) {
            controllers = main_settings.getJSONObject("controllers");
        } else {
            controllers = new JSONObject();
        }

        if (controllers.isNull("behavior_controller")) {
            if (!main_settings.isNull("behavior_controller")) {
                controllers.put("behavior_controller", main_settings.getJSONObject("behavior_controller"));
                main_settings.remove("behavior_controller");
            }
        }

        if (controllers.isNull("position_controller")) {
            if (!main_settings.isNull("position_controller")) {
                controllers.put("position_controller", main_settings.getJSONObject("position_controller"));
                main_settings.remove("position_controller");
            }
        }

        main_settings.put("controllers", controllers);
        BehaviorMate.ac.setArduinoController(system_settings.optString("arduino_controller", null), controllers.toString());
        startComms();
        configure_sensors();

        JSONObject valve_json = JSONHelper.setup_valve_json(main_settings.getInt("sync_pin"));
        behavior_comm.sendMessage(valve_json.toString());

        if (!main_settings.isNull("reward")) {
            configure_rewards();
        }

        this.commentKeys = new HashMap<>();
        if (!main_settings.isNull("comment_keys")) {
            JSONObject quick_comments = main_settings.getJSONObject("comment_keys");
            for (Iterator it = quick_comments.keys(); it.hasNext(); ) {
                Object key = it.next();
                Character key_char = ((String)key).charAt(0);
                commentKeys.put(key_char, quick_comments.getString((String)key));
            }
        }

        if (!main_settings.isNull("contexts")) {
            wait_ms(10);
            JSONArray contexts_array = main_settings.getJSONArray("contexts");
            for (int i = 0; i < contexts_array.length(); i++) {
                JSONObject context_info = contexts_array.getJSONObject(i);
                String context_class = context_info.optString("class", null);
                ContextList context_list = ContextsFactory.Create(context_info, context_class);
                contextLists.add(context_list);

                // add all the valves specified in the settings file to the valves object. Valves are stored in a
                // ValveGroup.Valve object and contain fields for pin, context list id, state, and count6
                ArrayList<Integer> pins = context_list.getPins();
                String id = context_list.getId();
                for (int j = 0; j < pins.size(); j++) {
                    valves.addValve(pins.get(j), id);
                }

                if (!context_list.setupComms(comms)) {
                    BehaviorMate.showError("Context List: " + context_list.getId() + " failed to connect to comm");
                }
            }

            for (ContextList context : contextLists) {
                context.registerContexts(contextLists);
            }
        }
    }

    /**
     * ?
     *
     * @param tag ?
     */
    private void resetLap(String tag) throws JSONException {
        if (started && !lock_lap) {
            JSONObject lap_log = new JSONObject();
            lap_log.put("time", time);
            lap_log.put("lap", lap_count);
            if (tag.equals("")) {
                lap_log.put("message", "no tag");
            }

            fWriter.write(lap_log.toString());
            lap_count++;

            for (ContextList contextList : contextLists) {
                contextList.reset();
            }
        }
    }

    private static void wait_ms(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// SETTINGS AND LOGS ///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * ?
     *
     * @throws Exception ?
     */
    private void reload_settings() {
        current_calibration = 0;
        try {
            reconfigureExperiment();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ?
     *
     * @throws Exception ?
     */
    public void RefreshSettings() {
        wait_ms(100);
        for (UdpClient c: comms) {
            c.closeSocket();
        }
        wait_ms(100);
        reload_settings();
    }

    /**
     * ?
     *
     * @param log ?
     */
    public void writeLog(JSONObject log) {
        if (fWriter == null) {
            return;
        }

        JSONObject log_message = new JSONObject();
        try {
            log_message.put("behavior_mate", log);
            log_message.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fWriter.write(log_message.toString().replace("\n", ""));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// VALVES/SENSORS/REWARDS ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Tests the valve specified in the UI text field. Linked to the TestValve button in the UI.
     * Creates then opens the valve for the amount of time specified in the duration box.
     */
    public void TestValve(int pin, int duration) throws JSONException {
        JSONObject valve_json = JSONHelper.open_valve_json(pin, duration);
        behavior_comm.sendMessage(valve_json.toString());
    }

    /**
     * Configures sensors defined in the settings file. For all sensors the following must be
     * specified: pin, type, report_pin,
     */
    private void configure_sensors() throws JSONException {
        JSONObject clear_message = new JSONObject();
        clear_message.put("sensors", new JSONObject());
        clear_message.getJSONObject("sensors").put("action", "clear");
        behavior_comm.sendMessage(clear_message.toString());

        if (main_settings.isNull("sensors")) {
            return;
        }

        JSONArray sensors = main_settings.getJSONArray("sensors");
        for (int i=0; i < sensors.length(); i++) {
            JSONObject create_subjson = sensors.getJSONObject(i);
            if (create_subjson.optString("type", "").equals("lickport")
                    || (create_subjson.optString("type", "").equals("piezoport"))) {
                lickport_pin = create_subjson.getInt("pin");
            }
            create_subjson.put("action", "create");
            JSONObject create_json = new JSONObject();
            create_json.put("sensors", create_subjson);

            behavior_comm.sendMessage(create_json.toString());
            //delay(150);
            //Thread.sleep(150);
            wait_ms(150);
        }
    }

    /**
     * Configures the reward zone contexts and establishes the initial reward zone locations.
     * Converts legacy settings files to use the new context_list format.
     *
     */
    private void configure_rewards() throws JSONException {
        JSONObject reward_info = main_settings.getJSONObject("reward");

        JSONArray contexts_array;
        if (main_settings.isNull("contexts")) {
            main_settings.put("contexts", new JSONArray());
        }

        contexts_array = main_settings.getJSONArray("contexts");

        if (!reward_info.isNull("id")) {
            String reward_context = reward_info.getString("id");
            for (int i=0; i < contexts_array.length(); i++) {
                JSONObject context = contexts_array.getJSONObject(i);
                if (context.getString("id").equals(reward_context)) {
                    JSONArray valve_list = context.getJSONArray("valves");
                    reward_valve = valve_list.getInt(0);
                    return;
                }
            }
        }

        reward_info.put("id", "reward");
        JSONObject reward_id = new JSONObject();
        reward_id.put("id", "reward");
        main_settings.put("reward", reward_id);

        if (!reward_info.isNull("pin")) {
            reward_valve = reward_info.getInt("pin");
            JSONArray context_valves = new JSONArray();
            context_valves.put(reward_valve);
            reward_info.put("valves", context_valves);
        }

        if (!reward_info.isNull("drop_size")) {
            JSONArray context_duration = new JSONArray();
            context_duration.put(reward_info.getInt("drop_size"));
            reward_info.put("durations", context_duration);
        }

        if (!reward_info.isNull("type")) {
            String reward_type = reward_info.getString("type");
            if (reward_type.equals("fixed")) {
                reward_info.remove("number");
            } else if (reward_type.equals("moving")) {
                try {
                    reward_info.getJSONArray("locations");
                    reward_info.remove("locations");
                } catch (RuntimeException ignored) {}
                reward_info.put("locations", reward_info.optInt("number", 1));
            }
        }

        reward_info.put("type", "operant");
        reward_info.put("sensor", lickport_pin);
        if (reward_info.isNull("display_color")) {
            JSONArray acolor = new JSONArray();
            acolor.put(0);
            acolor.put(204);
            acolor.put(0);
            reward_info.put("display_color", acolor);
        }

        contexts_array.put(reward_info);
    }

    /**
     *
     * @return true if a reward needs to be registered on the UI, false otherwise.
     */
    public boolean registerReward() {
        if (rewardPendingDisplay) {
            rewardPendingDisplay = false;
            return true;
        }
        return false;
    }

    /**
     *
     * @return true if a reward needs to be registered on the UI, false otherwise.
     */
    public boolean registerLick() {
        if (lickPendingDisplay) {
            lickPendingDisplay = false;
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// COMMS /////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * ?
     *
     * @return ?
     */
    public boolean testComms() throws JSONException {
        JSONObject test_arduino = new JSONObject();
        test_arduino.put("communicator", new JSONObject());
        test_arduino.getJSONObject("communicator").put("action", "test");
        behavior_comm.sendMessage(test_arduino.toString());

        int i;
        for (i = 0; i<50 && !behavior_comm.receiveMessage(json_buffer); i++) {
            wait_ms(20);
        }

        if (i == 50) {
            //BehaviorMate.showError("Failed to connect to behavior controller");
            behavior_comm.setStatus(false);
            comms_check_interval = 10000;
            //wait_ms(100);
            wait_ms(50);
            return false;
        } else {
            behavior_comm.setStatus(true);
            comms_check_interval = 60000;
            //wait_ms(100);
            wait_ms(50);
            return true;
        }

    }

    /**
     * ?
     */
    public void resetComms() throws JSONException {
        //wait_ms(50);
        for (UdpClient c : comms) {
            c.closeSocket();
        }
        wait_ms(125);
        try {
            startComms();
        } catch(Exception e) {
            e.printStackTrace();
        }
        BehaviorMate.ac.resetArduino(true);
        //resetArduino(true);
    }

    /**
     * ?
     *
     * @param comm ?
     */
    private void checkMessages(UdpClient comm) throws JSONException {
        for (int i = 0; ((i < 10) && (comm.receiveMessage(json_buffer))); i++) {
            json_buffer.json.getJSONObject(comm.getId());

            if (started) {
                json_buffer.json.put("time", time);
                fWriter.write(json_buffer.json.toString());
            } else {
                System.out.println("WARNING! message received, trial not recording \n\n" + json_buffer.json.toString());
            }
        }
    }

    /**
     * ?
     *
     * @throws Exception ?
     */
    private void startComms() throws Exception {
        comms = new ArrayList<>();
        position_comm = null;
        behavior_comm = null;
        reset_comm = null;
        last_reset_time = 0;

        JSONObject controllers;
        if (!main_settings.isNull("controllers")) {
            controllers = main_settings.getJSONObject("controllers");
        } else {
            controllers = new JSONObject();
        }

        for (Iterator it = controllers.keys(); it.hasNext(); ) {
            Object comm_key_o = it.next();
            String comm_key = (String)comm_key_o;
            JSONObject controller_json = controllers.getJSONObject(comm_key);
            UdpClient comm = new UdpClient(
                    controller_json.optString("ip", "127.0.0.1"),
                    controller_json.getInt("send_port"),
                    controller_json.getInt("receive_port"),
                    comm_key);
            controller_json.put("address", comm.getAddress());
            controllers.put(comm_key, controller_json);
            comms.add(comm);

            switch (comm_key) {
                case "position_controller":
                    position_comm = comm;
                    break;
                case "behavior_controller":
                    behavior_comm = comm;
                    break;
                case "reset_controller":
                    reset_comm = comm;
                    break;
            }
        }

        for (ContextList context : contextLists) {
            context.setupComms(comms);
        }

        if (position_comm == null) {
            System.out.println("position_comm null");
        }

        if ((behavior_comm != null) && (!started)) {
            testComms();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// ENDING THE EXPERIMENT ///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * End the current experiment and reset the state to await the next trial.
     */
    public void endExperiment() throws JSONException, IOException {
        if (!started) {
            return;
        }
        started = false;
        positionHasBeenZeroed = false;
        for (ContextList context : contextLists) {
            context.stop();
            context.reset();
            if (msg_buffer[0] != null) {
                this.writeLog(msg_buffer[0]);
                msg_buffer[0] = null;
            }
        }

        lap_count = 0;
        lick_count = 0;
        rewardCount = 0;
        position = 0;
        velocity = 0;
        sensor_counts = new HashMap<>();
        fWriter.writeEndLog();

        if (!main_settings.isNull("trial_shutdown")) {
            try {
                UdpHelper.sendMessages(main_settings.getJSONArray("trial_shutdown"), "");
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        BehaviorMate.ac.resetArduino();
//        if (!main_settings.optBoolean("disable_end_dialog", false)) {
//            fWriter.showBehaviorFileDeleteDialog();
//        }

        previousTrialfWriter = fWriter;
    }

    /**
     * Adds a function hook which will be run if the program terminates unexpectedly. This is meant
     * to ensure log files are closed out.
     */
    private void prepareExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run () {
                Date stopDate = Calendar.getInstance().getTime();
                started = false;
                for (ContextList context : contextLists) {
                    try {
                        context.stop();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        context.reset();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        context.shutdown();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (msg_buffer[0] != null) {
                        fWriter.write(msg_buffer[0].toString().replace("\n", ""));
                        msg_buffer[0] = null;
                    }
                }
                contextLists = new ArrayList<>();

                if (started) {
//                    JSONObject end_log = new JSONObject();
//                    try {
//                        end_log.put("time", time);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        end_log.put("stop", dateFormat.format(stopDate));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    if (fWriter !=  null) {
                        //fWriter.write(end_log.toString());
                        //fWriter.close();
                        fWriter.writeEndLog();
                    }
                }
                BehaviorMate.ac.shutdown();
            }
      }));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// GETTERS AND SETTERS ///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    public float getTime() {
        if (started) {
            return time;
        }
        return 0;
    }

    public int getTrialDuration() {
        return trial_duration;
    }

    public float getTrackLength() {
        return track_length;
    }

    public float getPosition() {
        if (offset_position < 0) {
            return lap_offset;
        }
        return offset_position;
    }

    public int getLapCount() {
        return lap_count;
    }

    public int getLickCount() {
        return lick_count;
    }

    public int getRewardCount() {
        return rewardCount;
    }

    public float getVelocity() {
        return velocity;
    }

    public float getPositionScale() {
        return this.position_scale;
    }

    public File getLogFile() {
        return fWriter.getFile();
    }

    public JSONObject[] getMsgBuffer() {
        return msg_buffer;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public int getRewardPin() {
        return reward_valve;
    }

    public boolean hasAContextBeenTriggered() {
        return aContextHasBeenTriggered;
    }

    public boolean hasTrialStarted() {
        return started;
    }

    // only require position to be zerod if lap_reset_true is set to true
    public boolean hasPositionBeenZeroed() {
        return main_settings.isNull("position_lap_reset") || positionHasBeenZeroed;
    }

    public Map<String, Boolean> areControllersConnected() throws JSONException {
        testComms();
        Map<String, Boolean> result = new HashMap<>();

        if (behavior_comm == null) {
            result.put("Behavior", false);
        } else {
            result.put("Behavior", behavior_comm.getStatus());
        }
        if (position_comm == null) {
            result.put("Position", false);
        } else {
            result.put("Position", position_comm.getStatus());
        }
        return result;
    }

    public JSONArray getValves() {
        return valves.getValves();
    }

    public void setPositionScale(float scale) {
        position_scale = scale;
    }

    /**
     * ?
     *
     * @param lock_status ?
     */
    public void setLapLock(boolean lock_status) {
        lock_lap = lock_status;
    }

    public void setPosition(float new_position) {
        position = new_position-lap_offset;
        distance = new_position-lap_offset;
        offset_position = new_position;
        offset_distance = new_position;
    }

    private void addVersionAndSettingToTDMLFile(String mouse_name, String experiment_group) {
        JSONObject version = new JSONObject();
        JSONObject start_log = new JSONObject();
        JSONObject settings_log = new JSONObject();
        try {
            version.put("version", BehaviorMate.VERSION);
            version.put("built_on", BehaviorMate.BUILTON);
            start_log.put("mouse", mouse_name);
            start_log.put("experiment_group", experiment_group);
            start_log.put("start", dateFormat.format(Calendar.getInstance().getTime()));
            settings_log.put("settings", main_settings);
        } catch (JSONException e) {
            BehaviorMate.showError("Error adding version and settings info to TDML.");
        }
        fWriter.write(version.toString());
        fWriter.write(start_log.toString());
        fWriter.write(settings_log.toString());
    }

    public String getLoadedDecorators() {
        String output = "";
        for (int i = 0; i < contextLists.size(); i++) {
            String decorator_class = contextLists.get(i).getClass().toString();
            decorator_class = decorator_class.substring(decorator_class.lastIndexOf(".")+1);
            decorator_class = decorator_class.substring(0, decorator_class.lastIndexOf("ContextDecorator"));
            output += decorator_class;
            if (i != contextLists.size()-1) {
                output += ", ";
            }
        }
        return output;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// COMMENTS ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void resetLastQuickComment() {
        lastQuickComment = new JSONObject();
        try {
            lastQuickComment.put(Str.KEY, "");
            lastQuickComment.put(Str.ACTION, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeQuickComment(char shortcutKey, Boolean pressed) {
        if (fWriter == null) {
            return;
        }
        try {
            if (pressed && (shortcutKey == lastQuickComment.optString(Str.KEY).charAt(0)) ) {
                return;
            }
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        String message = commentKeys.get(shortcutKey);
        if (message == null) {
            return;
        }
        if (commentKeys.containsKey(shortcutKey)) {
            JSONObject comment_message = new JSONObject();
            try {
                comment_message.put(Str.KEY, Character.toString(shortcutKey));
                if (pressed) {
                    comment_message.put(Str.ACTION, Str.START);
                } else {
                    comment_message.put(Str.ACTION, Str.STOP);
                }
                comment_message.put(Str.MESSAGE, commentKeys.get(shortcutKey));

                JSONObject comment_json = new JSONObject();
                comment_json.put(Str.BEHAVIOR_MATE, new JSONObject());
                comment_json.getJSONObject(Str.BEHAVIOR_MATE).put(Str.COMMENT_KEY, comment_message);
                comment_json.put(Str.TIME, getTime());
                fWriter.write(comment_json.toString());
                lastQuickComment = comment_message;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeComment(String comment) {
        if (fWriter == null) {
            return;
        }

        JSONObject comment_message = new JSONObject();
        try {
            comment_message.put(Str.MESSAGE, comment);
            JSONObject comment_json = new JSONObject();
            comment_json.put(Str.BEHAVIOR_MATE, new JSONObject());
            comment_json.getJSONObject(Str.BEHAVIOR_MATE).put(Str.COMMENT, comment_message);
            comment_json.put(Str.TIME, getTime());
            fWriter.write(comment_json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPreviousTrialBehaviorFile() {
        if (previousTrialfWriter == null) {
            return null;
        }
        return previousTrialfWriter.getFile().getName();
    }

}
