package losonczylab.behaviormate.util;

import losonczylab.behaviormate.BehaviorMate;
import org.json.JSONArray;
import org.json.JSONObject;

public class UdpHelper {
    /**
     * ?
     *
     * @param messages ?
     * @param mouse_name ?
     * @throws Exception ?
     */
    public static void sendMessages(JSONArray messages, String mouse_name) throws Exception {
        for (int i= 0; i < messages.length(); i++) {
            JSONObject messageInfo = messages.getJSONObject(i);
            UdpClient client = new UdpClient(messageInfo.getString("ip"), messageInfo.getInt("port"));
            JSONObject message = messageInfo.getJSONObject("message");
            message.put("filename", BehaviorMate.tc.fWriter.getFile().getName());
            message.put("mouse", mouse_name);
            client.sendMessage(message.toString());
            client.closeSocket();
        }
    }


}
