//package losonczylab.behaviormate;
//
//import processing.core.PApplet;
//import processing.core.PGraphics;
//import java.util.ArrayList;
//import java.util.Arrays;
//
///**
// * Class for displaying the current behavior of the animal on the track to the screen.
// */
//public class Display extends PApplet {
//    private static final int text_offset_x = 420;
//    private static final int map_offset_x = 150;
//    private static int tag_offset_x = 215;
//    private static int text_offset_y;
//    private static int map_offset_y;
//    private static int bar_offset_y;
//    private static int tag_offset_y;
//    private static int velocity_offset_x;
//    private static int velocity_bar_offset_x;
//    private static int licking_offset_x;
//    private static int licking_bar_offset_x;
//    private static int reward_offset_x;
//    private static int reward_bar_offset_x;
//    private static int lap_offset_x;
//    private static int lap_bar_offset_x;
//    private final ArrayList<ContextList> contextsContainer;
//    private static final int NUM_VALVES = 10;
//    private static final int NUM_SENSORS = 10;
//    private static final int TRACK_HEIGHT = 200; // position of the track, mouse, and contexts on y axis
//    private static final int TRACK_LENGTH = 10; // size of the track in the y direction
//    private static final int TRACK_WIDTH = 300;
//    private final int RED = color(255, 0, 0);
//    private final int GREEN = color(0, 255, 0);
//    private final int BLUE = color(0, 0, 150);
//    private final int WHITE = color(255, 255, 255);
//    private final int BLACK = color(0, 0, 0);
//    private final int GRAY = color(204, 204, 204);
//    private final int YELLOW = color(255, 255, 0);
//    private final int ORANGE = color(255, 100, 0);
//    private float lickRate;
//    private float lapRate;
//    private float lapErrorRate;
//    private float positionRate;
//    private float rewardRate;
//    private int lickCount;
//    private int[] valve_ids;
//    private int[] valve_states;
//    private int[] sensor_ids;
//    private int[] sensor_states;
//    private int rewardCount;
//    //private float lastLap;
//    private int lapCount;
//    private float displayScale;
//    //private String currentTag;
//    private float position_scale;
//    private String mouseName;
//    private String schedule;
//    private String totalTime;
//    private String bottom_message;
//    int valve_height;
//    int valve_text_height;
//    int valve_height_2;
//    int valve_text_height_2;
//    int sensor_height;
//    int sensor_text_height;
//    int sensor_height_2;
//    int sensor_text_height_2;
//
//    /**
//     * Constructs a new <code>Display</code> object with all rate and count variables set to 0.
//     */
//    public Display() {
//        lickRate = 0;
//        lapRate = 0;
//        positionRate = 0;
//        lickCount = 0;
//        lapCount = 0;
//        rewardCount = 0;
//        //lastLap = 0;
//        valve_states = repeatingIntArray(NUM_VALVES, 0);
//        valve_ids = repeatingIntArray(NUM_VALVES, -1);
//        sensor_states = repeatingIntArray(NUM_SENSORS, 0);
//        sensor_ids = repeatingIntArray(NUM_SENSORS, -1);
//        contextsContainer = new ArrayList<>();
//        schedule = "";
//        totalTime = "";
//        //currentTag = "";
//        position_scale = 0;
//        mouseName = "";
//        bottom_message = "";
//    }
//
//    // repeatingIntArray(5, 1) returns an int array with 1 repeated 5 times
//    private static int[] repeatingIntArray(int size, int element) {
//        int[] out = new int[size];
//        Arrays.fill(out, element);
//        return out;
//    }
//
//    /**
//     * Remove all contexts being displayed.
//     */
//    void resetContexts() {
//        //contextsContainer = new ArrayList<ContextList>();
//        contextsContainer.clear(); // likely better practice to call clear() than constructing new ArrayList
//    }
//
//    /**
//     * Set the length of the track in millimeters.
//     *
//     * @throws IllegalArgumentException ?
//     */
//    void setTrackLength() {
//        displayScale = 300f/BehaviorMate.tc.getTrackLength(); // .0 not needed
//    }
//
//    /**
//     * Used to set the String identifier of the mouse.
//     *
//     * @param mouseName ID of the mouse running the track.
//     * @throws IllegalArgumentException ?
//     */
//    void setMouseName(String mouseName) {
//        if (mouseName == null) {
//            throw new IllegalArgumentException("Argument mouseName can't be null.");
//        } else if (mouseName.isBlank()) {
//            System.out.println("Warning: mouseName class attribute being set to empty String.");
//        }
//        this.mouseName = mouseName;
//    }
//
//    /**
//     * Used to increment the number of recorded licks.
//     *
//     * @param count When <code>true</code>, the lick count will be incremented by 1.
//     */
//    void addLick(boolean count) {
//        lickRate = min(200, lickRate+50);
//        if (count) {
//            lickCount++;
//        }
//    }
//
//    /**
//     * Placeholder
//     *
//     * @param dy ?
//     */
//    // Todo: can dy be negative?
//    void setPositionRate(float dy) {
//        if (dy == 0) {
//            positionRate = positionRate/abs(positionRate) * max(0.0f, abs(positionRate)-0.5f);
//        } else {
//            positionRate = min(200,dy*5);
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param schedule ?
//     * @throws IllegalArgumentException ?
//     */
//    // Todo: what is a schedule?
//    void setSchedule(String schedule) {
//        if (schedule == null) {
//            throw new IllegalArgumentException("Argument schedule cannot be null.");
//        }
//        this.schedule = schedule;
//    }
//
//    /**
//     * Set the number laps the mouse has completed.
//     *
//     * @param count The updated lap count. Negative values will raise an
//     *              <code>IllegalArgumentException</code>.
//     * @throws IllegalArgumentException ?
//     */
//    // Todo: can this be 0?
//    void setLapCount(int count) {
//        if (count < 0) {
//            throw new IllegalArgumentException("Argument count must be nonnegative.");
//        }
//        lapCount = count;
//    }
//
//    /**
//     * Set the total number of licks recorded.
//     *
//     * @param count The updated lick count. Negative values will raise an
//     *              <code>IllegalArgumentException</code>.
//     * @throws IllegalArgumentException ?
//     */
//    void setLickCount(int count) {
//        if (count < 0) {
//            throw new IllegalArgumentException("Argument count must be nonnegative.");
//        }
//        lickCount = count;
//    }
//
//    /**
//     * ?
//     *
//     * @param count ?
//     * @throws IllegalArgumentException ?
//     */
//    void setRewardCount(int count) {
//        if (count < 0) {
//            throw new IllegalArgumentException("Argument count must be nonnegative.");
//        }
//        rewardCount = count;
//    }
//
//    /**
//     * ?
//     *
//     * @param time ?
//     * @throws IllegalArgumentException ?
//     */
//    void setTotalTime(int time) {
//        if (time < 0) {
//            throw new IllegalArgumentException("Argument time must be nonnegative.");
//        }
//        // this.totalTime = "/"+time;
//
//        totalTime = "/" + time; // this is probably better practice than implicit conversion
//    }
//
//    /**
//     * ?
//     *
//     * @param message ?
//     * @throws IllegalArgumentException ?
//     */
//    // Todo: can this be blank?
//    void setBottomMessage(String message) {
//        if (mouseName == null) {
//            throw new IllegalArgumentException("Argument mouseName can't be null.");
//        }
//        bottom_message = message;
//    }
//
//    /**
//     * Sets the state of the valve connected to the given pin.
//     *
//     * @param pin ? Todo: what are the possible pins
//     * @param state Valid states are 0, 1, and -1.
//     * @throws IllegalArgumentException ?
//     */
//    void setValveState(int pin, int state) {
//        if (pin < 0 || !(state == 0 || state == 1 || state == -1)) {
//            throw new IllegalArgumentException(
//                    "Arguments pin must be nonnegative and state must either be 0, 1, or -1.");
//        }
//
//        for (int i = 0; i < valve_ids.length; i++) {
//            if (valve_ids[i] == pin) {
//                valve_states[i] = state;
//                break;
//            } else if (valve_ids[i] == -1) {
//                valve_ids[i] = pin;
//                valve_states[i] = state;
//                break;
//            }
//        }
//    }
//
//    /**
//     * Sets the state of the sensor connected to the given pin.
//     *
//     * @param pin ? Todo: what are the possible pins
//     * @param state Valid states are 0, 1, and -1.
//     * @throws IllegalArgumentException ?
//     */
//    void setSensorState(int pin, int state) {
//        if (pin < 0 || !(state == 0 || state == 1 || state == -1)) {
//            throw new IllegalArgumentException(
//                    "Arguments pin must be nonnegative and state must either be 0, 1, or -1.");
//        }
//
//        for (int i = 0; i < sensor_ids.length; i++) {
//            if (sensor_ids[i] == pin) {
//                sensor_states[i] = state;
//                break;
//            } else if (sensor_ids[i] == -1) {
//                sensor_ids[i] = pin;
//                sensor_states[i] = state;
//                break;
//            }
//        }
//    }
//
//    /**
//     * Sets all valve states to 0.
//     */
//    void clearValveStates() {
//        valve_states = repeatingIntArray(NUM_VALVES, 0);
//        valve_ids = repeatingIntArray(NUM_VALVES, -1);
//    }
//
//    /**
//     * Sets all sensor states to 0.
//     */
//    void clearSensorStates() {
//        sensor_states = repeatingIntArray(NUM_SENSORS, 0);
//        sensor_ids = repeatingIntArray(NUM_SENSORS, -1);
//    }
//
//    /**
//     * Increment the total reward count.
//     */
//    void addReward() {
//        rewardRate = min(200, rewardRate+50);
//        rewardCount++;
//    }
//
//    /**
//     * ?
//     *
//     * @param scale ?
//     */
//    void setPositionScale(float scale) {
//        this.position_scale = scale;
//    }
//
//    /**
//     * ?
//     *
//     * @param tag ?
//     * @param position_error ?
//     * @throws IllegalArgumentException ?
//     */
////    void setCurrentTag(String tag, float position_error) {
////        if (tag == null || tag.isBlank()) {
////            throw new IllegalArgumentException("Argument tag cannot be null or blank.");
////        }
////        currentTag = tag;
////        lapRate = 200;
////        lapErrorRate = Math.min(200*Math.abs(position_error)/15, 200);
////    }
//
//    /**
//     * ?
//     *
//     * @param position ?
//     * @throws IllegalArgumentException ?
//     */
////    void setLastLap(float position) {
////        if (position < 0) {
////            throw new IllegalArgumentException("Argument position cannot be negative.");
////        }
////        lastLap = position;
////    }
//
//    /**
//     * ?
//     *
//     * @param contexts ?
//     * @throws IllegalArgumentException ?
//     */
//    public void setContextLocations(ContextList contexts) {
//        if (contexts == null) {
//            throw new IllegalArgumentException("Argument contexts cannot be null.");
//        }
//        contexts.setDisplayScale(this.displayScale);
//        contextsContainer.add(contexts);
//    }
//
//    /**
//     * Draw the UI elements for the first time on the <code>PApplet</code> object.
//     *
//     * @param app The <code>PApplet</code> to draw the UI on.
//     */
//    void prepGraphics(PApplet app) {
//        PGraphics pg = app.createGraphics(app.width, app.height);
//        pg.beginDraw();
//        pg.background(BLACK);
//
//        text_offset_y = 10;
//        map_offset_y = 0;
//        tag_offset_y = app.height-50;
//        bar_offset_y = tag_offset_y-20;
//
//        velocity_offset_x = tag_offset_x-30;
//        velocity_bar_offset_x = velocity_offset_x+20;
//        licking_offset_x = tag_offset_x+33;
//        licking_bar_offset_x = licking_offset_x+15;
//        reward_offset_x = tag_offset_x+90;
//        reward_bar_offset_x = reward_offset_x+18;
//        lap_offset_x = tag_offset_x+150;
//        lap_bar_offset_x = lap_offset_x+15;
//
//        valve_height = app.height-80;
//        valve_text_height = valve_height-5;
//        valve_height_2 = app.height-15;
//        valve_text_height_2 = valve_height_2-5;
//        sensor_height = app.height-80;
//        sensor_text_height = sensor_height-5;
//        sensor_height_2 = app.height-115;
//        sensor_text_height_2 = sensor_height_2-5;
//
//        // Draw the information panel in the top right of screen tracking position, lick count,
//        // and more.
//        pg.textSize(18);
//        pg.text("Position: ", text_offset_x, 20);
//        pg.text("Lick Count: ", text_offset_x, 40);
//        pg.text("Reward Count: ", text_offset_x, 60);
//        pg.text("Position Scale: ", text_offset_x, 80);
//        pg.text("Time: ", text_offset_x, 100);
//        pg.text("Lap Count: ", text_offset_x, 120);
//
//        //pg.fill(RED);
//        //pg.rect(map_offset_x, TRACK_HEIGHT, 300, 10); // ?
//
//        pg.textSize(14);
//        pg.fill(RED);
//        pg.text("velocity", velocity_offset_x, tag_offset_y);
//        pg.fill(GREEN);
//        pg.text("licking", licking_offset_x, tag_offset_y);
//        pg.fill(ORANGE);
//        pg.text("reward", reward_offset_x, tag_offset_y);
//        pg.fill(WHITE);
//        pg.text("lap", lap_offset_x, tag_offset_y);
//        pg.endDraw();
//    }
//
//    /**
//     * Update the UI.
//     *
//     * @param app The <code>PApplet</code> to draw the UI on.
//     */
//    void update(PApplet app) {
//        float time = BehaviorMate.tc.getTime();
//        float position = BehaviorMate.tc.getPosition();
//
//        app.background(BLACK);
//        app.textSize(18);
//        app.text("Position: ", text_offset_x, 20);
//        app.text("Lick Count: ", text_offset_x, 40);
//        app.text("Reward Count: ", text_offset_x, 60);
//        app.text("Position Scale: ", text_offset_x, 80);
//        app.text("Time: ", text_offset_x, 100);
//        app.text("Lap Count: ", text_offset_x, 120);
//
//        app.textSize(14);
//        app.fill(RED);
//        app.text("velocity", velocity_offset_x, tag_offset_y);
//        app.fill(GREEN);
//        app.text("licking", licking_offset_x, tag_offset_y);
//        app.fill(ORANGE);
//        app.text("reward", reward_offset_x, tag_offset_y);
//        app.fill(WHITE);
//        app.text("lap", lap_offset_x, tag_offset_y);
//
//        app.textSize(18);
//        if (lickRate > 0) {
//            lickRate -= 5;
//        }
//        if (lapRate > 0) {
//            lapRate -= 5;
//        }
//        if (lapErrorRate > 5) {
//            lapErrorRate -= 5;
//        } else {
//            lapErrorRate = 0;
//        }
//        if (rewardRate > 0) {
//            rewardRate -= 5;
//        }
//
//        app.fill(WHITE);
//        app.textSize(18);
//        app.text(mouseName, 35, 50);
//        app.text((int) position, 75+text_offset_x, 20);
//        app.text(lickCount, 105+text_offset_x, 40);
//        app.text(rewardCount, 135+text_offset_x, 60);
//        app.text((int) time + this.totalTime, 50+text_offset_x, 100);
//        app.text(lapCount, 100+text_offset_x, 120);
//
//        if (time > 0) {
//            app.fill(RED);
//            app.text("Recording", 35, 30);
//            app.fill(255);
//        }
//
//        app.text(String.format("%.2f", position_scale), text_offset_x+130, 80);
//        app.fill(YELLOW);
//        app.rect(map_offset_x, TRACK_HEIGHT, TRACK_WIDTH, TRACK_LENGTH); // draw the track bar
//        int yoffset = 140;
//
//        for (int i = 0; i < contextsContainer.size(); i++) {
////            System.out.printf(
////                    "Display.update(): contextsContainer = %s, size = %d \n\n", contextsContainer, contextsContainer.size());
//            if (i == 3) {
//                yoffset += 40;
//            }
//            ContextList list = contextsContainer.get(i);
//
//            app.textSize(14);
//            if (list.getDisplayColor() == null) {
//                app.fill(255);
//            } else {
//                int[] c_ = list.getDisplayColor();
//                app.fill(color(c_[0], c_[1], c_[2]));
//                float radius = list.displayRadius();
//                float width = 2*radius;
//                int size = list.size();
//                for (int j=0; j < size; j++) {
////                System.out.printf(
////                        "displayScale = %f, map_offset_x = %d, location = %d, radius = %f \n--------------------------\n",
////                        displayScale, map_offset_x, list.getLocation(j), 2*radius);
////
////                float x = 200;
////                float y = map_offset_x+list.getLocation(j)*displayScale-radius;
////                System.out.printf("rect(%f, %f)\n", x, y);
//                    // draw contexts
//                    app.rect(map_offset_x+list.getLocation(j)*displayScale-radius, TRACK_HEIGHT, width, TRACK_LENGTH);
//                }
//            }
//            app.text(list.getId() + ": "  + list.getStatus(), text_offset_x, yoffset+i*20);
//        }
//
//        app.textSize(14);
//        app.fill(YELLOW);
//        app.text(schedule, 60, 80);
//
//        app.fill(RED);
//        app.rect(map_offset_x+position*displayScale-5, TRACK_HEIGHT, TRACK_LENGTH, TRACK_LENGTH); // draw mouse position
//
//        app.fill(RED);
//        app.rect(velocity_bar_offset_x, bar_offset_y,10,-positionRate); // velocity bar
//
//        app.fill(GREEN);
//        app.rect(licking_bar_offset_x, bar_offset_y, 10, -lickRate); // lick bar
//
//        app.fill(ORANGE);
//        app.rect(reward_bar_offset_x, bar_offset_y, 10, -rewardRate); // reward bar
//
//        app.fill(WHITE);
//        app.rect(lap_bar_offset_x, bar_offset_y, 10, -lapRate);
//
//        app.fill(RED);
//        app.rect(lap_bar_offset_x, bar_offset_y, 10, -lapErrorRate);
//
//        app.fill(WHITE);
//        app.text(bottom_message, 180, 300);
//
//        drawValveStates(app);
//        drawSensorStates(app);
//    }
//
//    /**
//     * Placeholder
//     *
//     * @param app The <code>PApplet</code> to draw the UI on.
//     */
//    private void drawValveStates(PApplet app) {
//        for (int i = 0; i < 5; i++) {
//            if (valve_states[i] == 1) {
//                app.fill(GREEN);
//            } else if(valve_states[i] == -1) {
//                app.fill(RED);
//            } else {
//                break;
//            }
//            app.rect(450 + 30*i, valve_height, 25, 25);
//            app.fill(WHITE);
//            app.text(valve_ids[i], 450 + 30*i + 4, valve_text_height);
//        }
//        for (int i = 0; i < 5; i++) {
//            if (valve_states[i+5] == 1) {
//                app.fill(GREEN);
//            } else if(valve_states[i+5] == -1) {
//                app.fill(RED);
//            } else {
//                break;
//            }
//            app.rect(450 + 30*i, valve_height_2, 25, 25);
//            app.fill(WHITE);
//            app.text(valve_ids[i+5], 450+30*i + 4, valve_text_height_2);
//        }
//        app.fill(WHITE);
//    }
//
//    /**
//     * Placeholder
//     *
//     * @param app The <code>PApplet</code> to draw the UI on.
//     */
//    private void drawSensorStates(PApplet app) {
//        for (int i = 0; i < 5; i++) {
//            if (sensor_states[i] == 1) {
//                app.fill(GREEN);
//            } else if(sensor_states[i] == -1) {
//                app.fill(RED);
//            } else {
//                break;
//            }
//            app.rect(450 + 30*i, sensor_height, 25, 25);
//            app.fill(WHITE);
//            app.text(sensor_ids[i], 450 + 30*i + 4, sensor_text_height);
//        }
//
//        for (int i = 0; i < 5; i++) {
//            if (sensor_states[i+5] == 1) {
//                app.fill(GREEN);
//            } else if(sensor_states[i+5] == -1) {
//                app.fill(RED);
//            } else {
//                break;
//            }
//            app.rect(450 + 30*i, sensor_height_2, 25, 25);
//            app.fill(WHITE);
//            app.text(sensor_ids[i+5], 450+30*i + 4, sensor_text_height_2);
//        }
//        app.fill(WHITE);
//    }
//}
