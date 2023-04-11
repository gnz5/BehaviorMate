package losonczylab.behaviormate.lists;

import losonczylab.behaviormate.core.BasicContextList;
import losonczylab.behaviormate.core.Context;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.decorators.JointContextDecorator;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Placeholder
 */
public class JointContextList extends BasicContextList {

    /**
     * ?
     */
    protected String joint_list_id;

    /**
     * ?
     */
    protected ContextList joint_list;

    /**
     * ?
     */
    protected int offset;

    /**
     * ?
     */
    protected boolean fix_radius;

    /**
     * ?
     *
     * @param context_info JSONObject containing the configuration information for this context
     *                     from the settings file. <tt>context_info</tt> should have the parameter
     *                     <tt>joint_id</tt> set to do ?. <tt>radius</tt> and <tt>offset</tt> are each
     *                     optional and will default to 0 if not provided.
     */
    public JointContextList(JSONObject context_info) throws JSONException {
        super(context_info);
        joint_list_id = context_info.getString("joint_id");
        setRadius(context_info.optInt("radius", 0));
        //fix_radius = (!context_info.hasKey("radius"));
        fix_radius = (context_info.isNull("radius"));
        offset = context_info.optInt("offset", 0);
    }

    /**
     * ?
     *
     * @param contexts ?
     */
    public void registerContexts(ArrayList<ContextList> contexts) {
        joint_list = null;
        // for each context list in the arraylist
        for (int i = 0; i < contexts.size(); i++) {
            ContextList context_list = contexts.get(i); // the current context list
            // check if this context list's joint_list_id equals the current context's joint_list_id
            if (context_list.getId().equals(joint_list_id)) {
                // wrap the current context_list in a JointContextMasterDecorator
                context_list = new JointContextDecorator(context_list, this);
                // set the ith element of the arraylist to the wrapped current context list
                contexts.set(i, context_list);
                // set this context list's joint_list to the current wrapped context_list
                joint_list = context_list;
                break;
            }
        }

        if (joint_list == null) {
            //TODO: throw exception
            return;
        }

        //System.out.println(fix_radius);
        if (fix_radius) {
            setRadius(joint_list.getRadius());
        }
        update();
    }

    /**
     * ?
     */
    public void update() {
        if (joint_list.size() != size()) {
            super.clear();
            for (int i = 0; i < joint_list.size(); i++) {
                super.add(joint_list.getLocation(i) + offset);
            }
        } else {
            for (int i = 0; i < joint_list.size(); i++) {
                super.move(i, joint_list.getLocation(i) + offset);
            }
        }

        for (Context context : contexts) {
            context.reset();
        }
    }

    // Todo: can these unimplemented methods be commented out or were they overridden purposely so
    //  they would do nothing?
    public void move(int index, int location) { }
    public void shuffle() { }
    protected void add(int location) { }
    public void clear() { }
    public void reset() { }
}
