package losonczylab.behaviormate;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONException;
import org.json.JSONObject;

public class TrialAttrsController {

    @FXML
    private TextArea trialAttrsTextArea;

    void start() { }

    void run() { }

    @FXML
    protected void cancelBtnClick() {
        BehaviorMate.trialAttrsStage.close();
        //System.out.println("C: " + trialAttrsTextArea.getText());
    }

    @FXML
    protected void okBtnClick() {
        JSONObject trialAttrs = parseTrialAttrsFromForm();
        if (trialAttrs == null) {
            BehaviorMate.showError("Trial attributes must be valid JSON. Reenter or hit cancel.");
        } else {
            BehaviorMate.addTrialAttrsToMainSettings(trialAttrs);
            BehaviorMate.trialAttrsStage.close();
        }
        //System.out.println("D: " + trialAttrsTextArea.getText());
    }

    private JSONObject parseTrialAttrsFromForm() {
        JSONObject trialAttrs;
        try {
            trialAttrs = new JSONObject(trialAttrsTextArea.getText());
        } catch (JSONException e) {
            return null;
        }
        //System.out.println(trialAttrs);
        return trialAttrs;
    }

}
