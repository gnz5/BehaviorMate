package losonczylab.behaviormate.lists;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.BasicContextList;
import losonczylab.behaviormate.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ?
 */
public class SalienceContextList extends BasicContextList {

    /**
     * ?
     */
    private class Event {

        /**
         * ?
         */
        public float time;

        /**
         * Type of the cue in the settings file.
         */
        public String type;

        /**
         * What is communicated to arduino.
         */
        public String message;

        /**
         * ?
         */
        public String text;
    }

    /**
     * ?
     */
    private class MultiEvent extends Event {
        /**
         * ?
         */
        public String[] messages;

        /**
         * ?
         */
        public float[] times;

        /**
         * ?
         *
         * @return ?
         */
        public boolean shift() {
            this.message = this.messages[0];
            this.time = this.times[0];

            for (int i=0; i < messages.length-1; i++) {
                this.times[i] = this.times[i+1];
                this.messages[i] = this.messages[i+1];
            }

            this.messages[messages.length-1] = null;

            return this.message != null;
        }
    }

    /**
     * ?
     */
    private ArrayList<Event> schedule;

    /**
     * ?
     */
    private Event nextEvent;

    /**
     * ?
     */
    //private TreadmillController tc;

    /**
     * ?
     */
    //protected Display display;

    /**
     * ?
     */
    protected int[] display_color_active;

    /**
     * ?
     */
    protected boolean repeat;

    /**
     * ?
     */
    protected float repeat_interval;

    /**
     * ?
     */
    float stim_time;

    /**
     * ?
     */
    float event_time;

    /**
     * ?
     */
    float prestim_time;

    /**
     * ?
     */
    float poststim_time;

    /**
     * ?
     */
    float trial_length;

    /**
     * ?
     */
    int nblocks;

    /**
     * ?
     */
    private ArrayList<JSONObject> stim_array;

    /**
     * ?
     *
     * @param context_info JSONObject containing the configuration information for this context
     *                     from the settings file. The following JSON literal should be defined
     *                     in the settings file. The property key: <datatype, value> means that the key
     *                     is optional and will default to value if not provided and should be of type
     *                     datatype if provided.
     *
     * {
     * 	    "num_blocks": <int>,
     * 	    "stim_time": <float>,
     * 	    "prestim_time": <float>,
     * 	    "poststim_time": <float>,
     * 	    "repeat": <boolean, false>,
     * 	    "repeat_interval": <float, 0>,
     * 	    "sync_pin": <int, 100>,
     * 	    "intertrial_min": <int, Random(5, 10)>,
     * 	    "stims": [
     * 	                   {
     * 	                        "name": <String>,
     * 			                "pin": <int> or <int[]>,
     * 			                "frequency": <int> or <int []>,
     * 			                "duration": <int> or <int[]>,
     * 			                "address": <String, "behavior_controller">,
     * 			                "offset_times": <float[], null>,
     * 			                "sync_pin": <int, 100>
     *                      }
     *      ]
     * }
     *
     * @throws Exception
     */
    public SalienceContextList(JSONObject context_info) throws JSONException {
        super(context_info);
        this.nblocks = context_info.getInt("num_blocks");
        this.stim_time = (float) context_info.getDouble("stim_time");
        this.prestim_time = (float) context_info.getDouble("prestim_time");
        this.poststim_time = (float) context_info.getDouble("poststim_time");
        this.repeat = context_info.optBoolean("repeat", false);
        if (this.repeat) {
            this.repeat_interval = (float) context_info.optDouble("repeat_interval", 0);
        }

        if (this.display_color != null) {
            this.display_color_active = this.display_color;
            this.display_color = null;
        } else {
            System.out.println("display_color is null");
            this.display_color_active = new int[] {150, 50, 20};
        }

        this.trial_length = prestim_time + stim_time + poststim_time;
        this.event_time = -1;
    }

