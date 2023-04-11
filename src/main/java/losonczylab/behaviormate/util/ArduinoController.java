package losonczylab.behaviormate.util;

import losonczylab.behaviormate.BehaviorMate;
import losonczylab.behaviormate.core.ContextList;
import org.json.JSONException;
import org.json.JSONObject;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;

public class ArduinoController {
    /**
     * ?
     */
    class ArduinoProcess {
        private final String arduino_controller_path;
        private final String settings;
        private Process p;

        /**
         * ?
         *
         * @param arduino_controller_path ?
         * @param settings ?
         * @throws JSONException ?
         * @throws DataFormatException ?
         */
        public ArduinoProcess(String arduino_controller_path, String settings) throws JSONException, DataFormatException, IOException {
            this.settings = createArduinoSettings(settings);
            this.arduino_controller_path = arduino_controller_path;
            startProcess();
        }

        /**
         * ?
         */
        public void destroy() {
            if (this.p != null) {
                if (this.p.isAlive()) {
                    this.p.destroy();
                }
                this.p = null;
            }
        }

        /**
         * ?
         *
         * @param controller_info ?
         * @return ?
         * @throws JSONException ?
         * @throws DataFormatException ?
         */
        private String createArduinoSettings(String controller_info) throws JSONException, DataFormatException {
            JSONObject controller_json = new JSONObject(controller_info);

            if (controller_json.isNull("serial_port")) {
                throw new DataFormatException("Serial Port needs to be specified in order to start arduino controller process");
            } else {
                System.out.println("configuring " + controller_json.get("serial_port"));
            }

            int send_port = controller_json.getInt("send_port");
            controller_json.put("send_port", controller_json.getInt("receive_port"));
            controller_json.put("receive_port", send_port);
            controller_info = "\"" + controller_json.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";

            return controller_info;
        }

        /**
         * ?
         */
        public void startProcess() throws JSONException, IOException {
            String[] cmd = {arduino_controller_path, "", ""};
            if (this.settings != null) {
                cmd[1] = "-settings";
                cmd[2] = this.settings;
            } else {
                exception("unable to parse arduino controller settings");
            }

            try {
                this.p = Runtime.getRuntime().exec(cmd);
            } catch (Exception e) {
                System.out.println(e);
                exception(e.toString());
            }
        }
    }

    private HashMap<String, ArduinoProcess> arduino_controllers;

    public ArduinoController() {
        arduino_controllers = null;
    }

    /**
     *
     * @param arduino_path ?
     * @param controllers ?
     */
    public void setArduinoController(String arduino_path, JSONObject controllers) throws JSONException, IOException {
        if (arduino_controllers != null) {
            for (ArduinoProcess process : arduino_controllers.values()) {
                process.destroy();
            }

            if (arduino_path == null) {
                arduino_controllers = null;
            }
        }

        if (arduino_path != null) {
            if (arduino_controllers == null) {
                arduino_controllers = new HashMap<>();
            }

            for (Iterator<String> itr = controllers.keys(); itr.hasNext();) {
                String controller_key = itr.next();
                try {
                    if (!controllers.getJSONObject(controller_key).isNull("serial_port")) {
                        arduino_controllers.put(
                                controller_key, new ArduinoProcess(arduino_path, controllers.get(controller_key).toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    exception(controller_key + " Exception: " + e);
                }
            }
        }
    }

    /**
     * ?
     *
     * @param arduino_path ?
     * @param controllers ?
     * @throws JSONException ?
     */
    public void setArduinoController(String arduino_path, String controllers) throws JSONException, IOException {
        setArduinoController(arduino_path, new JSONObject(controllers));
    }

    /**
     * ?
     *
     * @param hardware_reset ?
     */
    public void resetArduino(boolean hardware_reset) throws JSONException {
        JSONObject resetArduino = new JSONObject();
        resetArduino.put("communicator", new JSONObject());
        resetArduino.getJSONObject("communicator").put("action", "reset");
        for (ContextList context : BehaviorMate.tc.contextLists) {
            UdpClient comm = context.getComm();
            if ((comm != null) && (comm.getId().equals("behavior_controller"))) {
                context.setStatus("resetting");
            }
        }

        if ((BehaviorMate.tc.reset_comm != null) && (hardware_reset)) {
            BehaviorMate.tc.reset_comm.sendMessage(resetArduino.toString());
        } else {
            BehaviorMate.tc.behavior_comm.sendMessage(resetArduino.toString());
        }
    }

    /**
     * ?
     */
    public void resetArduino() throws JSONException {
        resetArduino(false);
    }

    /**
     * ?
     *
     * @param message ?
     */
    public void exception(String message) throws JSONException, IOException {
        BehaviorMate.tc.endExperiment();
        if (message.length() > 100) {
            message = new StringBuilder(message).insert(100, "\n").toString();
        }
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * ?
     */
    public void shutdown() {
        if (arduino_controllers != null) {
            for (ArduinoProcess process : arduino_controllers.values()) {
                process.destroy();
            }
        }
    }
}
