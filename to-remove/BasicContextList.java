package losonczylab.behaviormate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    protected float duration;

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
    protected int[] display_color;

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
    protected String comm_id;

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
    protected Boolean fixed_duration;

    /**
     * ?
     */
    protected JSONObject log_json;

    public BasicContextList(JSONObject context_info) throws JSONException {
        //System.out.println("new  BasicContextList(" + context_info + ", " + track_length + ", " + comm_id + ")");
        this.contexts = new ArrayList<>();
        this.comm = null;
        this.comm_id = BehaviorMate.BEHAVIOR_CONTROLLER;
        this.sent = -1;
        this.tries = 0;
        this.waiting = false;
        this.fixed_duration = context_info.optBoolean("fixed_duration", false);
        this.log_json = new JSONObject();
        //this.log_json.setJSONObject(Str.CONTEXT, new JSONObject());
        this.log_json = this.log_json.put(Str.CONTEXT, new JSONObject());
        if (!context_info.isNull("class")) {
            //this.log_json.getJSONObject(Str.CONTEXT).setString("class", context_info.getString("class"));
            this.log_json.getJSONObject(Str.CONTEXT).put("class", context_info.getString("class"));
        }

        // sets startString and stopString as well as the id field
        setId(context_info.getString("id"));

        this.duration = (float) context_info.getDouble("max_duration");
        this.radius = context_info.optInt("radius", -1);
        this.active = -1;
        this.status = "";
        //this.track_length = track_length;
        this.context_info = context_info;

        // resolve the display color from rbg in the settings to an integer
        if (!context_info.isNull("display_color")) {
            JSONArray disp_color = context_info.getJSONArray("display_color");
            this.display_color = new int[] {disp_color.getInt(0), disp_color.getInt(1), disp_color.getInt(2)};
        } else {
            display_color = null;
        }

        // positions the contexts
        this.shuffle_contexts = false;
        JSONArray locations = null;
        try {
            locations = context_info.getJSONArray("locations");
        } catch (JSONException ignored) { }

        // if locations is null - specific locations for this context are not
        // supplied
        if (locations != null) {
            for (int i=0; i < locations.length(); i++) {
                add(locations.getInt(i));
            }
        } else {
            // either the field "number" is preserved for compatibility with
            // old settings files. now "locations" not being an integer instead
            // of a list is sufficient
            int num_contexts = context_info.optInt("locations", context_info.optInt("number", 0));

            if (num_contexts == 0) {
                // if no number is assigned, then assign this context to the
                // entire track
                add((int)(BehaviorMate.tc.getTrackLength()/2.0) + 2);
                this.radius = (int)(BehaviorMate.tc.getTrackLength()/2.0) + 2;
            } else {
                // otherwise add the contexts randomly and shuffle each lap
                for (int i=0; i < num_contexts; i++) {
                    add((int)(BehaviorMate.tc.getTrackLength()/2.0));
                    this.shuffle_contexts = true;
                    shuffle();
                    //setShuffle(true, track_length);
                }
            }
        }

        //sendCreateMessages();
    }

    void printContextLocations() {
        String output = "Contexts = ";
        for(Context context : contexts) {
            int location = context.getLocation();
            int radius = context.getRadius();
            output += "[" + String.valueOf(location-radius) + ", " + String.valueOf(location+radius) + "], ";
        }
        System.out.println(output + "{" + BehaviorMate.tc.getTime() + "}");
    }

    /**
     *
     * @return The <code>UdpClient</code> object belonging to this instance.
     */
    public UdpClient getComm() {
        return this.comm;
    }

    public void setComm(UdpClient comm) {
        this.comm = comm;
    }

    public float getPositionScale() {return 0;}

    public void setPositionScale(float position_scale) {}

    public float getPositionScaleMod() {return 0;}

    public void setPositionScaleMod(float position_scale_mod) {}

    public boolean isGainModified() {return false;}

    public void setGainModified(boolean gain_modified) {}

    public void setDisplayColorSuspended(int[] display_color_suspended) {}

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
                    valve_json = TreadmillController.setup_valve_json(valve_pin, context_info.getInt("frequency"));
                } else if (!context_info.isNull("inverted")) {
                    valve_json = TreadmillController.setup_valve_json(valve_pin, context_info.getBoolean("inverted"));
                } else {
                    valve_json = TreadmillController.setup_valve_json(valve_pin);
                }
                comm.sendMessage(valve_json.toString());
                JSONObject close_json = TreadmillController.close_valve_json(valve_pin);
                comm.sendMessage(close_json.toString());
            }

            this.active = -1;
            this.status = "reset";
            this.tries = 0;
            this.waiting = false;
            comm.sendMessage(context_setup_json.toString());
        } else {
            System.out.println("[" +this.id+ " "  + this.comm_id + "] SEND CREATE MESSAGES FAILED");
        }
    }

    /**
     * Setter method for this BasicContextList's UdpClient.
     *
     * @param comms channel to post messages for configuring, starting or stopping contexts.
     * @return <code>true</code> if the messages were successfully sent, <code>false</code> otherwise.
     */
    public boolean setupComms(ArrayList<UdpClient> comms) throws JSONException {
        for (UdpClient c: comms) {
            if (c.getId().equals(this.comm_id)) {
                this.comm = c;
                break;
            }
        }

        if (this.comm == null) {
            System.out.println("[" + this.id + " "  + this.comm_id + "] FAILED TO FIND COMM");
            return false;
        }

        sendCreateMessages();
        return true;
    }

    /**
     * ?
     *
     * @param contexts ?
     */
    public void registerContexts(ArrayList<ContextList> contexts) throws JSONException { }

    /**
     * Setter method for the id of this BasicContextList.
     * Also configures the startString and stopString valves.
     *
     * @param id Sent to this BasicContextList's <code>UdpClient</code>(<code>comm</code>)
     *           to identify this <code>BasicContextList</code>
     */
    protected void setId(String id) throws JSONException {
        this.id = id;
        this.log_json.getJSONObject(Str.CONTEXT).put("id", id);

        JSONObject context_message = new JSONObject();
        context_message.put("action", "start");
        context_message.put("id", this.id);
        JSONObject context_message_json = new JSONObject();
        context_message_json.put("contexts", context_message);
        this.startString = context_message_json.toString();

        context_message.put("action", "stop");
        context_message_json.put("contexts", context_message);
        this.stopString = context_message_json.toString();
    }

    /**
     *
     * @return ?
     */
    public String getCommId() {
        return this.comm_id;
    }

    /**
     * Returns the id of this BasicContextList.
     *
     * @return the identifier
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the length, in mm, the contexts will span in either direction.
     * @param radius ?
     */
    public void setRadius(int radius) {
        if (radius == 0) {
            radius = (int)(BehaviorMate.tc.getTrackLength()/2.0) + 2;
        }

        for (Context context : this.contexts) {
            context.setRadius(radius);
        }

        this.radius = radius;
        this.setDisplayScale(this.scale);
    }

    /**
     *
     * @return An int representing the length, in mm, the contexts span in either direction.
     */
    public int getRadius() {
        return this.radius;
    }

    /**
     * Sets the scaling used for displaying this BasicContextList's radius in the UI.
     *
     * @param scale the amount to scale the radius so it displays properly in the UI.
     *              Units are in pixel/mm.
     */
    public void setDisplayScale(float scale) {
        this.scale = scale;
        this.display_radius = ((float)this.radius) * scale;
    }

    /**
     *
     * @return the scaled width, in pixels, used to draw this BasicContextList's radius in the UI.
     */
    public float displayRadius() {
        return this.display_radius;
    }

    /**
     * @return An array of 3 integers, representing the red, green, and blue pixels (in the order)
     * used to display the implementor's currently active context.
     */
    @Override
    public int[] getDisplayColor() {
        return new int[0];
    }

    /**
     * Sets the string displayed in the UI describing the state of the contexts in this
     * BasicContextList.
     *
     * @param status The status to display in the UI.
     */
    public void setStatus(String status) {
        this.status = status;

        // if the status has been updated, then the last update has reached the
        // arduino
        waiting = false;
        this.tries = 0;
    }

    /**
     *
     * @return the string representing the current status of the contexts in the list.
     */
    public String getStatus() {
        return this.status;
    }

    /**
     *
     * @return The number of contexts wrapped by this BasicContextList.
     */
    public int size() {
        return this.contexts.size();
    }

    /**
     * Accessor for the location of a specific Context in the List.
     *
     * @param i index of the context to return
     * @return  The location of the context at the supplied index.
     */
    public int getLocation(int i) {
        return this.contexts.get(i).getLocation();
    }

    /**
     * Accessor for a specific Context in the List.
     *
     * @param i index of the context to return
     * @return  The context at the supplied index.
     */
    public Context getContext(int i) {
        return this.contexts.get(i);
    }

    /**
     * Add a new context to this BasicContext list at the given location.
     *
     * @param location Distance, in mm, from the start of the track to place this context.
     */
    protected void add(int location) {
        //System.out.println("BasicContextList.add(" + location + ")");
        this.contexts.add(new Context(
                location, this.duration, this.radius, this.contexts.size(), this.fixed_duration));
    }

    /**
     * Moves the context at the given index in <code>contexts</code> to the provided location (in mm).
     *
     * @param index The index of the context in <code>contexts</code>
     * @param location The new location of the context, in mm.
     */
    public void move(int index, int location) throws JSONException {
        this.contexts.get(index).move(location);
    }

    /**
     * Removes all contexts from this BasicContextList.
     */
    public void clear() {
        System.out.println("BasicContextList.clear()");
//        if (this.size() > 0) {
//            this.contexts = new ArrayList<Context>();
//        }
        // Is there a reason to check if size() > 0?
        contexts = new ArrayList<>();
    }

    /**
     * ?
     *
     */
    public void trialStart() { }

    /**
     * Resets the state of the contexts. Contexts which have been triggered are
     * reactivated and allowed to be triggered again. If <code>shuffle_contexts</code>
     * is <code>true</code>, the contexts will be shuffled.
     */
    public void reset() throws JSONException {
//        for (int i=0; i < this.contexts.size(); i++) {
//            this.contexts.get(i).reset();
//        }
        //System.out.println("BasicContextList.reset()");
        for (Context context : contexts) {
            context.reset();
        }

        if (this.shuffle_contexts) { // maybe this check should be in shuffle()?
            shuffle();
        }
    }

    /**
     * Resets the state of the contexts. Contexts which have been triggered are
     * reactivated and allowed to be triggered again. If <code>shuffle_contexts</code>
     * is <code>true</code>, the contexts will be shuffled.
     */
    public void end() throws JSONException {
        //System.out.println("BasicContextList.end()");
        this.reset();
    }

    /**
     * Gives each context a new random location on the track.
     */
    public void shuffle() throws JSONException {
        //System.out.println("BasicContextList.shuffle()");
        // return immediately if there are no contexts to shuffle
        if (this.contexts.size() == 0) {
            return;
        }

        if (this.contexts.size() == 1) {
            //this.move(0, (int) random(this.radius, BehaviorMate.tc.getTrackLength()-this.radius));
            this.move(0, ThreadLocalRandom.current().nextInt(this.radius, (int) BehaviorMate.tc.getTrackLength()-this.radius));
            return;
        }

        // initially position contexts evenly spaced
        int interval = (int)(BehaviorMate.tc.getTrackLength()-2*this.radius)/this.contexts.size();
        this.move(0, this.radius + interval/2);
        for (int i = 1; i < this.contexts.size(); i++) {
            this.move(i, this.contexts.get(i-1).getLocation() + interval);
        }

        // move the contexts randomly without allowing them to overlap
        //this.move(0, (int) random(this.radius,this.contexts.get(1).getLocation()-2*this.radius));
        this.move(0, ThreadLocalRandom.current().nextInt(this.radius,this.contexts.get(1).getLocation()-2*this.radius));

        for (int i = 1; i < this.contexts.size()-1; i++) {
            int prev_location = this.contexts.get(i-1).getLocation();
            int next_location = this.contexts.get(i+1).getLocation();
            //this.move(i, (int) random(prev_location+2*this.radius, next_location-2*this.radius));
            this.move(i, ThreadLocalRandom.current().nextInt(prev_location+2*this.radius, next_location-2*this.radius));
        }

        int prev_location = this.contexts.get(this.size()-2).getLocation();
        //this.move(this.size()-1, (int) random(prev_location+2*this.radius, BehaviorMate.tc.getTrackLength()-this.radius));
        this.move(this.size()-1, ThreadLocalRandom.current().nextInt(prev_location+2*this.radius, (int) BehaviorMate.tc.getTrackLength()-this.radius));
    }

    /**
     * An array whose ith element contains the location of the ith context of this
     * BasicContextList.
     *
     * @return An array containing context locations.
     */
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
    public boolean check() throws JSONException {
        //System.out.println("BasicContextList.check(" + position + ", " + time + ", " + lap + ", msg_buffer)");
        boolean inZone = false;
        int i = 0;
        int size = contexts.size();
        float time = BehaviorMate.tc.getTime();

        // This loop checks to see if any of the individual contexts are
        // triggered to be active both in space and time
        for (; i < size; i++) {
            if (contexts.get(i).check()) {
                //System.out.println("BasicContextList: a Context has been triggered");
                inZone = true;
                break;
            }
        }

        // Decide if the context defined by this ContextList needs to swtich
        // state and send the message to the UdpClient accordingly
        if (!waiting) {
            if ((!inZone) && (this.active != -1)) {
                this.status = "sent stop";
                this.active = -1;
                this.waiting = true;
                this.sent = time;
                this.sendMessage(this.stopString);
            } else if((inZone) && (this.active != i)) {
                this.active = i;
                this.waiting = true;
                this.sent = time;
                this.status = "sent start";
                this.sendMessage(this.startString);
            }
        }

        // Ensure that the context has actually started and reset if necessary
        if ((this.waiting) && (time-this.sent > 2)) {
            this.tries++;
            if (this.tries > 3) {
                System.out.println("[" + this.id + "] RESET CONTEXT " + this.tries);
                this.tries = 0;
                try {
                    sendCreateMessages();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("[" + this.id + "] RESEND TO CONTEXT " + this.tries);
                this.comm.setStatus(false);
                this.sent = time;
                if (!inZone) {
                    this.sendMessage(this.stopString);
                } else {
                    this.sendMessage(this.startString);
                }
            }
        }
//        if (active != -1) {
//            System.out.println("BasicContextList.check(): returning true {" + BehaviorMate.tc.getTime() + "}");
//        }
//        System.out.println(
//                "BasicContextList.check(" + BehaviorMate.tc.getPosition() + ", " + time + ", "
//                        + BehaviorMate.tc.getLapCount() + ", msg_buffer) --> " + (active != -1));
        return active != -1;
    }

    /**
     *
     * @return <code>true</code> if there is currently an active context or <code>false</code>
     * if all contexts are suspended.
     */
    public boolean isActive() {
        //System.out.println("BasicContextList.isActive() --> " + (active != -1));
        return this.active != -1;
    }

    /**
     *
     * @return The index of the currently active context.
     */
    public int activeIdx() {
        return this.active;
    }

    // Todo: suspend should probably be removed from BasicContextList
    /**
     * Suspend all contexts and send a "send stop" message.
     */
    public void suspend() throws JSONException {
        System.out.println("BasicContextList.suspend()");
        this.active = -1;
        this.status = "sent stop";
        this.sendMessage(this.stopString);
    }

    /**
     * Todo: seems to do the same thing as suspend. What are the parameters for?
     * Stop this context. Called at the end of trials to ensure that the context is shut off.
     *
     */
    public void stop() throws JSONException {
        //System.out.println("BasicContextList.stop()");
        this.active = -1;
        this.status = "sent stop";
        this.sendMessage(this.stopString);
        this.waiting = false;
    }
    // suspend vs stop: stop means the mouse "completed" or "ran through" the context whereas
    // suspend means it shouldn't be active for this lap
    /**
     * Todo: does this send a message to the arduino?
     *
     * @param message ?
     */
    public void sendMessage(String message) {
        //System.out.println("BasicContextList.sendMessage(" + message + ")");
        this.comm.sendMessage(message);
    }

    /**
     * ?
     */
    public void shutdown() throws JSONException { }
}
