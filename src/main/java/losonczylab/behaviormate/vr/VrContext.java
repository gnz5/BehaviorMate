package losonczylab.behaviormate.vr;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.BasicContextList;
import losonczylab.behaviormate.util.JSONHelper;
import losonczylab.behaviormate.util.Str;
import losonczylab.behaviormate.util.UdpClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import static losonczylab.behaviormate.util.JSONHelper.JSONArrayToStringArray;

/**
 * ?
 */
public abstract class VrContext extends BasicContextList {

    /**
     * ?
     */
    protected UdpClient[] comms;

    /**
     * ?
     */
    protected String[] comm_ids;

    /**
     * ?
     */
    protected JSONObject log_json;

    /**
     * ?
     */
    protected JSONObject vr_config;

    /**
     * ?
     *
     * @param context_info ?
     */
    public VrContext(JSONObject context_info) throws JSONException {
        super(context_info);
    
        //this.tc = tc;
        this.comm_ids = JSONArrayToStringArray(context_info.getJSONArray("display_controllers"));//.getStringArray();
        this.context_info = context_info;

        this.log_json = new JSONObject();
        this.log_json.put(Str.CONTEXT, (new JSONObject()).put("id", id));
        //this.log_json.getJSONObject(Str.CONTEXT).setString("id", id);

        JSONObject json_msg = new JSONObject();
        json_msg.put("action", "start");
        json_msg.put(Str.CONTEXT, this.id);
        this.startString = json_msg.toString();

        json_msg.put("action", "stop");
        this.stopString = json_msg.toString();
        this.vr_config = null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setupVr() throws JSONException {
        if (this.vr_config != null) {
            this.sendMessage(vr_config.toString());
        }
        this.sendMessage(this.stopString);
    }

    /**
     * ?
     *
     * @param comms Channel to post messages for configuring, starting or stopping contexts.
     * @return ?
     */
    public boolean setupComms(ArrayList<UdpClient> comms) throws JSONException {
        this.comms = new UdpClient[comm_ids.length];
        for (int i = 0; i < comm_ids.length; i++) {
            boolean found = false;
            for (UdpClient c : comms) {
                if (c.getId().equals(this.comm_ids[i])) {
                    found = true;
                    this.comms[i] = c;
                }
            }
            if (!found) {
                return false;
            }
        }

        setupVr();
        return true;
    }


    public void sendCreateMessages() {
        this.status = "off";
    }

    /**
     * ?
     */
    public void suspend() {
        this.status = "off";
        sendMessage(this.stopString);

        if (this.active != -1) {
            //float time = BehaviorMate.tc.getTime();
            try {
                this.log_json.getJSONObject(Str.CONTEXT).put("action", "stop");
                BehaviorMate.tc.writeLog(this.log_json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.active = -1;
        }
    }

    /**
     * ?
     *
     */
    public void stop() {
        if (this.active != -1) {
            try {
                this.log_json.getJSONObject(Str.CONTEXT).put("action", "stop");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            BehaviorMate.tc.getMsgBuffer()[0] = this.log_json;
        }

        this.active = -1;
        this.status = "off";
        sendMessage(this.stopString);
    }


    public void sendMessage(String message) {
        for (UdpClient udpClient : this.comms) {
            udpClient.sendMessage(message);
        }
    }


    protected void updateVr(boolean is_active, float position, float time, int lap) throws JSONException { }

    /**
     * ?
     *
     * @return ?
     */
    public boolean check() {
        JSONObject[] msg_buffer = BehaviorMate.tc.getMsgBuffer();

        boolean prev_active = (this.active != -1);
        boolean is_active = super.check();
        waiting = false;

        if (is_active && !prev_active) {
            status = "on";
            try {
                log_json.getJSONObject(Str.CONTEXT).put("action", "start");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msg_buffer[0] = this.log_json;
        } else if (!is_active && prev_active) {
            status = "off";
            try {
                log_json.getJSONObject(Str.CONTEXT).put("action", "stop");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msg_buffer[0] = log_json;
        }

        try {
            updateVr(is_active, BehaviorMate.tc.getPosition(), BehaviorMate.tc.getTime(), BehaviorMate.tc.getLapCount());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return is_active;
    }

//    /**
//     * ?
//     *
//     * @param msg_buffer ?
//     */
//    public void trialStart(JSONObject[] msg_buffer) {
//        setupVr();
//
//        if (this.vr_config != null) {
//            JSONObject config_msg = new JSONObject();
//            config_msg.setString("id", this.id);
//            config_msg.setJSONObject("vr_config", this.vr_config);
//            msg_buffer[0] = config_msg;
//        }
//    }

    /**
     * ?
     */
    public void end() throws JSONException {
        //JSONObject end_msg = new JSONObject();
        //end_msg.put(Str.CONTEXT, id);
        //end_msg.put("action", "clear");
        //this.sendMessage(end_msg.toString());
        sendMessage(JSONHelper.createNewEndMessage(Str.CONTEXT, id).toString());
    }

    /**
     * ?
     */
    public void shutdown() {
        //JSONObject end_msg = new JSONObject();
        //end_msg.put(Str.CONTEXT, id);
        //end_msg.put("action", "clear");
        //this.sendMessage(end_msg.toString());
        try {
            sendMessage(JSONHelper.createNewEndMessage(Str.CONTEXT, id).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
