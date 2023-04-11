//// REMOVE AFTER TESTING //
//
//package losonczylab.behaviormate;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
///**
// * Class for creating the panel containing the 3 buttons for calibrating, resetting,
// * and zeroing position.
// */
//class CalibrateBeltForm extends JPanel implements ActionListener {
//
//    /**
//     * Button to calibrate position.
//     * Todo: should probably be renamed positionButton
//     */
//    private final JButton jButton;
//
//    /**
//     * Button for resetting position.
//     */
//    private final JButton resetButton;
//
//    /**
//     * Button for zeroing position.
//     * Todo: what's the difference between calibrating, resetting, and zeroing position?
//     */
//    private final JButton zeroButton;
//
//    /**
//     * ?
//     */
//    private boolean calibrating;
//
//    /**
//     * ?
//     *
//     */
//    public CalibrateBeltForm() {
//        super(new GridLayout(0, 1));
//        //this.treadmillController = treadmillController;
//
//        jButton = new JButton("Calibrate Position");
//        jButton.addActionListener(this);
//        jButton.setPreferredSize(new Dimension(115, 25));
//        jButton.setFont(new Font("Arial", Font.PLAIN, 10));
//        resetButton = new JButton("Reset");
//        resetButton.setFont(new Font("Arial", Font.PLAIN, 10));
//        resetButton.addActionListener(this);
//        zeroButton = new JButton("Zero Position");
//        zeroButton.setFont(new Font("Arial", Font.PLAIN, 10));
//        zeroButton.setPreferredSize(new Dimension(115, 25));
//        zeroButton.addActionListener(this);
//
//        JPanel button_container = new JPanel(new GridLayout(0,1));
//        JLabel formLabel = new JLabel(" Position Controls");
//        button_container.add(formLabel);
//        button_container.add(jButton);
//        button_container.add(resetButton);
//        button_container.add(zeroButton);
//        add(button_container);
//
//        calibrating = false;
//    }
//
//    /**
//     * Used to enable or disable all 3 buttons.
//     * @param enabled <code>true</code> will enable all buttons and <code>false</code> will disable
//     *                all buttons.
//     */
//    public void setEnabled(boolean enabled) {
//        jButton.setEnabled(enabled);
//        resetButton.setEnabled(enabled);
//        zeroButton.setEnabled(enabled);
//    }
//
//    /**
//     * ?
//     */
//    void endCalibration() {
//        BehaviorMate.tc.EndBeltCalibration();
//        jButton.setText("Calibrate Position");
//        this.calibrating = false;
//    }
//
//    /**
//     * Implemented method of the ActionListener interface.
//     *
//     * @param e The ActionEvent that occurred in the java application.
//     */
//    public void actionPerformed(ActionEvent e) {
//        if (e.getSource() == jButton) {
//            if (calibrating) {
//                endCalibration();
//            } else {
//                BehaviorMate.tc.CalibrateBelt();
//                jButton.setText("End Calibration");
//                calibrating = true;
//            }
//        } else if (e.getSource() == resetButton) {
//            BehaviorMate.tc.ResetCalibration();
//        } else if (e.getSource() == zeroButton) {
//            BehaviorMate.tc.ZeroPosition();
//        }
//    }
//}