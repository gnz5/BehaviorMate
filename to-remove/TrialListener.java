//package losonczylab.behaviormate;
//
//import losonczylab.behaviormate.BehaviorMate;
//import org.json.JSONException;
//import org.json.JSONObject;
//import javax.swing.*;
//import java.io.File;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.zip.DataFormatException;
//
///**
// * ?
// */
//class TrialListener {
//    private ControlPanel controlPanel;
//    private CommentsBox commentsBox;
//
//    /**
//     * ?
//     */
//    class ArduinoProcess {
//        private final String arduino_controller_path;
//        private final String settings;
//        private Process p;
//
//        /**
//         * ?
//         *
//         * @param arduino_controller_path ?
//         * @param settings ?
//         * @throws JSONException ?
//         * @throws DataFormatException ?
//         */
//        public ArduinoProcess(String arduino_controller_path, String settings) throws JSONException, DataFormatException {
//            this.settings = createArduinoSettings(settings);
//            this.arduino_controller_path = arduino_controller_path;
//            startProcess();
//        }
//
//        /**
//         * ?
//         */
//        public void destroy() {
//            if (this.p != null) {
//                if (this.p.isAlive()) {
//                    this.p.destroy();
//                }
//                this.p = null;
//            }
//        }
//
//        /**
//         * ?
//         *
//         * @param controller_info ?
//         * @return ?
//         * @throws JSONException ?
//         * @throws DataFormatException ?
//         */
//        private String createArduinoSettings(String controller_info)
//                throws JSONException, DataFormatException {
//            JSONObject controller_json = new JSONObject(controller_info);
//
//            if (controller_json.isNull("serial_port")) {
//                throw new DataFormatException(
//                        "Serial Port needs to be specified in order to start " +
//                                "arduino controller process");
//            } else {
//                System.out.println(
//                        "configuring " + controller_json.get("serial_port"));
//            }
//
//            int send_port = controller_json.getInt("send_port");
//            controller_json.put("send_port",
//                    controller_json.getInt("receive_port"));
//            controller_json.put("receive_port", send_port);
//
//            controller_info = "\"" + controller_json.toString().replace(
//                    "\\", "\\\\").replace("\"", "\\\"") + "\"";
//
//            return controller_info;
//        }
//
//        /**
//         * ?
//         */
//        public void startProcess() {
//            String[] cmd = {arduino_controller_path, "", ""};
//            if (this.settings != null) {
//                cmd[1] = "-settings";
//                cmd[2] = this.settings;
//            } else {
//                exception("unable to parse arduino controller settings");
//            }
//
//            try {
//                this.p = Runtime.getRuntime().exec(cmd);
//            } catch (Exception e) {
//                System.out.println(e);
//                exception(e.toString());
//            }
//        }
//    }
//
//    private HashMap<String, ArduinoProcess> arduino_controllers;
//
//    /**
//     * ?
//     */
//    public TrialListener() {
//        controlPanel = null;
//        commentsBox = null;
//        //Process arduino_controller = null;
//        //Process position_controller = null;
//        //String arduino_controller_path = null;
//        arduino_controllers = null;
//    }
//
//    /**
//     * ?
//     *
//     * @param controlPanel ?
//     */
//    public void setControlPanel(ControlPanel controlPanel) {
//        this.controlPanel = controlPanel;
//    }
//
//    /**
//     * ?
//     *
//     * @param commentsBox ?
//     */
//    public void setCommentsBox(CommentsBox commentsBox) {
//        this.commentsBox = commentsBox;
//    }
//
//    /**
//     *
//     * @param arduino_path ?
//     * @param controllers ?
//     */
//    public void setArduinoController(String arduino_path, JSONObject controllers) {
//        if (arduino_controllers != null) {
//            for (ArduinoProcess process : arduino_controllers.values()) {
//                process.destroy();
//            }
//
//            if (arduino_path == null) {
//                arduino_controllers = null;
//            }
//        }
//
//        if (arduino_path != null) {
//            if (arduino_controllers == null) {
//                arduino_controllers = new HashMap<>();
//            }
//
//            for (Iterator<String> itr = controllers.keys(); itr.hasNext();) {
//                String controller_key = itr.next();
//                try {
//                    if (!controllers.getJSONObject(controller_key).isNull("serial_port")) {
//                        arduino_controllers.put(controller_key,
//                                new ArduinoProcess(arduino_path,
//                                        controllers.get(controller_key).toString()));
//                    }
//                } catch (Exception e) {
//                    System.out.println(e);
//                    exception(controller_key + " Exception: " + e);
//                }
//            }
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param arduino_path ?
//     * @param controllers ?
//     * @throws JSONException ?
//     */
//    public void setArduinoController(String arduino_path, String controllers) throws JSONException {
//        setArduinoController(arduino_path, new JSONObject(controllers));
//    }
//
//    /**
//     * ?
//     *
//     * @param logFile ?
//     */
//    public void started(File logFile) {
//        if (commentsBox != null) {
//            commentsBox.setCurrentFile(logFile);
//        }
//    }
//
//    /**
//     * ?
//     */
//    public void initialized() {
//        controlPanel.refreshSettings();
//    }
//
//    /**
//     * ?
//     */
//    public void ended() {
//        if (controlPanel != null) {
//            controlPanel.setEnabled(true);
//            controlPanel.showAttrsForm();
//        }
//
//        if (commentsBox != null) {
//            commentsBox.addOption("next trial");
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param message ?
//     */
//    public void exception(String message) {
//        BehaviorMate.tc.endExperiment();
//        if (message.length() > 100) {
//            message = new StringBuilder(message).insert(100, "\n").toString();
//        }
//        JOptionPane.showMessageDialog(null, message);
//    }
//
//    /**
//     * ?
//     *
//     * @param message ?
//     */
//    public void alert(String message) {
//        if (message.length() > 100) {
//            message = new StringBuilder(message).insert(100, "\n").toString();
//        }
//        final String _message = message;
//        Thread t = new Thread(new Runnable(){
//            public void run() {
//                JOptionPane.showMessageDialog(null, _message);
//            }
//        });
//        t.start();
//    }
//
//    /**
//     * ?
//     *
//     * @param filepath ?
//     */
//    public void showDeleteDialog(String filepath) {
//        final String _filepath = filepath;
//
//        Thread t = new Thread(new Runnable() {
//            public void run() {
//                Object[] options = {"Delete", "Save"};
//
//                int selectedValue = JOptionPane.showOptionDialog(
//                        null,
//                        "<html>Save File<br>" + _filepath + "?</html>",
//                        "Trial Ended",
//                        JOptionPane.YES_NO_OPTION,
//                        JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);
//                if (selectedValue == 0) {
//                    int option_value = JOptionPane.showConfirmDialog(
//                            null,
//                            "Confirm Delete\n" + _filepath,
//                            "Delete File",
//                            JOptionPane.YES_NO_OPTION);
//                    if (option_value == 0) {
//                        File f = new File(_filepath);
//                        if (!f.delete()) {
//                            JOptionPane.showMessageDialog(null, "Failed to delete file");
//                        }
//                    }
//                }
//            }
//        });
//
//        t.start();
//    }
//
//    /**
//     * ?
//     */
//    public void shutdown() {
//        if (arduino_controllers != null) {
//            for (ArduinoProcess process : arduino_controllers.values()) {
//                process.destroy();
//            }
//        }
//    }
//}