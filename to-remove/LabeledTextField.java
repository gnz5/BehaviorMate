//package losonczylab.behaviormate;
//
//import javax.swing.*;
//import java.awt.*;
//
///**
// * Used to create a panel containing a label with a text field below it. This is used for
// * changing the project name, mouse name, valve, and duration.
// */
//class LabeledTextField extends JPanel {
//    /**
//     * Text field for entering values.
//     */
//    private final JTextField textField;
//
//    /**
//     *
//     * @param text The text of the label above the text field.
//     * @param value The default text of the text field.
//     * @param width The width of the text field.
//     */
//    public LabeledTextField(String text, String value, int width) {
//        super(new BorderLayout());
//        JLabel label = new JLabel(text);
//        this.textField = new JTextField(value, width);
//        add(label, BorderLayout.NORTH);
//        JPanel text_container2 = new JPanel(new FlowLayout());
//        text_container2.add(textField);
//        add(text_container2, BorderLayout.CENTER);
//    }
//
//    /**
//     * Constructs a LabeledTextField with a blank text field.
//     *
//     * @param text The text of the label above the text field.
//     * @param width The width of the text field.
//     */
//    public LabeledTextField(String text, int width) {
//        this(text, "", width);
//    }
//
//    /**
//     * Used to enable or disable the text field.
//     *
//     * @param enabled Pass <code>true</code> to enable the text field and <code>false</code> to
//     *                disable it.
//     */
//    public void setEnabled(boolean enabled) {
//        this.textField.setEnabled(enabled);
//    }
//
//    /**
//     *
//     * @return The text of the text field.
//     */
//    public String getText() {
//        return this.textField.getText();
//    }
//
//    /**
//     * Sets the text of the text field.
//     *
//     * @param text The new text of the text field.
//     */
//    public void setText(String text) {
//        this.textField.setText(text);
//    }
//
//    /**
//     *
//     * @return The integer written in the text field by the user if it is a valid integer.
//     */
//    public int getInt() {
//        try {
//            return Integer.parseInt(this.getText());
//        } catch (NumberFormatException e) {
//            System.out.println(e);
//        }
//        return 0;
//    }
//}