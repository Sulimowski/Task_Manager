package com.example.usermanager;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserList {
    private static final String FILE_NAME = "users.ser";
    private Map<Integer, User> userList = new HashMap<>();
    private static final Logger logger = Logger.getLogger(UserList.class.getName());

    public Map<Integer, User> getUserList() {
        return userList;
    }

    public synchronized void putUser(User user) {
        userList.put(user.getID(), user);
        logger.info("Added user: " + user.getName() + " with ID: " + user.getID());
    }

    public synchronized void removeUser(User user) {
        userList.remove(user.getID());
        logger.info("Removed user: " + user.getName() + " with ID: " + user.getID());
    }

    public synchronized void saveUserList() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(userList);
            logger.info("User list saved successfully. Total users: " + userList.size());
            logger.info("Saved users: " + userList.keySet());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save user list.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadUserList() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            userList = (Map<Integer, User>) in.readObject();
            logger.info("User list loaded successfully. Total users: " + userList.size());
            logger.info("Loaded users: " + userList.keySet());
        } catch (FileNotFoundException e) {
            logger.info("No existing user list found, starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to load user list.", e);
        }
    }
}
