package losonczylab.behaviormate.decorators;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.core.ContextListDecorator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Wraps a BasicContextList. Disperses contexts randomly <b>outside</b> the "blocked" region
 * designated in the settings file. For example, if the blocked region of the track is between
 * 0 and 300 mm, contexts will be randomly dispersed between 300 mm and the end of the track.
 * This class supports multiple "blocked segments." For example, 100 to 300 mm and 500 to 550 mm
 * can be blocked in a single trial.
 */
public final class BlockedShuffleContextDecorator extends ContextListDecorator {

    /**
     * Contains the start of each blocked segment.
     */
    private final int[] blocked_locations_starts;

    /**
     * Contains the end of each blocked segment.
     */
    private final int[] blocked_locations_ends;

    /**
     * Contains the width of each blocked segment.
     */
    private final int[] blocked_location_widths;

    /**
     * The sum of the width of all blocked segments.
     */
    private int blocked_size;

    /**
     * @param context_list ContextList instance the decorator will wrap.
     * @param context_info JSONObject containing the configuration information
     *                     for this instance's <code>ContextList</code> from the settings file.
     *                     <code>context_info</code>> should have the property <tt>locations</tt>
     *                     as an array of arrays specifying the locations where the contexts should
     *                     <i>not</i> be present. For example "locations": [[100, 200], [350, 500]],
     *                     will prevent any contexts from being located within the 100 to 200 mm and
     *                     350 to 500 mm regions.
     */
    public BlockedShuffleContextDecorator(ContextList context_list, JSONObject context_info) throws JSONException {
        super(context_list);

        // Todo: not providing locations to this decorator should likely throw an exception
        if (context_info.isNull("locations")) {
            blocked_locations_starts = new int[0];
            blocked_locations_ends = new int[0];
            blocked_location_widths = new int[0];
        } else {
            JSONArray locations_array = context_info.getJSONArray("locations");

            blocked_locations_starts = new int[locations_array.length()];
            blocked_locations_ends = new int[locations_array.length()];
            blocked_location_widths = new int[locations_array.length()];
            for (int i = 0; i < locations_array.length(); i++) {
                JSONArray start_stop = locations_array.getJSONArray(i);
                blocked_locations_starts[i] = start_stop.getInt(0);
                blocked_locations_ends[i] = start_stop.getInt(1);
                blocked_location_widths[i] = start_stop.getInt(1) - start_stop.getInt(0);
            }
        }

        blocked_size = 0;
        for (int i=0; i < blocked_locations_starts.length; i++) {
            blocked_size += blocked_locations_ends[i] - blocked_locations_starts[i];
        }

        System.out.print("BlockedShuffleDecorator constructor: contexts = [");
        for (int i = 0; i < context_list.size(); i++) {
            System.out.printf("%d, ", context_list.getContext(i).getLocation());
        }
        System.out.print("]\n");

        shuffle();
        //System.out.println("BlockedShuffleDecorator constructor: blocked_locations_starts = " + Arrays.toString(blocked_locations_starts));
        //System.out.print("BlockedShuffleDecorator constructor: contexts = [");
        for (int i = 0; i < context_list.size(); i++) {
            //System.out.printf("%d, ", context_list.getContext(i).getLocation());
        }
        //System.out.print("]\n");
    }

    /**
     * Randomize the locations of the contexts within the non-blocked region.
     */
    public void shuffle() {
        // return immediately if there are no contexts to shuffle
        if (context_list.size() == 0) {
            return;
        }

        int radius = getRadius();
        float track_length = BehaviorMate.tc.getTrackLength() - blocked_size;
        int size = size();

        if (size == 1) {
            int new_location = ThreadLocalRandom.current().nextInt(radius, (int)(track_length-radius + 1));

            for (int i = 0; i < blocked_locations_ends.length; i++) {
                if (new_location >= blocked_locations_starts[i]) {
                    new_location += blocked_location_widths[i];
                }
            }

            context_list.move(0, new_location);
            return;
        }

        // initially position contexts evenly spaced
        int interval = (int)(track_length-2*radius)/size;
        move(0, radius + interval/2);
        for (int i = 1; i < size; i++) {
            move(i, getLocation(i-1) + interval);
        }

        // move the contexts randomly without allowing them to overlap
        int first_location = ThreadLocalRandom.current().nextInt(radius, getLocation(1)-2*radius);
        move(0, first_location);

        for (int i = 1; i < size-1; i++) {
            int prev_location = getLocation(i-1);
            int next_location = getLocation(i+1);
            int new_location = ThreadLocalRandom.current().nextInt(prev_location+2*radius, next_location-2*radius);
            move(i, new_location);
        }

        int prev_location = getLocation(size-2);
        int new_location = ThreadLocalRandom.current().nextInt(prev_location+2*radius, (int)track_length-radius);
        move(size-1, new_location);

        for (int j=0; j < blocked_locations_starts.length; j++) {
            int width = blocked_location_widths[j];
            for (int i=0; i < size; i++) {
                int location = getLocation(i);
                if (location >= blocked_locations_starts[j]) {
                    move(i, location + width);
                }
            }
        }
    }

    /**
     * Resets the wrapped <code>BasicContextList</code> and shuffles the contexts.
     */
    public void reset() {
        context_list.reset();
        shuffle();
    }

}
