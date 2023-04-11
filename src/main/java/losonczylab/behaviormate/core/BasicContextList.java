package losonczylab.behaviormate.core;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.util.JSONHelper;
import losonczylab.behaviormate.util.Str;
import losonczylab.behaviormate.util.UdpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Controls activating and stopping contexts as the animal progresses along the track.
 * See {@link #check()} for how this logic is controlled.
 */

public class BasicContextList implements ContextList {
    /**
     * <code>ArrayList</code> of <code>Context</code> objects holding information regarding the
     * time and location different contexts should be active. Contexts become inactive after
     * they're triggered and resent with each lap. Only 1 <code>Context</code> in this
     * <code>ArrayList</code> may be active at any given moment.
     */
    protected ArrayList<Context> contexts;

    /**
     * Distance (in mm) around each location that context spans.
     */
    protected int radius;

    /**
     * ? - Used when displaying
     */
    protected float scale;

    /**
     * Amount of time (in seconds) that a context may remain active once it has been triggered.
     */
    private final float duration;

    /**
     * Integer corresponding to the index of the currently active context in the
     * <code>ArrayList</code> of contexts. If -1 then no context is currently active.
     */
    protected int active;

    /**
     * Stores the time the last update was sent to this context in seconds.
     */
    protected float sent;

    /**
     * If true, a message has been sent and the ContextList is waiting for a response.
     */
    protected boolean waiting;

    /**
     * Counts the number of tries to send a message to the arduino so the
     * context can send reset messages if nothing is getting through.
     * Todo: is this the number of times to resend message before resetting messages?
     */
    protected int tries;

    /**
     * A status string to be displayed in the UI.
     */
    protected String status;

    /**
     * If set to true, the location of the contexts will be shuffled between
     * laps or when the ContextList is reset.
     */
    protected boolean shuffle_contexts;

    /**
     * The color to represent the current context in the UI. Stored as an array of 3 ints,
     * representing red, green, and blue pixels.
     */
    /*private*/ protected int[] display_color;

    /**
     * The radius to represent this context as in the UI.
     * Todo: why not just use BasicContextList.radius? Is this a scaled version of radius?
     */
    protected float display_radius;

    /**
     * UdpClient for sending messages to which relate to this context.
     * Todo: possible better description: Used to send messages to the arduino.
     */
    protected UdpClient comm;

    /**
     * ?
     */
    protected final String comm_id;

    /**
     * An identifier for use with the UI and the behavior file.
     */
    protected String id;

    /**
     * UDP message to be sent at the start of each <code>Context</code> in the
     * <code>ContextList</code>
     */
    protected String startString;

    /**
     * UDP message to be sent at the end of each <code>Context</code> in the
     * <code>ContextList</code>
     */
    protected String stopString;

    /**
     * Contains configuration information for this instance's <code>ContextList</code>.
     */
    protected JSONObject context_info;

    /**
     * ?
     */
    private final Boolean fixed_duration;

    /**
     * ?
     */
    protected final JSONObject log_json;

    /**
     * Store whether the context is currently active for this lap, or suspended.
     */
    private boolean suspended;

    /**
     * The color of the context on the display when it is suspended.
     */
    /*private*/ int[] display_color_suspended;

    /**
     * ?
     */
    private float position_scale;

    /**
     * ?
     */
    private float position_scale_mod;

    /**
     * ?
     */
    private boolean gain_modified;

    /**
     * Stores the pins on the arduino to activate when this context list is triggered.
     */
    private ArrayList<Integer> pins;

    /**
     * Constructor.
     *
     * @param context_info Contains configuration information for this context from the
     *                     settings file.
     *
     */
    public BasicContextList(JSONObject context_info) throws JSONException {
        //System.out.println("new  BasicContextList()");
        //System.out.println(context_info);
        contexts = new ArrayList<>();
        comm = null;
        comm_id = BehaviorMate.BEHAVIOR_CONTROLLER;
        sent = -1;
        tries = 0;
        waiting = false;
        fixed_duration = context_info.optBoolean("fixed_duration", false);

        log_json = new JSONObject();
        log_json.put(Str.CONTEXT, new JSONObject());
        if (!context_info.isNull("class")) {
            log_json.getJSONObject(Str.CONTEXT).put("class", context_info.getString("class"));
        }

        // sets startString and stopString as well as the id field
        setId(context_info.getString("id"));

        duration = (float) context_info.optDouble("max_duration", -1);
        radius = context_info.optInt("radius", -1);
        active = -1;
        status = "";
        this.context_info = context_info;

        // resolve the display color from rbg in the settings to an integer
        if (!context_info.isNull("display_color")) {
            JSONArray disp_color = context_info.getJSONArray("display_color");
            display_color = new int[] {disp_color.getInt(0), disp_color.getInt(1), disp_color.getInt(2)};
            display_color_suspended = new int[] {100, 100, 100};
        } else {
            display_color = null;
            display_color_suspended = null;
        }

        // positions the contexts
        shuffle_contexts = false;
        JSONArray locations = null, valves = null;
        try {
            locations = context_info.getJSONArray("locations");
        } catch (JSONException ignored) {}

        pins = new ArrayList<>();
        try {
            valves = context_info.getJSONArray("valves");
            for (int i = 0; i < valves.length(); i++) {
                pins.add(valves.getInt(i));
            }
        } catch (JSONException e) {
            throw new JSONException("Invalid \"valves\" property for context with id: " + getId());
        }

        // if locations is null - specific locations for this context are not supplied
        if (locations != null) {
            for (int i=0; i < locations.length(); i++) {
                add(locations.getInt(i));
            }
        } else {
            // either the field "number" is preserved for compatibility with old settings files. now "locations" not being
            // an integer instead of a list is sufficient
            int num_contexts = context_info.optInt("locations", context_info.optInt("number", 0));
            int middle = ((int) (BehaviorMate.tc.getTrackLength()/2.0)) + 2;

            if (num_contexts == 0) {
                // if no number is assigned, then assign this context to the entire track
                add(middle);
                radius = middle;
            } else {
                add(middle);
                // otherwise add the contexts randomly and shuffle each lap
                for (int i = 0; i < num_contexts-1; i++) {
                    add(middle);
                    shuffle_contexts = true;
                    shuffle();
                }
            }
            gain_modified = false;
        }
        suspended = false;
    }

//    public JSONArray getPinsAsJSONArray() {
//        return new JSONArray(pins);
//    }

    public ArrayList<Integer> getPins() {
        return pins;
    }

    @Override
    /**
     *
     * @return The <code>UdpClient</code> object belonging to this instance.
     */
    public UdpClient getComm() {
        return comm;
    }

    @Override
    public void setComm(UdpClient comm) {
        this.comm = comm;
    }

    @Override
    public float getPositionScale() {
        return position_scale;
    }

    @Override
    public void setPositionScale(float position_scale) {
        this.position_scale = position_scale;
    }

    @Override
    public float getPositionScaleMod() {
        return position_scale_mod;
    }

    @Override
    public void setPositionScaleMod(float position_scale_mod) {
        this.position_scale_mod = position_scale_mod;
    }

    @Override
    public boolean isGainModified() {
        return gain_modified;
    }

    @Override
    public void setGainModified(boolean gain_modified) {
        this.gain_modified = gain_modified;
    }

    @Override
    public void setDisplayColorSuspended(int[] display_color_suspended) {
        this.display_color_suspended = display_color_suspended;
    }

    @Override
    public JSONObject getContextInfo() {
        return context_info;
    }

    @Override
    public void setContextInfo(JSONObject context_info) {
        this.context_info = context_info;
    }

    /**
     * ?
     */
    @Override
    public void sendCreateMessages() throws JSONException {
        // comm may be null for certian subclasses of ContextList which to not
        // need to talk to the behavior arduino
        if (comm != null) {
            context_info.put("action", "create");
            JSONObject context_setup_json = new JSONObject();
            context_setup_json.put("contexts", context_info);

            // configure the valves, the pins which have devices responsible for
            // controlling this context
            JSONArray valves = null;
            if (!context_info.isNull("valves")) {
                valves = context_info.getJSONArray("valves");
            }

            for (int i=0; ((valves != null) && (i < valves.length())); i++) {
                int valve_pin = valves.getInt(i);
                JSONObject valve_json;

                // frequency causes this singal to oscillate in order to play a
                // tone
                if (!context_info.isNull("frequency")) {
                    valve_json = JSONHelper.setup_valve_json(valve_pin, context_info.getInt("frequency"));
                } else if (!context_info.isNull("inverted")) {
                    valve_json = JSONHelper.setup_valve_json(valve_pin, context_info.getBoolean("inverted"));
                } else {
                    valve_json = JSONHelper.setup_valve_json(valve_pin);
                }
                comm.sendMessage(valve_json.toString());
                JSONObject close_json = JSONHelper.close_valve_json(valve_pin);
                comm.sendMessage(close_json.toString());
            }

            active = -1;
            status = "reset";
            tries = 0;
            waiting = false;
            comm.sendMessage(context_setup_json.toString());
        } else {
            System.out.println("[" + id + " "  + comm_id + "] SEND CREATE MESSAGES FAILED");
        }
    }

    /**
     * Setter method for this BasicContextList's UdpClient.
     *
     * @param comms channel to post messages for configuring, starting or stopping contexts.
     * @return <code>true</code> if the messages were successfully sent, <code>false</code> otherwise.
     */
    @Override
    public boolean setupComms(ArrayList<UdpClient> comms) throws JSONException {
        //System.out.printf("BasicContextList.setupComms: comms = %s, comm_id = %s\n", comms, comm_id);
        for (UdpClient c: comms) {
            //System.out.printf("BasicContextList.setupComms: c.getId() = %s\n", c.getId());
            if (c.getId().equals(comm_id)) {
                comm = c;
                break;
            }
        }

        if (comm == null) {
            System.out.println("[" + id + " "  + comm_id + "] FAILED TO FIND COMM");
            return false;
        }

        try {
            sendCreateMessages();
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * ?
     *
     * @param contexts ?
     */
    @Override
    public void registerContexts(ArrayList<ContextList> contexts) throws JSONException {
        // what is this supposed to do?
    }

    /**
     * Setter method for the id of this BasicContextList.
     * Also configures the startString and stopString valves.
     *
     * @param id Sent to this BasicContextList's <code>UdpClient</code>(<code>comm</code>)
     *           to identify this <code>BasicContextList</code>
     */
    protected void setId(String id) throws JSONException {
        this.id = id;
        log_json.getJSONObject(Str.CONTEXT).put("id", id);

        JSONObject context_message = new JSONObject();
        context_message.put("action", "start");
        context_message.put("id", this.id);
        JSONObject context_message_json = new JSONObject();
        context_message_json.put("contexts", context_message);
        startString = context_message_json.toString();

        context_message.put("action", "stop");
        context_message_json.put("contexts", context_message);
        stopString = context_message_json.toString();
    }

    /**
     * Returns the id of this BasicContextList.
     *
     * @return the identifier
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the length, in mm, the contexts will span in either direction.
     * @param radius ?
     */
    @Override
    public void setRadius(int radius) {
        if (radius == 0) {
            radius = (int) (BehaviorMate.tc.getTrackLength()/2.0) + 2;
        }

        for (Context context : contexts) {
            context.setRadius(radius);
        }

        this.radius = radius;
        setDisplayScale(scale);
    }

    /**
     *
     * @return An int representing the length, in mm, the contexts span in either direction.
     */
    @Override
    public int getRadius() {
        return radius;
    }

    /**
     * Sets the scaling used for displaying this BasicContextList's radius in the UI.
     *
     * @param scale the amount to scale the radius so it displays properly in the UI.
     *              Units are in pixel/mm.
     */
    @Override
    public void setDisplayScale(float scale) {
        this.scale = scale;
        display_radius = ((float) radius) * scale;
    }

    /**
     *
     * @return the scaled width, in pixels, used to draw this BasicContextList's radius in the UI.
     */
    @Override
    public float displayRadius() {
        return display_radius;
    }

    /**
     *
     * @return An array of 3 integers, representing the red, green, and blue pixels (in the order)
     *         used to display the currently active context.
     */
    @Override
    public int[] getDisplayColor() {
        //System.out.print("BasicContextList.getDisplayColor() --> ");
        //if (!suspended) {
        if (!isSuspended()) {
            //System.out.print(Arrays.toString(display_color) + "\n");
            return display_color;
        } else {
            //System.out.print(Arrays.toString(display_color_suspended) + "\n");
            return display_color_suspended;
        }
    }

    /**
     * Sets the string displayed in the UI describing the state of the contexts in this
     * BasicContextList.
     *
     * @param status The status to display in the UI.
     */
    @Override
    public void setStatus(String status) {
        this.status = status;

        // if the status has been updated, then the last update has reached the
        // arduino
        waiting = false;
        tries = 0;
    }

    /**
     *
     * @return the string representing the current status of the contexts in the list.
     */
    @Override
    public String getStatus() {
        if (!suspended) {
            return status;
        } else {
            return "suspended";
        }
    }

    /**
     *
     * @return The number of contexts wrapped by this BasicContextList.
     */
    @Override
    public int size() {
        return contexts.size();
    }

    /**
     * Accessor for the location of a specific Context in the List.
     *
     * @param i index of the context to return
     * @return  The location of the context at the supplied index.
     */
    @Override
    public int getLocation(int i) {
        return contexts.get(i).getLocation();
    }

    /**
     * Accessor for a specific Context in the List.
     *
     * @param i index of the context to return
     * @return  The context at the supplied index.
     */
    @Override
    public Context getContext(int i) {
        return contexts.get(i);
    }

    /**
     * Moves the context at the given index in <code>contexts</code> to the provided location (in mm).
     *
     * @param index The index of the context in <code>contexts</code>
     * @param location The new location of the context, in mm.
     */
    @Override
    public void move(int index, int location) {
        contexts.get(index).move(location);
    }

    /**
     * Removes all contexts from this BasicContextList.
     */
    @Override
    public void clear() {
        //System.out.println("BasicContextList.clear()");
        if (this.size() > 0) {
            contexts = new ArrayList<>();
        }
    }

    /**
     * ?
     *
     */
    @Override
    public void trialStart() { }

    /**
     * Resets the state of the contexts. Contexts which have been triggered are
     * reactivated and allowed to be triggered again. If <code>shuffle_contexts</code>
     * is <code>true</code>, the contexts will be shuffled.
     */
    @Override
    public void reset() {
        //System.out.println("BasicContextList.reset()");
        for (Context context : contexts) {
            context.reset();
        }

        if (shuffle_contexts) { // maybe this check should be in shuffle()?
            shuffle();
        }
    }

    /**
     * Gives each context a new random location on the track.
     */
    @Override
    public void shuffle() {
        //System.out.println("BasicContextList.shuffle()");
        // return immediately if there are no contexts to shuffle
        if (contexts.size() == 0) {
            return;
        }

        int min = radius;
        int max = (int) BehaviorMate.tc.getTrackLength()-radius;
        int random_location = ThreadLocalRandom.current().nextInt(min, max);

        if (contexts.size() == 1) {
            move(0, random_location);
            return;
        }

        // initially position contexts evenly spaced
        int interval = (int) (BehaviorMate.tc.getTrackLength() - 2*radius)/contexts.size();
        move(0, radius + interval/2);
        for (int i = 1; i <contexts.size(); i++) {
            move(i, contexts.get(i-1).getLocation() + interval);
        }

        // move the contexts randomly without allowing them to overlap
        min = radius;
        max = contexts.get(1).getLocation() - 2*radius;
        random_location = ThreadLocalRandom.current().nextInt(min, max);
        move(0, random_location);
        //this.move(0, (int) random(this.radius,this.contexts.get(1).location()-2*this.radius));

        for (int i = 1; i < contexts.size()-1; i++) {
            int prev_location = contexts.get(i-1).getLocation();
            int next_location = contexts.get(i+1).getLocation();

            min = prev_location + 2*radius;
            max = next_location - 2*radius;
            random_location = ThreadLocalRandom.current().nextInt(min, max);
            move(i, random_location);
            //this.move(i, (int) random(prev_location+2*this.radius, next_location-2*this.radius));
        }

        int prev_location = contexts.get(this.size()-2).getLocation();

        min = prev_location + 2*radius;
        max = (int) BehaviorMate.tc.getTrackLength() - radius;
        random_location = ThreadLocalRandom.current().nextInt(min, max);
        move(size()-1, random_location);
        //this.move(this.size()-1, (int) random(prev_location+2*this.radius, this.track_length-this.radius));
    }

    /**
     * An array whose ith element contains the location of the ith context of this
     * BasicContextList.
     *
     * @return An array containing context locations.
     */
    @Override
    public int[] toList() {
        int[] list = new int[contexts.size()];
        for (int i = 0; i < contexts.size(); i++) {
            list[i] = contexts.get(i).getLocation();
        }
        return list;
    }

    /**
     * Check the state of the contexts contained in this list and send the
     * start/stop messages as necessary. This method gets called for each cycle
     * of the event loop when a trial is started.
     *
     * @return           <code>true</code> to indicate that the trial has started.
     *                   Note: all messages to the behavior comm are sent from
     *                   within this method returning true or false indicates
     *                   the state of the context, but does not actually
     *                   influence the connected arduinos or UI.
     */
    @Override
    public boolean check() {
        boolean inZone = false;
        int i = 0;
        float time = BehaviorMate.tc.getTime();
        JSONObject[] msg_buffer = BehaviorMate.tc.getMsgBuffer();

        // This loop checks to see if any of the individual contexts are
        // triggered to be active both in space and time
        for (; i < contexts.size(); i++) {
            if (contexts.get(i).check()) {
                //System.out.println("BasicContextList: a Context has been triggered **********");
                inZone = true;
                break;
            }
        }

        // Decide if the context defined by this ContextList needs to swtich
        // state and send the message to the UdpClient accordingly
        if (!waiting) {
            if (!inZone && active != -1) {
                active = -1;
                waiting = true;
                sent = time;
                status = "sent stop";
                sendMessage(stopString);

                if(gain_modified) {
                    try {
                        log_json.getJSONObject(Str.CONTEXT).put("action", "stop");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    msg_buffer[0] = log_json;
                    BehaviorMate.tc.setPositionScale(position_scale);
                }
            } else if(inZone && active != i) {
                active = i;
                waiting = true;
                sent = time;
                status = "sent start";
                sendMessage(startString);

                if(gain_modified) {
                    try {
                        log_json.getJSONObject(Str.CONTEXT).put("action", "start");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    msg_buffer[0] = log_json;
                    BehaviorMate.tc.setPositionScale(position_scale_mod);
                }
            }
        }

        // Ensure that the context has actually started and reset if necessary
        if (waiting && (time-sent > 2) ) {
            tries++;
            if (tries > 3) {
                System.out.println("[" + id + "] RESET CONTEXT " + tries);
                tries = 0;
                try {
                    sendCreateMessages();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("[" + id + "] RESEND TO CONTEXT " + tries);
                comm.setStatus(false);
                sent = time;
                if (!inZone) {
                    sendMessage(stopString);
                } else {
                    sendMessage(startString);
                }
            }
        }

        //System.out.println("BasicContextList.check() --> " + (active != -1) );
        return active != -1;
    }

    /**
     *
     * @return <code>true</code> if there is currently an active context or <code>false</code>
     * if all contexts are suspended.
     */
    @Override
    public boolean isActive() {
        //System.out.println("BasicContextList.isActive() --> " + (active != -1));
        return active != -1;
    }

    /**
     *
     * @return The index of the currently active context.
     */
    @Override
    public int activeIdx() {
        return active;
    }

    // Todo: suspend should probably be removed from BasicContextList
    /**
     * Suspend all contexts and send a "send stop" message.
     */
    @Override
    public void suspend() {
        //System.out.println("BasicContextList.suspend()");
        active = -1;
        status = "sent stop";
        suspended = true;
        sendMessage(stopString);
    }

    /**
     * Todo: seems to do the same thing as suspend. What are the parameters for?
     * Stop this context. Called at the end of trials to ensure that the context is shut off.
     *
     */
    @Override
    public void stop() {
        //System.out.println("BasicContextList.stop()");
        active = -1;
        status = "sent stop";
        waiting = false;
        sendMessage(stopString);
    }
    // suspend vs stop: stop means the mouse "completed" or "ran through" the context whereas
    // suspend means it shouldn't be active for this lap
    /**
     * Todo: does this send a message to the arduino?
     *
     * @param message ?
     */
    @Override
    public void sendMessage(String message) {
        //System.out.println("BasicContextList.sendMessage(" + message + ")");
        if (comm != null) {
            comm.sendMessage(message);
        } else {
            System.out.println("comm is null. The following message was not sent: " + message);
        }
    }

    /**
     * ?
     */
    @Override
    public void shutdown() { }

    /**
     * Add a new context to this BasicContext list at the given location.
     *
     * @param location Distance, in mm, from the start of the track to place this context.
     */
    protected void add(int location) {
        //System.out.println("BasicContextList.add(" + location + ")");
        contexts.add(new Context(location, duration, radius, contexts.size(), fixed_duration));
    }

    /**
     *
     * @return <code>true</code> if the wrapped ContextList is suspended, <code>false</code> otherwise.
     */
    public boolean isSuspended() {
        //System.out.println("BasicContextList.isSuspended() --> " + suspended);
        return suspended;
    }

    /**
     * ?
     */
    public void update() { }

    public void unsuspend() {
        //System.out.println("BasicContextList.unsuspend()");
        //this.active = -1;
        suspended = false;
    }
}
