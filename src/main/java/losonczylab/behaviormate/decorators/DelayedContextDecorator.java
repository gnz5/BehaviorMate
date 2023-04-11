package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ?
 */
public final class DelayedContextDecorator extends ContextListDecorator {

    /**
     * ?
     */
    private float current_time;

    /**
     * ContextList will be suspended until <tt>delay</tt> seconds after the start of the trial.
     */
    private final float delay;

    /**
     * ?
     */
    private float start_time;

    /**
     * @param context_list <code>ContextList</code> instance the decorator will wrap.
     * @param context_info JSONObject containing the configuration information
     *                     for this instance's <code>ContextList</code> from the settings file.
     *                     context_info should have the parameter <tt>delay</tt> set
     *                     to the amount of time (Todo: in ms?) to delay the start of the
     *                     contexts.
     */
    public DelayedContextDecorator(ContextList context_list, JSONObject context_info) throws JSONException {
        super(context_list);
        //System.out.println(String.format("new DelayedContextDecorator2(%s, %s)", context_list, context_info));
        delay = (float) context_info.getDouble("delay");
        start_time = 0;
        current_time = 0;
        setSuspendable(true);
    }

    /**
     *
     */
    public void suspend() {
        start_time = current_time + delay;
    }

    /**
     * Checks if the wrapped ContextList should be suspended at the given time and position.
     *
     * @return <code>true</code> if the ContextList should be suspended, <code>false</code> otherwise.
     */
    public boolean check_suspend() {
        float time = BehaviorMate.tc.getTime();
        boolean result = time <= delay;
        String output = "DelayedContextDecorator2.check_suspend(%f, %f, %d, %d, msg_buffer) --> %s";
        System.out.printf(
                (output) + "%n",
                BehaviorMate.tc.getPosition(), time, BehaviorMate.tc.getLapCount(), BehaviorMate.tc.getLickCount(), result);

        return time <= delay;
        //return !(current_time > start_time);
    }
}
