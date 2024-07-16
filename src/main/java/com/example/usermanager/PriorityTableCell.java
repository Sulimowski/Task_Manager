package com.example.usermanager;
import javafx.scene.control.TableCell;
public class PriorityTableCell extends TableCell<Task, String>{
    @Override
    protected void updateItem(String priority, boolean empty) {
        super.updateItem(priority, empty);

        if (priority == null || empty) {
            setText(null);
            setStyle("");
        } else {
            setText(priority);

            // Change the cell color based on the priority value
            switch (priority) {
                case "HIGH":
                    setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red;");
                    break;
                case "MEDIUM":
                    setStyle("-fx-background-color: #ffe5cc; -fx-text-fill: orange;");
                    break;
                case "LOW":
                    setStyle("-fx-background-color: #ccffcc; -fx-text-fill: green;");
                    break;
                default:
                    setStyle("");
                    break;
            }
        }
    }
}
