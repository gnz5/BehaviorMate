package losonczylab.behaviormate.vr;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.util.Str;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ?
 */
public class VrExtendedContextList extends VrContextList {
    /**
     * ?
     */
    protected int lap_factor;

    /**
     * ?
     */
    protected int backtrack;

    /**
     * ?
     */
    protected float previous_position;

    /**
     * ?
     *
     * @param context_info ?
     */
    public VrExtendedContextList(JSONObject context_info)
            throws Exception {
        super(context_info);

        this.lap_factor = 2;
        this.backtrack = 0;
        this.previous_position = -1;
    }

    /**
     * ?
     *
     * @return ?
     */
    public boolean check() {
        try {
            float position = BehaviorMate.tc.getPosition();
            int lap = BehaviorMate.tc.getLapCount();
            JSONObject[] msg_buffer = BehaviorMate.tc.getMsgBuffer();

            boolean inZone = false;
            int i=0;
            for (; i < contexts.size(); i++) {
                if (contexts.get(i).check()) {
                    inZone = true;
                    break;
                }
            }

            if ((this.previous_position != -1)
                    && (position - this.previous_position > BehaviorMate.tc.getTrackLength()/2)) {
                this.backtrack--;
            } else if ((this.backtrack < 0)
                    && (this.previous_position - position > BehaviorMate.tc.getTrackLength()/2)) {
                this.backtrack++;
            }
            this.previous_position = position;

            lap = Math.abs(lap + this.backtrack);
            //System.out.println(this.backtrack + " - " + lap);

            float adj_position = lap % this.lap_factor*BehaviorMate.tc.getTrackLength() + position;
            if ((this.active != -1) && (position != previous_location)) {
                position_data.put("y", adj_position/10);
                position_json.put("position", position_data);
                this.status = ""+(int)adj_position + " " + this.backtrack + " " + lap;

                sendMessage(position_json.toString());
                previous_location = position;
            }

            if ((!inZone) && (this.active != -1)) {
                this.active = -1;
                this.status = "off";
                sendMessage(this.stopString);

                log_json.getJSONObject(Str.CONTEXT).put("action", "stop");
                msg_buffer[0] = log_json;
            } else if((inZone) && (this.active != i)) {
                this.active = i;
                //this.status = ""+(int)adj_position;
                this.status = ""+(int)adj_position + " " + this.backtrack + " " + lap;
                if (!context_info.isNull("vr_file")) {
                    setupVr(context_info.getString("vr_file"));
                }
                sendMessage(this.startString);
                position_data.put("y", adj_position/10);
                position_json.put("position", position_data);

                sendMessage(position_json.toString());
                previous_location = position;

                log_json.getJSONObject(Str.CONTEXT).put("action", "start");
                msg_buffer[0] = log_json;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return active != -1;
    }

    /**
     * ?
     *
     * @param time ?
     * @param msg_buffer ?
     */
    public void stop(float time, JSONObject[] msg_buffer) throws JSONException {
        this.backtrack = 0;
        this.previous_position = -1;
        super.stop(time, msg_buffer);
    }
}
