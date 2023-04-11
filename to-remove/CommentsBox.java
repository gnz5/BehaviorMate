//package losonczylab.behaviormate;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.File;
//import java.util.Objects;
//
//// Todo: how does this section work?
//class CommentsBox extends JPanel implements ActionListener {
//    private final JComboBox<String> fileSelect;
//    private final JButton saveButton;
//    private final JTextArea commentArea;
//    private final String nextItem;
//    private String lastSelection;
//    private String savedString;
//    private File currentFile;
//    private String currentItem;
//    private String nextItemText;
//    private String currentItemText;
//
//    /**
//     * ?
//     *
//     */
//    public CommentsBox() {
//        currentFile = null;
//        currentItem = null;
//        nextItemText = "";
//        currentItemText = "";
//        savedString = "";
//
//        setLayout(new BorderLayout());
//        JPanel formContainer = new JPanel();
//        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
//
//        fileSelect = new JComboBox<>();
//        fileSelect.addActionListener(this);
//        formContainer.add(fileSelect);
//        formContainer.add(Box.createVerticalStrut(15));
//
//        saveButton = new JButton("save comment");
//        saveButton.addActionListener(this);
//        JPanel buttonPanel = new JPanel();
//        buttonPanel.add(saveButton);
//        saveButton.setPreferredSize(new Dimension(200, saveButton.getPreferredSize().height));
//        formContainer.add(buttonPanel);
//        add(formContainer, BorderLayout.WEST);
//
//        commentArea = new JTextArea(5,200);
//        add(commentArea, BorderLayout.CENTER);
//
//        add(new JLabel("Comments"), BorderLayout.NORTH);
//        nextItem = "next trial";
//        lastSelection = nextItem;
//        fileSelect.addItem(nextItem);
//    }
//
//    /**
//     * ?
//     *
//     * @param file ?
//     */
//    public void setCurrentFile(File file) {
//        if (fileSelect.getSelectedItem() == nextItem) {
//            currentItemText = commentArea.getText();
//        } else {
//            currentItemText = nextItemText;
//            commentArea.setText(nextItemText);
//        }
//
//        if (!savedString.equals("")) {
//            BehaviorMate.tc.addComment(savedString);
//            savedString = "";
//        }
//
//        fileSelect.removeActionListener(this);
//        fileSelect.removeAllItems();
//        currentFile = file;
//        currentItem = file.getName();
//
//        fileSelect.addItem(currentItem);
//        fileSelect.setSelectedItem(currentItem);
//
//        nextItemText = "";
//        lastSelection = currentItem;
//
//        fileSelect.addActionListener(this);
//    }
//
//    /**
//     * ?
//     *
//     * @param option ?
//     */
//    public void addOption(String option) {
//        if (option.equals(nextItem)) {
//            fileSelect.addItem(nextItem);
//        } else {
//            fileSelect.addItem(option);
//
//        }
//    }
//
//    /**
//     * ?
//     *
//     * @param e ?
//     */
//    public void actionPerformed(ActionEvent e) {
//        if (e.getSource() == saveButton) {
//            if (commentArea.getText().equals("")) {
//                return;
//            }
//            if (fileSelect.getSelectedItem() == nextItem) {
//                savedString = commentArea.getText();
//            } else {
//                BehaviorMate.tc.addComment(commentArea.getText());
//            }
//        } else if (e.getSource() == fileSelect) {
//            //if (!fileSelect.getSelectedItem().equals(lastSelection)) {
//            if (!Objects.equals(fileSelect.getSelectedItem(), lastSelection)) {
//                if (fileSelect.getSelectedItem() == nextItem) {
//                    currentItemText = commentArea.getText();
//                    commentArea.setText(nextItemText);
//                } else {
//                    nextItemText = commentArea.getText();
//                    commentArea.setText(currentItemText);
//                }
//
//                lastSelection = (String)fileSelect.getSelectedItem();
//            }
//        }
//    }
//}