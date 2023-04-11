package losonczylab.behaviormate.lists;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.BasicContextList;
import losonczylab.behaviormate.util.Str;
import losonczylab.behaviormate.util.UdpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * ?
 */
public class GainModifiedContextList extends BasicContextList {

    /**
     * ?
     */
    protected float position_scale_mod;

    /**
     * ?
     *
     * @param context_info JSONObject containing the configuration information for this context
     *                     from the settings file. <tt>context_info</tt> should have the parameter
     *                     <tt>position_scale</tt> set in order to ?.
     * @throws Exception
     */
    public GainModifiedContextList(JSONObject context_info) throws JSONException {
        super(context_info);

        //this.tc = tc;
        //position_scale = BehaviorMate.tc.getPositionScale();
        position_scale_mod = (float) context_info.getDouble("position_scale");
    }

    /**
     * Placeholder
     */
    public void sendCreateMessages() { }

    /**
     * Placeholder
     *
     * @param comms Channel to post messages for configuring, starting or stopping contexts.
     * @return
     */
    public boolean setupComms(ArrayList<UdpClient> comms) {
        return true;
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
        boolean inZone = false;
        int i = 0;
        JSONObject[] msg_buffer = BehaviorMate.tc.getMsgBuffer();

        // This loop checks to see if any of the individual contexts are
        // triggered to be active both in space and time
        for (; i < contexts.size(); i++) {
            if (contexts.get(i).check()) {
                inZone = true;
                break;
            }
        }

        // Decide if the context defined by this ContextList needs to swtich
        // state and send the message to the UdpClient accordingly
        if (!waiting) {
            if (!inZone && active != -1) {
                status = "stopped";
                active = -1;

                try {
                    this.log_json.getJSONObject(Str.CONTEXT).put("action", "stop");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg_buffer[0] = log_json;
                BehaviorMate.tc.setPositionScale(BehaviorMate.tc.getPositionScale());
            } else if(inZone && active != i) {
                active = i;
                status = "started";
                BehaviorMate.tc.setPositionScale(position_scale_mod);

                try {
                    log_json.getJSONObject(Str.CONTEXT).put("action", "start");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msg_buffer[0] = log_json;
            }
        }

        return active != -1;
    }

    /**
     * Suspend all contexts.
     * Todo: why doesn't this send a message?
     */
    public void suspend() {
        active = -1;
        status = "stopped";
    }

    /**
     * Todo: seems to do the same thing as suspend.
     * Stop this context. Called at the end of trials to ensure that the context is shut off.
     */
    public void stop() {
        active = -1;
        status = "stopped";
    }

    // Todo: why is this unimplemented?
    public void sendMessage(String message) { }
}


