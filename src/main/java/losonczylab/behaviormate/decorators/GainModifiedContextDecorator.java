package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Wraps a BasicContextList. Disables contexts based on lap count.
 */
public final class GainModifiedContextDecorator extends ContextListDecorator {

    /**
     * @param context_list ContextList instance the decorator will wrap.
     * @param context_info ?
     */
    public GainModifiedContextDecorator(ContextList context_list, JSONObject context_info) throws JSONException {
        super(context_list);
        //System.out.println("new GainModifiedContextDecorator(" + context_list + ", " + context_info + ")");
        this.context_list.setPositionScale(BehaviorMate.tc.getPositionScale());
        this.context_list.setPositionScaleMod((float) context_info.getDouble("position_scale"));
        this.context_list.setGainModified(true);
    }

    /**
     * Check if the context list should be suspended based on the current lap.
     *
     * @return           <code>true</code> if the context should be suspended, <code>false</code>
     *                   otherwise.
     */
    public boolean check_suspend() {
        return context_list.check();
    }
}