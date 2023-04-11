module losonczylab.behaviormate {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.json;

    opens losonczylab.behaviormate to javafx.fxml;
    exports losonczylab.behaviormate;
}