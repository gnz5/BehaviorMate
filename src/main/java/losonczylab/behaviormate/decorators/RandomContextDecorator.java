package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONObject;
import java.util.concurrent.ThreadLocalRandom;

/**
 * With this decorator, the ContextList will start out suspended. The rat will then have to randomly run
 * between <tt>min_lap</tt> and <tt>limit_lap</tt> laps before the ContextList is unsuspended for a single lap.
 * This process then repeats. If <tt>min_lap</tt> and <tt>limit_lap</tt> are not specified in the settings
 * file, they will default to 1 and 2 respectively, which results in the ContextList never being suspended,
 * except for the starting lap (lap 0).
 */
public final class RandomContextDecorator extends ContextListDecorator {

    /**
     * The lap number, at which the wrapped ContextList will be unsuspended.
     */
    private int n_lap;

    /**
     * Minimum number of laps to complete before the wrapped ContextList is unsuspended.
     */
    private final int min_lap;

    /**
     * Maximum number of laps to complete before the wrapped ContextList is unsuspended.
     */
    private final int limit_lap;

    /**
     * ?
     *
     * @param context_list <code>ContextList</code> instance the decorator will wrap.
     * @param context_info JSONObject containing the configuration information for this context
     *                     from the settings file. The <tt>seed</tt> property is optional.
     */
    public RandomContextDecorator(ContextList context_list, JSONObject context_info) {
        super(context_list);
        //System.out.println("new RandomContextDecorator2(" + context_list + ", " + context_info + ")");

        min_lap = context_info.optInt("min_lap", 1);
        limit_lap = context_info.optInt("limit_lap", 2);
        if (limit_lap < min_lap) {
            throw new IllegalArgumentException("Parameter limit_lap cannot be smaller than min_lap");
        }
        n_lap = min_lap + ThreadLocalRandom.current().nextInt(limit_lap-min_lap); //this.random.nextInt(this.limit_lap-this.min_lap);
        //System.out.println(context_info);
        System.out.printf("RandomContextDecorator min_lap = %d, limit_lap = %d, n_lap = %d%n", min_lap, limit_lap, n_lap);
        setSuspendable(true);
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

        // check if the lap count means that the context list should be suspended or unsuspended.
        if (lap > n_lap) {
            n_lap += (min_lap + ThreadLocalRandom.current().nextInt(limit_lap-min_lap));// this.random.nextInt(this.limit_lap - this.min_lap));
        }
        //System.out.println("RandomContextDecorator : lap = " + lap + ", n_lap = " + n_lap);
        //System.out.println("RandomContextDecorator.check_suspend(" + lap + ") --> " + (lap != n_lap));
        return (lap != n_lap);
    }

    public String toString() {
        return "RandomContextDecorator";
    }
}
