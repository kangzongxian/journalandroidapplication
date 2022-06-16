package util;

import android.app.Application;

// Extend Application to make it accessible throughout the whole App
public class JournalApi extends Application {

    private String username;
    private String userId;

    // Create a Singleton to refer
    private static JournalApi instance;

    // We return the same instance
    // If is null, then we create a new one
    public static JournalApi getInstance() {
        if (instance == null) {
            instance = new JournalApi();
        }

        return instance;
    }

    public JournalApi(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

