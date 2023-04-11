package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ThreadLocalRandom;

/**
 * ContextList starts off active, then after each completed lap, the ContextList list will be suspended
 * for some time, t. After t seconds, the ContextList will be unsuspended. This repeats for the duration
 * of the experiment. If <tt>random_iti</tt> in the settings file is set to true,<tt>iti_time_min</tt> and
 * <tt>iti_time_max</tt> must be set and will cause t to be a random float between <tt>iti_time_min</tt>
 * and <tt>iti_time_max</tt>. If <tt>random_iti</tt> is not set or set to false, <tt>iti_time</tt>
 * must be set and will cause t to be equal to <tt>iti_time</tt>.
 */

// Todo: what is the desired behavior if the ITI period ends in the middle of a lap. Should the ContextList
// Todo: become suspended again after the current lap or should it be suspended after the current lap and
// Todo: one complete lap have been completed?
// Currently, the former occurs. This can lead to situations where the mouse would get few or no
// rewards.
public final class TimedITIContextDecorator extends ContextListDecorator {

    /**
     * ?
     */
    private float next_start;

    /**
     * ?
     */
    private int start_lap;

    /**
     * ?
     */
    private float iti_time;

    /**
     * ?
     */
    private int iti_time_max;

    /**
     * ?
     */
    private final boolean random_iti;

    /**
     * ?
     *
     * @param context_list <code>ContextList</code> instance the decorator will wrap.
     * @param context_info JSONObject containing the configuration information for this context
     *                     from the settings file. The following JSON literal should be defined
     *                     in the settings file. The property key: <datatype, value> means that the key
     *                     is optional and will default to value if not provided and should be of type
     *                     datatype if provided.
     */
    public TimedITIContextDecorator(ContextList context_list, JSONObject context_info)
            throws RuntimeException, JSONException {
        super(context_list);

        random_iti = context_info.optBoolean("random_iti", false);

        if (random_iti) {
            int iti_time_min = context_info.getInt("iti_time_min");
            iti_time_max = context_info.getInt("iti_time_max");

            if (iti_time_min < 0 || iti_time_max <= 0 || iti_time_min >= iti_time_max) {
                throw new RuntimeException();
            }
        } else {
            iti_time = (float) context_info.optDouble("iti_time");

            if (iti_time <= 0) {
                throw new RuntimeException();
            }
        }

        if (context_info.optBoolean("no_display", false)) {
            //this.context_list.display_color_suspended = null;
            this.context_list.setDisplayColorSuspended(null);
        }

        next_start = 0;
        start_lap = 0;
        //isSuspendable = true;
        setSuspendable(true);
    }

    /**
     *
     * @return The string representing the current status of the contexts.
     */
    public String getStatus() {
        if (!context_list.isSuspended()) {
            return this.context_list.getStatus();
        } else {
            return "Next Trial: " + this.next_start + "s";
        }
    }

    /**
     * ?
     *
     * @return           ?
     */
    public boolean check_suspend() {
        float time = BehaviorMate.tc.getTime();
        int lap = BehaviorMate.tc.getLapCount();

        //System.out.println("BasicContextList.suspended = " + context_list.isSuspended());
        if (context_list.isSuspended()) {
            //System.out.println("ContextList is suspended");
            if (time > next_start) {
                start_lap = lap;
                BehaviorMate.tc.setLapLock(false);
                System.out.printf(
                        "(A) TimedITIContextDecorator.check_suspend(%f, %f, %d) [next_start = %f, start_lap = %d] --> false\n",
                        BehaviorMate.tc.getPosition(), time, lap, next_start, start_lap);
                return false;
            }
        } else if (lap > start_lap) {
            if (random_iti) {
                //int max = (iti_time_max - iti_time_min + 1) + iti_time_min;
                next_start = time + ThreadLocalRandom.current().nextInt(iti_time_max + 1);
            } else {
                next_start = time + iti_time;
            }

            BehaviorMate.tc.setLapLock(true);
            System.out.printf(
                    "(B) TimedITIContextDecorator.check_suspend(%f, %f, %d) [next_start = %f, start_lap = %d] --> true\n",
                    BehaviorMate.tc.getPosition(), time, lap, next_start, start_lap);
            return true;
        }

        System.out.printf(
                "(C) TimedITIContextDecorator.check_suspend(%f, %f, %d) [next_start = %f, start_lap = %d] --> %s\n",
                BehaviorMate.tc.getPosition(), time, lap, next_start, start_lap, context_list.isSuspended());
        return context_list.isSuspended();
    }

    /**
     * Resets the state of the contexts. Contexts which have been triggered are
     * reactivated and allowed to be triggered again. If <code>shuffle_contexts</code>
     * is <code>true</code>, the contexts will be shuffled.
     */
    public void reset() {
        next_start = 0;
        start_lap = 0;
        BehaviorMate.tc.setLapLock(false);
        context_list.reset();
    }
}