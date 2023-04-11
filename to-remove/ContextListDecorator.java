package losonczylab.behaviormate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Class for wrapping an instance of a class that implements the <code>ContextList</code> interface
 * and providing additional functionality. All methods simply call the corresponding method of the
 * wrapped <code>ContextList</code>. Subclasses of <code>ContextListDecorator</code> will override
 * some or all of these rather than using the wrapped class' implementation.
 */
public class ContextListDecorator implements ContextList {

    protected ContextList context_list;

    public ContextListDecorator(ContextList context_list) {
        //System.out.println("new ContextListDecorator(" + context_list + ")");
        this.context_list = context_list;
    }

    @Override
    public UdpClient getComm() {
        return context_list.getComm();
    }

    @Override
    public void setComm(UdpClient comm) {
        context_list.setComm(comm);
    }

    @Override
    public float getPositionScale() {
        return context_list.getPositionScale();
    }

    @Override
    public void setPositionScale(float position_scale) {
        context_list.setPositionScale(position_scale);
    }

    @Override
    public float getPositionScaleMod() {
        return context_list.getPositionScaleMod();
    }

    @Override
    public void setPositionScaleMod(float position_scale_mod) {
        context_list.setPositionScaleMod(position_scale_mod);
    }

    @Override
    public boolean isGainModified() {
        return context_list.isGainModified();
    }

    @Override
    public void setGainModified(boolean gain_modified) {
        context_list.setGainModified(gain_modified);
    }

    @Override
    public void setDisplayColorSuspended(int[] display_color_suspended) {
        context_list.setDisplayColorSuspended(display_color_suspended);
    }

    @Override
    public JSONObject getContextInfo() {
        return context_list.getContextInfo();
    }

    @Override
    public void setContextInfo(JSONObject context_info) {
        context_list.setContextInfo(context_info);
    }

    public void sendCreateMessages() throws JSONException {
        this.context_list.sendCreateMessages();
    }

    public boolean setupComms(ArrayList<UdpClient> comms) throws JSONException {
        return this.context_list.setupComms(comms);
    }

    public void registerContexts(ArrayList<ContextList> contexts) throws JSONException {
        this.context_list.registerContexts(contexts);
    }

    public String getId() {
        return this.context_list.getId();
    }

    public void setRadius(int radius) {
        this.context_list.setRadius(radius);
    }

    public int getRadius() {
        return this.context_list.getRadius();
    }

    public void setDisplayScale(float scale) {
        this.context_list.setDisplayScale(scale);
    }

    public float displayRadius() {
        return this.context_list.displayRadius();
    }

    /**
     * @return An array of 3 integers, representing the red, green, and blue pixels (in the order)
     * used to display the implementor's currently active context.
     */
    public int[] getDisplayColor() throws JSONException {
        return context_list.getDisplayColor();
    }

    public void setStatus(String status) {
        this.context_list.setStatus(status);
    }

    public String getStatus() {
        return this.context_list.getStatus();
    }

    public int size() {
        return this.context_list.size();
    }

    public int getLocation(int i) {
        return this.context_list.getLocation(i);
    }

    public Context getContext(int i) {
        return this.context_list.getContext(i);
    }

    public void move(int index, int location) throws JSONException {
        this.context_list.move(index, location);
    }


    public void clear() throws JSONException {
        this.context_list.clear();
    }

    public void shuffle() throws JSONException {
        this.context_list.shuffle();
    }

    public int[] toList() {
        return this.context_list.toList();
    }

    public boolean check() throws JSONException {
        boolean result = context_list.check();
        System.out.println(
                "ContextListDecorator.check() --> " + result);
        return result;
        //return this.context_list.check(position, time, lap, lick_count, sensor_counts, msg_buffer);
    }

    public void trialStart() {
        this.context_list.trialStart();
    }

    public void reset() throws JSONException {
        this.context_list.reset();
    }

    public void end() throws JSONException {
        this.context_list.end();
    }

    public boolean isActive() {
        return this.context_list.isActive();
    }

    public int activeIdx() {
        return this.context_list.activeIdx();
    }

    public void suspend() throws JSONException {
        this.context_list.suspend();
    }

    public void stop() throws JSONException {
        this.context_list.stop();
    }

    public void shutdown() throws JSONException {
        this.context_list.shutdown();
    }

    public void sendMessage(String message) {
        this.context_list.sendMessage(message);
    }

    public ContextList getContextListBase() {
        return this.context_list;
    }
}
