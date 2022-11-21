package com.mycompany.components.serverModels;

import javax.servlet.http.HttpSession;

public class User {

    private String username;
    private String hashedPassword;
    private HttpSession session;

    private User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    public static User newInstance(String username, String hashedPassword) {
        return new User(username, hashedPassword);
    }
}