    /**
     * ?
     */
    public void sendCreateMessages() throws JSONException {
        JSONArray stims = this.context_info.getJSONArray("stims");
        stim_array = new ArrayList<>();
        for (int i=0; i < stims.length(); i++) {
            JSONObject stim = stims.getJSONObject(i);
            stim_array.add(stim);
            if (!stim.isNull("pin")) {
                try {
                    JSONArray pins = stim.getJSONArray("pin");
                    try {
                        JSONArray frequencies = null;
                        if (!stim.isNull("frequency")) {
                            frequencies = stim.getJSONArray("frequency");
                        }

                        for (int j=0; j<pins.length(); j++) {
                            JSONObject valve_json;
                            if ((frequencies == null) || (frequencies.getInt(j) == 0)) {
                                valve_json = JSONHelper.setup_valve_json(pins.getInt(j));
                            } else {
                                valve_json = JSONHelper.setup_valve_json(pins.getInt(j), frequencies.getInt(j));
                            }

                            this.comm.sendMessage(valve_json.toString());
                        }
                    } catch (Exception e) {
                        throw e;
                    }
                } catch (RuntimeException e) {
                    JSONObject valve_json;
                    if (stim.isNull("frequency")) {
                        valve_json = JSONHelper.setup_valve_json(stim.getInt("pin"));
                    } else {
                        valve_json = JSONHelper.setup_valve_json(stim.getInt("pin"), stim.getInt("frequency"));
                    }
                    this.comm.sendMessage(valve_json.toString());
                }
            }
        }

        createSchedule();
    }

    /**
     * ?
     *
     * @param stim ?
     * @param time_counter ?
     * @return ?
     */
    private Event createEvent(JSONObject stim, float time_counter) throws JSONException {
        Event thisEvent = new Event();

        int duration = stim.getInt("duration");
        if (stim.optString("address", "behavior_controller").equals("behavior_controller")) {
            JSONObject open_json = JSONHelper.open_valve_json(stim.getInt("pin"), duration);
            thisEvent.message = open_json.toString();
        } else if (stim.getString("address").equals("local_controller")) {
            thisEvent.message = stim.toString();
        }

        thisEvent.time = time_counter + this.prestim_time;
        thisEvent.text = thisEvent.time + " " + stim.getString("name");
        thisEvent.type = stim.getString("name");

        return thisEvent;
    }

    /**
     * ?
     *
     * @param stim ?
     * @param time_counter ?
     * @return ?
     */
    private Event createMultiEvent(JSONObject stim, float time_counter) throws JSONException {
        MultiEvent thisEvent = new MultiEvent();

        if (stim.optString("address", "behavior_controller").equals("behavior_controller")) {

            JSONArray pins = stim.getJSONArray("pin");

            int[] durations = new int[pins.length()];
            JSONArray durations_array = null;
            try {
                durations_array = stim.getJSONArray("duration");
            } catch (RuntimeException e) {}

            for (int j=0; j < durations.length; j++) {
                if (durations_array != null) {
                    durations[j] = durations_array.getInt(j);
                } else {
                    durations[j] = stim.getInt("duration");
                }
            }

            String[] messages = new String[pins.length()];
            for (int j=0; j < pins.length(); j++) {
                JSONObject open_json = JSONHelper.open_valve_json(pins.getInt(j), durations[j]);
                messages[j] = open_json.toString();
            }
            thisEvent.messages = messages;

            if (!stim.isNull("offset_times")) {
                float time = time_counter + this.prestim_time;
                float[] times = new float[pins.length()];
                JSONArray offsets_array = stim.getJSONArray("offset_times");
                for (int j=0; j < times.length; j++) {
                    times[j] = time + (float) offsets_array.getDouble(j)/1000;
                }
                thisEvent.times = times;
                thisEvent.shift();
            } else {
                thisEvent.time = time_counter + this.prestim_time;
                thisEvent.message = null;
            }
        }

        thisEvent.text = thisEvent.time + " " + stim.getString("name");
        thisEvent.type = stim.getString("name");

        return thisEvent;
    }

    /**
     * ?
     *
     * @param start_time ?
     */
    public void createSchedule(float start_time) throws JSONException {
        schedule = new ArrayList<>();

        float time_counter = start_time;
        for (int j=0; j < nblocks; j++) {
            Collections.shuffle(stim_array);

            for (JSONObject jsonObject : stim_array) {
                Event startEvent = new Event();
                startEvent.time = time_counter;
                JSONObject start_json = JSONHelper.open_valve_json(context_info.getInt("sync_pin"), 100);
                startEvent.message = start_json.toString();
                startEvent.text = startEvent.time + ": start";
                startEvent.type = "start";
                schedule.add(startEvent);

                Event thisEvent;
                try {
                    //JSONArray pins = jsonObject.getJSONArray("pin");
                    thisEvent = createMultiEvent(jsonObject, time_counter);
                } catch (RuntimeException e) {
                    thisEvent = createEvent(jsonObject, time_counter);
                }

                schedule.add(thisEvent);
                Event endEvent = new Event();
                time_counter += trial_length;
                endEvent.time = time_counter;
                endEvent.text = endEvent.time + ": end";
                endEvent.type = "end";
                schedule.add(endEvent);

                //time_counter += (int) (random(this.context_info.getInt("intertrial_min", 5), this.context_info.getInt("intertrial_max", 10)));
                time_counter += ThreadLocalRandom.current().nextInt(this.context_info.optInt("intertrial_min", 5), this.context_info.optInt("intertrial_max", 10));
            }

        }

        if (!this.repeat) {
            schedule.get(schedule.size()-1).type = "end_experiment";
        }
        this.context_info.put("trial_length", schedule.get(schedule.size()-1).time);
        displaySchedule();

        nextEvent = schedule.get(0);
    }

