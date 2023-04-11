package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * ?
 */
public final class ScheduledContextDecorator extends ContextListDecorator {

    /**
     * ?
     */
    private final int repeat;

    /**
     * ?
     */
    private final boolean keep_on;

    /**
     * ?
     */
    private ArrayList<Integer> lap_list;

    /**
     * ?
     */
    private int last_lap;

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
     * 	    "lap_list": [ [<int[]>], [<int[]>], ... , [<int[]>] ],
     * 	    "no_display": <boolean, false>,
     * 	    "repeat": <int, 0>,
     * 	    "keep_on": <boolean, false>
     * }
     */
    public ScheduledContextDecorator(ContextList context_list, JSONObject context_info) throws JSONException {
        super(context_list);
        //System.out.println("new ScheduledContextDecorator(" + context_list + ", " + context_info + ")");

        this.lap_list = new ArrayList<>();
        JSONArray lap_array;

        if (!context_info.isNull("lap_list")) {
            lap_array = context_info.getJSONArray("lap_list");
            boolean lap_range;
            try {
                lap_array.getJSONArray(0);
                lap_range = true;
            } catch (RuntimeException e) {
                lap_range = false;
            }

            this.lap_list = new ArrayList<>(); // lap_list already initialized
            if (!lap_range) {

                int i = 0;
                for (; i < lap_array.length(); i++) {
                    this.lap_list.add(lap_array.getInt(i));
                }

                last_lap = lap_array.getInt(i-1);
            } else {
                JSONArray range = null;
                for (int j = 0; j < lap_array.length(); j++) {
                    range = lap_array.getJSONArray(j);
                    for (int i = range.getInt(0); i < range.getInt(1); i++) {
                        this.lap_list.add(i);
                    }
                }

                if (range != null) {
                    last_lap = range.getInt(1) - 1;
                } else {
                    last_lap = 0;
                }
            }
        }

        if (context_info.optBoolean("no_display", false)) {
            //this.context_list.display_color_suspended = null;
            this.context_list.setDisplayColorSuspended(null);
        }

        repeat = context_info.optInt("repeat", 0);
        keep_on = context_info.optBoolean("keep_on", false);

        //isSuspendable = true;
        setSuspendable(true);

        //System.out.println("ScheduledContextDecorator: last_lap = " + last_lap);
        //System.out.println("ScheduledContextDecorator: lap_list = " + lap_list);
    }

    /**
     * Check the state of the list as well as the contexts contained in this and decide if they
     * should be activated or not. Send the start/stop messages as necessary. this method gets
     * called for each cycle of the event loop when a trial is started.
     *
     * @return           ?
     */
    public boolean check_suspend() {
        int lap = BehaviorMate.tc.getLapCount();

        if (keep_on && lap > last_lap) {
            //System.out.println("ScheduledContextDecorator2.checksuspend() --> false");
            return false;
        }

        if (this.repeat == 0) {
            //System.out.println("ScheduledContextDecorator2.checksuspend() --> " + !lap_list.contains(lap));
            return !lap_list.contains(lap);
        }

        //System.out.println("ScheduledContextDecorator2.checksuspend() --> " + !this.lap_list.contains(lap % repeat));
        return !this.lap_list.contains(lap % repeat);
    }
}