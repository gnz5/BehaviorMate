//package losonczylab.behaviormate;
//
//import java.awt.Component;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JButton;
//import javax.swing.JLabel;
//import javax.swing.JTextField;
//import javax.swing.JComboBox;
//import javax.swing.JDialog;
//import javax.swing.JFileChooser;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import java.awt.event.ActionListener;
//import java.awt.event.ActionEvent;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import javax.swing.BoxLayout;
//import javax.swing.Box;
//import javax.swing.UIManager;
//import javax.swing.JOptionPane;
//import javax.swing.border.Border;
//import javax.swing.BorderFactory;
//import java.awt.FlowLayout;
//
//import java.awt.Dimension;
//import java.io.File;
//import java.util.Iterator;
//import java.util.ArrayList;
//
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import org.json.JSONObject;
//import org.json.JSONArray;
//import org.json.JSONException;
//import java.lang.NumberFormatException;
//
///**
// * ?
// */
//public class TrialAttrsForm extends JDialog implements ActionListener {
//    //private JButton fileChooserButton;
//    //private JComboBox comboBox;
//    //private JTextField pathTextField;
//    private JButton okayButton;
//    //private JButton cancelButton;
//    private String selectedFile;
//    private String selectedTag;
//    private ActionListener actionListener;
//    /**
//     * ?
//     */
//    private JSONObjectInput fieldsInput;
//    private JPanel input_container;
//
//    //private ArrayList<JLabel> inputBoxes;
//
//    private interface JSONInputField {
//        public Object getValue();
//    }
//
//    /**
//     * ?
//     */
//    private class LabeledInput extends JPanel implements JSONInputField {
//        private JLabel label;
//        private JTextField textField;
//
//        public LabeledInput(String labelText) {
//            label = new JLabel(labelText);
//            textField = new JTextField("", 15);
//            label.setPreferredSize(new Dimension(150,label.getPreferredSize().height));
//            add(label);
//            add(textField);
//        }
//
//        public String getValue() {
//            return textField.getText();
//        }
//    }
//
//    /**
//     * ?
//     */
//    class JSONArrayInput extends JSONObjectInput implements JSONInputField {
//        private JButton addFieldButton;
//        private ActionListener al;
//
//        private class JSONArrayInputActionListener implements ActionListener {
//            private JSONArrayInput arrayInput;
//
//            public JSONArrayInputActionListener(JSONArrayInput arrayInput) {
//                this.arrayInput = arrayInput;
//            }
//
//            public void actionPerformed(ActionEvent e) {
//                arrayInput.addArrayField();
//            }
//        }
//
//        public JSONArrayInput(JSONArray json) throws JSONException {
//            super(json);
//            addFieldButton = null;
//            this.al = new JSONArrayInputActionListener(this);
//        }
//
//        public void setAddFieldButton(JButton button) {
//            if (addFieldButton != null) {
//                addFieldButton.removeActionListener(al);
//            }
//            button.addActionListener(al);
//            this.addFieldButton = button;
//        }
//
//        public void addArrayField() {
//            try {
//                addInput(fields.getJSONObject(0));
//            } catch (JSONException e) { }
//
//            revalidate();
//        }
//
//        /**
//         * ?
//         *
//         * @return ?
//         */
//        public JSONArray getValue() {
//            JSONArray result = new JSONArray();
//            JSONObject field;
//            try {
//                field = fields.getJSONObject(0);
//            } catch (JSONException e) {
//                return fields;
//            }
//
//            for (JSONInputField inputBox : inputBoxes) {
//                try {
//                    String type = field.getString("type");
//                    if (type.equals("JSONObject") ||
//                            type.equals("JSONArray")) {
//                        try {
//                            result.put((JSONArray) inputBox.getValue());
//                        } catch (NumberFormatException e) {
//                        }
//                    } else {
//                        String value = (String) inputBox.getValue();
//                        if (value.equals("")) {
//                            continue;
//                        }
//
//                        switch (type) {
//                            case "String":
//                                result.put(value);
//                                break;
//                            case "int":
//                                try {
//                                    result.put(Integer.parseInt(value));
//                                } catch (NumberFormatException e) {}
//                                break;
//                            case "float":
//                                try {
//                                    result.put(Double.parseDouble(value));
//                                } catch (NumberFormatException e) {}
//                                break;
//                        }
//                    }
//                } catch (JSONException f) {
//                    f.printStackTrace();
//                }
//            }
//
//            return result;
//        }
//
//    }
//
//    /**
//     * ?
//     */
//    class JSONObjectInput extends JPanel implements JSONInputField {
//        protected ArrayList<JSONInputField> inputBoxes;
//        protected JSONArray fields;
//
//        public JSONObjectInput(JSONArray json) throws JSONException {
//            inputBoxes = new ArrayList<JSONInputField>();
//            fields = json;
//
//            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//            Border blackline = BorderFactory.createLineBorder(Color.black);
//            setBorder(blackline);
//
//            for (int i = 0; i < json.length(); i++) {
//                JSONObject field = json.getJSONObject(i);
//                addInput(field);
//            }
//        }
//
//        /**
//         * ?
//         *
//         * @param field ?
//         */
//        protected void addInput(JSONObject field) {
//            String key;
//            try {
//                key = field.getString("key");
//            } catch (JSONException e) {
//                return;
//            }
//
//            String label;
//            try {
//                label = field.getString("label");
//            } catch (JSONException e) {
//                label = key;
//            }
//
//            try {
//                if (field.getString("type").equals("JSONObject")) {
//                    add(new JLabel("<html><b>" + label + "</b></html>"));
//                    JSONObjectInput jsonInput = new JSONObjectInput(field.getJSONArray("fields"));
//                    add(jsonInput);
//                    inputBoxes.add(jsonInput);
//                    return;
//                }  else if (field.getString("type").equals("JSONArray")) {
//                    JPanel labelPanel = new JPanel();
//                    labelPanel.add(new JLabel("<html><b>" + label + "</b></html>"));
//                    JButton addField = new JButton("+");
//                    labelPanel.add(addField);
//                    add(labelPanel);
//                    JSONArrayInput jsonInput =
//                        new JSONArrayInput(field.getJSONArray("fields"));
//                    jsonInput.setAddFieldButton(addField);
//                    add(jsonInput);
//                    inputBoxes.add(jsonInput);
//                    return;
//                }
//            } catch (JSONException e) { }
//
//            LabeledInput input = new LabeledInput(label);
//            add(input);
//            inputBoxes.add(input);
//        }
//
//        public int numFields() {
//            return inputBoxes.size();
//        }
//
//        /**
//         * ?
//         *
//         * @return ?
//         */
//        public JSONArray getValue() {
//            for (int i=0; i<fields.length(); i++) {
//                try {
//                    JSONObject field = fields.getJSONObject(i);
//                    String type = field.getString("type");
//
//                    if (type.equals("String")) {
//                        try {
//                            field.put("value", (String)inputBoxes.get(i).getValue());
//                        } catch (JSONException e) {
//                            try {
//                                field.put("value", "");
//                            } catch (JSONException f) {}
//                        }
//                    } else if (type.equals("int")) {
//                        try {
//                            field.put("value", Integer.parseInt((String)inputBoxes.get(i).getValue()));
//                        } catch (JSONException e) {
//                            try {
//                                field.put("value", 0);
//                            } catch (JSONException f) {}
//                        }
//                    } else if (type.equals("float")) {
//                        try {
//                            field.put("value", Double.parseDouble((String)inputBoxes.get(i).getValue()));
//                        } catch (JSONException e) {
//                            try {
//                                field.put("value", 0.0f);
//                            } catch (JSONException f) {}
//                        }
//                    } else if (type.equals("JSONObject") || type.equals("JSONArray")) {
//                        try {
//                            field.put("value", (JSONArray) inputBoxes.get(i).getValue());
//                        } catch (JSONException e) {
//                            try {
//                                field.put("value", new JSONArray());
//                            } catch (JSONException f) {}
//                        }
//                    }
//                } catch (JSONException f) {
//                    f.printStackTrace();
//                }
//            }
//
//            return fields;
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param parent ?
//     */
//    public TrialAttrsForm(Component parent) {
//        setTitle("Trial Attributes");
//
//        JPanel frame_container = new JPanel(new BorderLayout());
//        input_container = new JPanel();
//        input_container.setLayout(new BoxLayout(input_container, BoxLayout.Y_AXIS));
//
//        selectedFile = null;
//        selectedTag = null;
//        actionListener = null;
//        fieldsInput = null;
//
//        frame_container.add(input_container, BorderLayout.CENTER);
//
//        okayButton = new JButton("OK");
//        okayButton.addActionListener(this);
//        JPanel accept_panel = new JPanel();
//        accept_panel.add(okayButton);
//        frame_container.add(accept_panel, BorderLayout.SOUTH);
//        JScrollPane scrollPane = new JScrollPane(frame_container);
//        add(scrollPane);
//        setSize(550, 300);
//    }
//
//    /**
//     * ?
//     *
//     * @param jsonObj ?
//     */
//    public void loadForm(JSONObject jsonObj) {
//        JSONArray fields = null;
//        fieldsInput = null;
//        input_container.removeAll();
//        try {
//            if (jsonObj.isNull("trial_attrs")) {
//                //hide();
//                dispose();
//                return;
//            }
//            jsonObj = jsonObj.getJSONObject("trial_attrs");
//            fields = jsonObj.getJSONArray("fields");
//        } catch (JSONException e) {
//            //hide();
//            dispose();
//            return;
//        }
//
//        try {
//            fieldsInput = new JSONObjectInput(fields);
//            input_container.add(fieldsInput);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        input_container.revalidate();
//        input_container.repaint();
//    }
//
//    public boolean showForm() {
//        if ((fieldsInput != null) && (fieldsInput.numFields() > 0)) {
//            //super.show();
//            super.setVisible(true);
//            return true;
//        }
//
//        return false;
//    }
//
//    public String getValues() {
//        return fieldsInput.getValue().toString();
//    }
//
//    public void addActionListener(ActionListener actionListener) {
//        this.actionListener = actionListener;
//    }
//
//    public void actionPerformed(ActionEvent e) {
//        if (e.getSource() == okayButton) {
//            //hide();
//            dispose();
//            if (actionListener != null) {
//                actionListener.actionPerformed(
//                    new ActionEvent(this, 0, "accepted"));
//            }
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param args ?
//     */
//    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//        } catch (Exception e) {}
//
//        JFrame frame = new JFrame("Test Form");
//        final TrialAttrsForm creator = new TrialAttrsForm(frame);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        JButton launchButton = new JButton("press");
//        launchButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                //creator.show();
//                creator.setVisible(true);
//            }
//        });
//
//        frame.add(launchButton);
//        frame.setSize(150,50);
//        frame.setVisible(true);
//    }
//}
