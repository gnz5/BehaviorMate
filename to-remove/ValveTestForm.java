//package losonczylab.behaviormate;
//
//import javax.swing.*;
//import javax.swing.plaf.basic.DefaultMenuLayout;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
///**
// * Class for making panel containing the LabeledTextFields for the Valve and Duration fields
// * and the button for opening the valve.
// */
//class ValveTestForm extends JPanel implements ActionListener {
//    private final LabeledTextField valveText;
//    private final LabeledTextField durationText;
//    private final JButton testValveButton;
//
//    /**
//     *
//     */
//    private boolean blocked;
//
//    /**
//     * ?
//     *
//     */
//    public ValveTestForm() {
//        super(new FlowLayout());
//
//        JPanel center_panel = new JPanel(new GridLayout(0,1));
//        valveText = new LabeledTextField("  Valve", ""+BehaviorMate.tc.getRewardPin(), 14);
//        durationText = new LabeledTextField("  Duration", "200", 14);
//        center_panel.add(valveText);
//        center_panel.add(durationText);
//        add(center_panel);
//
//        testValveButton = new JButton("Open Valve");
//        testValveButton.addActionListener(this);
//
//        JPanel button_container = new JPanel(new GridLayout(0,1));
//        button_container.add(testValveButton);
//        add(button_container);
//    }
//
//    /**
//     * ?
//     *
//     * @param enabled ?
//     */
//    public void setEnabled(boolean enabled) {
//        this.blocked = !enabled;
//        if (!enabled) {
//            testValveButton.setText("Enable");
//        } else {
//            testValveButton.setText("Open Valve");
//        }
//        //testValveButton.setEnabled(enabled);
//    }
//
//    /**
//     * ?
//     *
//     * @param e ?
//     */
//    public void actionPerformed(ActionEvent e) {
//        int valve = valveText.getInt();
//        int duration = durationText.getInt();
//
//        if (blocked) {
//            setEnabled(true);
//        } else if ((valve != 0) && (duration != 0)) {
//            BehaviorMate.tc.TestValve(valveText.getInt(), durationText.getInt());
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param pin_number ?
//     */
//    public void setPin(int pin_number) {
//        valveText.setText(""+pin_number);
//    }
//}