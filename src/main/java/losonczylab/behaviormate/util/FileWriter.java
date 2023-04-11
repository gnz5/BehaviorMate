package losonczylab.behaviormate.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import losonczylab.behaviormate.BehaviorMate;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.text.SimpleDateFormat;
import java.lang.InterruptedException;

/**
 * Write logs for experiment trials. Ensures that the output stream is opened
 * for each line written to the output file so that the file cannot be corrupted
 * in the event of an early termination of the UI program
 */
public class FileWriter {
    /**
     * Placeholder
     */
    public class WriterThread extends Thread {
        private final ConcurrentLinkedQueue<String> writeQueue;
        private final File logFile;
        private boolean run;
        private Thread t;
        private String messageBuffer;
        private FileOutputStream fos;
        private OutputStreamWriter osw;

        /**
         * Placeholder
         *
         * @param logFile Placeholder
         */
        WriterThread(File logFile) {
            this.run = true;
            writeQueue = new ConcurrentLinkedQueue<>();
            messageBuffer = null;
            this.logFile = logFile;
        }

        /**
         * Placeholder
         */
        public void run() {
            messageBuffer = writeQueue.poll();
            while (this.run || messageBuffer != null) {
                if (messageBuffer != null) {
                    try {
                        fos = new FileOutputStream(logFile, true);
                        osw = new OutputStreamWriter(fos);
                    } catch (IOException e) {
                        System.out.println(e);
                        BehaviorMate.showError("Error opening log file");
                    }

                    int j = 0;
                    for (; ((messageBuffer != null)); j++) {
                        try {
                            osw.write(messageBuffer.replaceAll("[\r|\n]|\\s{2,}","") + "\n");
                        } catch (IOException e) {
                            StringBuilder alert = new StringBuilder(e.toString());
                            StackTraceElement[] elements = e.getStackTrace();
                            for (int i = 0; ( (i < 3) && (i < elements.length) ); i++) {
                                alert.append("\n").append(elements[i].toString());
                            }
                            BehaviorMate.showError(alert.toString());
                            break;
                        }
                        messageBuffer = writeQueue.poll();
                    }

                    try {
                        osw.close();
                        fos.close();
                    } catch (IOException e) {
                        System.out.println(e);
                        BehaviorMate.showError("Error saving log file.");
                    }

                }

                try {
                    // Todo: why wait here?
                    Thread.sleep(25);
                } catch (InterruptedException ignored) {}

                messageBuffer = writeQueue.poll();
            }
        }

        /**
         * Placeholder
         */
        public void start() {
            if (t == null) {
                t = new Thread(this, "writeThread " + System.nanoTime());
                t.start();
            }
        }

        /**
         * Placeholder
         */
        public void stop_thread() {
            run = false;
        }

        /**
         * Placeholder
         *
         * @param msg Placeholder
         */
        public void queueMessage(String msg) {
            writeQueue.add(msg);
        }
    }

    WriterThread wt;
    File logFile;
    SimpleDateFormat logNameFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Create a new FileWriter. Generates a new log file at a path defined by
     * the current time and the mouse's name.
     *
     * @param pathname Path to the directory for storing log files.
     * @param mouse    Identifier for each mouse.
     */
    public FileWriter(String pathname, String mouse) throws IOException {
        String path = pathname + File.separatorChar + mouse;
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String logDate = logNameFormat.format(Calendar.getInstance().getTime());
        String behaviorFileName = mouse + "_" + logDate + ".tdml";
        String behaviorFilePath = BehaviorMate.getPlatformAgnosticPath(new String[] {pathname, mouse, behaviorFileName});
        logFile = new File(behaviorFilePath);
        wt = new WriterThread(logFile);
        wt.start();
    }

    /**
     * Placeholder
     *
     * @param filename Placeholder
     */
    public FileWriter(String filename) {
        logFile = new File(filename);
        wt = new WriterThread(logFile);
        wt.start();
        Date logDate = Calendar.getInstance().getTime();
        this.write(logNameFormat.format(logDate));
    }

    /**
     *
     * @return Placeholder
     */
    public File getFile() {
        return new File(logFile.getAbsolutePath());
    }

    /**
     * Write a line to the experiment's log file.
     * @param msg message to be written to the log file
     */
    public void write(String msg) {
        if (wt == null) {
            wt = new WriterThread(logFile);
            wt.start();
            wt.queueMessage(msg);
            wt.stop_thread();
            wt = null;
        } else {
            wt.queueMessage(msg);
        }
    }

    public void writeEndLog() {
        Date stopDate = Calendar.getInstance().getTime();
        JSONObject end_log = new JSONObject();
        try {
            end_log.put("time", BehaviorMate.tc.getTime());
            end_log.put("stop", BehaviorMate.tc.getDateFormat().format(stopDate));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        write(end_log.toString());
        close();
    }

    /**
     * Placeholder
     */
    public void close() {
        if (wt != null) {
            wt.stop_thread();
            wt = null;
        }
    }

//    public void showBehaviorFileDeleteDialog() {
//        if (BehaviorMate.showBehaviorFileDeleteDialog(logFile.getAbsolutePath())) {
//            logFile.delete();
//        }
//    }

//    public void showBehaviorFileDeleteDialog() {
//        String alertMessage = "Would you like to keep the generated behavior data file: \n\n" + logFile.getAbsolutePath() + "?";
//        ButtonType keep = new ButtonType("Keep");
//        ButtonType discard = new ButtonType("Discard");
//        Alert alert = new Alert(Alert.AlertType.NONE, alertMessage, keep, discard);
//        alert.showAndWait();
//
//        if (alert.getResult() == discard) {
//            String secondaryAlertMessage = "Confirm deletion.";
//            ButtonType neverMind = new ButtonType("Never mind, keep it");
//            ButtonType permanentlyDelete = new ButtonType("Permanently Discard");
//            Alert secondaryAlert = new Alert(Alert.AlertType.NONE, secondaryAlertMessage, neverMind, permanentlyDelete);
//            secondaryAlert.showAndWait();
//
//            if (secondaryAlert.getResult() == permanentlyDelete) {
//                logFile.delete();
//            }
//        }
//    }

}
