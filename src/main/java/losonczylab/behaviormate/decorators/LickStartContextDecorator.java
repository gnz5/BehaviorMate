package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * ?
 */
public final class LickStartContextDecorator extends ContextListDecorator {

    /**
     * ?
     */
    private int prev_lickcount;

    /**
     * ?
     */
    private int last_position;

    /**
     * ?
     */
    private float entered_time;

    /**
     * ?
     */
    private final float max_time;

    /**
     * ?
     *
     * @param context_list <code>ContextList</code> instance the decorator will wrap.
     * @param context_info JSONObject containing the configuration information for this context
     *                     from the settings file. The <tt>max_time</tt> parameter is optional
     *                     and will default to -1 if not provided.
     */
    public LickStartContextDecorator(ContextList context_list, JSONObject context_info) {
        super(context_list);
        last_position = -1;
        entered_time = -1;
        prev_lickcount = 0;
        max_time = context_info.optInt("max_time", -1);
    }

    /**
     * Resets the decorator's attributes to constructor defaults and resets the wrapped
     * <code>ContextList</code>.
     */
    public void reset() {
        last_position = -1;
        entered_time = -1;
        context_list.reset();
    }

    /**
     * Check the state of the list as well as the contexts contained in this and decide if they
     * should be activated or not. Send the start/stop messages as necessary. this method gets
     * called for each cycle of the event loop when a trial is started.
     *
     * @return           ?
     */
    public boolean check() {
        float time = BehaviorMate.tc.getTime();
        int lick_count = BehaviorMate.tc.getLickCount();

        boolean inPosition = (max_time == -1);
        if (!context_list.isActive()) {
            int size = context_list.size();
            for (int i = 0; !inPosition && i < size; i++) {
                if (context_list.getContext(i).checkPosition()) {
                    inPosition = true;
                    if (entered_time == -1) {
                        context_list.setStatus("no lick");
                        entered_time = time;
                    } else if (i == last_position) {
                        if (entered_time + max_time < time) {
                            prev_lickcount = lick_count;
                            context_list.setStatus("timed out");
                            return false;
                        }
                    }
                    last_position = i;
                    break;
                }
            }

            if (!inPosition) {
                entered_time = -1;
                context_list.setStatus("stopped");
                prev_lickcount = lick_count;
                return false;
            }

            if (lick_count != prev_lickcount) {
                prev_lickcount = lick_count;
                return context_list.check();
            } else {
                return false;
            }
        }

        prev_lickcount = lick_count;
        return context_list.check();
    }
}
