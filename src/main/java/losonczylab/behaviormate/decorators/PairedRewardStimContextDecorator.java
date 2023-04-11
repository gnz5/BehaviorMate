package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Wraps a BasicContextList. Disables contexts based on lap count.
 */
public final class PairedRewardStimContextDecorator extends ContextListDecorator {

    /**
     * @param context_list ContextList instance the decorator will wrap.
     * @param context_info JSONObject containing the configuration information
     *                     for this instance's <code>ContextList</code> from the settings file.
     *                     context_info should have the parameter <tt>n_lap</tt>
     *                     set in order to indicate when to turn off. This value
     *                     defaults to 2, meaning the context will be active on
     *                     alternating laps.
     */
    public PairedRewardStimContextDecorator(ContextList context_list, JSONObject context_info) {
        super(context_list);
        //System.out.println("new PairedRewardStimContextDecorator(" + context_list + ", " + context_info + ")");
    }


    /**
     * Check if the context list should be suspended based on the current lap.
     *
     * @return           <code>true</code> if the context should be suspended, <code>false</code>
     *                   otherwise.
     */
    public boolean check_suspend() {
        //System.out.println("PairedRewardStimContextDecorator.check_suspend()");
        return context_list.check();
    }
}