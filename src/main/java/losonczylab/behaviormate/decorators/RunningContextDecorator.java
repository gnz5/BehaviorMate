package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Suspends contexts when mouse is not running.
 */
public final class RunningContextDecorator extends ContextListDecorator {

    /**
     * ?
     */
    private float prev_time;

    /**
     * ?
     */
    private float prev_position;

    /**
     * ?
     */
    private int prev_lap;

    /**
     * ?
     */
    private final float min_dt;

    /**
     * ?
     */
    private final float min_dy;

    /**
     * ?
     */
    private final boolean use_abs_dy;

    /**
     * ?
     *
     * @param context_list <code>ContextList</code> instance the decorator will wrap.
     * @param context_info JSONObject containing the configuration information for this context
     *                     from the settings file. The following properties should be defined in the
     *                     settings file: <tt>threshold</tt>, <tt>max_dt</tt>, <tt>min_dt</tt>,
     *                     <tt>min_dy</tt>, <tt>use_abs_dy</tt>. If they are not defined they will
     *                     default to 0, 0.1, 0.2, 5, and false, respectively.
     */
    public RunningContextDecorator(ContextList context_list, JSONObject context_info) throws JSONException, IOException {
        super(context_list);
        //System.out.println("new RunningContextDecorator2(" + context_list + ", " + context_info + ")");
        //this.context_list.display_color_suspended = new int[] {100, 100, 100};
        this.context_list.setDisplayColorSuspended(new int[] {100, 100, 100});


        //float threshold = context_info.getFloat("threshold", 0.0f);
        //float max_dt = context_info.getFloat("max_dt", 0.1f);
        min_dt = (float) context_info.optDouble("min_dt", 0.2f);
        min_dy = (float) context_info.optDouble("min_dy", 5);
        use_abs_dy = context_info.optBoolean("use_abs_dy", false);

        prev_time = 0;
        prev_position = 0;
        prev_lap = 0;

        setSuspendable(true);
        context_list.suspend();
    }

    /**
     * Check the state of the list as well as the contexts contained in this and decide if they
     * should be activated or not. Send the start/stop messages as necessary. this method gets
     * called for each cycle of the event loop when a trial is started.
     *
     * @return           ?
     */
    public boolean check_suspend() {
        float position = BehaviorMate.tc.getPosition();
        float time = BehaviorMate.tc.getTime();
        int lap = BehaviorMate.tc.getLapCount();

        if (lap != prev_lap) {
            position += (lap - prev_lap) * BehaviorMate.tc.getTrackLength();

            float temp = (lap - prev_lap) * BehaviorMate.tc.getTrackLength();
            System.out.printf("position += %f\n", temp);
        }

        float dt = time - prev_time;
        if (dt < min_dt) {
            //System.out.println("RunningContextDecorator2.checksuspend() --> " + context_list.isSuspended());
            return context_list.isSuspended();
        }
        prev_time = time;

        float dy = position - prev_position;
        prev_position = position;
        prev_lap = lap;

        if (use_abs_dy) {
            //System.out.println("RunningContextDecorator2.checksuspend() --> " + (Math.abs(dy) < min_dy));
            return Math.abs(dy) < min_dy;
        } else {
            //System.out.println("RunningContextDecorator2.checksuspend() --> " + (dy < min_dy));
            return dy < min_dy;
        }
    }

    /**
     * Set instance attributes to their defaults and suspend the wrapped <code>ContextList</code>/
     *
     */
    public void stop() {
        this.prev_lap = 0;
        this.prev_position = 0;
        this.prev_time = 0;
        context_list.suspend();
    }
}