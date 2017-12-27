package jp.hotdrop.rtapp.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;
    public String email;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(User.class)
     */
    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
