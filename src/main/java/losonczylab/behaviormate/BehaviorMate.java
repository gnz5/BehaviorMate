package losonczylab.behaviormate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import losonczylab.behaviormate.core.TreadmillController;
import losonczylab.behaviormate.util.ArduinoController;
import losonczylab.behaviormate.util.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BehaviorMate extends Application {
    public static final String APP_NAME = "BehaviorMate";
    public static final String BUILTON = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z").format(Calendar.getInstance().getTime());
    public static final String VERSION = "1.0.0";
    public static final String VM = "Eclipse Adoptium Temurin-11.0.13+8";
    public static final String AUTHORS = "Jack Bowler, George Zakka, Attila Losonczy";
    public static final String COPYRIGHT = "Copyright © 2016-2022 Losonczy Lab";
    public static final String DEFAULT_SETTINGS_FILE = "settings.json";

    public static final String curdir = System.getProperty("user.dir");
    public static final String decoratorsDir = getPlatformAgnosticPath(new String[] {curdir, "decorators"});
    public static final String listsDir = getPlatformAgnosticPath(new String[] {curdir, "lists"});
    private static final String webLinksPath = getPlatformAgnosticPath(new String[] {curdir, "src", "main", "resources", "losonczylab", "behaviormate", "links.json"});
    public static final String BEHAVIOR_CONTROLLER = "behavior_controller";
    private static File settingsFile;
    public static TreadmillController tc;
    public static ArduinoController ac;
    public static JSONObject main_settings;
    public static JSONObject system_settings;
    private static BehaviorMateController behaviorMateController;
    private static TrialAttrsController trialAttrsController;
    static JSONObject webLinks;

    static Stage mainStage;
    static Stage trialAttrsStage;
    static Stage loadingStage;
    private static final int WIDTH = 875;
    private static final int HEIGHT = 800;
    private static final int TRIAL_ATTRS_WIDTH = 500;
    private static final int TRIAL_ATTRS_HEIGHT = 500;
    private static final int LOADING_WIDTH = 800;
    private static final int LOADING_HEIGHT = 400;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Set up loading screen
        FXMLLoader fxmlLoader = new FXMLLoader(BehaviorMate.class.getResource("loading.fxml"));
        Scene loading_Scene = new Scene(fxmlLoader.load(), LOADING_WIDTH, LOADING_HEIGHT);
        loadingStage = new Stage();
        loadingStage.setResizable(false);
        loadingStage.setScene(loading_Scene);
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.show();

        // Set up main page
        fxmlLoader = new FXMLLoader(BehaviorMate.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        behaviorMateController = fxmlLoader.getController(); // Controller doesn't get created until the Scene is created
        behaviorMateController.behaviorMate = this;
        behaviorMateController.start();
        webLinks = new JSONObject(JSONHelper.readFile(webLinksPath));
        mainStage = stage;
        mainStage.setOnCloseRequest( e -> endProgram() );
        mainStage.getIcons().add(new Image(String.valueOf(BehaviorMate.class.getResource("Icon.jpg"))));
        mainStage.setTitle(String.format("%s %s", APP_NAME, VERSION));
        mainStage.setScene(scene);
        mainStage.setResizable(false);

        // Set up trial attributes form, but do not display it
        fxmlLoader = new FXMLLoader(BehaviorMate.class.getResource("trialAttrs.fxml"));
        Scene trialAttrs_Scene = new Scene(fxmlLoader.load(), TRIAL_ATTRS_WIDTH, TRIAL_ATTRS_HEIGHT);
        trialAttrsController = fxmlLoader.getController();
        trialAttrsController.start();
        trialAttrsStage = new Stage();
        trialAttrsStage.setTitle("Trial Attributes");
        trialAttrsStage.setResizable(false);
        trialAttrsStage.setScene(trialAttrs_Scene);

        ac = new ArduinoController();

        while (true) {
            openSettingsFileSelector();
            if (settingsFile == null) {
                showError("No settings file selected. Terminating Program.");
                endProgram();
            }

            try {
                JSONHelper.loadSettings();
            } catch (Exception e) {
                showError("Error loading settings. \n\n" + e.getMessage());
                e.printStackTrace();
                continue;
            }

            try {
                tc = new TreadmillController();
                tc.reconfigureExperiment();
                behaviorMateController.run(); // must be called after treadmill controller is initialized
                loadingStage.close();
                mainStage.show();
                break;
            } catch (Exception e) {
                showError("Error setting up the experiment. \n\n" + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        try {
            alert.showAndWait();
        } catch (Exception e) {
            String msg = "Error displaying alert with message: \"%s\" \n due to: \"%s\" \n";
            System.out.printf(msg, message, e.getMessage());
        }
    }

    public static void showSettingsFilePopup() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Loaded Settings File");
        alert.setHeaderText(null);
        alert.setContentText(getSettingsFileName());
        try {
            alert.showAndWait();
        } catch (Exception e) {
            String msg = "Error displaying alert with message: \"%s\" \n due to: \"%s\" \n";
            System.out.printf(msg, BehaviorMate.getSettingsFileName(), e.getMessage());
        }
    }

    public static String getSettingsFileName() {
        return settingsFile.toString();
    }

    public static void setMainBehaviorMateSettings(JSONObject mainSettings) {
        main_settings = mainSettings;
    }

    public static void setSystemBehaviorMateSettings(JSONObject systemSettings) {
        system_settings = systemSettings;
    }

    public static void addTrialAttrsToMainSettings(JSONObject trialAttrs) {
        try {
            main_settings.put("trial_attributes", trialAttrs);
        } catch (JSONException e) {
            e.printStackTrace();
            BehaviorMate.showError("Error adding trial attributes.");
        }
    }

    // Display file selector, load settings, and refresh treadmill controller
    void refresh() {
        while (true) {
            openSettingsFileSelector();
            if (settingsFile == null) {
                showError("No settings file selected. Terminating Program.");
                endProgram();
            }

            try {
                JSONHelper.loadSettings();
            } catch (Exception e) {
                showError("Error loading settings. \n\n" + e.getMessage());
                e.printStackTrace();
                continue;
            }

            try {
                tc.RefreshSettings();
                break;
            } catch (Exception e) {
                showError("Error setting up the experiment. \n\n" + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    void endProgram() {
        mainStage.close();
        System.exit(0);
    }

    private void openSettingsFileSelector() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select BehaviorMate Settings File");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.setInitialFileName("settings.json");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        settingsFile = fileChooser.showOpenDialog(mainStage);
    }

    static void openTrialAttrsForm() {
        trialAttrsStage.show();
    }

    public static String getPlatformAgnosticPath(String[] directories) {
        String output = "";
        for (String directory : directories) {
            output += directory + File.separator;
        }
        output = output.substring(0, output.length()-1);
        return output;
    }

    public static void showBehaviorFileDeleteDialog() {
        File logFile = BehaviorMate.tc.getLogFile();
        String alertMessage = "Would you like to keep the generated behavior data file: \n\n" + logFile.getAbsolutePath() + "?";
        ButtonType keep = new ButtonType("Keep");
        ButtonType discard = new ButtonType("Discard");
        Alert alert = new Alert(Alert.AlertType.NONE, alertMessage, keep, discard);
        alert.showAndWait();

        if (alert.getResult() == discard) {
            String secondaryAlertMessage = "Confirm deletion.";
            ButtonType neverMind = new ButtonType("Never mind, keep it");
            ButtonType permanentlyDelete = new ButtonType("Permanently Discard");
            Alert secondaryAlert = new Alert(Alert.AlertType.NONE, secondaryAlertMessage, neverMind, permanentlyDelete);
            secondaryAlert.showAndWait();

            if (secondaryAlert.getResult() == permanentlyDelete) {
                logFile.delete();
            }
        }
    }
}

