package losonczylab.behaviormate.core;

import losonczylab.behaviormate.util.UdpClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ?
 */
public interface ContextList {

    /**
     * Moves the context at the given index in <code>contexts</code>, to the provided location (in mm).
     *
     * @param index The index of the context in <code>contexts</code>
     * @param location The new location of the context, in mm.
     */
    void move(int index, int location) throws JSONException;

    /**
     * Removes all contexts from the implementor.
     */
    void clear() throws JSONException;

    /**
     * Gives each context a new random location on the track.
     */
    void shuffle() throws JSONException;

    /**
     * An array whose ith element contains the location of the ith context of the implementor.
     *
     * @return An array containing context locations.
     */
    int[] toList();

    /**
     * Check the state of the contexts contained in this list and send the
     * start/stop messages as necessary.
     *
     * @return           <code>true</code> to indicate that the trial has started.
     */
    boolean check() throws IOException, JSONException;

    /**
     * ?
     *
     */
    void trialStart();

    /**
     * Resets the state of the contexts.
     */
    void reset() throws JSONException;

    /**
     *
     * @return <code>true</code> if there is currently an active context or <code>false</code>
     * if all contexts are suspended.
     */
    boolean isActive();

    /**
     *
     * @return The index of the currently active context.
     */
    int activeIdx();

    /**
     * Suspend all contexts.
     */
    void suspend() throws JSONException, IOException;

    /**
     * Stop this context.
     *
     */
    void stop() throws JSONException, IOException;

    /**
     * ?
     */
    void shutdown() throws JSONException;

    /**
     * Todo: does this send a message to the arduino?
     *
     * @param message ?
     */
    void sendMessage(String message);

    /**
     *
     * @return the scaled width, in pixels, used to draw the implementor's radius in the UI.
     */
    float displayRadius();

    boolean isGainModified();

    /**
     *
     * @param contexts ?
     */
    void registerContexts(ArrayList<ContextList> contexts) throws JSONException;

    /**
     * ?
     */
    void sendCreateMessages() throws JSONException;

    /**
     * Setter method for the UdpClient of the implementor.
     *
     * @param comms channel to post messages for configuring, starting or stopping contexts.
     * @return <code>true</code> if the messages were successfully sent, <code>false</code> otherwise.
     */
    boolean setupComms(ArrayList<UdpClient> comms) throws JSONException;

    /**
     *
     * @return The number of contexts wrapped by the implementor.
     */
    int size();

    /**
     *
     * @return The <code>UdpClient</code> object belonging to the implementor.
     */
    UdpClient getComm();

    JSONObject getContextInfo();

    /**
     * Returns the id of the implementor.
     *
     * @return the identifier
     */
    String getId();

    float getPositionScale();

    float getPositionScaleMod();

    /**
     *
     * @return An int representing the length, in mm, the context will span in either direction.
     */
    int getRadius();

    ArrayList<Integer> getPins();

    /**
     *
     * @return An array of 3 integers, representing the red, green, and blue pixels (in the order)
     *         used to display the implementor's currently active context.
     */
    int[] getDisplayColor();

    /**
     *
     * @return The string representing the current status of the contexts.
     */
    String getStatus();

    /**
     * Todo: doesn't this return the location of the ith context?
     * Accessor for a specific Context in the list.
     *
     * @param i index of the context to return
     * @return  the context at the supplied index.
     */
    int getLocation(int i);

    /**
     * Todo: I assume the description of getLocation(int) should apply to this method instead.
     * @param i ?
     * @return ?
     */
    Context getContext(int i);

    void setComm(UdpClient comm);

    void setContextInfo(JSONObject context_info);

    /**
     * Sets the scaling used for displaying the implementor's radius in the UI.
     *
     * @param scale the amount to scale the radius so it displays properly in the UI.
     *              Units are in pixel/mm.
     */
    void setDisplayScale(float scale);

    void setDisplayColorSuspended(int[] display_color_suspended);

    void setGainModified(boolean gain_modified);

    void setPositionScale(float position_scale);

    void setPositionScaleMod(float position_scale_mod);

    /**
     * Sets the length, in mm, the context will span in either direction.
     * @param radius ?
     */
    void setRadius(int radius);

    /**
     * Sets the string displayed in the UI describing the state of the contexts of the implementor.
     *
     * @param status The status to display in the UI.
     */
    void setStatus(String status);

}
