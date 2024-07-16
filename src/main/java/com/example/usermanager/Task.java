package com.example.usermanager;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Task implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String title;
    private LocalDate deadline;
    private String body;
    private transient String priority;

    public Task(String title, LocalDate deadline) {
        this.title = title;
        this.deadline = deadline;
        this.body = "";
        determinePriority();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        determinePriority();
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
        determinePriority();
    }

    private void determinePriority() {
        LocalDate now = LocalDate.now();
        long daysDifference = ChronoUnit.DAYS.between(now, deadline);

        if (daysDifference <= 3) {
            priority = "HIGH";  // If 3 days or less until deadline
        } else if (daysDifference <= 7) {
            priority = "MEDIUM";  // If less than or equal to a week until deadline
        } else {
            priority = "LOW";  // Defaulting to LOW for cases beyond a week
        }
    }

    public String getPriority() {
        return priority;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        determinePriority();
        out.writeObject(priority);
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        priority = (String) in.readObject();
    }
}
