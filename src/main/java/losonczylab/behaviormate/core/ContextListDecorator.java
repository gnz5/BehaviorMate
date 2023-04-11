package losonczylab.behaviormate.core;

import losonczylab.behaviormate.util.UdpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class for wrapping an instance of a class that implements the <code>ContextList</code> interface
 * and providing additional functionality. All methods simply call the corresponding method of the
 * wrapped <code>ContextList</code>. Subclasses of <code>ContextListDecorator</code> will override
 * some or all of these rather than using the wrapped class' implementation.
 */
public class ContextListDecorator implements ContextList {

    private boolean isSuspendable = false;
    protected BasicContextList context_list;

    public ContextListDecorator(ContextList context_list) {
        //System.out.println("new ContextListDecorator(" + context_list + ")");
        this.context_list = (BasicContextList) context_list;
    }

    protected void setSuspendable(boolean isSuspendable) {
        this.isSuspendable = isSuspendable;
    }

    @Override
    public UdpClient getComm() {
        return context_list.getComm();
    }

    @Override
    public ArrayList<Integer> getPins() {
        return context_list.getPins();
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

    @Override
    public void sendCreateMessages() throws JSONException {
        context_list.sendCreateMessages();
    }

    @Override
    public boolean setupComms(ArrayList<UdpClient> comms) throws JSONException {
        return context_list.setupComms(comms);
    }

    @Override
    public void registerContexts(ArrayList<ContextList> contexts) throws JSONException {
        context_list.registerContexts(contexts);
    }

    @Override
    public String getId() {
        return context_list.getId();
    }

    @Override
    public void setRadius(int radius) {
        context_list.setRadius(radius);
    }

    @Override
    public int getRadius() {
        return context_list.getRadius();
    }

    @Override
    public void setDisplayScale(float scale) {
        context_list.setDisplayScale(scale);
    }

    @Override
    public float displayRadius() {
        return context_list.displayRadius();
    }

    /**
     * @return An array of 3 integers, representing the red, green, and blue pixels (in the order)
     * used to display the implementor's currently active context.
     */
    @Override
    public int[] getDisplayColor() {
        if (!check_suspend()) {
            //System.out.println("ContextListDecorator.getDisplayColor() --> " + Arrays.toString(context_list.display_color));
            return context_list.display_color;
        } else {
            //System.out.println("ContextListDecorator.getDisplayColor() --> " + Arrays.toString(context_list.display_color_suspended));
            return context_list.display_color_suspended;
        }
        //return context_list.getDisplayColor();
    }

    @Override
    public void setStatus(String status) {
        context_list.setStatus(status);
    }

    @Override
    public String getStatus() {
        return context_list.getStatus();
    }

    @Override
    public int size() {
        return context_list.size();
    }

    @Override
    public int getLocation(int i) {
        return context_list.getLocation(i);
    }

    @Override
    public Context getContext(int i) {
        return context_list.getContext(i);
    }

    @Override
    public void move(int index, int location) {
        context_list.move(index, location);
    }

    @Override
    public void clear() {
        context_list.clear();
    }

    @Override
    public void shuffle() {
        context_list.shuffle();
    }

    @Override
    public int[] toList() {
        return context_list.toList();
    }

    @Override
    public void trialStart() {
        context_list.trialStart();
    }

    @Override
    public void reset() {
        context_list.reset();
    }

//    @Override
//    public void end() throws JSONException {
//        context_list.end();
//    }

    @Override
    public boolean isActive() {
        return context_list.isActive();
    }

    @Override
    public int activeIdx() {
        return context_list.activeIdx();
    }

    @Override
    public void suspend() {
        context_list.suspend();
    }

    @Override
    public void stop() {
        context_list.stop();
    }

    @Override
    public void shutdown() {
        context_list.shutdown();
    }

    @Override
    public void sendMessage(String message) {
        context_list.sendMessage(message);
    }

    @Override
    public boolean check() {
        //System.out.println("ContextDecorator2.check(" + position + ", " + time + ", " + lap + ", 3 more args..)");
        if (isSuspendable) {
            if (check_suspend()) {
                // Is it currently suspended? If not suspend it
                if (context_list.isActive()) {
                    context_list.suspend();
                }
            } else {
                context_list.unsuspend();
            }

            if (context_list.isSuspended()) {
                return false;
            }

            // If the context list is not suspended call the check method for the default ContextList behavior.
            return context_list.check();
        } else {
            return false;
        }
    }

    public boolean check_suspend() {
        // Decorators that want to be suspendable must override this method.
        //System.out.println("ContextListDecorator.check_suspend() --> false");
        return false;
    }
}
