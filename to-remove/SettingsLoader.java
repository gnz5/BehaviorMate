//package losonczylab.behaviormate;
//
//import java.awt.Component;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.JButton;
//import javax.swing.JTextField;
//import javax.swing.JComboBox;
//import javax.swing.JDialog;
//import javax.swing.JFileChooser;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import java.awt.event.ActionListener;
//import java.awt.event.ActionEvent;
//import java.awt.BorderLayout;
//import java.awt.Dimension;
//import java.io.File;
//import java.util.Iterator;
//import org.json.JSONObject;
//
//public final class SettingsLoader extends JDialog implements ActionListener {
//    private final JButton fileChooserButton;
//    private final JComboBox comboBox;
//    private final JTextField pathTextField;
//    private final JButton okayButton;
//    private final JButton cancelButton;
//    static final String SETTINGS_FILE = "settings.json";
//    private String selectedFile;
//    private String selectedTag;
//    private ActionListener actionListener;
//
//    public SettingsLoader(Component parent) {
//        JPanel frame_container = new JPanel(new BorderLayout());
//        JPanel input_container = new JPanel();
//        selectedFile = null;
//        selectedTag = null;
//        actionListener = null;
//
//        comboBox = new JComboBox<String>();
//        comboBox.setPreferredSize(new Dimension(150,comboBox.getPreferredSize().height));
//        File defaultFile = new File(SETTINGS_FILE);
//        pathTextField = new JTextField(defaultFile.getAbsolutePath(), 25);
//
//        JSONObject jsonObj = BehaviorMate.parseJsonFile(SETTINGS_FILE);
//
//        if (jsonObj != null) {
//            Iterator<?> keys = jsonObj.keys();
//            while (keys.hasNext()) {
//                String key = (String) keys.next();
//                if (!key.startsWith("_")) {
//                    comboBox.addItem(key);
//                }
//            }
//            selectedFile = defaultFile.getAbsolutePath();
//            selectedTag = (String) comboBox.getSelectedItem();
//        }
//
//        pathTextField.setEnabled(false);
//        input_container.add(pathTextField);
//        fileChooserButton = new JButton("...");
//        fileChooserButton.setPreferredSize(new Dimension(40,comboBox.getPreferredSize().height));
//        input_container.add(fileChooserButton);
//        fileChooserButton.addActionListener(this);
//        input_container.add(comboBox);
//        frame_container.add(input_container, BorderLayout.CENTER);
//
//        okayButton = new JButton("OK");
//        okayButton.addActionListener(this);
//        cancelButton = new JButton("Cancel");
//        cancelButton.addActionListener(this);
//        JPanel accept_panel = new JPanel();
//        accept_panel.add(okayButton);
//        accept_panel.add(cancelButton);
//        frame_container.add(accept_panel, BorderLayout.SOUTH);
//
//        add(frame_container);
//        setSize(550, 120);
//    }
//
//    String getSelectedFile() {
//        return selectedFile;
//    }
//
//    String getSelectedTag() {
//        return selectedTag;
//    }
//
//    public void addActionListener(ActionListener actionListener) {
//        this.actionListener = actionListener;
//    }
//
//    public void actionPerformed(ActionEvent e) {
//        if (e.getSource() == fileChooserButton) {
//            final JFileChooser fileChooser = new JFileChooser();
//            fileChooser.setCurrentDirectory(new File("."));
//            fileChooser.setAcceptAllFileFilterUsed(false);
//            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON file", "json"));
//            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("behavior file", "tdml"));
//            int returnVal = fileChooser.showOpenDialog(null);
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                File file = fileChooser.getSelectedFile();
//                comboBox.removeAllItems();
//                pathTextField.setText(file.getAbsolutePath());
//                JSONObject json = BehaviorMate.parseJsonFile(file.getAbsolutePath());
//                if (json != null) {
//                    Iterator<?> keys = json.keys();
//                    while (keys.hasNext()) {
//                        String key = (String) keys.next();
//                        if (!key.startsWith("_")) {
//                        comboBox.addItem(key);
//                        }
//                    }
//                }
//            }
//        } else if (e.getSource() == okayButton) {
//            this.selectedFile = pathTextField.getText();
//            this.selectedTag = (String) comboBox.getSelectedItem();
//            if (actionListener != null) {
//                actionListener.actionPerformed(
//                    new ActionEvent(this, 0, "accepted"));
//            }
//            //hide();
//            dispose();
//        } else if (e.getSource() == cancelButton) {
//            //hide();
//            dispose();
//        }
//    }
//
//    public static void main(String[] args) {
//        JFrame frame = new JFrame("Test Form");
//        final SettingsLoader loader = new SettingsLoader(frame);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        JButton launchButton = new JButton("press");
//        launchButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                //loader.show();
//                loader.setVisible(true);
//            }
//        });
//
//        frame.add(launchButton);
//        frame.setSize(150,100);
//        frame.setVisible(true);
//    }
//}
