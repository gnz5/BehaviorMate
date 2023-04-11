package losonczylab.behaviormate.vr;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.BasicContextList;
import losonczylab.behaviormate.util.JSONHelper;
import losonczylab.behaviormate.util.Str;
import losonczylab.behaviormate.util.UdpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import static losonczylab.behaviormate.util.JSONHelper.JSONArrayToStringArray;

/**
 * ?
 */
public class VrContextList extends BasicContextList {
    /**
     * ?
     */
    protected float previous_location;

    /**
     * ?
     */
    protected UdpClient[] comms;

    /**
     * ?
     */
    protected JSONObject position_json;

    /**
     * ?
     */
    protected JSONObject position_data;

    /**
     * ?
     */
    protected String[] comm_ids;

    /**
     * ?
     */
    protected String sceneName;

    /**
     * ?
     */
    protected float startPosition;

    /**
     * ?
     */
    protected JSONObject vr_config;

    /**
     * ?
     *
     * @param context_info ?
     */
    public VrContextList(JSONObject context_info) throws JSONException {
        super(context_info);

        //this.tc = tc;
        this.vr_config = null;
        this.comm_ids = JSONArrayToStringArray(context_info.getJSONArray("display_controllers")); //.getStringArray();
        this.context_info = context_info;

        this.sceneName = context_info.optString("scene_name", "_vrMate_main");

        position_data = new JSONObject();
        position_json = new JSONObject();
        this.log_json.getJSONObject(Str.CONTEXT).put("scene", this.sceneName);

        this.previous_location = -1;

        JSONObject start_msg = new JSONObject();
        start_msg.put("action", "start");
        start_msg.put(Str.CONTEXT, this.id);
        this.startString = start_msg.toString();

        JSONObject stop_msg = new JSONObject();
        stop_msg.put("action", "stop");
        stop_msg.put(Str.CONTEXT, this.id);
        this.stopString = stop_msg.toString();

        this.startPosition = (float) context_info.optDouble("start_position", -1.0f);
    }

    /**
     * ?
     */
    public void setupVr() throws JSONException {
        JSONObject scene_msg = new JSONObject();
        scene_msg.put("action", "editContext");
        scene_msg.put("type", "scene");
        scene_msg.put("scene", this.sceneName);
        scene_msg.put(Str.CONTEXT, this.id);
        sendMessage(scene_msg.toString());

        if (!this.context_info.isNull("vr_file")) {
            setupVr(this.context_info.getString("vr_file"));
        } else {
            this.vr_config = null;
        }
    }

    /**
     * ?
     *
     * @param vr_file ?
     */
    public void setupVr(String vr_file) throws JSONException {
        this.vr_config = new JSONObject(JSONHelper.parseJsonFile(vr_file).toString()); //parseJSONObject(BehaviorMate.parseJsonFile(vr_file).toString());
        JSONArray objects = vr_config.getJSONArray("objects");
        JSONObject msg_json = new JSONObject();
        msg_json.put("action", "editContext");
        msg_json.put(Str.CONTEXT, this.id);
        msg_json.put("type", "cue");

        //TODO: if no id is set, set one here.
        for (int i = 0; i < objects.length(); i++) {
            msg_json.put("object", objects.getJSONObject(i));
            sendMessage(msg_json.toString());
        }

        if (!vr_config.isNull("skybox")) {
            msg_json.put("type", "skybox");
            msg_json.put("skybox", vr_config.getString("skybox"));
            sendMessage(msg_json.toString());
        }
    }

    /**
     * ?
     *
     * @param comms channel to post messages for configuring, starting or stopping contexts.
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
                    JSONObject msg_json = new JSONObject();
                    msg_json.put("view", this.context_info.getJSONArray("views").getJSONObject(i));
                    c.sendMessage(msg_json.toString());
                    c.sendMessage(stopString);
                    try {
                        Thread.sleep(10);
                    } catch (Exception ignored){}
                }
            }
            if (!found) {
                return false;
            }

        }

        setupVr();
        return true;
    }

    /**
     * ?
     *
     * @return ?
     */
    public boolean check() {
        float position = BehaviorMate.tc.getPosition();
        //float time = BehaviorMate.tc.getTime();
        JSONObject[] msg_buffer = BehaviorMate.tc.getMsgBuffer();

        boolean inZone = false;
        int i = 0;
        for (; i < this.contexts.size(); i++) {
            if (this.contexts.get(i).check()) {
                inZone = true;
                break;
            }
        }

        if ((this.active != -1) && (position != previous_location)) {
            try {
                position_data.put("y", position/10);
                position_json.put("position", position_data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendMessage(position_json.toString());
            previous_location = position;
        }

        if ((!inZone) && (this.active != -1)) {
            this.active = -1;
            this.status = "off";
            sendMessage(this.stopString);

            try {
                this.log_json.getJSONObject(Str.CONTEXT).put("action", "stop");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msg_buffer[0] = this.log_json;
        } else if((inZone) && (this.active != i)) {
            this.active = i;
            this.status = "on";
            sendMessage(this.startString);
            if (this.startPosition != -1) {
                BehaviorMate.tc.setPosition(this.startPosition);
            }
            try {
                position_data.put("y", position/10);
                position_json.put("position", position_data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendMessage(position_json.toString());
            previous_location = position;

            try {
                this.log_json.getJSONObject(Str.CONTEXT).put("action", "start");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            msg_buffer[0] = this.log_json;
        }

        return (this.active != -1);
    }

//    /**
//     * ?
//     *
//     * @param msg_buffer ?
//     */
//    public void trialStart(JSONObject[] msg_buffer) {
//        setupVr();
//
//        JSONObject config_msg = new JSONObject();
//        config_msg.setString("id", this.id);
//        config_msg.setJSONObject("vr_config", this.vr_config);
//        msg_buffer[0] = config_msg;
//    }

    /**
     * ?
     */
    public void end() throws JSONException {
        JSONObject end_msg = new JSONObject();
        end_msg.put(Str.CONTEXT, this.id);
        end_msg.put("action", "clear");
        this.sendMessage(end_msg.toString());
    }

    /**
     * ?
     */
    public void suspend() {
        this.active = -1;
        this.status = "off";
        sendMessage(this.stopString);

        //float time = BehaviorMate.tc.getTime();
        try {
            this.log_json.getJSONObject(Str.CONTEXT).put("action", "stop");
            BehaviorMate.tc.writeLog(this.log_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ?
     *
     * @param time ?
     * @param msg_buffer ?
     */
    public void stop(float time, JSONObject[] msg_buffer) throws JSONException {
        if (this.active != -1) {
            this.log_json.getJSONObject(Str.CONTEXT).put("action", "stop");
            msg_buffer[0] = this.log_json;
        }

        this.active = -1;
        this.status = "off";
        sendMessage(this.stopString);
    }

    /**
     * ?
     *
     * @param message ?
     */
    public void sendMessage(String message) {
        if (this.comms == null) {
            System.out.println("comms null");
            System.out.println(message);
            return;
        }

        for (UdpClient udpClient : this.comms) {
            udpClient.sendMessage(message);
        }
    }

    /**
     * ?
     */
    public void shutdown() {
        JSONObject end_msg = new JSONObject();
        try {
            end_msg.put(Str.CONTEXT, this.id);
            end_msg.put("action", "clear");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.sendMessage(end_msg.toString());
    }
}
