//package losonczylab.behaviormate;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.Iterator;
//
///**
// * Class for containing the entire right side (except the Comments section) of the
// * BehaviorMate application, from the "Project Name" label to the "Start" button.
// */
//class ControlPanel extends JPanel implements ActionListener {
//    private final LabeledTextField mouseNameBox;
//    private final LabeledTextField experimentGroupBox;
//    private final ValveTestForm valveTestForm;
//    private final CalibrateBeltForm calibrateBeltForm;
//    private final JButton showAttrsButton;
//    private final JButton refreshButton;
//    private final JButton restartCommButton;
//    private final JButton startButton;
//    private final TrialAttrsForm trialAttrsForm;
//    private final JFrame parent;
//    private boolean attrsCompleted;
//
//    /**
//     * ?
//     *
//     * @param parent ?
//     */
//    public ControlPanel(JFrame parent) {
//        BehaviorMate.settingsLoader.addActionListener(this);
//        this.parent = parent;
//
//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        experimentGroupBox = new LabeledTextField("              Project Name", 14);
//        add(experimentGroupBox);
//        mouseNameBox = new LabeledTextField("              Mouse Name", 14);
//        add(mouseNameBox);
//
//        add(Box.createVerticalStrut(20));
//
//        calibrateBeltForm = new CalibrateBeltForm();
//        calibrateBeltForm.setPreferredSize(new Dimension(200, 100));
//        add(calibrateBeltForm);
//        add(Box.createVerticalStrut(25));
//
//        valveTestForm = new ValveTestForm();
//        valveTestForm.setPreferredSize(new Dimension(200, 250));
//        add(valveTestForm);
//        //add(Box.createVerticalStrut(25));
//
//        // BUTTON PANEL START ************
//        JPanel buttonPanel = new JPanel(new GridLayout(0,1));
//
//        showAttrsButton= new JButton("Edit Trial Attributes");
//        showAttrsButton.addActionListener(this);
//
//        buttonPanel.add(showAttrsButton);
//        refreshButton = new JButton("Re-Load Settings");
//        refreshButton.addActionListener(this);
//
//        buttonPanel.add(refreshButton);
//        restartCommButton = new JButton("Re-Start Comms");
//        restartCommButton.addActionListener(this);
//
//        buttonPanel.add(restartCommButton);
//        startButton = new JButton("Start");
//        startButton.addActionListener(this);
//        buttonPanel.add(startButton);
//
//        add(buttonPanel);
//        // BUTTON PANEL STOP ************
//
//        trialAttrsForm = new TrialAttrsForm(this);
//        trialAttrsForm.addActionListener(this);
//
//        showAttrsForm();
//    }
//
//    /**
//     * ?
//     *
//     * @param pin_number ?
//     */
//    public void setTestValve(int pin_number) {
//        valveTestForm.setPin(pin_number);
//    }
//
//    /**
//     * ?
//     */
//    public void showAttrsForm() {
//        attrsCompleted = !trialAttrsForm.showForm();
//    }
//
//    private void updateAttrs() {
//        String trialAttrs = "";
//        try {
//            trialAttrs = trialAttrsForm.getValues();
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(this, "Unable to parse Trial Attributes\n" + e.getMessage());
//            showAttrsForm();
//            return;
//        }
//
//        try {
//            BehaviorMate.tc.addSettings(trialAttrs);
//        } catch (Exception exc) {
//            exc.printStackTrace();
//
//            StringBuilder msg = new StringBuilder(exc.toString());
//            StackTraceElement[] elements = exc.getStackTrace();
//            for (int i = 0; (i < 3 && i < elements.length); i++) {
//                msg.append("\n ").append(elements[i].toString());
//            }
//            JOptionPane.showMessageDialog(null, msg.toString());
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param enabled ?
//     */
//    public void setEnabled(boolean enabled) {
//        if (!enabled) {
//            mouseNameBox.setEnabled(false);
//            experimentGroupBox.setEnabled(false);
//            valveTestForm.setEnabled(false);
//            refreshButton.setEnabled(false);
//            showAttrsButton.setEnabled(false);
//            calibrateBeltForm.setEnabled(false);
//            startButton.setText("Stop");
//        } else {
//            startButton.setText("Start");
//            mouseNameBox.setEnabled(true);
//            experimentGroupBox.setEnabled(true);
//            valveTestForm.setEnabled(true);
//            showAttrsButton.setEnabled(true);
//            refreshButton.setEnabled(true);
//            calibrateBeltForm.setEnabled(true);
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param filename ?
//     * @param tag ?
//     * @return ?
//     * @throws JSONException ?
//     */
//    public static JSONObject findSettings(String filename, String tag) throws JSONException {
//        JSONObject settings = BehaviorMate.parseJsonFile(filename, tag);
//        if (settings.isNull("uses")) {
//            return settings;
//        }
//
//        JSONArray settings_names = null;
//        try {
//            settings_names = settings.getJSONArray("uses");
//        } catch (JSONException ignored) { }
//
//        if (settings_names != null) {
//            for (int i = 0; i < settings_names.length(); i++) {
//                JSONObject settings_update;
//                try {
//                    JSONObject settings_info = settings_names.getJSONObject(i);
//                    settings_update = findSettings(
//                            settings_info.getString("file"), settings_info.getString("tag"));
//                } catch (JSONException e) {
//                    settings_update = findSettings(filename, settings_names.getString(i));
//                }
//
//                Iterator<String> key_itr = settings.keys();
//                while (key_itr.hasNext()) {
//                    String key = key_itr.next();
//                    settings_update.put(key, settings.get(key));
//                }
//                settings = settings_update;
//            }
//        } else {
//            JSONObject settings_update;
//            try {
//                JSONObject settings_info = settings.getJSONObject("uses");
//                settings_update = findSettings(
//                        settings_info.getString("file"), settings_info.getString("tag"));
//            } catch (JSONException e) {
//                settings_update = findSettings(filename, settings.getString("uses"));
//            }
//
//            Iterator<String> key_itr = settings.keys();
//            while (key_itr.hasNext()) {
//                String key = key_itr.next();
//                settings_update.put(key, settings.get(key));
//            }
//            settings = settings_update;
//        }
//
//        return settings;
//    }
//
//    /**
//     * ?
//     */
//    public void refreshSettings() {
//        String title = String.format("%s %s %s",
//                BehaviorMate.APP_NAME, BehaviorMate.VERSION, BehaviorMate.settingsLoader.getSelectedTag());
//        parent.setTitle(title);
//        //parent.setResizable(false);
//        String filename = BehaviorMate.settingsLoader.getSelectedFile();
//        String tag = BehaviorMate.settingsLoader.getSelectedTag();
//
//        JSONObject settings;
//        try {
//            settings = findSettings(filename, tag);
//            JSONObject system_settings = BehaviorMate.parseJsonFile(filename, "_system");
//            if (system_settings == null) {
//                system_settings = BehaviorMate.parseJsonFile(SettingsLoader.SETTINGS_FILE, "_system");
//            }
//
//            BehaviorMate.tc.RefreshSettings(settings.toString(), system_settings.toString());
//        } catch (Exception exc) {
//            exc.printStackTrace();
//
//            StringBuilder msg = new StringBuilder(exc.toString());
//            StackTraceElement[] elements = exc.getStackTrace();
//            for (int i = 0; ( (i < 3) && (i < elements.length) ); i++) {
//                msg.append("\n ").append(elements[i].toString());
//            }
//            JOptionPane.showMessageDialog(null, msg.toString());
//            return;
//        }
//
//        setTestValve(BehaviorMate.tc.getRewardPin());
//        trialAttrsForm.loadForm(settings);
//        showAttrsForm();
//    }
//
//    /**
//     * ?
//     */
//    public void startTrial() {
//        this.calibrateBeltForm.endCalibration();
//
//        if (startButton.getText().equals("Start")) {
//            if (!attrsCompleted) {
//                JOptionPane.showMessageDialog(this, "Complete Trial Attributes Form");
//                return;
//            }
//
//            if (mouseNameBox.getText().equals("")) {
//                JOptionPane.showMessageDialog(this, "Mouse Name is Blank");
//                return;
//            }
//
//            if (experimentGroupBox.getText().equals("")) {
//                JOptionPane.showMessageDialog(this, "Project Name is Blank");
//                return;
//            }
//
//            if (BehaviorMate.tc.Start(mouseNameBox.getText(), experimentGroupBox.getText())) {
//                setEnabled(false);
//            } else {
//                JOptionPane.showMessageDialog(
//                        this, "Unable to Start ... Scan lap reset? or check behavior save directory");
//            }
//
//            BehaviorMate.tc.writeSettingsInfo(
//                    BehaviorMate.settingsLoader.getSelectedFile(), BehaviorMate.settingsLoader.getSelectedTag());
//        } else {
//            BehaviorMate.tc.endExperiment();
//            setEnabled(true);
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param e ?
//     */
//    public void actionPerformed(ActionEvent e) {
//        if (e.getSource() == startButton) {
//            startTrial();
//        } else if (e.getSource() == showAttrsButton) {
//            showAttrsForm();
//        } else if (e.getSource() == refreshButton) {
//            BehaviorMate.settingsLoader.setLocationRelativeTo(this);
//            BehaviorMate.settingsLoader.setVisible(true);
//        } else if (e.getSource() == BehaviorMate.settingsLoader) {
//            refreshSettings();
//        } else if (e.getSource() == trialAttrsForm) {
//            attrsCompleted = true;
//            updateAttrs();
//        } else if (e.getSource() == restartCommButton) {
//            BehaviorMate.tc.resetComms();
//        }
//    }
//}