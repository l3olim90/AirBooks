module PAThree2021 {
    requires javafx.fxml;
    requires javafx.controls;
    opens application.Controller to javafx.fxml;
    opens application;
    exports application;
}