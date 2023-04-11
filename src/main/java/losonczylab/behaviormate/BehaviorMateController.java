package losonczylab.behaviormate;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import losonczylab.behaviormate.core.Context;
import losonczylab.behaviormate.core.ContextList;
import losonczylab.behaviormate.util.Int;
import losonczylab.behaviormate.util.Str;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BehaviorMateController {
    @FXML
    private AnchorPane graphicsPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Polygon positionMarker;
    @FXML
    private Rectangle track;

    // Left side of the UI
    @FXML
    private TextField projectName;
    @FXML
    private TextField mouseName;
    @FXML
    private TextField valve;
    @FXML
    private TextField duration;
    @FXML
    private Button calibratePositionBtn;
    @FXML
    private Button resetBtn;
    @FXML
    private Button zeroPositionBtn;
    @FXML
    private Button openValveBtn;
    @FXML
    private Button editTrialAttrsBtn;
    @FXML
    private Button reloadSettingsBtn;
    @FXML
    private Button restartCommsBtn;
    @FXML
    private Button startBtn;
    @FXML
    private MenuButton commentsMenuBtn;
    @FXML
    private TextField commentsTextField;
    @FXML
    private Button saveCommentsBtn;

    // Right side of the UI
    @FXML
    private TableView contextListTable;
    @FXML
    private TableColumn idColumn;
    @FXML
    private TableColumn statusColumn;
    @FXML
    private TableColumn valvesColumn;

    @FXML
    private TableView valvesTable;
    @FXML
    private TableColumn valveColumn;
    @FXML
    private TableColumn stateColumn;
    @FXML
    private TableColumn countColumn;

    @FXML
    private TableView metricsTable;
    @FXML
    private TableColumn metricNameColumn;
    @FXML
    private TableColumn metricValueColumn;
    Metric timeMetric = new Metric("Time", "0 / 0");
    Metric lapCountMetric = new Metric("Lap Count", "0");
    Metric positionMetric = new Metric("Position", "0");
    Metric rewardCountMetric = new Metric("Reward Count", "0");
    Metric lickCountMetric = new Metric("Lick Count", "0");
    Metric positionScaleMetric = new Metric("Position Scale", "0");

    @FXML
    private ProgressBar velocityBar;
    @FXML
    private ProgressBar lickingBar;
    @FXML
    private ProgressBar rewardBar;

    @FXML
    private Label decoratorsLabel;
    @FXML
    private Label settingsFileLabel;

    // Bottom of the UI
    @FXML
    private Circle behaviorControllerIndicator;
    @FXML
    private Circle positionControllerIndicator;
    @FXML
    private Label fpsLabel;
    @FXML
    private Label commentsLabel;

    BehaviorMate behaviorMate;

    private ArrayList<Rectangle> contexts;
    private static final String DEFAULT_DURATION = "200"; // in ms
    private boolean calibrating = false;
    private static final int MILLISPERUIUPDATE = 40;
    private static final int MILLISPERCOMMSCHECK = 5000;
    private static final int MILLISPERTREADMILLUPDATE = 20;
    private double startX;
    private double startY;
    private double velocityRate;
    private static double lickRate;
    private static double rewardRate;
    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0 ;
    private boolean arrayFilled = false ;

    private static final Paint RED = Color.valueOf("ff0000");
    private static final Paint GREEN = Color.valueOf("00ff00");
    private static final Paint BLACK = Color.valueOf("000000");
    private static final Paint YELLOW = Color.valueOf("ffff00");

    private static float uiPixelPerUnitLength; // pixel width of UI track divided by track length in mm

    private static Timer treadmillTimer;
    private static Timeline uiLoop; // UI loop that updates around the same frequency as the treadmillLoop
    private static volatile Map<String, Boolean> commStatuses; // declared volatile because written to by commCheckService and ready by main application thread
    AnimationTimer frameRateMeter;

    void start() {
        setupFrameRateTracker();
        movePosMarkerToDefaultPos();
        duration.setText(DEFAULT_DURATION);
        setupUILoop();
    }

    void run() {
        setUiPixelPerUnitLength();
        setupTableViews();
        setupCommentsMenuButton();
        updatePositionMarker();
        updateButtonsAndLabels();
        updateProgressBars();
        updateValves();
        updateContextListStatuses();
        updateDecoratorAndSettingsLabels();
        updateControllerIndicators();
        setupCommCheckService();
        uiLoop.play();

        try {
            drawContexts();
        } catch (Exception e) {
            BehaviorMate.showError("Error drawing contexts. \n\n" + e.getMessage());
            e.printStackTrace();
            behaviorMate.endProgram();
        }
    }

    void stop() {
        try {
            BehaviorMate.tc.endExperiment();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            behaviorMate.endProgram();
        }
        treadmillTimer.cancel();
        enableAllButtons();
        BehaviorMate.showBehaviorFileDeleteDialog();
        startBtn.setText("Start");
        setupCommentsMenuButton();
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Functions relating to buttons ///////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @FXML
    protected void calibratePositionBtnClick() {
        if (calibrating) {
            calibratePositionBtn.setText("Calibrate Position");
            BehaviorMate.tc.EndBeltCalibration();
            calibrating = false;
        } else {
            calibratePositionBtn.setText("Calibrating...");
            BehaviorMate.tc.CalibrateBelt();
            calibrating = true;
        }
    }

    @FXML
    protected void resetBtnClick() {
        BehaviorMate.tc.ResetCalibration();
    }

    @FXML
    protected void zeroPositionBtnClick() {
        BehaviorMate.tc.ZeroPosition();
    }

    @FXML
    protected void openValveBtnClick() throws JSONException {
        int valve_as_int = -1;
        int duration_as_int = -1;

        if (valve.getText().isBlank() || duration.getText().isBlank()) {
            BehaviorMate.showError("Valve pin and duration cannot be blank.");
            return;
        }

        try {
            valve_as_int = Integer.parseInt(valve.getText());
            duration_as_int = Integer.parseInt(duration.getText());
        } catch (NumberFormatException ignored) {
            BehaviorMate.showError("Valve pin and duration must be integers.");
        }

        if (valve_as_int < 1 || valve_as_int > 99) {
            BehaviorMate.showError("Valve pin must be between 1 and 99 inclusive.");
            return;
        }

        if (duration_as_int <= 0 || duration_as_int > 10000) {
            BehaviorMate.showError("Duration must greater than 0 ms and less than 10000 ms.");
            return;
        }

        BehaviorMate.tc.TestValve(valve_as_int, duration_as_int);
    }

    @FXML
    protected void editTrialAttributesBtnClick() {
        behaviorMate.openTrialAttrsForm();
    }

    @FXML
    protected void reloadSettingsBtnClick() {
        try {
            behaviorMate.refresh();
            run();
        } catch (Exception e) {
            e.printStackTrace();
            behaviorMate.endProgram();
        }

    }

    @FXML
    protected void restartCommsBtnClick() {
        Service resetCommsService = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        BehaviorMate.tc.resetComms();
                        return null;
                    }
                };
            }
        };
        resetCommsService.start();
    }

    @FXML
    protected void startBtnClick() {
        // End the trial
        if (BehaviorMate.tc.hasTrialStarted()) {
            stop();
        }
        // Start the trial if possible
        else {
            String mouse = mouseName.getText();
            String project = projectName.getText();
            String validFileChars = "[a-zA-Z0-9-_\\.]";
            if (mouse == null || mouse.isBlank() || project == null || project.isBlank()) {
                BehaviorMate.showError("Mouse name and project name cannot be empty.");
            } else if (mouse.replaceAll(validFileChars, "").compareTo("") != 0 || project.replaceAll(validFileChars, "").compareTo("") != 0) {
                BehaviorMate.showError("Mouse name and project name may only contain letters, numbers, periods, or underscores.");
            }
            else if (!BehaviorMate.tc.hasPositionBeenZeroed()) {
                BehaviorMate.showError("Please zero position and try again.");
            } else {
                disableMostButtons();
                try {
                    BehaviorMate.tc.EndBeltCalibration();
                    BehaviorMate.tc.Start(mouse, project);
                } catch (Exception e) {
                    e.printStackTrace();
                    behaviorMate.endProgram();
                }
                setupTreadmillService();
                startBtn.setText("End Trial");
            }
        }
    }

    @FXML
    protected void setCommentsFile() {
        // todo
    }

    @FXML
    protected void saveCommentBtnClick() {
        if (!BehaviorMate.tc.hasTrialStarted()) {
            if (commentsMenuBtn.getText().compareTo(Str.NEXT_TRIAL) == 0) {
                commentsLabel.setText("Trial has not started yet. Comment not saved.");
            } else if (commentsMenuBtn.getText().compareTo("Comments") == 0) {
                commentsLabel.setText("Select an option from Comments drop-down menu. Comment not saved.");
            }
            commentsLabel.setOpacity(1);
        } else if (!commentsTextField.getText().isBlank()) {
            BehaviorMate.tc.writeComment(commentsTextField.getText());
            commentsLabel.setText("Comment written.");
            commentsLabel.setOpacity(1);
        }
    }

    @FXML
    protected void keyPressed(KeyEvent event) {
        writeQuickComment(event, true);
    }

    @FXML
    protected void keyReleased(KeyEvent event) {
        writeQuickComment(event, false);
    }

    @FXML
    private void writeQuickComment(KeyEvent event, Boolean pressed) {
        // user isn't typing anything so register keyboard clicks as comment shortcuts
        if (!(valve.isFocused() || duration.isFocused() || commentsTextField.isFocused())) {
            char key = ' ';
            try {
                String input = event.getText();
                if (input.length() > 0) {
                    key = input.charAt(0);
                }
            } catch (Exception e) {
                System.out.println(e);
                return;
            }

            if (BehaviorMate.tc != null && BehaviorMate.tc.hasTrialStarted() && key != ' ') {
                BehaviorMate.tc.writeQuickComment(key, pressed);
                commentsLabel.setText("Quick comment written (" + key + ").");
                commentsLabel.setOpacity(1);
            }
        }
    }

    private void disableMostButtons() {
        calibratePositionBtn.setDisable(true);
        resetBtn.setDisable(true);
        zeroPositionBtn.setDisable(true);
        editTrialAttrsBtn.setDisable(true);
        reloadSettingsBtn.setDisable(true);
        restartCommsBtn.setDisable(true);
        commentsMenuBtn.setDisable(true);
        projectName.setDisable(true);
        mouseName.setDisable(true);
    }

    private void enableAllButtons() {
        calibratePositionBtn.setDisable(false);
        resetBtn.setDisable(false);
        zeroPositionBtn.setDisable(false);
        editTrialAttrsBtn.setDisable(false);
        reloadSettingsBtn.setDisable(false);
        restartCommsBtn.setDisable(false);
        commentsMenuBtn.setDisable(false);
        projectName.setDisable(false);
        mouseName.setDisable(false);
    }

    @FXML
    private void showFullSettingsFilePath() {
        BehaviorMate.showSettingsFilePopup();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Utility Functions for drawing contexts ////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    private Rectangle drawSquare(float location, float radius, Paint color) {
        float uiRadius = uiPixelPerUnitLength*radius;
        float uiLocation = uiPixelPerUnitLength*location;
        float leftX = (float) track.getLayoutX() + uiLocation - uiRadius;
        Rectangle square = new Rectangle();
        square.relocate(leftX, track.getLayoutY()+1);
        square.setWidth(2*uiRadius);
        square.setHeight(track.getHeight()-2);
        square.setFill(color);
        square.setStroke(BLACK);
        return square;
    }

    private void drawContexts() {
        // Remove contexts from UI
        if (contexts != null) {
            for (Rectangle context : contexts) {
                ObservableList<Node> uiElements = graphicsPane.getChildren();
                uiElements.remove(context);
            }
        }

        // Add contexts back to UI
        contexts = new ArrayList<Rectangle>();
        for (ContextList contextList : BehaviorMate.tc.contextLists) {
            for (int i = 0; i < contextList.size(); i++) {
                Context context = contextList.getContext(i);
                Rectangle contextSquare = drawSquare(context.getLocation(), context.getRadius(), rgbToHex(contextList.getDisplayColor()));
                contexts.add(contextSquare);
            }
        }
        graphicsPane.getChildren().addAll(contexts);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// Other UI Functions ////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    private void movePosMarkerToDefaultPos() {
        double positionMarkerHeight = positionMarker.getPoints().get(5) - positionMarker.getPoints().get(1);
        double positionMarkerWidth = positionMarker.getPoints().get(2) - positionMarker.getPoints().get(0);
        startX = track.getLayoutX()-positionMarkerWidth/2;
        startY = track.getLayoutY()-positionMarkerHeight;
        positionMarker.relocate(startX, startY);
    }

    private void setupTableViews() {
        //if (contextListStatusMetrics.isEmpty()) {
        if (metricsTable.getItems().isEmpty()) {
            metricNameColumn.setCellValueFactory(new PropertyValueFactory<>("metric"));
            metricValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            metricsTable.getItems().add(timeMetric);
            metricsTable.getItems().add(lapCountMetric);
            metricsTable.getItems().add(positionMetric);
            metricsTable.getItems().add(rewardCountMetric);
            metricsTable.getItems().add(lickCountMetric);
            metricsTable.getItems().add(positionScaleMetric);
        }

        if (contextListTable.getItems().isEmpty()) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            valvesColumn.setCellValueFactory(new PropertyValueFactory<>("valves"));

            for (int i = 0; i < BehaviorMate.tc.contextLists.size(); i++) {
                ContextList list = BehaviorMate.tc.contextLists.get(i);
                String valves = list.getPins().toString().replace("[", "").replace("]", "");
                ContextListMetric contextListMetric = new ContextListMetric(list.getId(), list.getStatus(), valves);
                contextListTable.getItems().add(contextListMetric);
            }

        }

        if (valvesTable.getItems().isEmpty()) {
            valveColumn.setCellValueFactory(new PropertyValueFactory<>("valve"));
            stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
            countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        }

    }

    private void setupCommentsMenuButton() {
        commentsMenuBtn.getItems().clear();

        MenuItem nextTrialItem = new MenuItem(Str.NEXT_TRIAL);
        nextTrialItem.setOnAction(event -> commentsMenuBtn.setText(nextTrialItem.getText()));
        commentsMenuBtn.getItems().add(nextTrialItem);

        String previousBehaviorFile = BehaviorMate.tc.getPreviousTrialBehaviorFile();
        if (previousBehaviorFile != null) {
            MenuItem previousTrialItem = new MenuItem(previousBehaviorFile);
            previousTrialItem.setOnAction(event -> commentsMenuBtn.setText(previousTrialItem.getText()));
            commentsMenuBtn.getItems().add(previousTrialItem);
        }
    }

    private void updateControllerIndicators() {
        if (commStatuses == null) {
            return;
        }
        // update controller statuses
        try {
            behaviorControllerIndicator.setFill(commStatuses.get("Behavior") ? GREEN : RED);
            positionControllerIndicator.setFill(commStatuses.get("Position") ? GREEN : RED);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updatePositionMarker() {
        float position_as_percent = BehaviorMate.tc.getPosition() / BehaviorMate.tc.getTrackLength();
        double displayedPosition = startX + track.getWidth() * position_as_percent;
        positionMarker.relocate(displayedPosition, startY);

        if (BehaviorMate.tc.hasAContextBeenTriggered()) {
            positionMarker.setFill(YELLOW);
        } else {
            positionMarker.setFill(RED);
        }

    }

    private void updateButtonsAndLabels() {
        positionMetric.setValue((int) BehaviorMate.tc.getPosition());
        lapCountMetric.setValue(BehaviorMate.tc.getLapCount());
        timeMetric.setValue( ((int) BehaviorMate.tc.getTime()) + " / " + BehaviorMate.tc.getTrialDuration());
        rewardCountMetric.setValue(BehaviorMate.tc.getRewardCount());
        lickCountMetric.setValue(BehaviorMate.tc.getLickCount());
        positionScaleMetric.setValue(BehaviorMate.tc.getPositionScale());
        metricsTable.refresh();

//        for(int i = 0; i < BehaviorMate.tc.contextLists.size(); i++) {
//            ContextList list = BehaviorMate.tc.contextLists.get(i);
//
//            for(int j = 0; j < contextListStatusMetrics.size(); j++) {
//                Metric metric = contextListStatusMetrics.get(j);
//                //ContextMetric contextMetric = contextListStatusMetrics_new.get(j);
//                if (metric.getMetric().compareTo(list.getId()) == 0) {
//                    metric.setValue(list.getStatus());
//                }
//                //if (contextMetric.getId().compareTo(list.getId()) == 0) {
//                //    contextMetric.setStatus(list.getStatus());
//                    //contextMetric.setCount(list.getC());
//                //}
//            }
//        }
        contextListTable.refresh();
        openValveBtn.setText(String.valueOf(BehaviorMate.tc.getRewardPin()));
        Double commentsLabelOpacity = commentsLabel.getOpacity()-0.01;
        if (commentsLabelOpacity > 0) {
            commentsLabel.setOpacity(commentsLabelOpacity);
        } else {
            commentsLabel.setOpacity(0);
        }
    }

    private void updateProgressBars() {
        velocityRate += BehaviorMate.tc.getVelocity()/125;
        velocityBar.setProgress(velocityRate);
        velocityRate *= 0.8;
        if (BehaviorMate.tc.registerLick()) {
            lickRate += 0.2;
        }
        lickingBar.setProgress(lickRate);
        lickRate *= 0.8;
        if (BehaviorMate.tc.registerReward()) {
            rewardRate += 0.2;
        }
        rewardBar.setProgress(rewardRate);
        rewardRate *= 0.8;
    }

    private void updateContextListStatuses() {
        ArrayList<ContextList> contextList = BehaviorMate.tc.contextLists;
        for (int i = 0; i < contextList.size(); i++) {
            ContextList list = contextList.get(i);
            ContextListMetric currentContextListMetric = getContextListMetricWithId(list.getId());
            currentContextListMetric.setStatus(list.getStatus());
            contextListTable.refresh();
        }
    }

    private void updateValves() {
        JSONArray valvesJSON = BehaviorMate.tc.getValves();

        for (int j = 0; j < valvesJSON.length(); j++) {
            JSONObject currentValve;
            String pin = "", state = "", count = "";
            try {
                currentValve = valvesJSON.getJSONObject(j);
                pin = String.valueOf(currentValve.getInt(Str.PIN));
                state = String.valueOf(currentValve.getInt(Str.STATE));
                count = String.valueOf(currentValve.getInt(Str.COUNT));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ValveMetric currentValveMetric = getValveMetricWithPin(pin);

            if (currentValveMetric == null) {
                currentValveMetric = new ValveMetric(pin, state);
                valvesTable.getItems().add(currentValveMetric);
            } else {
                currentValveMetric.setState(state);
                currentValveMetric.setCount(count);
            }
            valvesTable.refresh();

        }
    }

    private void updateDecoratorAndSettingsLabels() {
        decoratorsLabel.setText("Decorator(s): " + BehaviorMate.tc.getLoadedDecorators());
        settingsFileLabel.setText("Loaded Settings File: " + BehaviorMate.getSettingsFileName());
    }

    private void setupUILoop() {
        uiLoop = new Timeline(
                new KeyFrame(Duration.millis(MILLISPERUIUPDATE),
                        event -> {
                            try {
                                drawContexts();
                                updatePositionMarker();
                                updateButtonsAndLabels();
                                updateProgressBars();
                                updateValves();
                                updateContextListStatuses();
                                updateControllerIndicators();
                            } catch (Exception e) {
                                BehaviorMate.showError("Error updating UI. \n\n" + e.getMessage());
                                e.printStackTrace();
                                behaviorMate.endProgram();
                            }
                        }));
        uiLoop.setCycleCount(Timeline.INDEFINITE);
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Menu Bar Functions /////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @FXML
    private void goToPaper() {
        openURLLink("Paper");
    }

    @FXML
    private void goToRepo() {
        openURLLink("Repository");
    }

    @FXML
    private void goToWebsite() {
        openURLLink("Website");
    }

    @FXML
    private void goToUpdate() {
        openURLLink("Update");
    }

    @FXML
    private void goToBugReport() {
        openURLLink("BugReport");
    }

    @FXML
    private void goToJavaDocs() {
        openURLLink("JavaDocs");
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About BehaviorMate");
        alert.setHeaderText(null);
        String message = String.format("BehaviorMate %s\nBuilt on: %s\n\nVM: %s\n\nAuthors: %s\n%s",
                BehaviorMate.VERSION, BehaviorMate.BUILTON, BehaviorMate.VM, BehaviorMate.AUTHORS, BehaviorMate.COPYRIGHT);
        alert.setContentText(message);
        try {
            alert.showAndWait();
        } catch (Exception e) {
            String msg = "Error displaying alert with message: \"%s\" \n due to: \"%s\" \n";
            System.out.printf(msg, message, e.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// Misc Utility Functions ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    private Paint rgbToHex(int[] rgb) {
        if (rgb.length == 0) {
            return GREEN;
        }
        return Color.valueOf(String.format("#%02x%02x%02x", rgb[0], rgb[1], rgb[2]));
    }

    private void setUiPixelPerUnitLength() {
        uiPixelPerUnitLength = (float) track.getWidth()/BehaviorMate.tc.getTrackLength();
    }

    private void openURLLink(String url_key) {
        String url = null;
        try {
            url = "https:\\\\" + BehaviorMate.webLinks.getString(url_key);
        } catch (JSONException e) {
            BehaviorMate.showError("Error in links.json file.");
        }
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
        } catch (IOException e) {
            BehaviorMate.showError(String.format("Could not open default browser to: %s", url));
        }
    }

    private ContextListMetric getContextListMetricWithId(String id) {
        ObservableList<ContextListMetric> tableItems = contextListTable.getItems();

        for(ContextListMetric contextListMetric : tableItems) {
            if (contextListMetric.id.compareTo(id) == 0) {
                return contextListMetric;
            }
        }
        return null;
    }

    private ValveMetric getValveMetricWithPin(String pin) {
        ObservableList<ValveMetric> tableItems = valvesTable.getItems();

        for(ValveMetric valveMetric : tableItems) {
            if (valveMetric.valve.compareTo(pin) == 0) {
                return valveMetric;
            }
        }
        return null;
    }

    private void setupFrameRateTracker() {
        frameRateMeter = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long oldFrameTime = frameTimes[frameTimeIndex] ;
                frameTimes[frameTimeIndex] = now ;
                frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length ;
                if (frameTimeIndex == 0) {
                    arrayFilled = true ;
                }
                if (arrayFilled) {
                    long elapsedNanos = now - oldFrameTime ;
                    long elapsedNanosPerFrame = elapsedNanos / frameTimes.length ;
                    double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame ;
                    fpsLabel.setText(String.format("FPS: %d", 5*((int)(frameRate/5)) ));
                }
            }
        };
        frameRateMeter.start();
    }

    private void setupCommCheckService() {
        ScheduledService commCheckScheduledService = new ScheduledService() {
            @Override
            protected Task createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() {
                        try {
                            commStatuses = BehaviorMate.tc.areControllersConnected();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            BehaviorMate.showError("Failed to fetch controller statuses.");
                        }
                        return true;
                    }
                };
            }
        };
        commCheckScheduledService.setPeriod(Duration.millis(MILLISPERCOMMSCHECK));
        commCheckScheduledService.start();
    }

    private void setupTreadmillService() {
        treadmillTimer = new Timer();
        treadmillTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (BehaviorMate.tc.mainExperimentLoop()) {
                        // must wrap call to stop method with this because only FX Animation thread can update IO
                        // and stop() displays a file save dialogue.
                        Platform.runLater(() -> {
                            stop();
                        });
                    }
                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }
            }}, 0, MILLISPERTREADMILLUPDATE);
    }

    /**
     * Class used for displaying rows of metric table in the top right of the UI.
     */
    public class Metric {
        private String metric;
        private String value;

        public Metric() {}

        public Metric(int metric, String value) {
            this.metric = String.valueOf(metric);
            this.value = value;
        }

        public Metric(String metric, String value) {
            this.metric = metric;
            this.value = value;
        }

        public String getMetric() {
            return metric;
        }

        public String getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = String.valueOf(value);
        }

        public void setValue(float value) {
            this.value = String.valueOf(value);
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * Class used for displaying rows of the context list table in the top left of the UI.
     */
    public class ContextListMetric {
        private String id;
        private String status;
        private String valves;

        public ContextListMetric() {}

        public ContextListMetric(String id, String status, String valves) { //}, int count) {
            this.id = id;
            this.status = status;
            this.valves = valves;
        }

        public String getId() {
            return id;
        }

        public String getStatus() {
            return status;
        }

        public String getValves() {
            return valves;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setValves(String valves) {
            this.valves = valves;
        }
    }

    /**
     * Class used for displaying rows of the valve table in the bottom left of the UI.
     */
    public class ValveMetric {
        private String valve;
        private String state;
        private String count;

        public ValveMetric() {}

        public ValveMetric(String valve, String state) {
            this.valve = valve;
            this.count = "0";
            setState(state);
        }

        public String getValve() {
            return valve;
        }

        public String getState() {
            return state;
        }

        public String getCount() {
            return count;
        }

        public void setValve(String valve) {
            this.valve = valve;
        }

        public void setState(String state) {
            if (state.compareTo(String.valueOf(Int.VALVE_ON)) == 0) {
                this.state = "ON";
            } else  if (state.compareTo(String.valueOf(Int.VALVE_OFF)) == 0) {
                this.state = "OFF";
            } else {
                this.state = "?";
            }
        }

        public void setCount(String count) {
            this.count = count;
        }
    }
}

//        treadmillLoop = new Timeline(
//                new KeyFrame(Duration.millis(MILLISPERUIUPDATE),
//                        event -> {
//                            try {
//                                if (BehaviorMate.tc.mainExperimentLoop()) {
//                                    stop();
//                                }
//                            } catch (JSONException | IOException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }));
//        treadmillLoop.setCycleCount(Timeline.INDEFINITE);
//        treadmillLoop.play();
//
//treadmillLoop.stop();
//private static Timeline treadmillLoop; // runs the main experiment loop