    /**
     * ?
     */
    public void createSchedule() throws JSONException {
        createSchedule(0f);
    }

    /**
     * ?
     */
    public void displaySchedule() {
        StringBuilder result = new StringBuilder();
        int i;
        for (i=0; ((i < schedule.size()) && (i < 20)); i++) {
            result = new StringBuilder("" + result + schedule.get(i).text + "\n");
        }

        if (i < schedule.size()) {
            result.append("...");
        }

        //this.display.setSchedule(result.toString());
//        if (!this.repeat) {
//            //display.setMouseName("Next Trial: " + schedule.get(schedule.size()-1).time + "s");
//        }
    }

    /**
     * ?
     *
     * @param time ?
     * @param msg_buffer ?
     */
    public void startTrial(float time, JSONObject[] msg_buffer) {
        JSONObject start_log = new JSONObject();
        Date dateTime = Calendar.getInstance().getTime();
        try {
            start_log.put("time", time);
            start_log.put("trial_start", BehaviorMate.tc.getDateFormat().format(dateTime));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        msg_buffer[0] = start_log;
    }

    /**
     * ?
     *
     * @param time ?
     * @param msg_buffer ?
     */
    public void endTrial(float time, JSONObject[] msg_buffer) {
        JSONObject end_log = new JSONObject();
        Date dateTime = Calendar.getInstance().getTime();
        try {
            end_log.put("time", time);
            end_log.put("trial_end", BehaviorMate.tc.getDateFormat().format(dateTime));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        msg_buffer[0] = end_log;
    }

    /**
     * Check the state of the list as well as the contexts contained in this and decide if they
     * should be activated or not. Send the start/stop messages as necessary. this method gets
     * called for each cycle of the event loop when a trial is started.
     *
     * @return           <code>true</code> to indicate that the trial has started. Note: all messages
     *                   to the behavior comm are sent from within this method returning true or false
     *                   indicates the state of the context, but does not actually influence the
     *                   connected arduinos or UI.
     */
    public boolean check() {
        float time = BehaviorMate.tc.getTime();
        JSONObject[] msg_buffer = BehaviorMate.tc.getMsgBuffer();

        if ( (this.event_time != -1) && (time > (this.event_time + this.stim_time)) ) {
            this.display_color = null;
            this.status = "post-stim";
            this.event_time = -1;
        }

        if ( (nextEvent != null) && (time > nextEvent.time) ) {
            boolean removeEvent = true;
            switch (nextEvent.type) {
                case "start":
                    this.status = "pre-stim";
                    startTrial(time, msg_buffer);
                    this.comm.sendMessage(nextEvent.message);
                    break;
                case "end":
                    endTrial(time, msg_buffer);
                    break;
                case "end_experiment":
                    try {
                        BehaviorMate.tc.endExperiment();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    this.status = nextEvent.type;
                    this.event_time = time;
                    this.display_color = this.display_color_active;
                    //if (nextEvent instanceof MultiEvent m_nextEvent) {
                    MultiEvent m_nextEvent = new MultiEvent();
                    if (nextEvent.getClass().getTypeName().equals(MultiEvent.class.getTypeName())){
                        if (m_nextEvent.message == null) {
                            for (int i = 0; i < m_nextEvent.messages.length; i++) {
                                this.comm.sendMessage(m_nextEvent.messages[i]);
                            }
                        } else {
                            this.comm.sendMessage(m_nextEvent.message);
                            m_nextEvent.shift();
                            removeEvent = (m_nextEvent.message == null);
                        }
                    } else {
                        this.comm.sendMessage(nextEvent.message);
                    }
                }

            if (removeEvent) {
                schedule.remove(0);
                if (schedule.size() == 0) {
                    if (!this.repeat) {
                        try {
                            BehaviorMate.tc.endExperiment();
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            createSchedule(Math.round(time + this.repeat_interval));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    nextEvent = schedule.get(0);
                    displaySchedule();
                }
            }

            return true;
        }

        return false;
    }

    /**
     * ?
     *
     */
    public void stop() {
        try {
            createSchedule();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.stop();
    }
}
