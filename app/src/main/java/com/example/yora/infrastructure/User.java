package com.example.yora.infrastructure;

public class User {
    private int _id;
    private String _userName;
    private String _displayName;
    private String _avatarUrl;
    private boolean _isLoggedIn;
    private boolean _hasPassword;
    private String _email;


    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public String getUserName() {
        return _userName;
    }

    public void setUserName(String userName) {
        _userName = userName;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public void setDisplayName(String displayName) {
        _displayName = displayName;
    }

    public String getAvatarUrl() {
        return _avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        _avatarUrl = avatarUrl;
    }

    public boolean isLoggedIn() {
        return _isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        _isLoggedIn = loggedIn;
    }

    public boolean isHasPassword() {
        return _hasPassword;
    }

    public void setHasPassword(boolean hasPassword) {
        _hasPassword = hasPassword;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }
}
