package losonczylab.behaviormate.util;

import org.json.JSONObject;

/**
 * Wrapper class for JSONObject to allow for JSONObjects to be returned by
 * reference during calls to UDPComm
 */
public class JSONBuffer {

    /**
     * The object to be wrapped.
     */
    public JSONObject json;
}
