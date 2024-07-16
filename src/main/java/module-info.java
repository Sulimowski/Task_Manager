module com.example.usermanager {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.logging;

    opens com.example.usermanager to javafx.fxml;
    exports com.example.usermanager;
}