package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

/**
 * Used to specify blocks of time when the ContextList will be active. These blocks can be set to repeat. For example,
 * to make the ContextList activate from time = 3 to time = 5 and from time = 7 to time = 8 use
 *      "times": [[3,5], [7,8]]
 * To make it so these blocks repeat every 10 seconds so that the ContextList will be active at times 3-5, 7-8, 13-15,
 * 17-18, 23-25, 37-38, etc., use
 *      "times": [[3,5], [7,8]],
 *      "repeat": 10
 * <tt>Repeat</tt> must be greater than 0 and <tt>times</tt> must be a non-empty array whose elements are arrays with
 * exactly 2 elements.
 */
public final class TimedContextDecorator extends ContextListDecorator {

    /**
     * ?
     */
    private final float[] times_original;

    /**
     * ?
     */
    private final boolean adjust_zero_lap;

    /**
     * ?
     */
    private final float repeat;

    /**
     * ?
     */
    private float[] times;

    /**
     * ?
     */
    private int time_idx;

    /**
     * ?
     */
    private int zero_lap;

    /**
     * ?
     */
    private int actual_lap;

    /**
     * ?
     *
     * @param context_list <code>ContextList</code> instance the decorator will wrap.
     * @param context_info JSONObject containing the configuration information for this context
     *                     from the settings file. The following JSON literal should be defined
     *                     in the settings file. The property key: <datatype, value> means that the key
     *                     is optional and will default to value if not provided and should be of type
     *                     datatype if provided.
     *
     * {
     * 	    "times": [ [<float>, <float>], [<float>, <float>], ... , [<float>, <float>] ],
     * 	    "repeat": <float, -1>,
     * 	    "no_display": <boolean, false>,
     * 	    "adjust_zero_lap": <boolean, true>
     * }
     */
    public TimedContextDecorator(ContextList context_list, JSONObject context_info) throws RuntimeException, JSONException {
        super(context_list);
        //System.out.println("new TimedContextDecorator2(" + context_list + ", " + context_info + ")");

        if (context_info.isNull("times")) {
            time_idx = -1;
            times = null;
            times_original = null;
        } else {
            JSONArray times_array = context_info.getJSONArray("times");

            if (times_array.length() == 0) {
                throw new RuntimeException("\"times\" property in settings file cannot be an empty array.");
            }

            times = new float[2 * times_array.length()];
            for (int i = 0; i < times_array.length(); i++) {
                JSONArray start_stop = times_array.getJSONArray(i);

                if (start_stop.length() != 2) {
                    throw new RuntimeException(
                            "\"times\" property in settings file must be an array whose elements" +
                                    " are arrays, each with 2 elements. Ex: [[1,2], [3,4]]");
                }

                times[2*i] = (float) start_stop.getDouble(0);
                times[2*i + 1] = (float) start_stop.getDouble(1);
            }

            time_idx = 0;
            times_original = times.clone();
        }

        repeat = (float) context_info.optDouble("repeat", -1);
        //System.out.println("repeat = " + repeat + ", times = " + Arrays.toString(times));
        if (repeat < 2) {
            throw new RuntimeException("\"Repeat\" property in settings file cannot be less than 2.");
        }

        if (context_info.optBoolean("no_display", false)) {
            //this.context_list.display_color_suspended = null;
            this.context_list.setDisplayColorSuspended(null);
        }

        adjust_zero_lap = context_info.optBoolean("adjust_zero_lap", true);
        zero_lap = 0;
        //isSuspendable = true;
        setSuspendable(true);
    }

    /**
     * Resets the wrapped <code>ContextList</code>.
     */
    public void reset() {
        super.reset();
    }


    /**
     * ?
     *
     * @return           ?
     */
    public boolean check_suspend() {
        float time = BehaviorMate.tc.getTime();

        if (time_idx != -1) {
            if (time_idx >= times.length) {
                if (repeat != -1) {
                    for (int i = 0; i < times.length; i++) {
                        times[i] = times[i] + repeat;
                    }
                    time_idx = 0;
                } else {
                    time_idx = -1;
                }
            } else if (time >= times[time_idx]) {
                time_idx++;
            }

            if (time_idx % 2 == 0 || time_idx == -1) {
                if (adjust_zero_lap) {
                    zero_lap = actual_lap;
                }

                if (!context_list.isSuspended()) {
                    reset();
                }
                System.out.printf("TimedContextDecorator2.check_suspend(%f, %f, %d) [time_idx = %d, times = %s] --> true\n",
                        BehaviorMate.tc.getPosition(), time, BehaviorMate.tc.getLapCount(), time_idx, Arrays.toString(times));
                return true;
            }
        } else {
            if (adjust_zero_lap) {
                zero_lap = actual_lap;
            }
            System.out.printf("TimedContextDecorator2.check_suspend(%f, %f, %d) [time_idx = %d, times = %s] --> true\n",
                    BehaviorMate.tc.getPosition(), time, BehaviorMate.tc.getLapCount(), time_idx, Arrays.toString(times));
            return true;
        }
        System.out.printf("TimedContextDecorator2.check_suspend(%f, %f, %d) [time_idx = %d, times = %s] --> false\n",
                BehaviorMate.tc.getPosition(), time, BehaviorMate.tc.getLapCount(), time_idx, Arrays.toString(times));
        return false;
    }

    /**
     * Check the state of the list as well as the contexts contained in this
     * and decide if they should be activated or not. Send the start/stop messages
     * as necessary. this method gets called for each cycle of the event loop
     * when a trial is started.
     *
     * @return           ?
     */
    public boolean check() {

        this.actual_lap = BehaviorMate.tc.getLapCount();
        boolean result = super.check();
        //System.out.println("TimedContextDecorator2.check(6) --> " + result);
        return result;
        //return super.check(position, time, lap-this.zero_lap, lick_count, sensor_counts, msg_buffer);
    }
}