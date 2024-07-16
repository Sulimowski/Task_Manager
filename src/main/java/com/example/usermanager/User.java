package com.example.usermanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    private int id;
    private String name;
    private transient ObservableList<Task> tasks; // Transient field

    private static final Logger logger = Logger.getLogger(User.class.getName());

    public User(String name) {
        this.id = idGenerator.incrementAndGet();
        this.name = name;
        this.tasks = FXCollections.observableArrayList();
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ObservableList<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        tasks.add(task);
        logger.info("Added task: " + task.getTitle() + " to user: " + name);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(tasks.size());
        for (Task task : tasks) {
            out.writeObject(task);
        }
        logger.info("Serialized user: " + name + " with " + tasks.size() + " tasks.");
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        tasks = FXCollections.observableArrayList();
        int taskCount = in.readInt();
        for (int i = 0; i < taskCount; i++) {
            Task task = (Task) in.readObject();
            tasks.add(task);
        }
        logger.info("Deserialized user: " + name + " with " + tasks.size() + " tasks.");
    }

    @Override
    public String toString() {
        return name;
    }
}
