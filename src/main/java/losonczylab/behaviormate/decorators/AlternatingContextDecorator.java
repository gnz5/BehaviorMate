package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONObject;

/**
 * Wraps a BasicContextList. Disables contexts based on lap count.
 */
public final class AlternatingContextDecorator extends ContextListDecorator {

    /**
     * The context list will be suspended every <tt>n_lap</tt> laps, otherwise it will be active.
     */
    private int n_lap;

    /**
     * Delay toggling the suspend state of the context list until <tt>offset_lap</tt>
     * laps have passed.
     */
    private int offset_lap;

    /**
     * @param context_list ContextList instance the decorator will wrap.
     * @param context_info JSONObject containing the configuration information
     *                     for this instance's <code>ContextList</code> from the settings file.
     *                     context_info should have the parameter <tt>n_lap</tt>
     *                     set in order to indicate when to turn off. This value
     *                     defaults to 2, meaning the context will be active on
     *                     alternating laps.
     */
    public AlternatingContextDecorator(ContextList context_list, JSONObject context_info) {
        super(context_list);
        //System.out.println("new AlternatingContextDecorator2(" + context_list + ", " + context_info + ")");
        n_lap = context_info.optInt("n_lap", 2);
        offset_lap = context_info.optInt("offset_lap", 0);
        setSuspendable(true);
    }


    /**
     * Check if the context list should be suspended based on the current lap.
     *
     * @return           <code>true</code> if the context should be suspended, <code>false</code>
     *                   otherwise.
     */
    public boolean check_suspend() {
        int lap = BehaviorMate.tc.getLapCount();
        //System.out.printf("AlternatingContextDecorator2: lap = %d, n_lap = %d%n", lap, n_lap);
        boolean result = !((lap - offset_lap) % n_lap == 0);
//        String output = "AlternatingContextDecorator2.check_suspend(%f, %f, %d, %d, msg_buffer) --> %s";
//        System.out.printf((output) + "%n",
//                BehaviorMate.tc.getPosition(), BehaviorMate.tc.getTime(), lap, BehaviorMate.tc.getLickCount(), result);

        return !((lap - offset_lap) % n_lap == 0);
    }
}