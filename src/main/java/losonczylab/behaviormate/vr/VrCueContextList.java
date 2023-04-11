package losonczylab.behaviormate.vr;

import losonczylab.behaviormate.util.Str;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * ?
 */
public class VrCueContextList extends VrContext {
    /**
     * ?
     */
    protected ArrayList<Integer> object_locations;

    /**
     * ?
     */
    protected int true_size;

    /**
     * ?
     */
    protected float display_radius_unscale;

    /**
     * ?
     *
     * @param context_info ?
     */
    public VrCueContextList(JSONObject context_info) throws JSONException {
        super(context_info);

        JSONArray objects = this.context_info.getJSONArray("objects");
        this.display_radius_unscale = (float) this.context_info.optDouble("display_radius", 0);
        this.true_size = objects.length();
        this.shuffle_contexts = this.context_info.optBoolean("shuffle", false);
        object_locations = new ArrayList<>();
        for (int i = 0; i < objects.length(); i++) {
            this.object_locations.add(objects.getJSONObject(i).getJSONArray("Position").getInt(1)*10);
        }
    }

    /**
     * ?
     */
    public void setupVr() throws JSONException {
        this.sendMessage(this.stopString);
        JSONObject setup_msg = new JSONObject();
        setup_msg.put("action", "editContext");
        setup_msg.put(Str.CONTEXT, this.id);
        setup_msg.put("type", "cue");

        JSONArray objects = this.context_info.getJSONArray("objects");
        for (int i = 0; i < objects.length(); i++) {
            setup_msg.put("object", objects.getJSONObject(i));
            this.sendMessage(setup_msg.toString());
        }
    }

    /**
     * ?
     */
    public void move(int index, int location) {
        if (this.comms == null) {
            return;
        }

        this.contexts.get(index).move(location);
        this.object_locations.set(index, location);
        JSONObject setup_msg = new JSONObject();
        try {
            setup_msg.put("action", "editContext");
            setup_msg.put(Str.CONTEXT, this.id);
            setup_msg.put("type", "move_cue");
            JSONArray objects = this.context_info.getJSONArray("objects");
            for (int i = 0; i < objects.length(); i++) {
                setup_msg.put("object", objects.getJSONObject(i));
                JSONArray position = setup_msg.getJSONObject("object").getJSONArray("Position");
                position.put(1, location/10.0f);
                setup_msg.getJSONObject("object").put("Position", position);
                this.sendMessage(setup_msg.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * ?
     */
    public int size() {
        return this.true_size;
    }

    /**
     * ?
     */
    public int getLocation(int i) {
        return this.object_locations.get(i);
    }

    /**
     * ?
     */
    public void setDisplayScale(float scale) {
        this.scale = scale;
        this.display_radius = this.display_radius_unscale * scale;
    }
}
