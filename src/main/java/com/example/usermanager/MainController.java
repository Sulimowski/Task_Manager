package com.example.usermanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableView<Task> taskTable;

    @FXML
    private TableColumn<Task, String> titleColumn;

    @FXML
    private TableColumn<Task, LocalDate> deadlineColumn;
    @FXML
    private TableColumn<Task, String> priorityColumn;

    @FXML
    private TextArea taskBodyTextArea;

    private ObservableList<User> users = FXCollections.observableArrayList();
    private UserList userList = new UserList();
    private User currentUser;

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    @FXML
    private void initialize() {
        configureUserTable();
        configureTaskTable();
        loadUserListFromFile();
        userTable.setItems(users);
    }

    private void configureUserTable() {
        if (nameColumn != null) {
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            userTable.setItems(users);
            userTable.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> showUserTasks(newValue));
        } else {
            logger.log(Level.SEVERE, "Error: nameColumn is null.");
        }
    }

    private void configureTaskTable() {
        if (titleColumn != null && deadlineColumn != null) {
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
            priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));

            priorityColumn.setCellFactory(column -> new PriorityTableCell());

            // Listener for showing task body
            taskTable.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> showTaskBody(newValue));
        } else {
            logger.log(Level.SEVERE, "Error: TableColumn instances (titleColumn or deadlineColumn) are null.");
        }
    }

    private void loadUserListFromFile() {
        userList.loadUserList();
        users.setAll(userList.getUserList().values());
        logger.info("Loaded user list into observable list. Total users: " + users.size());
        verifyLoadedUsers();
    }

    private void verifyLoadedUsers() {
        if (users.size() != userList.getUserList().size()) {
            logger.log(Level.SEVERE, "Mismatch in user count. Expected: " + userList.getUserList().size() + ", but found: " + users.size());
        }
    }

    @FXML
    private void handleAddUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Enter user's name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            User newUser = new User(name);
            users.add(newUser);
            userList.putUser(newUser);
            saveUserList();
            userTable.refresh();
            logger.info("User added: " + name);
        });
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Deleting user: " + selectedUser.getName());
            alert.setContentText("Are you sure you want to delete this user?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                users.remove(selectedUser);
                userList.removeUser(selectedUser);
                saveUserList();
                logger.info("User deleted: " + selectedUser.getName());
            }
        } else {
            showAlert("Error", "No user selected", "Please select a user to delete.");
        }
    }

    @FXML
    private void handleAddTask() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "No user selected", "Please select a user to add a task.");
            return;
        }

        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add Task");
        dialog.setHeaderText("Enter task details:");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.add(new Label("Title:"), 0, 0);
        grid.add(new Label("Deadline (YYYY-MM-DD):"), 0, 1);
        grid.add(new Label("Body:"), 0, 2);

        TextField titleInput = new TextField();
        TextField deadlineInput = new TextField();
        TextArea bodyInput = new TextArea();

        grid.add(titleInput, 1, 0);
        grid.add(deadlineInput, 1, 1);
        grid.add(bodyInput, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == addButton) {
                try {
                    String title = titleInput.getText().trim();
                    LocalDate deadline = LocalDate.parse(deadlineInput.getText().trim());
                    String body = bodyInput.getText();

                    Task newTask = new Task(title, deadline);
                    newTask.setBody(body);
                    selectedUser.addTask(newTask);
                    saveUserList();
                    taskTable.refresh();
                    return newTask;
                } catch (DateTimeParseException e) {
                    showAlert("Error", "Invalid date format", "Please enter the date in the format YYYY-MM-DD.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> showAlert("Success", "Task added",
                "Task added successfully for user: " + selectedUser.getName()));
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            currentUser.getTasks().remove(selectedTask);
            saveUserList();
            taskTable.refresh();
            logger.info("Task deleted: " + selectedTask.getTitle());
        } else {
            showAlert("Error", "No task selected", "Please select a task to delete.");
        }
    }

    @FXML
    private void handleEditTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            Dialog<Task> dialog = new Dialog<>();
            dialog.setTitle("Edit Task");
            dialog.setHeaderText("Edit task details:");

            ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.add(new Label("Title:"), 0, 0);
            grid.add(new Label("Deadline (YYYY-MM-DD):"), 0, 1);
            grid.add(new Label("Body:"), 0, 2);

            TextField titleInput = new TextField(selectedTask.getTitle());
            TextField deadlineInput = new TextField(selectedTask.getDeadline().toString());
            TextArea bodyInput = new TextArea(selectedTask.getBody());

            grid.add(titleInput, 1, 0);
            grid.add(deadlineInput, 1, 1);
            grid.add(bodyInput, 1, 2);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(button -> {
                if (button == saveButton) {
                    try {
                        String title = titleInput.getText().trim();
                        LocalDate deadline = LocalDate.parse(deadlineInput.getText().trim());
                        String body = bodyInput.getText();

                        selectedTask.setTitle(title);
                        selectedTask.setDeadline(deadline);
                        selectedTask.setBody(body);

                        saveUserList(); // Save changes to user list
                        taskTable.refresh();
                        return selectedTask;
                    } catch (DateTimeParseException e) {
                        showAlert("Error", "Invalid date format", "Please enter the date in the format YYYY-MM-DD.");
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(task -> showAlert("Success", "Task edited",
                    "Task edited successfully: " + task.getTitle()));
        } else {
            showAlert("Error", "No task selected", "Please select a task to edit.");
        }
    }

    @FXML
    private void handleSaveTasks() {
        saveUserList();
        showAlert("Success", "Tasks saved", "Tasks saved successfully.");
    }

    private void showUserTasks(User user) {
        if (user != null) {
            currentUser = user;
            taskTable.setItems(currentUser.getTasks());
        }

    }

    private void showTaskBody(Task task) {
        if (task != null) {
            taskBodyTextArea.setText(task.getBody());
        } else {
            taskBodyTextArea.clear();
        }
    }

    private void saveUserList() {
        userList.saveUserList();
        logger.info("User list saved.");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
