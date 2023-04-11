package losonczylab.behaviormate.core;

import losonczylab.behaviormate.BehaviorMate;

/**
 * Class for representing a single event or feature of the environment, such as a hallway, reward,
 * or cue. A context is active by default and some contexts are suspendable. The user must decide
 * on the scheme by which contexts are suspended, for example randomly or every other lap.
 */
public final class Context {

    /**
     * The midpoint, in mm from the start of the track, of the context.
     */
    private int location;

    /**
     * The amount of time, in seconds, the context is active after it has been triggered. Will be
     * set to the <tt>max_duration</tt> property in the settings file.
     */
    private final float duration;

    /**
     * One half the total length, in mm, along the track where this context is active. If the context is
     * placed from 100 mm to 200 mm on the track, its radius is 50 mm.
     */
    private int radius;

    /**
     * The most recent time the context was activated in seconds.
     */
    private float started_time;

    /**
     * <code>True</code> will cause the context to be active for exactly the time specified in the
     * duration attribute. If <code>false</code>, the context will be active for <i>up to</i> the
     * time in the duration attribute if it is in the proper location.
     */
    private final boolean fixed_duration;

    /**
     * ?
     */
    private boolean enabled;

    /**
     * ?
     */
    private final int id;

    /**
     *
     * @param location The location, in mm from the start of the track, of the context.
     * @param duration The amount of time the context is active after it has been triggered.
     * @param radius One half the total length, in mm, along the track where this context is active.
     *               If the context is placed from 100 mm to 200 mm on the track, its radius is 50 mm.
     * @param id ?
     * @param fixed_duration ?
     */
    public Context(int location, float duration, int radius, int id, boolean fixed_duration) {
        //System.out.println("Context(" + location + ", " + duration + ", " + radius + ", " + id + ", " + fixed_duration + ")");

        if (location > BehaviorMate.tc.getTrackLength() || duration > BehaviorMate.tc.getTrialDuration()
            || radius > BehaviorMate.tc.getTrackLength()/2) {
            throw new IllegalArgumentException("Invalid Context location, duration, or radius. Please check settings file.");
        }

        this.location = location;
        this.duration = duration;
        this.radius = radius;
        this.id = id;
        this.fixed_duration = fixed_duration;
        // true when the context has been activated, false while it is suspended
        enabled = true;
        started_time = -1;
    }

    /**
     * Setter method for the radius attribute.
     */
    void setRadius(int radius) {
        if(radius <= 0 && radius != -1) {
            throw new IllegalArgumentException("Context.setRadius: Parameter radius must be greater than 0 or -1.");
        }

        this.radius = radius;
    }

    /**
     * @return <code>true</code> if the context is enabled, false otherwise.
     */
    public boolean isEnabled() {
        //System.out.println("Context.isEnabled() = " + enabled + "[" + id + "]");
        return enabled;
    }

    /**
     * Disables the context.
     */
    public void disable() {
        //System.out.println("Context.disable()" + "[" + id + "]");
        enabled = false;
    }

    /**
     *
     * @return The midpoint of the context in mm.
     */
    public int getLocation() {
        //System.out.println("Context.location()"+ "[" + id + "]");
        return location;
    }

    public int getRadius() {
        return radius;
    }

    /**
     *
     * @return <code>true</code> if the context is active in this location, <code>false</code> otherwise.
     */
    public boolean checkPosition() {
        float position = BehaviorMate.tc.getPosition();
        if (this.radius == -1) {
            //System.out.println("Context.checkPosition(" + position + ") = " + true + "[" + id + "]");
            return true;
        }

//        boolean output = (position > (location - radius)) && (position < (location + radius));
//        System.out.println("Context.checkPosition(" + position + ") = " + output + "[" + id + "]");
//        if (output) {
//            System.out.printf("Context.checkPosition(): %f > %d && %f < %d --> %s {%f}\n"
//                    , position, (location - radius), position, (location + radius), output, BehaviorMate.tc.getTime());
//        }
        return (position > (location - radius)) && (position < (location + radius));
    }

    // assumes position has already been checked
    // every cycle in the event loop the check() method is called on each context of the contextlist
    /**
     * Based on the time should the context be on? This assumes position has already been checked.
     *
     * @return <code>true</code> if the context should be active, <code>false</code> otherwise.
     */
    boolean checkTime() {
        //System.out.print("Context.checkTime(" + time + ") = ");
        // Todo: does a duration of -1 mean it is always active?
        if (duration == -1 || started_time == -1) {
            return true;
        }

        // Checks if the current time is past the end time (start + duration) of the context
        // and disables the context if it is
        if ((started_time + duration) < BehaviorMate.tc.getTime()) {
            disable();
            return false;
        }

        //System.out.println("true" + "[" + id + "]");
        return true;
    }

    /**
     * @return <code>True</code> if the fixed-duration context should be active, <code>false</code>
     * otherwise.
     */
    private boolean check_fixed_duration() {
        float position = BehaviorMate.tc.getPosition();
        float time = BehaviorMate.tc.getTime();

        //System.out.print("Context.check_fixed_duration(" + position + ", " + time + ") = " + "[" + id + "]");
        // Todo: Should this not also check that position < (this.location + this.radius)
        if (this.enabled && (position > (this.location - this.radius))) {
            if (this.started_time == -1) {
                this.started_time = time;
                this.enabled = false;
                //System.out.println("true" + "[" + id + "]");
                return true;
            }
        }

        if (this.started_time != -1) {
            if ((this.started_time + this.duration) > time) {
                //System.out.println("true" + "[" + id + "]");
                return true;
            } else {
                this.started_time = -1;
                //System.out.println("false" + "[" + id + "]");
                return false;
            }
        }

        //System.out.println("false" + "[" + id + "]");
        return false;
    }

    /**
     * Checks that both the positional and time requirements for the context have been met.
     *
     * @return <code>True</code> if the context should be active, <code>false</code> otherwise.
     */
    public boolean check() {
        if (fixed_duration) {
            boolean result = check_fixed_duration();
            //System.out.println("Context.check(" + position + ", " + time + ") = " + result + "[" + id + "]");

            return result;//check_fixed_duration(position, time);
        }

        boolean result = (checkPosition() && checkTime());
        //System.out.println("Context.check(" + position + ", " + time + ") = " + result + "[" + id + "]");
//        if (result) {
//            System.out.println("Context.check(): triggered [" + String.valueOf(location-radius) + ", "
//                    + (location + radius) + "] {" + BehaviorMate.tc.getTime() + "}");
//        }

        return result;//(checkPosition(position) && checkTime(time));
    }

    /**
     * Changes the location at which the context will be activated.
     *
     * @param location The new location of the context in millimeters.
     */
    public void move(int location) {
        //System.out.println("Context.move(" + location + ")" + "[" + id + "]");
        this.location = location;
    }

    /**
     * ?
     */
    public void reset() {
        //System.out.println("Context.reset()" + "[" + id + "]");
        if (!this.fixed_duration) {
            this.started_time = -1;
        }

        this.enabled = true;
    }
}
